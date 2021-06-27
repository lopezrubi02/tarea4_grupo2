package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import com.example.tarea4_grupo2.service.SendMailService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class UsuarioController {

    @Autowired
    SendMailService sendMailService;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    DireccionesRepository direccionesRepository;
    @Autowired
    CategoriasRepository categoriasRepository;
    @Autowired
    PedidosRepository pedidosRepository;
    @Autowired
    DistritosRepository distritosRepository;
    @Autowired
    RestauranteRepository restauranteRepository;
    @Autowired
    PlatoRepository platoRepository;
    @Autowired
    PedidoHasPlatoRepository pedidoHasPlatoRepository;
    @Autowired
    RepartidorRepository repartidorRepository;
    @Autowired
    FotosPlatosRepository fotosPlatosRepository;
    @Autowired
    MetodosDePagoRepository metodosDePagoRepository;
    @Autowired
    TarjetasOnlineRepository tarjetasOnlineRepository;

    @GetMapping(value={"/cliente/paginaprincipal","/cliente/","/cliente"})
    public String paginaprincipal(HttpSession session, Model model) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        return "cliente/paginaPrincipal";
    }

    /** para validar patron de contraseña **/
    public  boolean validarContrasenia(String contrasenia1) {
        Pattern pattern1 = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$");
        Matcher matcher1 = pattern1.matcher(contrasenia1);
        return matcher1.matches();
    }

    public boolean validarcorreounico(String correo, Usuario usuario){
        boolean errorcorreo = false;
        Usuario usuarioxcorreo = usuarioRepository.findByEmail(usuario.getEmail());
        if(usuarioxcorreo != null){
            errorcorreo = true;
        }
        return errorcorreo;
    }

    public boolean validarstringsexo(String stringsexo){
        boolean errorstring = true;
        if(stringsexo.equalsIgnoreCase("Femenino")){
            errorstring = false;
        }else if(stringsexo.equalsIgnoreCase("Masculino")){
            errorstring = false;
        }else{
            errorstring = true;
        }
        return errorstring;
    }

    /** Para validación de DNI con la api **/
    public boolean validarDNI(String dni){
        Boolean dniValido = false;

        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        try{

            // reemplazar DNI
            String urlString = "https://api.ateneaperu.com/api/reniec/dni?sNroDocumento="+dni;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if(status > 299){
                System.out.println("EROR PAPU");
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                System.out.println(connection.getResponseMessage());
                System.out.println(connection.getResponseCode());
                System.out.println(connection.getErrorStream());
                reader.close();
            } else {
                System.out.println("/GET");
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            }
            System.out.println(responseContent.toString());
            JSONObject jsonObj = new JSONObject(responseContent.toString());
            //System.out.println(jsonObj.get("nombres"));

            // Validar si existe documento
            if(!jsonObj.get("nombres").equals("")){
                System.out.println("DNI valido");
                dniValido = true;
            }

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dniValido;
    }

    /**                     Registro cliente                **/
    @GetMapping("/nuevocliente")
    public String nuevoCliente(Model model,@ModelAttribute("usuario") Usuario usuario)
    {
        List<Distritos> listadistritos = distritosRepository.findAll();
        model.addAttribute("listadistritos",listadistritos);
        return "cliente/registroCliente";
    }

    @PostMapping("/guardarNuevo")
    public String guardarCliente(@RequestParam("direccion") String direccion,
                                 @RequestParam("iddistrito") int iddistrito,
                                 @RequestParam("password2") String pass2,
                                 Model model,
                                 @ModelAttribute("usuario") @Valid Usuario usuario,
                                 BindingResult bindingResult) throws MalformedURLException {

        if (bindingResult.hasErrors()) {
            List<Distritos> listadistritos = distritosRepository.findAll();
            model.addAttribute("listadistritos", listadistritos);
            return "cliente/registroCliente";
        } else {
            boolean errorcorreo = validarcorreounico(usuario.getEmail(), usuario);
            boolean errorstringsexo = validarstringsexo(usuario.getSexo());
            boolean dniexiste = validarDNI(usuario.getDni());

            if (errorcorreo == true || errorstringsexo == true || direccion == null || dniexiste == false) {
                if(errorcorreo==true){
                    model.addAttribute("errorcorreo", "Ya hay una cuenta registrada con el correo ingresado.");
                }
                if(dniexiste == false){
                    model.addAttribute("errordni","Ingrese un DNI válido");
                }
                List<Distritos> listadistritos = distritosRepository.findAll();
                model.addAttribute("listadistritos", listadistritos);
                return "cliente/registroCliente";
            } else {
                if (usuario.getContraseniaHash().equals(pass2)) {
                    String contraxvalidarpatron = usuario.getContraseniaHash();

                    boolean validarcontra = validarContrasenia(contraxvalidarpatron);

                    if (validarcontra == true) {

                        String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());

                        usuario.setContraseniaHash(contraseniahashbcrypt);
                        usuario.setRol("Cliente");
                        usuario.setCuentaActiva(1);
                        usuarioRepository.save(usuario);

                        //Para guardar direccion
                        Usuario usuarionuevo = usuarioRepository.findByDniAndEmailEquals(usuario.getDni(), usuario.getEmail());
                        int idusuarionuevo = usuarionuevo.getIdusuarios();

                        Direcciones direccionactual = new Direcciones();
                        direccionactual.setDireccion(direccion);
                        Optional<Distritos> distritoopt = distritosRepository.findById(iddistrito);

                        if(distritoopt.isPresent()){
                            Distritos distritosactual = distritoopt.get();
                            direccionactual.setDistrito(distritosactual);
                            //direccionactual.setUsuariosIdusuarios(idusuarionuevo);
                            direccionactual.setUsuario(usuarionuevo);
                            direccionactual.setActivo(1);
                            System.out.println("debería guardar direccion");
                            direccionesRepository.save(direccionactual);
                            System.out.println("ya guardó direccion");

                            /* Envio de correo de confirmacion */
                            String subject = "Cuenta creada en Spicyo";
                            String aws = "ec2-user@ec2-3-84-20-210.compute-1.amazonaws.com";
                            String direccionurl = "http://" + aws + ":8081/login";
                            String mensaje = "¡Hola!<br><br>" +
                                    "Ahora es parte de Spicyo. Para ingresar a su cuenta haga click: <a href='" + direccionurl + "'>AQUÍ</a> <br><br>Atte. Equipo de Spicy :D</b>";
                            String correoDestino = usuario.getEmail();
                            sendMailService.sendMail(correoDestino, "saritaatanacioarenas@gmail.com", subject, mensaje);

                            return "cliente/confirmarCuenta";
                        }else{
                            List<Distritos> listadistritos = distritosRepository.findAll();
                            model.addAttribute("listadistritos", listadistritos);
                            return "cliente/registroCliente";
                        }
                    } else {
                        List<Distritos> listadistritos = distritosRepository.findAll();
                        model.addAttribute("listadistritos", listadistritos);
                        model.addAttribute("errorpatroncontra", "La contraseña no cumple con los requisitos: mínimo 8 caracteres, un número y un caracter especial");
                        model.addAttribute("usuario", usuario);
                        return "cliente/registroCliente";
                    }
                } else {
                    List<Distritos> listadistritos = distritosRepository.findAll();
                    model.addAttribute("listadistritos", listadistritos);
                    return "cliente/registroCliente";
                }
            }
        }
    }

        /**         Para exportar a excel historial de pedidos           **/
    @GetMapping("/cliente/historialpedidosexcel")
    public ResponseEntity<InputStreamResource> exportAllData(@RequestParam("id") int id) throws Exception {

        ByteArrayInputStream stream2 = exportAllData1(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=historialdepedidos.xls");

        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(stream2));
    }

    private static CellStyle createHeadStyle(final Workbook wb) {
        CellStyle style = createVHCenterStyle(wb);
        final Font font = wb.createFont();
        font.setFontName ("Songti");
        font.setFontHeight((short) 150);
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle createVHCenterStyle(final Workbook wb) {
        CellStyle style = wb.createCellStyle (); // objeto de estilo
        style.setVerticalAlignment (VerticalAlignment.CENTER); // vertical
        style.setAlignment (HorizontalAlignment.CENTER); // horizontal
        style.setWrapText (true); // Especifica el salto de línea automático cuando no se puede mostrar el contenido de la celda
        // agregar borde
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }
    public ByteArrayInputStream exportAllData1(int id) throws IOException {
        String[] columns = { "MONTO TOTAL (S/)", "RESTAURANTE", "FECHA PEDIDO", "DIRECCION ENTREGA", "METODO DE PAGO"};

        Workbook workbook = new HSSFWorkbook();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        Sheet sheet = workbook.createSheet("Personas");
        CellStyle headStyle = createHeadStyle(workbook);

        Row row = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headStyle);
        }

        List<PedidosclienteaexcelDTO> listapedidos = pedidosRepository.listapedidosexcel(id);

        int initRow = 1;
        for (PedidosclienteaexcelDTO pedidoexcel : listapedidos) {
            row = sheet.createRow(initRow);
            row.createCell(0).setCellValue(pedidoexcel.getMontototal());
            row.createCell(1).setCellValue(pedidoexcel.getNombre());
            row.createCell(2).setCellValue(pedidoexcel.getFechahorapedido());
            row.createCell(3).setCellValue(pedidoexcel.getDireccion());
            row.createCell(4).setCellValue(pedidoexcel.getMetodo());
            initRow++;
        }

        workbook.write(stream);
        workbook.close();
        return new ByteArrayInputStream(stream.toByteArray());
    }

    @GetMapping("/cliente/reportes")
    public String reportesCliente(Model model,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarios=sessionUser.getIdusuarios();

        List<Pedidos> listapedidosusuario = pedidosRepository.findAllByIdclienteEquals(idusuarios);
        boolean ultimopedido1 = true; //true -> hay al menos un pedido registrado
        if(listapedidosusuario.isEmpty()){
            ultimopedido1 = false; //false -> no hay pedidos registrados
        }
        model.addAttribute("haypedidos",ultimopedido1);

        if(ultimopedido1==true){
            LocalDate dateactual = LocalDate.now();
            String fechaactual1 = String.valueOf(dateactual);
            String[] fechaactual = fechaactual1.split("-");
            String a = fechaactual[0];
            String m = fechaactual[1];
            int anioactual = Integer.parseInt(a);
            int mesactual = Integer.parseInt(m);
            int anio = anioactual;
            int mes = mesactual;
            String mes_mostrar = String.valueOf(mes);

            if(mes<10){
                mes_mostrar='0' + mes_mostrar; //agrega cero si el menor de 10

            }
            String fechamostrar = anio + "-" + mes_mostrar;

            Optional<Usuario> optUsuario = usuarioRepository.findById(idusuarios);
            if (optUsuario.isPresent()) {
                Usuario cliente = optUsuario.get();

                List<Top3Restaurantes_ClienteDTO> top3Restaurantes_clienteDTOS = pedidosRepository.obtenerTop3Restaurantes(idusuarios, anio, mes);
                List<Top3Platos_ClientesDTO> top3Platos_clientesDTOS = pedidosRepository.obtenerTop3Platos(idusuarios, anio, mes);
                List<TiempoMedio_ClienteDTO> tiempoMedio_clienteDTOS = pedidosRepository.obtenerTiemposPromedio(idusuarios, anio, mes);

                DineroAhorrado_ClienteDTO dineroAhorrado_clienteDTO = pedidosRepository.dineroAhorrado(idusuarios, anio, mes);
                model.addAttribute("cliente", cliente);
                model.addAttribute("listaTop3Restaurantes",top3Restaurantes_clienteDTOS );
                model.addAttribute("listaTop3Platos", top3Platos_clientesDTOS);
                model.addAttribute("listaPromedioTiempo", tiempoMedio_clienteDTOS);
                model.addAttribute("diferencia", dineroAhorrado_clienteDTO);
                model.addAttribute("listaHistorialConsumo", pedidosRepository.obtenerHistorialConsumo(idusuarios, anio, mes));
                model.addAttribute("fechaseleccionada",fechamostrar);
                model.addAttribute("id",idusuarios);
                return "cliente/reportes";
            } else {
                return "redirect:/cliente/miperfil";
            }
        }else{
            return "cliente/reportes";
        }

    }

    @PostMapping("/cliente/recepcionCliente")
    public String recepcionCliente(@RequestParam("fechahorapedido") String fechahorapedido,
                                   RedirectAttributes redirectAttributes,
                                   Model model,
                                   HttpSession session
                                   ) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarios=sessionUser.getIdusuarios();

        String[] fecha = fechahorapedido.split("-", 2);

        LocalDate dateactual = LocalDate.now();
        String fechaactual1 = String.valueOf(dateactual);


        List<Pedidos> listapedidosusuario = pedidosRepository.findAllByIdclienteEquals(idusuarios);
        boolean ultimopedido1 = true; //true -> hay al menos un pedido registrado
        if(listapedidosusuario.isEmpty()){
            ultimopedido1 = false; //false -> no hay pedidos registrados
        }
        model.addAttribute("haypedidos",ultimopedido1);

        try {
            String a = fecha[0];
            String m = fecha[1];
            int anio = Integer.parseInt(a);
            int mes = Integer.parseInt(m);

            Optional<Usuario> clienteopt = usuarioRepository.findById(idusuarios);
            List<Top3Restaurantes_ClienteDTO> listaTop3Restaurantes = pedidosRepository.obtenerTop3Restaurantes(idusuarios, anio, mes);
            List<Top3Platos_ClientesDTO> listaTop3Platos = pedidosRepository.obtenerTop3Platos(idusuarios, anio, mes);
            List<TiempoMedio_ClienteDTO> listaPromedioTiempo = pedidosRepository.obtenerTiemposPromedio(idusuarios, anio, mes);
            List<HistorialConsumo_ClienteDTO> listaHistorialConsumo = pedidosRepository.obtenerHistorialConsumo(idusuarios, anio, mes);
            DineroAhorrado_ClienteDTO dineroAhorrado_clienteDTO = pedidosRepository.dineroAhorrado(idusuarios, anio, mes);
            if (clienteopt.isPresent()) {
                if(listaHistorialConsumo.isEmpty() && listaPromedioTiempo.isEmpty() && listaTop3Platos.isEmpty() && listaTop3Restaurantes.isEmpty() && (dineroAhorrado_clienteDTO == null)){
                    redirectAttributes.addFlashAttribute("alerta", "No se han encontrado datos para los reportes");
                    return "redirect:/cliente/reportes";
                }else {
                    model.addAttribute("listaTop3Restaurantes", listaTop3Restaurantes);
                    model.addAttribute("listaTop3Platos", listaTop3Platos);
                    model.addAttribute("listaPromedioTiempo", listaPromedioTiempo);
                    model.addAttribute("listaHistorialConsumo", listaHistorialConsumo);
                    model.addAttribute("diferencia", dineroAhorrado_clienteDTO);
                    model.addAttribute("fechaseleccionada",fechahorapedido);
                    model.addAttribute("id",idusuarios);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            redirectAttributes.addFlashAttribute("alerta2", "No se añadieron campos de búsqueda");
            return "redirect:/cliente/reportes";
        }
        return "cliente/reportes";
    }

    /** Imágenes **/
    @GetMapping("/cliente/imagerestaurante/{id}")
    public ResponseEntity<byte[]>mostrarImagenRest(@PathVariable("id") int id){
        Optional<Restaurante> opt = restauranteRepository.findById(id);

        if(opt.isPresent()){
            Restaurante r = opt.get();

            byte[] imagenComoBytes = r.getFoto();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(
                    MediaType.parseMediaType(r.getFotocontenttype()));

            return new ResponseEntity<>(
                    imagenComoBytes,
                    httpHeaders,
                    HttpStatus.OK);
        }else{
            return null;
        }
    }
    @GetMapping("/cliente/imagenplato/{id}")
    public ResponseEntity<byte[]>mostrarimagenplato(@PathVariable("id") int id){
        Optional<FotosPlatos> optft = fotosPlatosRepository.fotoplatoxidplato(id);

        if(optft.isPresent()){
            FotosPlatos fp = optft.get();

            byte[] imagenComoBytes = fp.getFoto();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(
                    MediaType.parseMediaType(fp.getFotocontenttype()));

            return new ResponseEntity<>(
                    imagenComoBytes,
                    httpHeaders,
                    HttpStatus.OK);
        }else{
            return null;
        }
    }

    @GetMapping("/cliente/imagenrepartidor/{id}")
    public ResponseEntity<byte[]>mostrarImagenRepart(@PathVariable("id") int id){
        Optional<Repartidor> opt = repartidorRepository.findById(id);

        if(opt.isPresent()){
            Repartidor r = opt.get();

            byte[] imagenComoBytes = r.getFoto();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(
                    MediaType.parseMediaType(r.getFotocontenttype()));

            return new ResponseEntity<>(
                    imagenComoBytes,
                    httpHeaders,
                    HttpStatus.OK);
        }else{
            return null;
        }
    }

    /** Para hallar las coordenadas de los bounds **/
    public List<String> coordenadasdistrito(String distrito){
        String address = distrito + ",Lima";
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyBLdwYvQItwrhBKLPqbEumrEURYFFlks-Y")
                .build();
        GeocodingResult[] results = new GeocodingResult[0];
        try {
            results = GeocodingApi.geocode(context,
                    address).await();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String latx = gson.toJson(results[0].geometry.bounds.northeast.lat);
        String lngx = gson.toJson(results[0].geometry.bounds.northeast.lng);
        String laty = gson.toJson(results[0].geometry.bounds.southwest.lat);
        String lngy = gson.toJson(results[0].geometry.bounds.southwest.lng);

        List<String> listascoordenadas = new ArrayList<>();
        listascoordenadas.add(latx);
        listascoordenadas.add(lngx);
        listascoordenadas.add(laty);
        listascoordenadas.add(lngy);
        listascoordenadas.add(latx);
        listascoordenadas.add(lngy);
        listascoordenadas.add(laty);
        listascoordenadas.add(lngx);
        return listascoordenadas;
    }
    /** Para hallar los distritos cercanos **/
    public String hallardistritocercano(String lat, String lng){
        String distritohallado = "a";
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyBLdwYvQItwrhBKLPqbEumrEURYFFlks-Y")
                .build();
        GeocodingResult[] results2 = new GeocodingResult[0];
        LatLng latlnggen = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));

        try {
            results2 = GeocodingApi.reverseGeocode(context, latlnggen).await();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
        distritohallado = gson2.toJson(results2[0].addressComponents[3].longName);
        int tam = Integer.parseInt(gson2.toJson(results2[0].addressComponents.length));
        for(int i=0; i<=tam-1; i++){
            if(gson2.toJson(results2[0].addressComponents[i].types[0]).equalsIgnoreCase("\"LOCALITY\"")){
                distritohallado = gson2.toJson(results2[0].addressComponents[i].longName);
            }
        }

        String sinComillas = distritohallado.replace("\"", "");
        return sinComillas;
    }

    @GetMapping("/cliente/prueba2")
    public String pruebaaa2() throws IOException, InterruptedException, ApiException {
        List<String> listascoordenadas = coordenadasdistrito("San Miguel");
        String latx = listascoordenadas.get(6);
        String lngx = listascoordenadas.get(7);

        String distritohallado1 = hallardistritocercano(latx,lngx);
        System.out.println("distrito hallado");
        System.out.println(distritohallado1);
        return "cliente/prueba";
    }

    /** Realizar pedido **/
    @GetMapping("/cliente/realizarpedido")
    public String realizarpedido(Model model, HttpSession session, RedirectAttributes attr,
                                 @RequestParam(value = "idcategoriarest" ,defaultValue = "0") String categoriarest,
                                 @RequestParam(value = "preciopromedio", defaultValue = "0") String preciopromedio,
                                 @RequestParam(value = "direccion", defaultValue = "0") String direccion,
                                 @RequestParam(value = "calificacion", defaultValue = "0") String calificacionpromedio
                                 ) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();
        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuarioactual);
        Pedidos pedidopendiente = pedidosRepository.pedidoencurso(idusuarioactual);
        if(listapedidospendientes.size() >= 1 || pedidopendiente != null){
            String mensajependidopendiente = "No puede realizar otro pedido a otro restaurante que sea diferente al que ya ha seleccionado.";
            if(pedidopendiente != null){
                mensajependidopendiente = "No puede realizar otro pedido mientras tenga un pedido en curso";
                attr.addFlashAttribute("hayunpedidoencurso",mensajependidopendiente);
                return "redirect:/cliente/progresopedido";
            }
            attr.addFlashAttribute("hayunpedidoencurso",mensajependidopendiente);
            return "redirect:/cliente/carritoproductos";
        }else{
            List<String> listaidprecio = new ArrayList<>();
            listaidprecio.add("Menor a 15");
            listaidprecio.add("Entre 15 y 25");
            listaidprecio.add("Entre 25 y 40");
            listaidprecio.add("Mayor a 40");
            model.addAttribute("listaidprecio",listaidprecio);
            List<String> listaidcalificacion = new ArrayList<>();
            listaidcalificacion.add("1 estrella");
            listaidcalificacion.add("2 estrellas");
            listaidcalificacion.add("3 estrellas");
            listaidcalificacion.add("4 estrellas");
            listaidcalificacion.add("5 estrellas");
            model.addAttribute("listaidcalificacion",listaidcalificacion);

            List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuario_Idusuarios(idusuarioactual);
            List<Categorias> listacategorias = categoriasRepository.findAll();
            List<Restaurante> listarestaurantes = restauranteRepository.findAll();
            model.addAttribute("listacategorias", listacategorias);
            model.addAttribute("listadirecciones", listadireccionescliente);
            model.addAttribute("listarestaurantes",listarestaurantes);

            try{
                int direccionxenviar = Integer.parseInt(direccion);
                String distrito1;
                String distrito2;
                String distrito3;
                String distrito4;
                List<Restaurante> restauranteshallados = new ArrayList<>();
                if (direccionxenviar == 0) {
                    model.addAttribute("direccionseleccionada", listadireccionescliente.get(0).getDireccion());
                    model.addAttribute("iddireccionxenviar", listadireccionescliente.get(0).getIddirecciones());
                    //para mostrar restaurantes de acuerdo a direccion mostrada por default
                    String distritobuscar = listadireccionescliente.get(0).getDistrito().getNombredistrito();
                    System.out.println(distritobuscar);
                    List<String> coordenadasboundsdistrito = coordenadasdistrito(distritobuscar);
                    distrito1 = hallardistritocercano(coordenadasboundsdistrito.get(0),coordenadasboundsdistrito.get(1));
                    distrito2 = hallardistritocercano(coordenadasboundsdistrito.get(2),coordenadasboundsdistrito.get(3));
                    distrito3 = hallardistritocercano(coordenadasboundsdistrito.get(4), coordenadasboundsdistrito.get(5));
                    distrito4 = hallardistritocercano(coordenadasboundsdistrito.get(6),coordenadasboundsdistrito.get(7));
                    model.addAttribute("distrito1",distrito1);
                    model.addAttribute("distrito2",distrito2);
                    model.addAttribute("distrito3",distrito3);
                    model.addAttribute("distrito4",distrito4);
                    System.out.println("distritos hallados");
                    System.out.println(distrito1 + "-" + distrito2 + "-" + distrito3 + "-" + distrito4);
                    List<Restaurante> restaurantesxdistritodefault = restauranteRepository.listarestaurantesxdistrito(distritobuscar);
                    List<Restaurante> restaurantescercanosxdistrito1 = restauranteRepository.listarestaurantesxdistrito(distrito1);
                    List<Restaurante> restaurantescercanosxdistrito2 = restauranteRepository.listarestaurantesxdistrito(distrito2);
                    List<Restaurante> restaurantescercanosxdistrito3 = restauranteRepository.listarestaurantesxdistrito(distrito3);
                    List<Restaurante> restaurantescercanosxdistrito4 = restauranteRepository.listarestaurantesxdistrito(distrito4);
                    System.out.println("restaurantes de db");
                    for (Restaurante resthallado : restaurantesxdistritodefault) {
                        resthallado.getNombre();
                        resthallado.getDistrito().getNombredistrito();
                        restauranteshallados.add(resthallado);
                    }
                    for (Restaurante resthallado : restaurantescercanosxdistrito1) {
                        resthallado.getNombre();
                        resthallado.getDistrito().getNombredistrito();
                        restauranteshallados.add(resthallado);
                    }
                    for (Restaurante resthallado : restaurantescercanosxdistrito2) {
                        resthallado.getNombre();
                        resthallado.getDistrito().getNombredistrito();
                        restauranteshallados.add(resthallado);
                    }for (Restaurante resthallado : restaurantescercanosxdistrito3) {
                        resthallado.getNombre();
                        resthallado.getDistrito().getNombredistrito();
                        restauranteshallados.add(resthallado);
                    }for (Restaurante resthallado : restaurantescercanosxdistrito4) {
                        resthallado.getNombre();
                        resthallado.getDistrito().getNombredistrito();
                        restauranteshallados.add(resthallado);
                    }
                    model.addAttribute("listarestaurantes",restauranteshallados);
                } else {
                    Optional<Direcciones> direccionopt = Optional.ofNullable(direccionesRepository.findDireccionesByIddireccionesAndUsuario_Idusuarios(direccionxenviar, idusuarioactual));
                    if (direccionopt.isPresent()) {
                        Direcciones direccionseleccionada = direccionopt.get();
                        model.addAttribute("iddireccionxenviar", direccionxenviar);
                        model.addAttribute("direccionseleccionada", direccionseleccionada.getDireccion());
                        //para mostrar restaurantes de acuerdo a direccion mostrada por default
                        String distritobuscar = listadireccionescliente.get(0).getDistrito().getNombredistrito();
                        System.out.println(distritobuscar);
                        List<String> coordenadasboundsdistrito = coordenadasdistrito(distritobuscar);
                        distrito1 = hallardistritocercano(coordenadasboundsdistrito.get(0),coordenadasboundsdistrito.get(1));
                        distrito2 = hallardistritocercano(coordenadasboundsdistrito.get(2),coordenadasboundsdistrito.get(3));
                        distrito3 = hallardistritocercano(coordenadasboundsdistrito.get(4), coordenadasboundsdistrito.get(5));
                        distrito4 = hallardistritocercano(coordenadasboundsdistrito.get(6),coordenadasboundsdistrito.get(7));
                        model.addAttribute("distrito1",distrito1);
                        model.addAttribute("distrito2",distrito2);
                        model.addAttribute("distrito3",distrito3);
                        model.addAttribute("distrito4",distrito4);
                        System.out.println("distritos hallados");
                        System.out.println(distrito1 + "-" + distrito2 + "-" + distrito3 + "-" + distrito4);
                        List<Restaurante> restaurantesxdistritodefault = restauranteRepository.listarestaurantesxdistrito(distritobuscar);
                        List<Restaurante> restaurantescercanosxdistrito1 = restauranteRepository.listarestaurantesxdistrito(distrito1);
                        List<Restaurante> restaurantescercanosxdistrito2 = restauranteRepository.listarestaurantesxdistrito(distrito2);
                        List<Restaurante> restaurantescercanosxdistrito3 = restauranteRepository.listarestaurantesxdistrito(distrito3);
                        List<Restaurante> restaurantescercanosxdistrito4 = restauranteRepository.listarestaurantesxdistrito(distrito4);
                        System.out.println("restaurantes de db");
                        for (Restaurante resthallado : restaurantesxdistritodefault) {
                            System.out.println(resthallado.getNombre());
                            System.out.println(resthallado.getDistrito().getNombredistrito());
                            restauranteshallados.add(resthallado);
                        }
                        for (Restaurante resthallado : restaurantescercanosxdistrito1) {
                            System.out.println(resthallado.getNombre());
                            System.out.println(resthallado.getDistrito().getNombredistrito());
                            restauranteshallados.add(resthallado);
                        }
                        for (Restaurante resthallado : restaurantescercanosxdistrito2) {
                            System.out.println(resthallado.getNombre());
                            System.out.println(resthallado.getDistrito().getNombredistrito());
                            restauranteshallados.add(resthallado);
                        }for (Restaurante resthallado : restaurantescercanosxdistrito3) {
                            System.out.println(resthallado.getNombre());
                            System.out.println(resthallado.getDistrito().getNombredistrito());
                            restauranteshallados.add(resthallado);
                        }for (Restaurante resthallado : restaurantescercanosxdistrito4) {
                            System.out.println(resthallado.getNombre());
                            System.out.println(resthallado.getDistrito().getNombredistrito());
                            restauranteshallados.add(resthallado);
                        }
                        model.addAttribute("listarestaurantes",restauranteshallados);
                    }else{
                        return "redirect:/cliente/realizarpedido";
                    }
                }
                //Filtro de categorias
                int idcategoriarest = Integer.parseInt(categoriarest);
                Optional<Categorias> catopt = categoriasRepository.findById(idcategoriarest);
                if(catopt.isPresent()){
                    List<Restaurante> listarestauranteseleccionado = new ArrayList<>();
                    if(idcategoriarest!=0){
                        for(Restaurante resthallados : restauranteshallados){
                            List<Categorias> catxrest = resthallados.getCategoriasrestList();
                            for(Categorias cat : catxrest){
                                if(cat.getIdcategorias() == idcategoriarest){
                                    listarestauranteseleccionado.add(resthallados);
                                    System.out.println(resthallados.getNombre());
                                }
                            }
                        }
                        model.addAttribute("listarestaurantes",listarestauranteseleccionado);
                    }else{
                        return "redirect:/cliente/realizarpedido";
                    }
                    model.addAttribute("catelegida",idcategoriarest);
                }
                //filtro precios
                int precio = Integer.parseInt(preciopromedio);
                List<Restaurante> listaRestFiltroPrecio = new ArrayList<>();
                if(precio!=0) {
                    switch (precio) {
                        case 1:
                            for(Restaurante rest : restauranteshallados){
                                listaRestFiltroPrecio.addAll(restauranteRepository.listarestprecio1(rest.getDistrito().getNombredistrito()));
                            }
                            if (listaRestFiltroPrecio.isEmpty()) {
                                attr.addFlashAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                                return "redirect:/cliente/realizarpedido";
                            } else {
                                model.addAttribute("listarestaurantes", listaRestFiltroPrecio);
                                model.addAttribute("precioselec", precio);
                            }
                            break;
                        case 2:
                            for(Restaurante rest : restauranteshallados){
                                listaRestFiltroPrecio.addAll(restauranteRepository.listarestprecio2(rest.getDistrito().getNombredistrito()));
                            }
                            if (listaRestFiltroPrecio.isEmpty()) {
                                attr.addFlashAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                                return "redirect:/cliente/realizarpedido";
                            } else {
                                model.addAttribute("listarestaurantes", listaRestFiltroPrecio);
                                model.addAttribute("precioselec", precio);
                            }
                            break;
                        case 3:
                            for(Restaurante rest : restauranteshallados){
                                listaRestFiltroPrecio.addAll(restauranteRepository.listarestprecio3(rest.getDistrito().getNombredistrito()));
                            }
                            if (listaRestFiltroPrecio.isEmpty()) {
                                attr.addFlashAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                                return "redirect:/cliente/realizarpedido";
                            } else {
                                model.addAttribute("listarestaurantes", listaRestFiltroPrecio);
                                model.addAttribute("precioselec", precio);
                            }
                            break;
                        case 4:
                            for(Restaurante rest : restauranteshallados){
                                listaRestFiltroPrecio.addAll(restauranteRepository.listarestprecio4(rest.getDistrito().getNombredistrito()));
                            }
                            if (listaRestFiltroPrecio.isEmpty()) {
                                attr.addFlashAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                                return "redirect:/cliente/realizarpedido";
                            } else {
                                model.addAttribute("listarestaurantes", listaRestFiltroPrecio);
                                model.addAttribute("precioselec", precio);
                            }
                            break;
                    }
                }
                //filtro calificaciones
                int calificacion = Integer.parseInt(calificacionpromedio);
                if(calificacion!=0) {
                    if (calificacion > 4) {
                        return "redirect:/cliente/realizarpedido";
                    } else {
                        List<Restaurante> listarestcal = new ArrayList<>();
                        for(Restaurante resthallados : restauranteshallados){
                            if(resthallados.getCalificacionpromedio() == calificacion){
                                listarestcal.add(resthallados);
                                System.out.println(resthallados.getCalificacionpromedio());
                                System.out.println(resthallados.getNombre());
                            }
                        }
                        model.addAttribute("listarestaurantes", listarestcal);
                        model.addAttribute("calsel", calificacion);
                        if (listarestcal.isEmpty()) {
                            attr.addFlashAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                            return "redirect:/cliente/realizarpedido";
                        }
                    }
                }
                return "cliente/realizar_pedido_cliente";
            }catch (NumberFormatException e){
                return "cliente/realizar_pedido_cliente";
            }
        }
    }

    @GetMapping("/cliente/direccionxenviar")
    public String direccionxenviar(Model model,
                                   @RequestParam(value = "direccion", defaultValue = "0") String direccion,
                                   HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();
        try {
            int direccionxenviar = Integer.parseInt(direccion);
            Optional<Direcciones> direccionopt = Optional.ofNullable(direccionesRepository.findDireccionesByIddireccionesAndUsuario_Idusuarios(direccionxenviar, idusuarioactual));
            if(direccionopt.isPresent()){
                List<String> listaidprecio = new ArrayList<>();
                listaidprecio.add("Menor a 15");
                listaidprecio.add("Entre 15 y 25");
                listaidprecio.add("Entre 25 y 40");
                listaidprecio.add("Mayor a 40");
                model.addAttribute("listaidprecio",listaidprecio);
                List<String> listaidcalificacion = new ArrayList<>();
                listaidcalificacion.add("1 estrella");
                listaidcalificacion.add("2 estrellas");
                listaidcalificacion.add("3 estrellas");
                listaidcalificacion.add("4 estrellas");
                listaidcalificacion.add("5 estrellas");
                model.addAttribute("listaidcalificacion",listaidcalificacion);

                Direcciones direccionseleccionada = direccionopt.get();
                List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuario_Idusuarios(idusuarioactual);
                List<Categorias> listacategorias = categoriasRepository.findAll();
                model.addAttribute("listacategorias", listacategorias);
                model.addAttribute("listadirecciones", listadireccionescliente);
                model.addAttribute("iddireccionxenviar",direccionxenviar);
                model.addAttribute("direccionseleccionada",direccionseleccionada.getDireccion());
                //para mostrar restaurantes de acuerdo a direccion seleccionada
                String distritobuscar = direccionseleccionada.getDistrito().getNombredistrito();
                System.out.println("distrito a mandar");
                System.out.println(distritobuscar);
                List<String> coordenadasboundsdistrito = coordenadasdistrito(distritobuscar);
                if(coordenadasboundsdistrito != null){
                    String distrito1 = hallardistritocercano(coordenadasboundsdistrito.get(0),coordenadasboundsdistrito.get(1));
                    String distrito2 = hallardistritocercano(coordenadasboundsdistrito.get(2),coordenadasboundsdistrito.get(3));
                    String distrito3 = hallardistritocercano(coordenadasboundsdistrito.get(4), coordenadasboundsdistrito.get(5));
                    String distrito4 = hallardistritocercano(coordenadasboundsdistrito.get(6),coordenadasboundsdistrito.get(7));
                    model.addAttribute("distrito1",distrito1);
                    model.addAttribute("distrito2",distrito2);
                    model.addAttribute("distrito3",distrito3);
                    model.addAttribute("distrito4",distrito4);
                    System.out.println("distritos hallados");
                    System.out.println(distrito1 + "-" + distrito2 + "-" + distrito3 + "-" + distrito4);
                    List<Restaurante> restaurantesxdistritodefault = restauranteRepository.listarestaurantesxdistrito(distritobuscar);
                    List<Restaurante> restaurantescercanosxdistrito1 = restauranteRepository.listarestaurantesxdistrito(distrito1);
                    List<Restaurante> restaurantescercanosxdistrito2 = restauranteRepository.listarestaurantesxdistrito(distrito2);
                    List<Restaurante> restaurantescercanosxdistrito3 = restauranteRepository.listarestaurantesxdistrito(distrito3);
                    List<Restaurante> restaurantescercanosxdistrito4 = restauranteRepository.listarestaurantesxdistrito(distrito4);
                    List<Restaurante> restauranteshallados = new ArrayList<>();
                    System.out.println("restaurantes de db");
                    for (Restaurante resthallado : restaurantesxdistritodefault) {
                        System.out.println(resthallado.getNombre());
                        System.out.println(resthallado.getDistrito().getNombredistrito());
                        restauranteshallados.add(resthallado);
                    }
                    for (Restaurante resthallado : restaurantescercanosxdistrito1) {
                        System.out.println(resthallado.getNombre());
                        System.out.println(resthallado.getDistrito().getNombredistrito());
                        restauranteshallados.add(resthallado);
                    }
                    for (Restaurante resthallado : restaurantescercanosxdistrito2) {
                        System.out.println(resthallado.getNombre());
                        System.out.println(resthallado.getDistrito().getNombredistrito());
                        restauranteshallados.add(resthallado);
                    }for (Restaurante resthallado : restaurantescercanosxdistrito3) {
                        System.out.println(resthallado.getNombre());
                        System.out.println(resthallado.getDistrito().getNombredistrito());
                        restauranteshallados.add(resthallado);
                    }for (Restaurante resthallado : restaurantescercanosxdistrito4) {
                        System.out.println(resthallado.getNombre());
                        System.out.println(resthallado.getDistrito().getNombredistrito());
                        restauranteshallados.add(resthallado);
                    }
                    model.addAttribute("listarestaurantes",restauranteshallados);
                }
                return "cliente/realizar_pedido_cliente";
            }else{
                return "redirect:/cliente/realizarpedido";
            }
        }catch (NumberFormatException e){
            System.out.println(e.getMessage());
            return "redirect:/cliente/realizarpedido";
        }
    }

    @PostMapping("/cliente/filtrarnombre")
    public String filtronombre(Model model,
                               @RequestParam(value = "searchField" ,defaultValue = "") String buscar,
                               @RequestParam(value = "direccion") int direccionxenviar,
                               @RequestParam(value = "distrito1") String distrito1, @RequestParam(value = "distrito2") String distrito2,
                               @RequestParam(value = "distrito3") String distrito3, @RequestParam(value = "distrito4") String distrito4,
                               RedirectAttributes redirectAttributes, HttpSession session){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();
        if(buscar == ""){
            return "redirect:/cliente/realizarpedido";
        }else{
            List<Plato> listaplatos = new ArrayList<>();
            listaplatos.addAll(platoRepository.buscarPlatoxNombre(distrito1,buscar));
            listaplatos.addAll(platoRepository.buscarPlatoxNombre(distrito2,buscar));
            listaplatos.addAll(platoRepository.buscarPlatoxNombre(distrito3,buscar));
            listaplatos.addAll(platoRepository.buscarPlatoxNombre(distrito4,buscar));
            List<Restaurante> listarestaurantes = new ArrayList<>();
            listarestaurantes.addAll(restauranteRepository.buscarRestaurantexNombre(distrito1,buscar));
            listarestaurantes.addAll(restauranteRepository.buscarRestaurantexNombre(distrito2,buscar));
            listarestaurantes.addAll(restauranteRepository.buscarRestaurantexNombre(distrito3,buscar));
            listarestaurantes.addAll(restauranteRepository.buscarRestaurantexNombre(distrito4,buscar));
            if(listaplatos.size()==0 && listarestaurantes.size()==0){
                redirectAttributes.addFlashAttribute("alertabusqueda", "No hay coincidencia de búsqueda");
                return "redirect:/cliente/realizarpedido";
            }else{
                List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuario_Idusuarios(idusuarioactual);
                model.addAttribute("listadirecciones", listadireccionescliente);


                Optional<Direcciones> direccionopt = direccionesRepository.findById(direccionxenviar);
                if(direccionopt.isPresent()){
                    Direcciones direccionseleccionada = direccionopt.get();
                    model.addAttribute("iddireccionxenviar",direccionxenviar);
                    model.addAttribute("direccionseleccionada",direccionseleccionada.getDireccion());
                    listaplatos.addAll(platoRepository.buscarPlatoxNombre(direccionseleccionada.getDistrito().getNombredistrito(),buscar));
                    listarestaurantes.addAll(restauranteRepository.buscarRestaurantexNombre(direccionseleccionada.getDistrito().getNombredistrito(),buscar));
                    model.addAttribute("distrito1",distrito1);
                    model.addAttribute("distrito2",distrito2);
                    model.addAttribute("distrito3",distrito3);
                    model.addAttribute("distrito4",distrito4);
                }
                model.addAttribute("listarestaurantesbuscado",listarestaurantes);
                model.addAttribute("listaplatosbuscado",listaplatos);
                model.addAttribute("nombrebuscado",buscar);
                return "cliente/busquedanombre";
            }
        }
    }

    /** restaurante a ordenar **/
     @GetMapping("/cliente/restaurantexordenar")
     public String restaurantexordenar(@RequestParam("idrestaurante") String idrest, Model model,
                                   @RequestParam("direccion") String direccion, HttpSession session,
                                       RedirectAttributes attr){
         Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
         int idusuarioactual=sessionUser.getIdusuarios();
         List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuarioactual);
         Pedidos pedidopendiente = pedidosRepository.pedidoencurso(idusuarioactual);
         try {
             int idrestaurante = Integer.parseInt(idrest);
             int direccionxenviar = Integer.parseInt(direccion);
             Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);
             Optional<Direcciones> diropt = direccionesRepository.findById(direccionxenviar);
             if (diropt.isPresent() && restopt.isPresent()) {
                 Restaurante rest = restopt.get();
                 if (restopt.isPresent()) {

                     int idrestsel = idrestaurante;
                     if (listapedidospendientes.size() >= 0 || pedidopendiente != null) {
                         if (pedidopendiente != null) {
                             String mensajependidopendiente = "No puede realizar otro pedido mientras tenga un pedido en curso";
                             attr.addFlashAttribute("hayunpedidoencurso", mensajependidopendiente);
                             return "redirect:/cliente/progresopedido";
                         }
                         for (Pedidos pedidoencurso : listapedidospendientes) {
                             idrestsel = pedidoencurso.getRestaurantepedido().getIdrestaurante();
                         }
                         if (idrestsel == idrestaurante || pedidopendiente == null) {
                             int cantreviews = restauranteRepository.cantreviews(idrestaurante);

                             List<Plato> platosxrest = platoRepository.buscarPlatosPorIdRestauranteDisponilidadActivo(idrestaurante);

                             model.addAttribute("restaurantexordenar", rest);
                             model.addAttribute("cantreviews", cantreviews);
                             model.addAttribute("platosxrest", platosxrest);
                             model.addAttribute("direccionxenviar", direccionxenviar);
                             return "cliente/restaurante_orden_cliente";
                         } else {
                             String mensajependidopendiente = "No puede realizar otro pedido a otro restaurante que sea diferente al que ya ha seleccionado.";
                             attr.addFlashAttribute("hayunpedidoencurso", mensajependidopendiente);
                             return "redirect:/cliente/carritoproductos";
                         }
                     } else {
                         int cantreviews = restauranteRepository.cantreviews(idrestaurante);

                         List<Plato> platosxrest = platoRepository.buscarPlatosPorIdRestauranteDisponilidadActivo(idrestaurante);

                         model.addAttribute("restaurantexordenar", rest);
                         model.addAttribute("cantreviews", cantreviews);
                         model.addAttribute("platosxrest", platosxrest);
                         model.addAttribute("direccionxenviar", direccionxenviar);
                         return "cliente/restaurante_orden_cliente";
                     }
                 } else {
                     return "redirect:/cliente/realizarpedido";
                 }
             } else {
                 return "redirect:/cliente/realizarpedido";
             }
         }catch(NumberFormatException e){
             return "redirect:/cliente/realizarpedido";
         }
     }

    @GetMapping("/cliente/platoxpedir")
    public String platoxpedir(Model model,
                              @RequestParam("idplato") String idplato,
                              @RequestParam("idrestaurante") String idrest,
                              @RequestParam("direccion") String direccion, HttpSession session,
                              RedirectAttributes attr) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual = sessionUser.getIdusuarios();
        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuarioactual);
        Pedidos pedidopendiente = pedidosRepository.pedidoencurso(idusuarioactual);
        try {
            int idplatopedir = Integer.parseInt(idplato);
            int idrestaurante = Integer.parseInt(idrest);
            int direccionxenviar = Integer.parseInt(direccion);
            Optional<Plato> platoopt = platoRepository.findById(idplatopedir);
            Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);
            Optional<Direcciones> diropt = direccionesRepository.findById(direccionxenviar);

            if (platoopt.isPresent() && restopt.isPresent() && diropt.isPresent()) {

                int idrestsel = idrestaurante;
                if (listapedidospendientes.size() >= 0 || pedidopendiente != null) {
                    if (pedidopendiente != null) {
                        String mensajependidopendiente = "No puede realizar otro pedido mientras tenga un pedido en curso";
                        attr.addFlashAttribute("hayunpedidoencurso", mensajependidopendiente);
                        return "redirect:/cliente/progresopedido";
                    }
                    for (Pedidos pedidoencurso : listapedidospendientes) {
                        idrestsel = pedidoencurso.getRestaurantepedido().getIdrestaurante();
                    }
                    if (idrestsel == idrestaurante || pedidopendiente == null) {
                        Plato platoseleccionado = platoopt.get();
                        model.addAttribute("platoseleccionado", platoseleccionado);
                        model.addAttribute("idrestaurante", idrestaurante);
                        model.addAttribute("iddireccionxenviar", direccionxenviar);
                        return "cliente/detalles_plato";
                    } else {
                        String mensajependidopendiente = "No puede realizar otro pedido a otro restaurante que sea diferente al que ya ha seleccionado.";
                        attr.addFlashAttribute("hayunpedidoencurso", mensajependidopendiente);
                        return "redirect:/cliente/carritoproductos";
                    }
                } else {
                    Plato platoseleccionado = platoopt.get();
                    model.addAttribute("platoseleccionado", platoseleccionado);
                    model.addAttribute("idrestaurante", idrestaurante);
                    model.addAttribute("iddireccionxenviar", direccionxenviar);
                    return "cliente/detalles_plato";
                }

            } else {
                return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante + "&direccion=" + direccionxenviar;
            }

        }catch(NumberFormatException e){
            return "redirect:/cliente/realizarpedido";
        }
    }

    @PostMapping("/cliente/platopedido")
    public String platopedido(@RequestParam("cubierto") String cubiertosxpenviar,
                              @RequestParam("cantidad") String cantidad,
                              @RequestParam("descripcion") String descripcion,
                              @RequestParam(value = "idrestaurante") String idrestaurante,
                              @RequestParam("idplato") String idplato,
                              HttpSession session,
                              Model model, RedirectAttributes redirectAttributes,
                              @RequestParam("direccion") String direccionxenviar){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idcliente=sessionUser.getIdusuarios();

    Optional<Restaurante> restauranteopt = restauranteRepository.findById(Integer.valueOf(idrestaurante));
    Optional<Plato> platoopt = platoRepository.findById(Integer.valueOf(idplato));
    Optional<Direcciones> diropt = direccionesRepository.findById(Integer.valueOf(direccionxenviar));

    try {
        if (platoopt.isPresent() && restauranteopt.isPresent() && diropt.isPresent()) {
            Plato platoelegido = platoopt.get();

            Pedidos pedidoencurso = pedidosRepository.pedidoencursoxrestaurante(idcliente, Integer.parseInt(idrestaurante));
            int cubiertos = Integer.parseInt(cubiertosxpenviar);

            if (pedidoencurso == null) {
                try {
                    if (Integer.valueOf(cantidad) > 0) {
                        if(cubiertos == 1 || cubiertos == 0) {
                            Pedidos pedidos = new Pedidos();
                            pedidos.setIdcliente(idcliente);

                            Restaurante restelegido = restauranteopt.get();

                            pedidos.setRestaurantepedido(restelegido);

                            Direcciones direccionentrega = diropt.get();

                            pedidos.setDireccionentrega(direccionentrega);
                            List<Pedidos> listapedidoscliente = pedidosRepository.findAll();
                            int tam = listapedidoscliente.size();
                            Pedidos ultimopedido = listapedidoscliente.get(tam - 1);
                            int idultimopedido = ultimopedido.getIdpedidos();
                            PedidoHasPlatoKey pedidoHasPlatoKey = new PedidoHasPlatoKey(idultimopedido, Integer.valueOf(idplato));
                            PedidoHasPlato pedidoHasPlato = new PedidoHasPlato(pedidoHasPlatoKey, pedidos, platoelegido, descripcion, Integer.valueOf(cantidad), cubiertos);
                            pedidos.addpedido(pedidoHasPlato);
                            pedidos.setMontototal("0");
                            pedidosRepository.save(pedidos);
                            listapedidoscliente = pedidosRepository.findAll();
                            tam = listapedidoscliente.size();
                            ultimopedido = listapedidoscliente.get(tam - 1);
                            idultimopedido = ultimopedido.getIdpedidos();
                            pedidoHasPlatoKey.setPedidosidpedidos(idultimopedido);
                            //PedidoHasPlatoKey pedidoHasPlatoKey = new PedidoHasPlatoKey(idultimopedido,idplato);
                            pedidoHasPlato.setId(pedidoHasPlatoKey);
                            //PedidoHasPlato pedidoHasPlato = new PedidoHasPlato(pedidoHasPlatoKey,pedidos,platoelegido,descripcion,cantidad,cubiertos);
                            pedidoHasPlatoRepository.save(pedidoHasPlato);
                        }else{
                            return "redirect:/cliente/platoxpedir?idrestaurante="+ idrestaurante + "&idplato=" + idplato + "&direccion=" + direccionxenviar;
                        }
                    } else {
                        redirectAttributes.addFlashAttribute("cantidad1", "No ha ingresado una cantidad");
                        return "redirect:/cliente/platoxpedir?idrestaurante="+ idrestaurante + "&idplato=" + idplato + "&direccion=" + direccionxenviar;
                    }
                }catch(NumberFormatException e ) {
                    return "redirect:/cliente/platoxpedir?idrestaurante="+ idrestaurante + "&idplato=" + idplato + "&direccion=" + direccionxenviar;
                }
            } else {
                try {
                    if(Integer.valueOf(cantidad) > 0 ) {
                        if (cubiertos == 1 || cubiertos == 0){
                            System.out.println("+1 plato al pedido");
                            System.out.println(platoelegido.getNombre());
                            Pedidos pedidos = pedidoencurso;
                            int idultimopedido = pedidoencurso.getIdpedidos();
                            PedidoHasPlatoKey pedidoHasPlatoKey = new PedidoHasPlatoKey(idultimopedido, Integer.valueOf(idplato));
                            PedidoHasPlato pedidoHasPlato = new PedidoHasPlato(pedidoHasPlatoKey, pedidos, platoelegido, descripcion, Integer.valueOf(cantidad), cubiertos);
                            pedidoHasPlatoKey.setPedidosidpedidos(idultimopedido);
                            //PedidoHasPlatoKey pedidoHasPlatoKey = new PedidoHasPlatoKey(idultimopedido,idplato);
                            pedidoHasPlato.setId(pedidoHasPlatoKey);
                            pedidoHasPlatoRepository.save(pedidoHasPlato);
                            redirectAttributes.addFlashAttribute("platoagregado", "Plato agregado al carrito");
                        }else{
                            return "redirect:/cliente/platoxpedir?idrestaurante="+ idrestaurante + "&idplato=" + idplato + "&direccion=" + direccionxenviar;
                        }
                    }
                    else{
                        redirectAttributes.addFlashAttribute("cantidad2", "No ha ingresado una cantidad");
                        return "redirect:/cliente/platoxpedir?idrestaurante="+ idrestaurante + "&idplato=" + idplato + "&direccion=" + direccionxenviar;
                    }
                }catch(NumberFormatException e) {
                    return "redirect:/cliente/platoxpedir?idrestaurante="+ idrestaurante + "&idplato=" + idplato + "&direccion=" + direccionxenviar;
                }
            }
            return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante + "&direccion=" + direccionxenviar;
        } else {
            return "redirect:/cliente/realizarpedido";
        }
    }catch(NumberFormatException e) {
        return "redirect:/cliente/carritoproductos";
    }

    }

    @GetMapping("/cliente/carritoproductos")
    public String carritoproductos(Model model, HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario = sessionUser.getIdusuarios();

        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);

        if(listapedidospendientes.isEmpty()){
            model.addAttribute("lista",0);
        }else{
            model.addAttribute("lista",1);
            for (Pedidos pedidoencurso : listapedidospendientes){
                List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                System.out.println(pedidoencurso.getIdpedidos());
                System.out.println(pedidoencurso.getDireccionentrega().getIddirecciones());
                MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                model.addAttribute("platosxpedido",platosxpedido);
                model.addAttribute("pedidoencurso",pedidoencurso);
                model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
            }
        }
        return "cliente/carrito_productos";
    }

    /** Eliminar plato **/
    @GetMapping("/cliente/eliminarplato")
    public String eliminarplato(HttpSession session, Model model, RedirectAttributes redirectAttributes,
                                   @RequestParam("idplato") String idplato){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        try{
            int idplatoint = Integer.parseInt(idplato);

            Optional<Plato> platoopt = platoRepository.findById(idplatoint);
            if(platoopt.isPresent()){

                List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);

                if(listapedidospendientes.isEmpty()){
                    model.addAttribute("lista",0);
                }else{
                    model.addAttribute("lista",1);

                    for (Pedidos pedidoencurso : listapedidospendientes){
                        List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                        int cantplatosrest = platosxpedido.size();

                        for(PedidoHasPlato plato1 : platosxpedido){
                            int idplatoobtenido = plato1.getPlato().getIdplato();
                            if(idplatoobtenido == idplatoint){
                                PedidoHasPlatoKey pedidoHasPlatoKey = plato1.getId();
                                pedidoHasPlatoRepository.deleteById(pedidoHasPlatoKey);
                            }
                        }
                        if(cantplatosrest == 1) {
                            pedidosRepository.deleteById(pedidoencurso.getIdpedidos());
                        }
                    }
                }
                redirectAttributes.addFlashAttribute("platoeliminado", "Plato eliminado exitosamente");
            }
        }catch(NumberFormatException exception){
            System.out.println(exception.getMessage());
        }
        return "redirect:/cliente/carritoproductos";
    }

    /** Vaciar carrito **/
    @GetMapping("/cliente/vaciarcarrrito")
    public String vaciarcarrito(Model model, HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);

        if(listapedidospendientes.isEmpty()){
            model.addAttribute("lista",0);
        }else{
            model.addAttribute("lista",1);
            for (Pedidos pedidoencurso : listapedidospendientes){
                List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                for(PedidoHasPlato plato1 : platosxpedido){
                    PedidoHasPlatoKey pedidoHasPlatoKey = plato1.getId();
                    pedidoHasPlatoRepository.deleteById(pedidoHasPlatoKey);
                }
                pedidosRepository.deleteById(pedidoencurso.getIdpedidos());
            }
        }
        return "redirect:/cliente/carritoproductos";
    }

    @GetMapping("/cliente/checkout")
    public String checkout(Model model, HttpSession session,
                           @RequestParam(value = "idmetodo",defaultValue = "0") int idmetodo){

         Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        Optional<MetodosDePago> metodoopt = metodosDePagoRepository.findById(idmetodo);
        if(metodoopt.isPresent()){
            MetodosDePago metodosel = metodoopt.get();
            model.addAttribute("metodoelegido",idmetodo);
        }
        List<MetodosDePago> listametodos = metodosDePagoRepository.findAll();
        model.addAttribute("listametodospago",listametodos);

        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);
        if(listapedidospendientes.isEmpty()){
            return "redirect:/cliente/realizarpedido";
        }else{

            for (Pedidos pedidoencurso : listapedidospendientes){
                List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                MontoPagar_PedidoHasPlatoDTO montoPagar_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montopagar(pedidoencurso.getIdpedidos());
                model.addAttribute("platosxpedido",platosxpedido);
                model.addAttribute("pedidoencurso",pedidoencurso);
                model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
                model.addAttribute("montopagar", montoPagar_pedidoHasPlatoDTO);
            }
            return "cliente/checkoutcarrito";
        }
    }

    /** Para validar tarjeta de crédito  **/
    public boolean validartarjeta(String tarjetaxevaluar) {
        boolean valido = false;
        List<String> cards = new ArrayList<String>();
        System.out.println("antes de validar");
        System.out.println(valido);
        cards.add(tarjetaxevaluar);  //Masked to avoid any inconvenience unknowingly

        String regex = "^(?:(?<visa>4[0-9]{12}(?:[0-9]{3})?)|" +
                "(?<mastercard>5[1-5][0-9]{14})|" +
                "(?<discover>6(?:011|5[0-9]{2})[0-9]{12})|" +
                "(?<amex>3[47][0-9]{13})|" +
                "(?<diners>3(?:0[0-5]|[68][0-9])?[0-9]{11})|" +
                "(?<jcb>(?:2131|1800|35[0-9]{3})[0-9]{11}))$";

        Pattern pattern = Pattern.compile(regex);

        for (String card : cards) {
            //Match the card
            Matcher matcher = pattern.matcher(card);
            if (matcher.matches()) {
                //If card is valid then verify which group it belong
                valido = true;
                System.out.println("despues de validar");
                System.out.println(matcher.group("mastercard"));
                System.out.println(matcher.group("visa"));
                System.out.println(matcher.group("discover"));
                System.out.println(matcher.group("diners"));
            }
        }
        return valido;
    }

    /** Pagar pedido **/
    @PostMapping("/cliente/guardarcheckout")
    public String getcheckout(@RequestParam(value = "idmetodo",defaultValue = "0") int idmetodo,
                              @RequestParam(value = "montoexacto",defaultValue = "0") int montoexacto,
                              @RequestParam(value = "numerotarjeta", defaultValue = "") String numerotarjeta,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        Optional<Usuario> clienteopt = usuarioRepository.findById(idusuario);
        Usuario cliente = clienteopt.get();

        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);

        if(listapedidospendientes.isEmpty()){
            return "redirect:/cliente/realizarpedido";
        }else{
            Optional<MetodosDePago> metodoopt = metodosDePagoRepository.findById(idmetodo);
            if(metodoopt.isPresent()) {
                MetodosDePago metodosel = metodoopt.get();
                model.addAttribute("metodoelegido", idmetodo);
                System.out.println(idmetodo);
                System.out.println(metodosel.getMetodo());
                for (Pedidos pedidoencurso : listapedidospendientes) {
                    List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                    System.out.println(pedidoencurso.getIdpedidos());
                    System.out.println(pedidoencurso.getDireccionentrega().getIddirecciones());
                    MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                    MontoPagar_PedidoHasPlatoDTO montoPagar_pedidoHasPlatoDTO;
                    if(pedidoencurso.getRestaurantepedido().getDistrito() == pedidoencurso.getDireccionentrega().getDistrito()){
                        montoPagar_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montopagar2(pedidoencurso.getIdpedidos());
                        model.addAttribute("montopagar", montoPagar_pedidoHasPlatoDTO);
                    }else {
                        montoPagar_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montopagar(pedidoencurso.getIdpedidos());
                        model.addAttribute("montopagar", montoPagar_pedidoHasPlatoDTO);
                    }
                    model.addAttribute("platosxpedido",platosxpedido);
                    model.addAttribute("pedidoencurso",pedidoencurso);
                    model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
                    System.out.println(montoPagar_pedidoHasPlatoDTO);
                    System.out.println(montoTotal_pedidoHasPlatoDTO);
                    pedidoencurso.setMetododepago(metodosel);
                    if(idmetodo == 3){
                        if(montoexacto != 0){
                            System.out.println(montoexacto);
                            if(montoexacto >= (montoPagar_pedidoHasPlatoDTO.getpreciopagar())){
                                pedidoencurso.setMontoexacto(String.valueOf(montoexacto));
                            }
                            else{
                                redirectAttributes.addFlashAttribute("pago1", "El monto exacto a pagar no es suficiente");
                                return "redirect:/cliente/checkout";
                            }
                        }
                        else{
                            redirectAttributes.addFlashAttribute("pago2", "No ha ingresado un monto exacto");
                            return "redirect:/cliente/checkout";
                        }
                    }
                    if(idmetodo == 1){
                        System.out.println(numerotarjeta);
                        if(numerotarjeta == null){
                            return "redirect:/cliente/checkout";
                        }else{

                            boolean tarjetavalida = validartarjeta(numerotarjeta);

                            if(tarjetavalida == true){
                                List<TarjetasOnline> tarjetasxusuario = tarjetasOnlineRepository.findAllByNumerotarjetaAndClienteEquals(numerotarjeta, cliente);

                                if(tarjetasxusuario.isEmpty()){
                                    TarjetasOnline tarjetaxguardar = new TarjetasOnline();
                                    tarjetaxguardar.setNumerotarjeta(numerotarjeta);
                                    tarjetaxguardar.setCliente(cliente);
                                    tarjetasOnlineRepository.save(tarjetaxguardar);
                                }
                            }else{
                                redirectAttributes.addFlashAttribute("tarjetanovalida", "El número de tarjeta no es válido. Las tarjetas validas son Visa, MasterCard, DinersClub, Discover, JCB");
                                return "redirect:/cliente/checkout";
                            }

                        }
                    }
                    if(pedidoencurso.getRestaurantepedido().getDistrito() == pedidoencurso.getDireccionentrega().getDistrito()){
                        pedidoencurso.setComisionrepartidor(4);
                        pedidoencurso.setComisionsistema(1);
                    }else{
                        pedidoencurso.setComisionrepartidor(6);
                        pedidoencurso.setComisionsistema(2);
                    }
                    pedidoencurso.setMontototal(String.valueOf(montoPagar_pedidoHasPlatoDTO.getpreciopagar()));
                    pedidoencurso.setEstadorestaurante("pendiente");
                    pedidoencurso.setEstadorepartidor("indefinido");
                    System.out.println(LocalDateTime.now());
                    pedidoencurso.setFechahorapedido(LocalDateTime.now());
                    pedidosRepository.save(pedidoencurso);
                }
                redirectAttributes.addFlashAttribute("checkout", "Pedido listo");
            }
            return "redirect:/cliente/paginaprincipal";
        }
    }
    /** Progreso de último pedido**/
    @GetMapping("/cliente/progresopedido")
    public String progresopedido(Model model, HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        //vista cliente nuevo
        List<Pedidos> listapedidosusuario = pedidosRepository.findAllByIdclienteEquals(idusuario);
        boolean ultimopedido1 = true; //true -> hay al menos un pedido registrado
        if(listapedidosusuario.isEmpty()){
            ultimopedido1 = false; //false -> no hay pedidos registrados
        }
        model.addAttribute("ultimopedido",ultimopedido1);

        if(ultimopedido1 == true){
            //TODO mostrar más datos en la vista de progreso pedido
            List<Pedidos> listapedidoscliente = pedidosRepository.pedidosfinxcliente(idusuario);
            int tam = listapedidoscliente.size();
            Pedidos ultimopedido = listapedidoscliente.get(tam-1);
            int idultimopedido = ultimopedido.getIdpedidos();

            List<PedidoHasPlato> pedidoHasPlatoencurso = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(idultimopedido);
            Optional<Pedidos> pedidoencursoopt = pedidosRepository.findById(pedidoHasPlatoencurso.get(0).getPedido().getIdpedidos());
            Pedidos pedidoencurso = pedidoencursoopt.get();
            model.addAttribute("pedido",pedidoencurso);
            model.addAttribute("lista",pedidoHasPlatoencurso);

            boolean calificar = false;
            if(pedidoencurso.getEstadorestaurante().equalsIgnoreCase("entregado") && pedidoencurso.getEstadorepartidor().equalsIgnoreCase("entregado")){
                calificar = true;
            }
            model.addAttribute("calificar",calificar);
            boolean cancelar = false;
            String estadorestaurante = pedidoencurso.getEstadorestaurante();
            if(estadorestaurante.equalsIgnoreCase("pendiente")){
                cancelar = true;
            }
            model.addAttribute("cancelar",cancelar);
        }

        return "cliente/ultimopedido_cliente";
    }

    /** Cancelar pedido **/
    @GetMapping("/cliente/cancelarpedido")
    public String cancelarpedido(Model model, HttpSession session,RedirectAttributes attr){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        Pedidos pedidoxcancelar = pedidosRepository.pedidoxcancelar(idusuario);
        if(pedidoxcancelar!=null){
            List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoxcancelar.getIdpedidos());
            for(PedidoHasPlato plato1 : platosxpedido){
                PedidoHasPlatoKey pedidoHasPlatoKey = plato1.getId();
                pedidoHasPlatoRepository.deleteById(pedidoHasPlatoKey);
            }
            pedidosRepository.deleteById(pedidoxcancelar.getIdpedidos());
            attr.addFlashAttribute("pedidocancelado","Pedido cancelado exitosamente");
            return "redirect:/cliente/paginaprincipal";
        }else{
            return "redirect:/cliente/progresopedido";
        }
    }

    /** Calificar pedido **/
    @GetMapping("/cliente/calificarpedido")
    public String calificarpedido(){
        return "cliente/calificarpedido";
    }

    @PostMapping("/cliente/guardarcalificacion")
    public String guardarcalificacion(Model model, HttpSession session,
                                      @RequestParam("comentarios") String comentarios,
                                      @RequestParam("estrellasrestaurante") int calrest,
                                      @RequestParam("estrellasrepartidor") int calrep){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        List<Pedidos> listapedidoscliente = pedidosRepository.pedidosfinxcliente(idusuario);
        int tam = listapedidoscliente.size();
        Pedidos ultimopedido = listapedidoscliente.get(tam-1);
        int idultimopedido = ultimopedido.getIdpedidos();

        List<PedidoHasPlato> pedidoHasPlatoencurso = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(idultimopedido);
        Optional<Pedidos> pedidoencursoopt = pedidosRepository.findById(pedidoHasPlatoencurso.get(0).getPedido().getIdpedidos());
        Pedidos pedidoencurso = pedidoencursoopt.get();

        if(calrest != 0){
            pedidoencurso.setCalificacionrestaurante(calrest);
        }
        if(calrep != 0){
            pedidoencurso.setCalificacionrepartidor(calrep);
        }
        if(comentarios != null){
            pedidoencurso.setComentario(comentarios);
        }
        return "redirect:/cliente/paginaprincipal";
    }

    /** Editar mi perfil **/
    @GetMapping("/cliente/miperfil")
    public String miperfil(
            //@ModelAttribute("usuario") Usuario usuario,
                           Model model, HttpSession session) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        Optional<Usuario> optional = usuarioRepository.findById(idusuario);

        if(optional.isPresent()){
            Usuario usuario = optional.get();
            model.addAttribute("usuario", usuario);

            List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1);
            model.addAttribute("listadirecciones", listadireccionescliente);

            List<TarjetasOnline> listatarjetas = tarjetasOnlineRepository.findAllByClienteEquals(usuario);
            model.addAttribute("listatarjetas",listatarjetas);
        }
        return "cliente/miPerfil";
    }

    @PostMapping("/cliente/miperfil")
    public String updatemiperfil(@ModelAttribute("usuario") @Valid Usuario usuario,
                                 BindingResult bindingResult,
                                 @RequestParam("pass2") String password2,
                                 HttpSession session,
                                 Model model, RedirectAttributes redirectAttributes) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");

        int idusuario=usuario.getIdusuarios();
        Optional<Usuario> usuarioopt = usuarioRepository.findById(idusuario);
        Usuario usuarioperfil = usuarioopt.get();

        if(bindingResult.hasFieldErrors("telefono") || bindingResult.hasFieldErrors("contraseniaHash")){
            if(bindingResult.hasFieldErrors("telefono")){
               String  msgT="El teléfono no es válido";
                model.addAttribute("msgT",msgT);
            }
            List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1);
            model.addAttribute("listadirecciones", listadireccionescliente);
            model.addAttribute("usuario",usuarioperfil);
            List<TarjetasOnline> listatarjetas = tarjetasOnlineRepository.findAllByClienteEquals(usuario);
            model.addAttribute("listatarjetas",listatarjetas);
            System.out.println("aqui no guarda 1");
            return "cliente/miPerfil";
        } else {
            if(password2.isEmpty()){
                int cantcontra = sessionUser.getContraseniaHash().length();
                if(usuario.getContraseniaHash().length() < cantcontra){
                    model.addAttribute("errorpatroncontra", "Debe completar el campo confirmar contraseña");
                    System.out.println("mensaje confirmar contra");
                    List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1);
                    model.addAttribute("listadirecciones", listadireccionescliente);
                    model.addAttribute("usuario",usuarioperfil);
                    List<TarjetasOnline> listatarjetas = tarjetasOnlineRepository.findAllByClienteEquals(usuario);
                    model.addAttribute("listatarjetas",listatarjetas);
                    return "cliente/miPerfil";
                }
                sessionUser.setTelefono(usuario.getTelefono());
                System.out.println("deberia guardaaar solo telefono");
                usuarioRepository.save(sessionUser);
                return "redirect:/cliente/miperfil";
            }else{
                if (usuario.getContraseniaHash().equals(password2)) {
                    String contraxvalidarpatron = usuario.getContraseniaHash();
                    boolean validarcontra = validarContrasenia(contraxvalidarpatron);
                    if (validarcontra == true) {
                        String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());
                        sessionUser.setTelefono(usuario.getTelefono());
                        sessionUser.setContraseniaHash(contraseniahashbcrypt);
                        System.out.println("deberia guardaaar");
                        redirectAttributes.addFlashAttribute("perfilact", "Cuenta actualizada exitósamente");
                        usuarioRepository.save(sessionUser);
                        return "redirect:/cliente/miperfil";
                    }else{
                        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1);
                        model.addAttribute("listadirecciones", listadireccionescliente);
                        model.addAttribute("errorpatroncontra", "La contraseña no cumple con los requisitos: mínimo 8 caracteres, un número y un caracter especial");
                        model.addAttribute("usuario",usuarioperfil);
                        List<TarjetasOnline> listatarjetas = tarjetasOnlineRepository.findAllByClienteEquals(usuario);
                        model.addAttribute("listatarjetas",listatarjetas);
                        System.out.println("aqui no guarda 2");

                        return "cliente/miPerfil";
                    }
                }else{
                    model.addAttribute("errorpatroncontra", "Las contraseñas no son iguales");
                    List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1);
                    model.addAttribute("listadirecciones", listadireccionescliente);
                    model.addAttribute("usuario",usuarioperfil);
                    List<TarjetasOnline> listatarjetas = tarjetasOnlineRepository.findAllByClienteEquals(usuario);
                    model.addAttribute("listatarjetas",listatarjetas);
                    System.out.println("aqui no guarda 3");

                    return "cliente/miPerfil";
                }
            }
        }
    }

    /** borrar dirección **/
    @GetMapping("/cliente/borrardireccion")
    public String borrardireccion(@RequestParam("iddireccion") String iddireccion,
                                  Model model) {
        try {
            Optional<Direcciones> direccionopt = direccionesRepository.findById(Integer.valueOf(iddireccion));
            Direcciones direccionborrar = direccionopt.get();
            if (direccionborrar != null) {
                direccionborrar.setActivo(0);
                direccionesRepository.save(direccionborrar);
            }
            return "redirect:/cliente/miperfil";
        }catch(NumberFormatException e){
            return "redirect:/cliente/miperfil";
        }
    }

    /** Guardar nueva dirección **/
    @GetMapping("/cliente/agregardireccion")
    public String agregardireccion(Model model) {

        List<Distritos> listadistritos = distritosRepository.findAll();
        model.addAttribute("listadistritos",listadistritos);

        return "cliente/registrarNuevaDireccion";
    }

    @PostMapping("/cliente/guardarnuevadireccion")
    public String guardarnuevadireccion(@RequestParam("direccion") String direccion,
                                        @RequestParam("iddistrito") int iddistrito,
                                        HttpSession session) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        Optional<Usuario> usuarioopt = usuarioRepository.findById(idusuario);
        Usuario usuario = usuarioopt.get();

        Direcciones direccioncrear = new Direcciones();
        direccioncrear.setDireccion(direccion);

        Optional<Distritos> distritoopt = distritosRepository.findById(iddistrito);
        if(distritoopt.isPresent()){ //validando que direccion no vacía
            Distritos distritonuevo = distritoopt.get();
            direccioncrear.setDistrito(distritonuevo);
            direccioncrear.setUsuario(usuario);
            direccioncrear.setActivo(1);
            direccionesRepository.save(direccioncrear);
        }
        return "redirect:/cliente/miperfil";
    }
}
