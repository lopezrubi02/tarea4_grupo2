package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import com.example.tarea4_grupo2.service.SendMailService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
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
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

            System.out.println("******************");
            if((!(jsonObj.get("nombres").equals(""))) && (!(jsonObj.get("nombres").equals(null)))) {
                System.out.println(jsonObj.get("nombres"));
                System.out.println("DNI valido");
                dniValido = true;
            }else{
                System.out.println("NO SE ENCONTRO POR LA API");
            }
            // Validar si existe documento

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dniValido;
    }
    /** Para validar correo **/
    public boolean isValid(String email) {
        String emailREGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailREGEX );
        if (email == null){
            return false;
        }
        return pattern .matcher(email).matches();
    }

    /**                     Registro cliente                **/
    @GetMapping("/nuevocliente")
    public String nuevoCliente(Model model,@ModelAttribute("usuario") Usuario usuario)
    {
        List<Distritos> listadistritos = distritosRepository.findAll();
        model.addAttribute("listadistritos",listadistritos);
        String direction=null;
        model.addAttribute("direction",direction);
        System.out.println(direction);
        return "cliente/registroCliente";
    }

    @PostMapping("/guardarNuevo")
    public String guardarCliente(@RequestParam("direccion_real") String direccion,
                                 @RequestParam("iddistrito") int iddistrito,
                                 @RequestParam("password2") String pass2,
                                 Model model,
                                 @ModelAttribute("usuario") @Valid Usuario usuario,
                                 BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            List<Distritos> listadistritos = distritosRepository.findAll();
            model.addAttribute("listadistritos", listadistritos);
            model.addAttribute("direction", direccion);
            boolean correovalido = isValid(usuario.getEmail());
            return "cliente/registroCliente";
        } else {
            boolean errorcorreo = validarcorreounico(usuario.getEmail(), usuario);
            boolean errorstringsexo = validarstringsexo(usuario.getSexo());
            boolean dniexiste = validarDNI(usuario.getDni());
            boolean correovalido = isValid(usuario.getEmail());
            if (errorcorreo == true || errorstringsexo == true || direccion == null || dniexiste == false || correovalido == false) {
                if(errorcorreo==true){
                    model.addAttribute("errorcorreo", "Ya hay una cuenta registrada con el correo ingresado.");
                }
                if(dniexiste == false){
                    model.addAttribute("errordni","Ingrese un DNI válido");
                }if(correovalido == false){
                    model.addAttribute("errorcorreo","Formato de correo no válido");
                }
                List<Distritos> listadistritos = distritosRepository.findAll();
                model.addAttribute("direction", direccion);
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

                        Direcciones direccionactual = new Direcciones();
                        direccion = direccion.split(",")[0];
                        direccionactual.setDireccion(direccion);
                        Optional<Distritos> distritoopt = distritosRepository.findById(iddistrito);

                        if(distritoopt.isPresent()){
                            Distritos distritosactual = distritoopt.get();
                            direccionactual.setDistrito(distritosactual);
                            direccionactual.setUsuario(usuarionuevo);
                            direccionactual.setActivo(1);
                            System.out.println("debería guardar direccion");
                            direccionesRepository.save(direccionactual);
                            System.out.println("ya guardó direccion");

                            /* Envio de correo de confirmacion */
                            String subject = "Cuenta creada en Spicyo";
                            String aws = "g-spicyo.publicvm.com";
                            String direccionurl = "http://" + aws + ":8080/login";
                            String mensaje = "¡Hola!<br><br>" +
                                    "Ahora es parte de Spicyo. Para ingresar a su cuenta haga click: <a href='" + direccionurl + "'>AQUÍ</a> <br><br>Atte. Equipo de Spicy :D</b>";
                            String correoDestino = usuario.getEmail();
                            sendMailService.sendMail(correoDestino, "saritaatanacioarenas@gmail.com", subject, mensaje);

                            return "cliente/confirmarCuenta";
                        }else{
                            List<Distritos> listadistritos = distritosRepository.findAll();
                            model.addAttribute("listadistritos", listadistritos);
                            model.addAttribute("direction", direccion);
                            return "cliente/registroCliente";
                        }
                    } else {
                        List<Distritos> listadistritos = distritosRepository.findAll();
                        model.addAttribute("listadistritos", listadistritos);
                        model.addAttribute("errorpatroncontra", "La contraseña no cumple con los requisitos mínimos.");
                        model.addAttribute("usuario", usuario);
                        model.addAttribute("direction", direccion);
                        return "cliente/registroCliente";
                    }
                } else {
                    List<Distritos> listadistritos = distritosRepository.findAll();
                    model.addAttribute("listadistritos", listadistritos);
                    model.addAttribute("errorpatroncontra", "Las contraseñas no coinciden");
                    model.addAttribute("direction", direccion);
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// Date format: yyyy-MM-dd year month day yyyy-MM-dd HH:mm:ss year month day hour minute second
        List<PedidosclienteaexcelDTO> listapedidos = pedidosRepository.listapedidosexcel(id);
        int initRow = 1;
        for (PedidosclienteaexcelDTO pedidoexcel : listapedidos) {
            row = sheet.createRow(initRow);
            row.createCell(0).setCellValue(pedidoexcel.getMontototal());
            row.createCell(1).setCellValue(pedidoexcel.getNombre());
            row.createCell(2).setCellValue(sdf.format(pedidoexcel.getFechahorapedido()));
            row.createCell(3).setCellValue(pedidoexcel.getDireccion());
            row.createCell(4).setCellValue(pedidoexcel.getMetodo());
            initRow++;
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);

        workbook.write(stream);
        workbook.close();
        return new ByteArrayInputStream(stream.toByteArray());
    }

    @GetMapping("/cliente/reportes")
    public String reportesCliente(Model model,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarios = sessionUser.getIdusuarios();

        List<PedidosclienteaexcelDTO> listapedidosusuario = pedidosRepository.listapedidosexcel(idusuarios);
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

    /** Realizar pedido **/
    @GetMapping("/cliente/realizarpedido")
    public String realizarpedido(Model model, HttpSession session, RedirectAttributes attr,
                                 @RequestParam(value = "idcategoriarest" ,defaultValue = "0") String categoriarest,
                                 @RequestParam(value = "preciopromedio", defaultValue = "0") String preciopromedio,
                                 @RequestParam(value = "direccion", defaultValue = "0") String direccion,
                                 @RequestParam(value = "calificacion", defaultValue = "0") String calificacionpromedio
                                 ) {
        System.out.println("************************************");
        System.out.println(direccion);
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();
        Optional<Usuario> useropt = usuarioRepository.findById(idusuarioactual);
        Usuario user = useropt.get();
        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuarioactual);
        Pedidos pedidopendiente = pedidosRepository.pedidoencurso(idusuarioactual);
        System.out.println("TRACER 0 ******");
        if(listapedidospendientes.size() >= 1 || (pedidopendiente != null && !pedidopendiente.getEstadorestaurante().equalsIgnoreCase("rechazado"))){
            System.out.println("TRACER 1*******");
            String mensajependidopendiente = "No puede realizar otro pedido a otro restaurante que sea diferente al que ya ha seleccionado.";
            if(pedidopendiente != null && !pedidopendiente.getEstadorestaurante().equalsIgnoreCase("rechazado")){                mensajependidopendiente = "No puede realizar otro pedido mientras tenga un pedido en curso";
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

            List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuarioAndActivoEquals(user, 1);
            List<Categorias> listacategorias = categoriasRepository.findAll();
            model.addAttribute("listacategorias", listacategorias);
            model.addAttribute("listadirecciones", listadireccionescliente);

            try{
                int direccionxenviar = Integer.parseInt(direccion);

                List<Restaurante> restauranteshallados = new ArrayList<>();
                if (direccionxenviar == 0) {
                    model.addAttribute("direccionseleccionada", listadireccionescliente.get(0).getDireccion());
                    model.addAttribute("iddireccionxenviar", listadireccionescliente.get(0).getIddirecciones());
                    //para mostrar restaurantes de acuerdo a direccion mostrada por default
                    String distritobuscar = listadireccionescliente.get(0).getDistrito().getNombredistrito();
                    System.out.println("DISTRITO DE DIRECCION SELECCIONADA");
                    System.out.println(distritobuscar);

                    List<Restaurante> restaurantescercanosxdistritodefault = restauranteRepository.listarestaurantesxdistrito(distritobuscar);
                    for (Restaurante resthallado : restaurantescercanosxdistritodefault) {
                        if(resthallado.getUsuario().getCuentaActiva()==1){
                            restauranteshallados.add(resthallado);
                            System.out.println(resthallado.getNombre());
                            System.out.println(resthallado.getDistrito().getNombredistrito());
                        }
                    }
                    Distritos dist = distritosRepository.findByNombredistrito(distritobuscar);
                    String listadr = dist.getDistritosalrededor();
                    for (int i = 0; i < listadr.split(",").length; i++) {
                        System.out.println(listadr.split(",")[i]);
                        List<Restaurante> restaurantescercanosxdistrito = restauranteRepository.listarestaurantesxdistrito(listadr.split(",")[i]);
                        for (Restaurante resthallado : restaurantescercanosxdistrito) {
                            if(resthallado.getUsuario().getCuentaActiva()==1){
                                restauranteshallados.add(resthallado);
                                System.out.println(resthallado.getNombre());
                                System.out.println(resthallado.getDistrito().getNombredistrito());
                            }
                        }
                    }

                    HashSet<Restaurante> set = new HashSet<Restaurante>(restauranteshallados);
                    restauranteshallados = new ArrayList<Restaurante>(set);
                    model.addAttribute("restalrededor",listadr);
                    model.addAttribute("listarestaurantes",restauranteshallados);
                } else {
                    Optional<Direcciones> direccionopt = Optional.ofNullable(direccionesRepository.findDireccionesByIddireccionesAndUsuario_Idusuarios(direccionxenviar, idusuarioactual));
                    if (direccionopt.isPresent()) {
                        Direcciones direccionseleccionada = direccionopt.get();
                        model.addAttribute("iddireccionxenviar", direccionxenviar);
                        model.addAttribute("direccionseleccionada", direccionseleccionada.getDireccion());
                        //para mostrar restaurantes de acuerdo a direccion elegida
                        String distritobuscar = direccionseleccionada.getDistrito().getNombredistrito();
                        System.out.println("DISTRITO A BUSCAR 2");
                        System.out.println(distritobuscar);

                        List<Restaurante> restaurantescercanosxdistritodefault = restauranteRepository.listarestaurantesxdistrito(distritobuscar);
                        for (Restaurante resthallado : restaurantescercanosxdistritodefault) {
                            if(resthallado.getUsuario().getCuentaActiva()==1){
                                restauranteshallados.add(resthallado);
                                System.out.println(resthallado.getNombre());
                                System.out.println(resthallado.getDistrito().getNombredistrito());
                            }
                        }
                        Distritos dist = distritosRepository.findByNombredistrito(distritobuscar);
                        String listadr = dist.getDistritosalrededor();
                        for (int i = 0; i < listadr.split(",").length; i++) {
                            System.out.println(listadr.split(",")[i]);
                            List<Restaurante> restaurantescercanosxdistrito = restauranteRepository.listarestaurantesxdistrito(listadr.split(",")[i]);
                            for (Restaurante resthallado : restaurantescercanosxdistrito) {
                                if(resthallado.getUsuario().getCuentaActiva()==1){
                                    restauranteshallados.add(resthallado);
                                    System.out.println(resthallado.getNombre());
                                    System.out.println(resthallado.getDistrito().getNombredistrito());
                                }
                            }
                        }

                        HashSet<Restaurante> set = new HashSet<Restaurante>(restauranteshallados);
                        restauranteshallados = new ArrayList<Restaurante>(set);
                        if(restauranteshallados.isEmpty()){
                            model.addAttribute("listavacia","No hay restaurantes cercanos cerca a tu zona");
                            return "redirect:/cliente/realizarpedido";
                        }
                        model.addAttribute("restalrededor",listadr);
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
                        System.out.println("FILTRO CATEGORIAS");
                        for(Restaurante resthallados : restauranteshallados){
                            List<Categorias> catxrest = resthallados.getCategoriasrestList();
                            for(Categorias cat : catxrest){
                                if(cat.getIdcategorias() == idcategoriarest){
                                    System.out.println("RESTAURANTE HALLADO FILTRO CATEGORIA");
                                    System.out.println(resthallados.getNombre());
                                    listarestauranteseleccionado.add(resthallados);
                                }
                            }
                        }
                        if(listarestauranteseleccionado.isEmpty()){
                            System.out.println("MANDA MENSAJE FILTRO CATEGORIAS");
                            model.addAttribute("alertaprecio","No se encontraron restaurantes para el filtro aplicado");
                        }
                        model.addAttribute("listarestaurantes",listarestauranteseleccionado);
                        model.addAttribute("catelegida",idcategoriarest);
                    }else{
                        return "redirect:/cliente/realizarpedido";
                    }
                }
                //filtro precios
                int precio = Integer.parseInt(preciopromedio);
                List<Restaurante> listaRestFiltroPrecio = new ArrayList<>();
                if(precio!=0) {
                    System.out.println("FILTRO PRECIO");
                    switch (precio) {
                        case 1:
                            for(Restaurante rest : restauranteshallados){
                                System.out.println("RESTAURANTES HALLADOS FILTRO PRECIO");
                                listaRestFiltroPrecio.addAll(restauranteRepository.listarestprecio1(rest.getDistrito().getNombredistrito()));
                            }
                            break;
                        case 2:
                            for(Restaurante rest : restauranteshallados){
                                System.out.println("RESTAURANTES HALLADOS FILTRO PRECIO");
                                listaRestFiltroPrecio.addAll(restauranteRepository.listarestprecio2(rest.getDistrito().getNombredistrito()));
                            }
                            break;
                        case 3:
                            for(Restaurante rest : restauranteshallados){
                                System.out.println("RESTAURANTES HALLADOS FILTRO PRECIO");
                                listaRestFiltroPrecio.addAll(restauranteRepository.listarestprecio3(rest.getDistrito().getNombredistrito()));
                            }
                            break;
                        case 4:
                            for(Restaurante rest : restauranteshallados){
                                System.out.println("RESTAURANTES HALLADOS FILTRO PRECIO");
                                listaRestFiltroPrecio.addAll(restauranteRepository.listarestprecio4(rest.getDistrito().getNombredistrito()));
                            }
                            break;
                    }
                    if (listaRestFiltroPrecio.isEmpty()) {
                        model.addAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                    }
                    model.addAttribute("listarestaurantes", listaRestFiltroPrecio);
                    model.addAttribute("precioselec", precio);
                }
                //filtro calificaciones
                int calificacion = Integer.parseInt(calificacionpromedio);
                if(calificacion!=0) {
                    if (calificacion > 4) {
                        return "redirect:/cliente/realizarpedido";
                    } else {
                        System.out.println("FILTRO CALIFICACIONES");
                        List<Restaurante> listarestcal = new ArrayList<>();
                        for(Restaurante resthallados : restauranteshallados){
                            System.out.println("CALIFICACIONES A COMPARAR");
                            if(resthallados.getCalificacionpromedio() != null){
                                System.out.println(Math.round(resthallados.getCalificacionpromedio()));
                                if(Math.round(resthallados.getCalificacionpromedio()) == calificacion){
                                    listarestcal.add(resthallados);
                                    System.out.println("RESTAURANTES HALLADOS");
                                    System.out.println(resthallados.getCalificacionpromedio());
                                    System.out.println(resthallados.getNombre());
                                }
                            }
                        }
                        model.addAttribute("listarestaurantes", listarestcal);
                        model.addAttribute("calsel", calificacion);
                        if (listarestcal.isEmpty()) {
                            model.addAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
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
        Optional<Usuario> useropt = usuarioRepository.findById(idusuarioactual);
        Usuario user = useropt.get();
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
                List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuarioAndActivoEquals(user, 1);
                List<Categorias> listacategorias = categoriasRepository.findAll();
                model.addAttribute("listacategorias", listacategorias);
                model.addAttribute("listadirecciones", listadireccionescliente);
                model.addAttribute("iddireccionxenviar",direccionxenviar);
                model.addAttribute("direccionseleccionada",direccionseleccionada.getDireccion());
                //para mostrar restaurantes de acuerdo a direccion seleccionada
                String distritobuscar = direccionseleccionada.getDistrito().getNombredistrito();
                System.out.println("DISTRITO A BUSCAR 3");
                System.out.println(distritobuscar);
                List<Restaurante> restauranteshallados = new ArrayList<>();

                List<Restaurante> restaurantescercanosxdistritodefault = restauranteRepository.listarestaurantesxdistrito(distritobuscar);
                for (Restaurante resthallado : restaurantescercanosxdistritodefault) {
                    if(resthallado.getUsuario().getCuentaActiva()==1){
                        restauranteshallados.add(resthallado);
                        System.out.println(resthallado.getNombre());
                        System.out.println(resthallado.getDistrito().getNombredistrito());
                    }
                }
                Distritos dist = distritosRepository.findByNombredistrito(distritobuscar);
                String listadr = dist.getDistritosalrededor();
                for (int i = 0; i < listadr.split(",").length; i++) {
                    System.out.println(listadr.split(",")[i]);
                    List<Restaurante> restaurantescercanosxdistrito = restauranteRepository.listarestaurantesxdistrito(listadr.split(",")[i]);
                    for (Restaurante resthallado : restaurantescercanosxdistrito) {
                        if(resthallado.getUsuario().getCuentaActiva()==1){
                            restauranteshallados.add(resthallado);
                            System.out.println(resthallado.getNombre());
                            System.out.println(resthallado.getDistrito().getNombredistrito());
                        }
                    }
                }
                HashSet<Restaurante> set = new HashSet<Restaurante>(restauranteshallados);
                restauranteshallados = new ArrayList<Restaurante>(set);
                System.out.println(restauranteshallados.size());
                if(restauranteshallados.isEmpty()){
                    model.addAttribute("listavacia","No hay restaurantes cercanos a tu zona");
                }else{
                    model.addAttribute("restalrededor",listadr);
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
                               @RequestParam(value = "direccion") String direccion,
                               @RequestParam(value = "restalrededor") String listadr,
                               RedirectAttributes redirectAttributes, HttpSession session){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();
        Optional<Usuario> useropt = usuarioRepository.findById(idusuarioactual);
        Usuario user = useropt.get();
        try{
            int direccionxenviar = Integer.parseInt(direccion);
            if(buscar.equalsIgnoreCase("")){
                System.out.println("REGRESA A VISTA REALIZAR PEDIDO");
                Optional<Direcciones> direccionopt = direccionesRepository.findById(direccionxenviar);
                if(direccionopt.isPresent()){
                    return "redirect:/cliente/realizarpedido?direccion=" + direccionxenviar;
                }else{
                 return "redirect:/cliente/realizarpedido";
             }
            }else {
                System.out.println("DISTRITOS RECIBE FILTRO NOMBRE");
                List<Plato> listaplatos = new ArrayList<>();
                for (int i = 0; i < listadr.split(",").length; i++) {
                    System.out.println(listadr.split(",")[i]);
                    List<Plato> platoshallados = platoRepository.buscarPlatoxNombre(listadr.split(",")[i], buscar);
                    for (Plato platomostrar : platoshallados) {
                        if (platomostrar.getActivo() == 1) {
                            listaplatos.add(platomostrar);
                        }
                    }
                }
            List<Restaurante> listarestaurantes = new ArrayList<>();
            for (int i = 0; i < listadr.split(",").length; i++) {
                System.out.println(listadr.split(",")[i]);
                List<Restaurante> restaurantescercanosxdistrito = restauranteRepository.buscarRestaurantexNombre(listadr.split(",")[i], buscar);
                for (Restaurante resthallado : restaurantescercanosxdistrito) {
                    if (resthallado.getUsuario().getCuentaActiva() == 1) {
                        listarestaurantes.add(resthallado);
                        System.out.println(resthallado.getNombre());
                        System.out.println(resthallado.getDistrito().getNombredistrito());
                    }
                }
            }
            Optional<Direcciones> direccionopt = direccionesRepository.findById(direccionxenviar);
            if (direccionopt.isPresent()) {
                Direcciones direccionseleccionada = direccionopt.get();
                model.addAttribute("iddireccionxenviar", direccionxenviar);
                model.addAttribute("direccionseleccionada", direccionseleccionada.getDireccion());
                listaplatos.addAll(platoRepository.buscarPlatoxNombre(direccionseleccionada.getDistrito().getNombredistrito(), buscar));
                listarestaurantes.addAll(restauranteRepository.buscarRestaurantexNombre(direccionseleccionada.getDistrito().getNombredistrito(), buscar));
                model.addAttribute("restalrededor", listadr);
            }
            List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuarioAndActivoEquals(user, 1);
            model.addAttribute("listadirecciones", listadireccionescliente);

            HashSet<Restaurante> set = new HashSet<Restaurante>(listarestaurantes);
            listarestaurantes = new ArrayList<Restaurante>(set);
            HashSet<Plato> set2 = new HashSet<Plato>(listaplatos);
            listaplatos = new ArrayList<Plato>(set2);
            if(listaplatos.size() == 0 && listarestaurantes.size() == 0) {
                System.out.println("NO ENCONTRÓ BUSQUEDA FILTRO POR NOMBRE");
                model.addAttribute("alertabusqueda", "No hay coincidencia de búsqueda");
            }else {
                System.out.println("DEBIÓ ENCONTRAR BUSQUEDA FILTRO POR NOMBRE");
            }
            model.addAttribute("nombrebuscado", buscar);
            model.addAttribute("listaplatosbuscado", listaplatos);
            model.addAttribute("listarestaurantesbuscado", listarestaurantes);
            return "cliente/busquedanombre";
        }

        }catch(NumberFormatException e){
            return "redirect:/cliente/realizarpedido";
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
             Optional<Direcciones> diropt = Optional.ofNullable(direccionesRepository.findDireccionesByIddireccionesAndUsuario_Idusuarios(direccionxenviar, idusuarioactual));
             if (diropt.isPresent() && restopt.isPresent()) {
                 //Para verificar que el idrest recibido es cercano a la direccion recibida
                 Direcciones direccionseleccionada = diropt.get();
                 String distritobuscar = direccionseleccionada.getDistrito().getNombredistrito();
                 List<Restaurante> restauranteshallados = new ArrayList<>();
                 List<Restaurante> restaurantescercanosxdistritodefault = restauranteRepository.listarestaurantesxdistrito(distritobuscar);
                 for (Restaurante resthallado : restaurantescercanosxdistritodefault) {
                     if(resthallado.getUsuario().getCuentaActiva()==1){
                         restauranteshallados.add(resthallado);
                         System.out.println(resthallado.getNombre());
                         System.out.println(resthallado.getDistrito().getNombredistrito());
                     }
                 }
                 Distritos dist = distritosRepository.findByNombredistrito(distritobuscar);
                 String listadr = dist.getDistritosalrededor();
                 for (int i = 0; i < listadr.split(",").length; i++) {
                     System.out.println(listadr.split(",")[i]);
                     List<Restaurante> restaurantescercanosxdistrito = restauranteRepository.listarestaurantesxdistrito(listadr.split(",")[i]);
                     for (Restaurante resthallado : restaurantescercanosxdistrito) {
                         if(resthallado.getUsuario().getCuentaActiva()==1){
                             restauranteshallados.add(resthallado);
                             System.out.println(resthallado.getNombre());
                             System.out.println(resthallado.getDistrito().getNombredistrito());
                         }
                     }
                 }

                 boolean continuar = false;
                 for(Restaurante restobt : restauranteshallados){
                     if(restobt.getIdrestaurante() == idrestaurante){
                         continuar = true;
                     }
                 }
                 Restaurante rest = restopt.get();
                 if(continuar == true && rest.getUsuario().getCuentaActiva()==1){
                     if (restopt.isPresent()) {
                         int idrestsel = idrestaurante;
                         System.out.println("VALIDACIONES NO REALIZAR PEDIDO");
                         if(listapedidospendientes.size() >= 1 || (pedidopendiente != null && !pedidopendiente.getEstadorestaurante().equalsIgnoreCase("rechazado"))){
                             if (pedidopendiente != null && !pedidopendiente.getEstadorestaurante().equalsIgnoreCase("rechazado")) {                                 System.out.println("HAY PEDIDO PENDIENTE");
                                 String mensajependidopendiente = "No puede realizar otro pedido mientras tenga un pedido en curso";
                                 attr.addFlashAttribute("hayunpedidoencurso", mensajependidopendiente);
                                 return "redirect:/cliente/progresopedido";
                             }
                             System.out.println("PEDIDO EN CURSO ");
                             for (Pedidos pedidoencurso : listapedidospendientes) {
                                 idrestsel = pedidoencurso.getRestaurantepedido().getIdrestaurante();
                             }
                             if (idrestsel == idrestaurante && pedidopendiente == null) {
                                 int cantreviews = restauranteRepository.cantreviews(idrestaurante);
                                 List<Plato> platosxrest = platoRepository.buscarPlatosPorIdRestauranteDisponilidadActivo(idrestaurante);
                                 model.addAttribute("restaurantexordenar", rest);
                                 model.addAttribute("cantreviews", cantreviews);
                                 model.addAttribute("platosxrest", platosxrest);
                                 model.addAttribute("direccionxenviar", direccionxenviar);
                                 return "cliente/restaurante_orden_cliente";
                             }else{
                                 System.out.println("YA SELECCIONÓ UN RESTAURANTE");
                                 String mensajependidopendiente = "No puede realizar otro pedido a otro restaurante que sea diferente al que ya ha seleccionado.";
                                 attr.addFlashAttribute("hayunpedidoencurso", mensajependidopendiente);
                                 return "redirect:/cliente/carritoproductos";
                             }
                         }else {
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
                 }else{
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
            // TODO validar plato activo en 1 y disponibilidad en 1, caso contrario redirigir a vista restaurantexpedir
            Optional<Plato> platoopt = platoRepository.findById(idplatopedir);
            Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);
            Optional<Direcciones> diropt = Optional.ofNullable(direccionesRepository.findDireccionesByIddireccionesAndUsuario_Idusuarios(direccionxenviar, idusuarioactual));

            if (platoopt.isPresent() && restopt.isPresent() && diropt.isPresent()) {
                //Para verificar que el idrest recibido es cercano a la direccion recibida
                Direcciones direccionseleccionada = diropt.get();
                String distritobuscar = direccionseleccionada.getDistrito().getNombredistrito();
                List<Restaurante> restauranteshallados = new ArrayList<>();
                List<Restaurante> restaurantescercanosxdistritodefault = restauranteRepository.listarestaurantesxdistrito(distritobuscar);
                for (Restaurante resthallado : restaurantescercanosxdistritodefault) {
                    if(resthallado.getUsuario().getCuentaActiva()==1){
                        restauranteshallados.add(resthallado);
                        System.out.println(resthallado.getNombre());
                        System.out.println(resthallado.getDistrito().getNombredistrito());
                    }
                }
                Distritos dist = distritosRepository.findByNombredistrito(distritobuscar);
                String listadr = dist.getDistritosalrededor();
                for (int i = 0; i < listadr.split(",").length; i++) {
                    System.out.println(listadr.split(",")[i]);
                    List<Restaurante> restaurantescercanosxdistrito = restauranteRepository.listarestaurantesxdistrito(listadr.split(",")[i]);
                    for (Restaurante resthallado : restaurantescercanosxdistrito) {
                        if(resthallado.getUsuario().getCuentaActiva()==1){
                            restauranteshallados.add(resthallado);
                            System.out.println(resthallado.getNombre());
                            System.out.println(resthallado.getDistrito().getNombredistrito());
                        }
                    }
                }

                boolean continuar = false;
                for(Restaurante restobt : restauranteshallados){
                    if(restobt.getIdrestaurante() == idrestaurante){
                        continuar = true;
                    }
                }
                Restaurante rest = restopt.get();
                if(continuar == true && rest.getUsuario().getCuentaActiva() == 1){
                    int idrestsel = idrestaurante;
                    if(listapedidospendientes.size() >= 1 || (pedidopendiente != null && !pedidopendiente.getEstadorestaurante().equalsIgnoreCase("rechazado"))){
                        if (pedidopendiente != null && !pedidopendiente.getEstadorestaurante().equalsIgnoreCase("rechazado")) {                            String mensajependidopendiente = "No puede realizar otro pedido mientras tenga un pedido en curso";
                            attr.addFlashAttribute("hayunpedidoencurso", mensajependidopendiente);
                            return "redirect:/cliente/progresopedido";
                        }
                        for (Pedidos pedidoencurso : listapedidospendientes) {
                            idrestsel = pedidoencurso.getRestaurantepedido().getIdrestaurante();
                        }
                        Plato platoseleccionado = platoopt.get();
                        if (idrestsel == idrestaurante && pedidopendiente == null  && platoseleccionado.getActivo() == 1 && platoseleccionado.getDisponibilidad() == 1) {
                            Pedidos pedidoencurso = pedidosRepository.pedidoencursoxrestaurante(idusuarioactual, idrestaurante);
                            if (pedidoencurso != null) {
                                List<PedidoHasPlato> pedidoencurso2 = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                                if (!pedidoencurso2.isEmpty()) {
                                    for (PedidoHasPlato pedidoencurso3 : pedidoencurso2) {
                                        if (pedidoencurso3.getPlato().getIdplato() == idplatopedir) {
                                            String comentariohecho = pedidoencurso3.getDescripcion();
                                            int cantpedida = pedidoencurso3.getCantidadplatos();
                                            int cubiertoelegido = pedidoencurso3.isCubiertos();
                                            System.out.println("HAY PEDIDO EN CURSO, MANDA A EDITAR PLATO **************");
                                            System.out.println(comentariohecho);
                                            System.out.println(cantpedida);
                                            System.out.println(cubiertoelegido);
                                            model.addAttribute("comentariohecho", comentariohecho);
                                            model.addAttribute("cantpedida", cantpedida);
                                            model.addAttribute("cubiertoelegido", cubiertoelegido);
                                        }
                                    }
                                }
                            }

                            model.addAttribute("platoseleccionado", platoseleccionado);
                            model.addAttribute("idrestaurante", idrestaurante);
                            model.addAttribute("iddireccionxenviar", direccionxenviar);
                            return "cliente/detalles_plato";
                        } else {
                                if(platoseleccionado.getActivo() == 0 && platoseleccionado.getDisponibilidad() == 0) {
                                    return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante + "&direccion=" + direccionxenviar;
                                }
                                else{
                                    if(idrestsel != idrestaurante && pedidopendiente != null){
                                        String mensajependidopendiente = "No puede realizar otro pedido a otro restaurante que sea diferente al que ya ha seleccionado.";
                                        attr.addFlashAttribute("hayunpedidoencurso", mensajependidopendiente);

                                    }
                                    return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante + "&direccion=" + direccionxenviar;
                                }
                        }
                    } else {
                        Plato platoseleccionado = platoopt.get();
                        model.addAttribute("platoseleccionado", platoseleccionado);
                        model.addAttribute("idrestaurante", idrestaurante);
                        model.addAttribute("iddireccionxenviar", direccionxenviar);
                        return "cliente/detalles_plato";
                    }
                }else{
                    return "redirect:/cliente/realizarpedido";
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
            // TODO validar plato activo en 1 y disponibilidad en 1, caso contrario redirigir a vista restaurantexpedir
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
                            pedidoHasPlato.setId(pedidoHasPlatoKey);
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
    public String carritoproductos(Model model, HttpSession session, RedirectAttributes redirectAttributes){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario = sessionUser.getIdusuarios();

        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);

        if(listapedidospendientes.isEmpty()){
            model.addAttribute("lista",0);
        }else{
            model.addAttribute("lista",1);
            for (Pedidos pedidoencurso : listapedidospendientes){
                // TODO validar plato activo en 1 y disponibilidad en 1, caso contrario borrar ese plato de la db
                List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                boolean carritoactualizado = false;
                for (PedidoHasPlato php : platosxpedido){
                    System.out.println("TRACER 2 ****************************");
                    System.out.println(php.getPlato().getActivo());
                    System.out.println(php.getPlato().getDisponibilidad());
                    if(php.getPlato().getActivo() == 0 || php.getPlato().getDisponibilidad() == 0){
                        System.out.println("TRACER 3  *****************");
                        PedidoHasPlatoKey pedidoHasPlatoKey = php.getId();
                        System.out.println(pedidoHasPlatoKey.getPedidosidpedidos());
                        pedidoHasPlatoRepository.deleteById(pedidoHasPlatoKey);//si el pedido solo tiene un plato, como se va a eliminar el plato, se deberia eliminar el pedido
                        System.out.println("debe borrar plato");
                        carritoactualizado = true;
                    }
                }
                if(carritoactualizado){
                    redirectAttributes.addFlashAttribute("carritoact","Tu carrito se ha actualizado");
                }
                System.out.println(pedidoencurso.getIdpedidos());
                System.out.println(pedidoencurso.getDireccionentrega().getIddirecciones());
                try {
                    MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                    platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                    int descuento = pedidoHasPlatoRepository.descuento(pedidoencurso.getIdpedidos());
                    int preciodescuento =  montoTotal_pedidoHasPlatoDTO.getpreciototal() - descuento;
                    model.addAttribute("platosxpedido", platosxpedido);
                    model.addAttribute("pedidoencurso", pedidoencurso);
                    model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
                    model.addAttribute("preciodescuento", preciodescuento);
                    model.addAttribute("descuento", Integer.parseInt(String.valueOf(descuento)));
                    System.out.println(LocalDateTime.now());
                    pedidoencurso.setFechahorapedido(LocalDateTime.now());
                }catch(Exception e){
                    MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                    platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                    int descuento = 0;
                    int preciodescuento = montoTotal_pedidoHasPlatoDTO.getpreciototal() - descuento;
                    model.addAttribute("platosxpedido", platosxpedido);
                    model.addAttribute("pedidoencurso", pedidoencurso);
                    model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
                    model.addAttribute("preciodescuento", preciodescuento);
                    model.addAttribute("descuento", Integer.parseInt(String.valueOf(descuento)));
                    System.out.println(LocalDateTime.now());
                    pedidoencurso.setFechahorapedido(LocalDateTime.now());
                }
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
                           @RequestParam(value = "idmetodo",defaultValue = "0") int idmetodo, RedirectAttributes attr){

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
                try {

                    List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                        // TODO validar plato activo en 1 y disponibilidad en 1, caso contrario borrar ese plato de la db
                        boolean carritoactualizado = false;
                        for (PedidoHasPlato php : platosxpedido) {
                            System.out.println("TRACER 2 ****************************");
                            System.out.println(php.getPlato().getActivo());
                            System.out.println(php.getPlato().getDisponibilidad());
                            if (php.getPlato().getActivo() == 0 || php.getPlato().getDisponibilidad() == 0) {
                                System.out.println("TRACER 3  *****************");
                                PedidoHasPlatoKey pedidoHasPlatoKey = php.getId();
                                System.out.println(pedidoHasPlatoKey.getPedidosidpedidos());
                                pedidoHasPlatoRepository.deleteById(pedidoHasPlatoKey);//si el pedido solo tiene un plato, como se va a eliminar el plato, se deberia eliminar el pedido
                                System.out.println("debe borrar plato");
                                carritoactualizado = true;
                            }
                        }
                        if(carritoactualizado){
                            attr.addFlashAttribute("carritoact","Tu carrito se ha actualizado");
                        }
                    MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                    int montoPagar = pedidoHasPlatoRepository.pagarTodo(pedidoencurso.getIdpedidos());
                    int descuento = pedidoHasPlatoRepository.descuento(pedidoencurso.getIdpedidos());
                    int montototal_pagar = montoPagar - descuento;
                    int preciodescuento = montoTotal_pedidoHasPlatoDTO.getpreciototal() - descuento;
                    model.addAttribute("platosxpedido",platosxpedido);
                    model.addAttribute("pedidoencurso",pedidoencurso);
                    model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
                    model.addAttribute("montopagar", montototal_pagar);
                    model.addAttribute("preciodescuento", preciodescuento);
                    model.addAttribute("descuento", descuento);
                }catch(Exception e){
                    List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                    MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                    int montoPagar = pedidoHasPlatoRepository.pagarTodo(pedidoencurso.getIdpedidos());
                    int descuento = 0;
                    int montototal_pagar = montoPagar - descuento;
                    int preciodescuento = montoTotal_pedidoHasPlatoDTO.getpreciototal() - descuento;
                    model.addAttribute("platosxpedido",platosxpedido);
                    model.addAttribute("pedidoencurso",pedidoencurso);
                    model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
                    model.addAttribute("preciodescuento", preciodescuento);
                    model.addAttribute("montopagar", montototal_pagar);
                    model.addAttribute("descuento", descuento);

                }
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
                    try {
                        List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                        System.out.println(pedidoencurso.getIdpedidos());
                        System.out.println(pedidoencurso.getDireccionentrega().getIddirecciones());
                        MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                        int pagarTodo = pedidoHasPlatoRepository.pagarTodo(pedidoencurso.getIdpedidos());
                        int descuento = pedidoHasPlatoRepository.descuento(pedidoencurso.getIdpedidos());
                        int montototal_pagar = pagarTodo - descuento;
                        model.addAttribute("platosxpedido", platosxpedido);
                        model.addAttribute("pedidoencurso", pedidoencurso);
                        model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
                        model.addAttribute("montopagar", montototal_pagar);

                        System.out.println(pagarTodo);
                        System.out.println(descuento);
                        System.out.println(montototal_pagar);
                        System.out.println(montoTotal_pedidoHasPlatoDTO);
                        pedidoencurso.setMontototal(String.valueOf(montototal_pagar));
                        pedidoencurso.setMetododepago(metodosel);

                        if (idmetodo == 3) {
                            if (montoexacto != 0) {
                                System.out.println(montoexacto);
                                if (montoexacto >= (montototal_pagar)) {
                                    pedidoencurso.setMontoexacto(String.valueOf(montoexacto));
                                } else {
                                    redirectAttributes.addFlashAttribute("pago1", "El monto exacto a pagar no es suficiente");
                                    return "redirect:/cliente/checkout";
                                }
                            } else {
                                redirectAttributes.addFlashAttribute("pago2", "No ha ingresado un monto exacto");
                                return "redirect:/cliente/checkout";
                            }
                        }
                        if (idmetodo == 1) {
                            System.out.println(numerotarjeta);
                            if (numerotarjeta == null) {
                                return "redirect:/cliente/checkout";
                            } else {

                                boolean tarjetavalida = validartarjeta(numerotarjeta);

                                if (tarjetavalida == true) {
                                    List<TarjetasOnline> tarjetasxusuario = tarjetasOnlineRepository.findAllByNumerotarjetaAndClienteEquals(numerotarjeta, cliente);

                                    if (tarjetasxusuario.isEmpty()) {
                                        TarjetasOnline tarjetaxguardar = new TarjetasOnline();
                                        tarjetaxguardar.setNumerotarjeta(numerotarjeta);
                                        tarjetaxguardar.setCliente(cliente);
                                        tarjetasOnlineRepository.save(tarjetaxguardar);
                                    }
                                } else {
                                    redirectAttributes.addFlashAttribute("tarjetanovalida", "El número de tarjeta no es válido. Las tarjetas validas son Visa, MasterCard, DinersClub, Discover, JCB");
                                    return "redirect:/cliente/checkout";
                                }

                            }
                        }
                        if (pedidoencurso.getRestaurantepedido().getDistrito() == pedidoencurso.getDireccionentrega().getDistrito()) {
                            pedidoencurso.setComisionrepartidor(4);
                            pedidoencurso.setComisionsistema(1);
                        } else {
                            pedidoencurso.setComisionrepartidor(6);
                            pedidoencurso.setComisionsistema(2);
                        }
                        pedidoencurso.setEstadorestaurante("pendiente");
                        pedidoencurso.setEstadorepartidor("indefinido");
                        pedidosRepository.save(pedidoencurso);
                    }catch (Exception e){
                        List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(pedidoencurso.getIdpedidos());
                        System.out.println(pedidoencurso.getIdpedidos());
                        System.out.println(pedidoencurso.getDireccionentrega().getIddirecciones());
                        MontoTotal_PedidoHasPlatoDTO montoTotal_pedidoHasPlatoDTO = pedidoHasPlatoRepository.montototal(pedidoencurso.getIdpedidos());
                        int pagarTodo = pedidoHasPlatoRepository.pagarTodo(pedidoencurso.getIdpedidos());
                        int descuento = 0;
                        int montototal_pagar = pagarTodo - descuento;
                        model.addAttribute("platosxpedido", platosxpedido);
                        model.addAttribute("pedidoencurso", pedidoencurso);
                        model.addAttribute("montototal", montoTotal_pedidoHasPlatoDTO);
                        model.addAttribute("montopagar", montototal_pagar);

                        System.out.println(pagarTodo);
                        System.out.println(descuento);
                        System.out.println(montototal_pagar);
                        System.out.println(montoTotal_pedidoHasPlatoDTO);
                        pedidoencurso.setMontototal(String.valueOf(montototal_pagar));
                        pedidoencurso.setMetododepago(metodosel);

                        if (idmetodo == 3) {
                            if (montoexacto != 0) {
                                System.out.println(montoexacto);
                                if (montoexacto >= (montototal_pagar)) {
                                    pedidoencurso.setMontoexacto(String.valueOf(montoexacto));
                                } else {
                                    redirectAttributes.addFlashAttribute("pago1", "El monto exacto a pagar no es suficiente");
                                    return "redirect:/cliente/checkout";
                                }
                            } else {
                                redirectAttributes.addFlashAttribute("pago2", "No ha ingresado un monto exacto");
                                return "redirect:/cliente/checkout";
                            }
                        }
                        if (idmetodo == 1) {
                            System.out.println(numerotarjeta);
                            if (numerotarjeta == null) {
                                return "redirect:/cliente/checkout";
                            } else {

                                boolean tarjetavalida = validartarjeta(numerotarjeta);

                                if (tarjetavalida == true) {
                                    List<TarjetasOnline> tarjetasxusuario = tarjetasOnlineRepository.findAllByNumerotarjetaAndClienteEquals(numerotarjeta, cliente);

                                    if (tarjetasxusuario.isEmpty()) {
                                        TarjetasOnline tarjetaxguardar = new TarjetasOnline();
                                        tarjetaxguardar.setNumerotarjeta(numerotarjeta);
                                        tarjetaxguardar.setCliente(cliente);
                                        tarjetasOnlineRepository.save(tarjetaxguardar);
                                    }
                                } else {
                                    redirectAttributes.addFlashAttribute("tarjetanovalida", "El número de tarjeta no es válido. Las tarjetas validas son Visa, MasterCard, DinersClub, Discover, JCB");
                                    return "redirect:/cliente/checkout";
                                }

                            }
                        }
                        if (pedidoencurso.getRestaurantepedido().getDistrito() == pedidoencurso.getDireccionentrega().getDistrito()) {
                            pedidoencurso.setComisionrepartidor(4);
                            pedidoencurso.setComisionsistema(1);
                        } else {
                            pedidoencurso.setComisionrepartidor(6);
                            pedidoencurso.setComisionsistema(2);
                        }
                        pedidoencurso.setEstadorestaurante("pendiente");
                        pedidoencurso.setEstadorepartidor("indefinido");
                        pedidosRepository.save(pedidoencurso);
                    }
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

        if(ultimopedido1 == true){
            List<Pedidos> listapedidoscliente = pedidosRepository.pedidosfinxcliente(idusuario);
            if(!listapedidoscliente.isEmpty()){
                int tam = listapedidoscliente.size();
                int idultimopedido;
                if(tam == 0){
                    Pedidos ultimopedido = listapedidoscliente.get(0);
                    idultimopedido = ultimopedido.getIdpedidos();
                }else{
                    Pedidos ultimopedido = listapedidoscliente.get(tam-1);
                    idultimopedido = ultimopedido.getIdpedidos();
                }

                List<PedidoHasPlato> pedidoHasPlatoencurso = pedidoHasPlatoRepository.findAllByPedido_Idpedidos(idultimopedido);
                Optional<Pedidos> pedidoencursoopt = pedidosRepository.findById(pedidoHasPlatoencurso.get(0).getPedido().getIdpedidos());
                Pedidos pedidoencurso = pedidoencursoopt.get();
                System.out.println("*********************");
                System.out.println(pedidoencurso.getIdpedidos());
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
            }else{
                ultimopedido1 = false;
            }
        }
        model.addAttribute("ultimopedido",ultimopedido1);
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
    public String calificarpedido(HttpSession session){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        List<Pedidos> listapedidosusuario = pedidosRepository.findAllByIdclienteEquals(idusuario);
        boolean ultimopedido1 = false; //true -> hay al menos un pedido registrado
        if(!listapedidosusuario.isEmpty()){
            System.out.println("LISTA CON UN PEDIDO REGISTRADO");
            ultimopedido1 = true; //false -> no hay pedidos registrados
        }
        if(ultimopedido1 == true) {
            List<Pedidos> listapedidoscliente = pedidosRepository.pedidosfinxcliente(idusuario);
            if(listapedidoscliente.size() == 0){
                return "redirect:/cliente/progresopedido";
            }else{
                if(listapedidoscliente.get(listapedidoscliente.size() - 1).getEstadorepartidor().equalsIgnoreCase("entregado") && listapedidoscliente.get(listapedidoscliente.size() - 1).getEstadorestaurante().equalsIgnoreCase("entregado")){
                    return "cliente/calificarpedido";
                }else{
                    return "redirect:/cliente/progresopedido";
                }
            }
        }else{
            return "redirect:/cliente/progresopedido";
        }
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
            //actualizacion de calificacion promedio de restaurante
            int idrestpedido = pedidoencurso.getRestaurantepedido().getIdrestaurante();
            BigDecimal calificacion = pedidosRepository.calificacionPromedio(idrestpedido);
            Optional<Restaurante> restopt = restauranteRepository.findById(idrestpedido);
            Restaurante restget = restopt.get();
            MathContext m = new MathContext(3);
            calificacion = calificacion.round(m);
            restget.setCalificacionpromedio(calificacion.floatValue());
            restauranteRepository.save(restget);
        }
        if(calrep != 0){
            pedidoencurso.setCalificacionrepartidor(calrep);
            pedidosRepository.save(pedidoencurso);
            int idreppedido = pedidoencurso.getRepartidor().getIdusuarios();
            BigDecimal calificacionrep = pedidosRepository.calificacionpromediorepartidor(idreppedido);
            Optional<Repartidor> repopt = Optional.ofNullable(repartidorRepository.findRepartidorByIdusuariosEquals(idreppedido));
            Repartidor repget = repopt.get();
            MathContext m = new MathContext(3);
            calificacionrep = calificacionrep.round(m);
            repget.setCalificacionpromedio(calificacionrep.floatValue());
            repartidorRepository.save(repget);
        }
        if(comentarios != null){
            pedidoencurso.setComentario(comentarios);
        }
        pedidosRepository.save(pedidoencurso);
        return "redirect:/cliente/paginaprincipal";
    }

    /** Editar mi perfil **/
    @GetMapping("/cliente/miperfil")
    public String miperfil(Model model, HttpSession session) {

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
                        redirectAttributes.addFlashAttribute("perfilact", "Cuenta actualizada exitosamente");
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

    /** borrar tarjeta **/
    @GetMapping("/cliente/borrartarjeta")
    public String borrartarjeta(@RequestParam("idtarjeta") String idtarjeta,
                                Model model, HttpSession session, RedirectAttributes redirectAttributes){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        Optional<Usuario> optional = usuarioRepository.findById(idusuario);
        Usuario userlog = optional.get();

        try{
            List<TarjetasOnline> tarjetasxusuario = tarjetasOnlineRepository.findAllByIdtarjetasonlineAndClienteEquals(Integer.parseInt(idtarjeta),userlog);
            if(!tarjetasxusuario.isEmpty()){
                tarjetasOnlineRepository.deleteById(Integer.parseInt(idtarjeta));
                redirectAttributes.addFlashAttribute("perfilact", "Tarjeta borrada exitosamente");
            }
        }catch(NumberFormatException e){
            System.out.println(e.getMessage());
        }
        return "redirect:/cliente/miperfil";

    }

    /** borrar dirección **/
    @GetMapping("/cliente/borrardireccion")
    public String borrardireccion(@RequestParam("iddireccion") String iddireccion,
                                  Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        Optional<Usuario> optional = usuarioRepository.findById(idusuario);
        Usuario userlog = optional.get();
        try {
            Direcciones direccionborrar = direccionesRepository.findDireccionesByIddireccionesAndUsuario_Idusuarios(Integer.valueOf(iddireccion),idusuario);
            //Direcciones direccionborrar = direccionopt.get();
            if (direccionborrar != null) {
                direccionborrar.setActivo(0);
                direccionesRepository.save(direccionborrar);
                redirectAttributes.addFlashAttribute("perfilact", "Dirección borrada exitosamente");
            }
        }catch(NumberFormatException e){
            System.out.println(e.getMessage());
        }
        return "redirect:/cliente/miperfil";

    }

    /** Guardar nueva dirección **/
    @GetMapping("/cliente/agregardireccion")
    public String agregardireccion(Model model) {

        List<Distritos> listadistritos = distritosRepository.findAll();
        model.addAttribute("listadistritos",listadistritos);
        String direction=null;
        model.addAttribute("direction",direction);
        System.out.println(direction);
        return "cliente/registrarNuevaDireccion";
    }

    @PostMapping("/cliente/guardarnuevadireccion")
    public String guardarnuevadireccion(@RequestParam("direccion_real") String direccion,
                                        @RequestParam("iddistrito") int iddistrito,
                                        HttpSession session) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        Optional<Usuario> usuarioopt = usuarioRepository.findById(idusuario);
        Usuario usuario = usuarioopt.get();

        Direcciones direccioncrear = new Direcciones();
        direccion = direccion.split(",")[0];
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
