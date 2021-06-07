package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import com.example.tarea4_grupo2.service.SendMailService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
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

    @GetMapping(value={"/cliente/paginaprincipal","/cliente/","/cliente"})
    public String paginaprincipal(HttpSession session, Model model) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        
        List<Pedidos> pedidoscanceladosxrest = pedidosRepository.listapedidoscanceladosxrest(idusuario);
        model.addAttribute("listacancelados",pedidoscanceladosxrest);
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

    public boolean validardnixrolunico(String dni, String rol, Usuario usuario){
        boolean errordni = false;
        Usuario usuarioxdni = usuarioRepository.findByDniAndRolEquals(usuario.getDni(),"Cliente");
        if(usuarioxdni != null){
            errordni = true;
        }
        return errordni;
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
            boolean errordnixrol = validardnixrolunico(usuario.getDni(), "Cliente", usuario);
            boolean errorstringsexo = validarstringsexo(usuario.getSexo());

            if (errorcorreo == true || errordnixrol == true || errorstringsexo == true) {
                if(errorcorreo==true){
                    model.addAttribute("errorcorreo", "Ya hay una cuenta registrada con el correo ingresado.");
                }
                if(errordnixrol==true){
                    model.addAttribute("errordni", "Ya hay una cuenta registrada con este dni en este rol");
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
                            direccionactual.setUsuariosIdusuarios(idusuarionuevo);
                            direccionactual.setActivo(1);
                            direccionesRepository.save(direccionactual);

                            /* Envio de correo de confirmacion */
                            String subject = "Cuenta creada en Spicyo";
                            //TODO modificar direcion url despues de despliegue aws.
                            String direccionurl = "http://localhost:8090/login";
                            //Pegar aquí los datos del AWS;
                            // String aws =
                            //String direccionurl = "http://" + aws + ":8090/login";
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

    @GetMapping("/cliente/reportes")
    public String reportesCliente(Model model,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarios=sessionUser.getIdusuarios();

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

            for(Top3Restaurantes_ClienteDTO t : top3Restaurantes_clienteDTOS){
                System.out.println(t.getRestaurante());
            }

            DineroAhorrado_ClienteDTO dineroAhorrado_clienteDTO = pedidosRepository.dineroAhorrado(idusuarios, anio, mes);
            model.addAttribute("cliente", cliente);
            model.addAttribute("listaTop3Restaurantes",top3Restaurantes_clienteDTOS );
            model.addAttribute("listaTop3Platos", top3Platos_clientesDTOS);
            model.addAttribute("listaPromedioTiempo", tiempoMedio_clienteDTOS);
            model.addAttribute("diferencia", dineroAhorrado_clienteDTO);
            model.addAttribute("listaHistorialConsumo", pedidosRepository.obtenerHistorialConsumo(idusuarios, anio, mes));
            model.addAttribute("fechaseleccionada",fechamostrar);
            return "cliente/reportes";
        } else {
            return "redirect:/cliente/miperfil";
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
                    //model.addAttribute("fechaactual",fechaactual1);
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

        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
        List<Categorias> listacategorias = categoriasRepository.findAll();
        List<Restaurante> listarestaurantes = restauranteRepository.findAll();
        model.addAttribute("listacategorias", listacategorias);
        model.addAttribute("listadirecciones", listadireccionescliente);
        model.addAttribute("listarestaurantes",listarestaurantes);

            try{
                int direccionxenviar = Integer.parseInt(direccion);
                if (direccionxenviar == 0) {
                    model.addAttribute("direccionseleccionada", listadireccionescliente.get(0).getDireccion());
                    model.addAttribute("iddireccionxenviar", listadireccionescliente.get(0).getIddirecciones());
                } else {

                    Optional<Direcciones> direccionopt = Optional.ofNullable(direccionesRepository.findDireccionesByIddireccionesAndUsuariosIdusuariosEquals(direccionxenviar, idusuarioactual));
                        if (direccionopt.isPresent()) {
                            Direcciones direccionseleccionada = direccionopt.get();
                            model.addAttribute("iddireccionxenviar", direccionxenviar);
                            model.addAttribute("direccionseleccionada", direccionseleccionada.getDireccion());
                        }
                }
            }catch(NumberFormatException exception){
                System.out.println(exception.getMessage());
                return "redirect:/cliente/realizarpedido";
            }
        try{
            int idcategoriarest = Integer.parseInt(categoriarest);
            Optional<Categorias> catopt = categoriasRepository.findById(idcategoriarest);
            if(catopt.isPresent()){

                List<Restaurante> listarestauranteseleccionado = restauranteRepository.listarestxcategoria(idcategoriarest);

                if(idcategoriarest!=0){
                    model.addAttribute("listarestaurantes",listarestauranteseleccionado);
                }else{
                    model.addAttribute("listarestaurantes",listarestaurantes);
                }
                model.addAttribute("catelegida",idcategoriarest);
            }
        }catch(NumberFormatException exception){
            System.out.println(exception.getMessage());
            return "redirect:/cliente/realizarpedido";
        }

        try {
            int precio = Integer.parseInt(preciopromedio);
            if(precio!=0) {
                switch (precio) {
                    case 1:
                    List<Restaurante> listaRestFiltroPrecio = restauranteRepository.listarestprecio1();
                    if (listaRestFiltroPrecio.isEmpty()) {
                        attr.addFlashAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                        return "redirect:/cliente/realizarpedido";
                    } else {
                        model.addAttribute("listarestaurantes", listaRestFiltroPrecio);
                        model.addAttribute("precioselec", precio);
                    }
                    break;
                    case 2:
                    listaRestFiltroPrecio = restauranteRepository.listarestprecio2();
                    if (listaRestFiltroPrecio.isEmpty()) {
                        attr.addFlashAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                        return "redirect:/cliente/realizarpedido";
                    } else {
                        model.addAttribute("listarestaurantes", listaRestFiltroPrecio);
                        model.addAttribute("precioselec", precio);
                    }
                    break;
                    case 3:
                    listaRestFiltroPrecio = restauranteRepository.listarestprecio3();
                    if (listaRestFiltroPrecio.isEmpty()) {
                        attr.addFlashAttribute("alertaprecio", "No se encontraron restaurantes para el filtro aplicado");
                        return "redirect:/cliente/realizarpedido";
                    } else {
                        model.addAttribute("listarestaurantes", listaRestFiltroPrecio);
                        model.addAttribute("precioselec", precio);
                    }
                    break;
                    case 4:
                    listaRestFiltroPrecio = restauranteRepository.listarestprecio4();
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
        }catch (NumberFormatException e){
            return "cliente/realizar_pedido_cliente";
        }

        try {
            int calificacion = Integer.parseInt(calificacionpromedio);
        if(calificacion!=0) {
            if (calificacion > 4) {
                return "redirect:/cliente/realizarpedido";
            } else {
                List<Restaurante> listarestcal = restauranteRepository.listarestcalificacion(calificacion);
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

    @GetMapping("/cliente/direccionxenviar")
    public String direccionxenviar(Model model,
                                   @RequestParam(value = "direccion", defaultValue = "0") String direccion,
                                   HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();
        System.out.println("****************************error numero");
        try {
            int direccionxenviar = Integer.parseInt(direccion);
            Optional<Direcciones> direccionopt = Optional.ofNullable(direccionesRepository.findDireccionesByIddireccionesAndUsuariosIdusuariosEquals(direccionxenviar, idusuarioactual));
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
                List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
                List<Categorias> listacategorias = categoriasRepository.findAll();
                List<Restaurante> listarestaurantes = restauranteRepository.findAll();
                model.addAttribute("listacategorias", listacategorias);
                model.addAttribute("listadirecciones", listadireccionescliente);
                model.addAttribute("listarestaurantes",listarestaurantes);
                model.addAttribute("iddireccionxenviar",direccionxenviar);
                model.addAttribute("direccionseleccionada",direccionseleccionada.getDireccion());
                return "cliente/realizar_pedido_cliente";
            }else{
                return "redirect:/cliente/realizarpedido";
            }
        }catch (NumberFormatException e){
            System.out.println(e.getMessage());
            System.out.println("error");
            return "redirect:/cliente/realizarpedido";
        }
    }

    @PostMapping("/cliente/filtrarnombre")
    public String filtronombre(Model model,
                               @RequestParam(value = "searchField" ,defaultValue = "") String buscar,
                               @RequestParam(value = "direccion") int direccionxenviar,
                               RedirectAttributes redirectAttributes,
                               HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();
        if(buscar.isEmpty()){
            return "redirect:/cliente/realizarpedido";
        }else{
            List<Plato> listaplatos = platoRepository.buscarPlatoxNombre(buscar);
            List<Restaurante> listarestaurantes = restauranteRepository.buscarRestaurantexNombre(buscar);

            if(listaplatos.size()==0 && listarestaurantes.size()==0){
                redirectAttributes.addFlashAttribute("alertabusqueda", "No hay coincidencia de búsqueda");
                return "redirect:/cliente/realizarpedido";
            }else{
                List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
                model.addAttribute("listadirecciones", listadireccionescliente);
                model.addAttribute("listarestaurantesbuscado",listarestaurantes);
                model.addAttribute("listaplatosbuscado",listaplatos);
                model.addAttribute("nombrebuscado",buscar);
                Optional<Direcciones> direccionopt = direccionesRepository.findById(direccionxenviar);
                if(direccionopt.isPresent()){
                    Direcciones direccionseleccionada = direccionopt.get();
                    model.addAttribute("iddireccionxenviar",direccionxenviar);
                    model.addAttribute("direccionseleccionada",direccionseleccionada.getDireccion());
                }
                return "cliente/busquedanombre";
            }
        }

    }

    /** restaurante a ordenar **/

     @GetMapping("/cliente/restaurantexordenar")
     public String restaurantexordenar(@RequestParam("idrestaurante") int idrestaurante, Model model,
                                   @RequestParam("direccion") int direccionxenviar
                                     ){

         Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);
         Optional<Direcciones> diropt = direccionesRepository.findById(direccionxenviar);
        if(diropt.isPresent() && restopt.isPresent()){
            Restaurante rest = restopt.get();

            if (rest!=null){
                int cantreviews = restauranteRepository.cantreviews(idrestaurante);

                List<Plato> platosxrest = platoRepository.buscarPlatosPorIdRestauranteDisponilidadActivo(idrestaurante);

                model.addAttribute("restaurantexordenar",rest);
                model.addAttribute("cantreviews",cantreviews);
                model.addAttribute("platosxrest",platosxrest);
                model.addAttribute("direccionxenviar",direccionxenviar);
                return "cliente/restaurante_orden_cliente";

            }else{
                return "redirect:/cliente/realizarpedido";
            }
        }else{
            return "redirect:/cliente/realizarpedido";
        }

     }

    @GetMapping("/cliente/platoxpedir")
    public String platoxpedir(Model model,
                              @RequestParam("idplato") int idplatopedir,
                              @RequestParam("idrestaurante") int idrestaurante,
                              @RequestParam("direccion") int direccionxenviar){

         Optional<Plato> platoopt = platoRepository.findById(idplatopedir);
         Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);
         Optional<Direcciones> diropt = direccionesRepository.findById(direccionxenviar);

         if(platoopt.isPresent() && restopt.isPresent() && diropt.isPresent()){
             Plato platoseleccionado = platoopt.get();
             model.addAttribute("platoseleccionado",platoseleccionado);
             model.addAttribute("idrestaurante",idrestaurante);
             model.addAttribute("iddireccionxenviar",direccionxenviar);
             return "cliente/detalles_plato";
         }else{
            return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante + "&direccion=" + direccionxenviar;
         }
    }

    @PostMapping("/cliente/platopedido")
    public String platopedido(@RequestParam("cubierto") boolean cubiertos,
                              @RequestParam("cantidad") int cantidad,
                              @RequestParam("descripcion") String descripcion,
                              @RequestParam(value = "idrestaurante") int idrestaurante,
                              @RequestParam("idplato") int idplato,
                              HttpSession session,
                              Model model,
                              @RequestParam("direccion") int direccionxenviar){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idcliente=sessionUser.getIdusuarios();

         Optional<Restaurante> restauranteopt = restauranteRepository.findById(idrestaurante);
         Optional<Plato> platoopt = platoRepository.findById(idplato);
         Optional<Direcciones> diropt = direccionesRepository.findById(direccionxenviar);

         if(platoopt.isPresent() && restauranteopt.isPresent() && diropt.isPresent()){
             Plato platoelegido = platoopt.get();

            // List<PedidoHasPlato> pedidoHasPlatoList =  pedidoHasPlatoRepository.findAll();
             //System.out.println(pedidoHasPlatoList.get(1).getPlato().getIdplato());
            // System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa");
             //    Pedidos pedidoencursoxrestaurante(int idcliente, int restaurante_idrestaurante);

             Pedidos pedidoencurso = pedidosRepository.pedidoencursoxrestaurante(idcliente, idrestaurante);

             if(pedidoencurso == null){
                 Pedidos pedidos = new Pedidos();
                 pedidos.setIdcliente(idcliente);

                 Restaurante restelegido = restauranteopt.get();

                 pedidos.setRestaurantepedido(restelegido);

                 Direcciones direccionentrega = diropt.get();

                 pedidos.setDireccionentrega(direccionentrega);
                 List<Pedidos> listapedidoscliente = pedidosRepository.listapedidoxcliente(idcliente,idrestaurante);
                 int tam = listapedidoscliente.size();
                 Pedidos ultimopedido = listapedidoscliente.get(tam-1);
                 int idultimopedido = ultimopedido.getIdpedidos();

                 PedidoHasPlatoKey pedidoHasPlatoKey = new PedidoHasPlatoKey(idultimopedido,idplato);
                 PedidoHasPlato pedidoHasPlato = new PedidoHasPlato(pedidoHasPlatoKey,pedidos,platoelegido,descripcion,cantidad,cubiertos);
                 pedidos.addpedido(pedidoHasPlato);

                 pedidosRepository.save(pedidos);
                 listapedidoscliente = pedidosRepository.listapedidoxcliente(idcliente,idrestaurante);
                 tam = listapedidoscliente.size();
                 ultimopedido = listapedidoscliente.get(tam-1);
                 idultimopedido = ultimopedido.getIdpedidos();
                 pedidoHasPlatoKey.setPedidosidpedidos(idultimopedido);
                 //PedidoHasPlatoKey pedidoHasPlatoKey = new PedidoHasPlatoKey(idultimopedido,idplato);
                 pedidoHasPlato.setId(pedidoHasPlatoKey);
                 //PedidoHasPlato pedidoHasPlato = new PedidoHasPlato(pedidoHasPlatoKey,pedidos,platoelegido,descripcion,cantidad,cubiertos);
                 pedidoHasPlatoRepository.save(pedidoHasPlato);
             }else{
                 System.out.println("+1 plato al pedido");
                 System.out.println(platoelegido.getNombre());
                 Pedidos pedidos = pedidoencurso;
                 int idultimopedido = pedidoencurso.getIdpedidos();
                 PedidoHasPlatoKey pedidoHasPlatoKey = new PedidoHasPlatoKey(idultimopedido,idplato);
                 PedidoHasPlato pedidoHasPlato = new PedidoHasPlato(pedidoHasPlatoKey,pedidos,platoelegido,descripcion,cantidad,cubiertos);
                 pedidoHasPlatoKey.setPedidosidpedidos(idultimopedido);
                 //PedidoHasPlatoKey pedidoHasPlatoKey = new PedidoHasPlatoKey(idultimopedido,idplato);
                 pedidoHasPlato.setId(pedidoHasPlatoKey);
                 pedidoHasPlatoRepository.save(pedidoHasPlato);
             }

             return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante + "&direccion=" + direccionxenviar;
         }else{
             return "redirect:/cliente/realizarpedido";
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
                List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedidoIdpedidos(pedidoencurso.getIdpedidos());
                System.out.println(pedidoencurso.getIdpedidos());
                System.out.println(pedidoencurso.getDireccionentrega().getIddirecciones());
                model.addAttribute("platosxpedido",platosxpedido);
                model.addAttribute("pedidoencurso",pedidoencurso);
            }
        }
        return "cliente/carrito_productos";
    }

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

                List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedidoIdpedidos(pedidoencurso.getIdpedidos());
                for(PedidoHasPlato plato1 : platosxpedido){
                    PedidoHasPlatoKey pedidoHasPlatoKey = plato1.getId();
                    pedidoHasPlatoRepository.deleteById(pedidoHasPlatoKey);
                    System.out.println("deberia borrar plato ****************************");
                    //plato1.getPedido().removePlato(plato1.getPlato());
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
        System.out.println(idmetodo);
        Optional<MetodosDePago> metodoopt = metodosDePagoRepository.findById(idmetodo);
        if(metodoopt.isPresent()){
            MetodosDePago metodosel = metodoopt.get();
            model.addAttribute("metodoelegido",idmetodo);
            System.out.println(idmetodo);
        }
        List<MetodosDePago> listametodos = metodosDePagoRepository.findAll();
        model.addAttribute("listametodospago",listametodos);

        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);

        if(listapedidospendientes.isEmpty()){
            return "redirect:/cliente/realizarpedido";
        }else{

            for (Pedidos pedidoencurso : listapedidospendientes){
                List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedidoIdpedidos(pedidoencurso.getIdpedidos());
                System.out.println(pedidoencurso.getIdpedidos());
                System.out.println(pedidoencurso.getDireccionentrega().getIddirecciones());
                model.addAttribute("platosxpedido",platosxpedido);
                model.addAttribute("pedidoencurso",pedidoencurso);
            }
            return "cliente/checkoutcarrito";
        }

    }

    @PostMapping("/cliente/guardarcheckout")
    public String getcheckout(@RequestParam(value = "idmetodo",defaultValue = "0") int idmetodo,
    Model model,
    HttpSession session,
    RedirectAttributes redirectAttributes){
        //revisar el metodo, sale error//
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        System.out.println(idmetodo);
        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);

        if(listapedidospendientes.isEmpty()){
            return "redirect:/cliente/realizarpedido";
        }else{
            Optional<MetodosDePago> metodoopt = metodosDePagoRepository.findById(idmetodo);
            if(metodoopt.isPresent()) {
                MetodosDePago metodosel = metodoopt.get();
                model.addAttribute("metodoelegido", idmetodo);
                System.out.println(idmetodo);
                for (Pedidos pedidoencurso : listapedidospendientes) {
                    List<PedidoHasPlato> platosxpedido = pedidoHasPlatoRepository.findAllByPedidoIdpedidos(pedidoencurso.getIdpedidos());
                    System.out.println(pedidoencurso.getIdpedidos());
                    System.out.println(pedidoencurso.getDireccionentrega().getIddirecciones());
                    //model.addAttribute("platosxpedido",platosxpedido);
                    //model.addAttribute("pedidoencurso",pedidoencurso);
                    Pedidos pedido = new Pedidos();
                    pedidosRepository.save(pedido);

                }
            }
            return "redirect:/cliente/paginaprincipal";
        }
    }


    @GetMapping("/cliente/progresopedido")
    public String progresopedido(Model model, HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        List<Pedidos> listapedidospendientes = pedidosRepository.listapedidospendientes(idusuario);
        List<PedidoHasPlato> pedidoHasPlatoencurso = pedidoHasPlatoRepository.findAllByPedidoIdpedidos(55);
        System.out.println(pedidoHasPlatoencurso.get(0).getPlato().getNombre());
        System.out.println("*****************");
        Optional<Pedidos> pedidoencursoopt = pedidosRepository.findById(55);
        Pedidos pedidoencurso = pedidoencursoopt.get();
        model.addAttribute("pedido",pedidoencurso);
        model.addAttribute("lista",pedidoHasPlatoencurso);

        return "cliente/ultimopedido_cliente";

    }

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

        System.out.println("calificacionesssssssssssssss");
        System.out.println(calrep);
        System.out.println(calrest);
        System.out.println(comentarios);
        return "redirect:/cliente/paginaprincipal";
    }

    /** Mi perfil **/

    @GetMapping("/cliente/miperfil")
    public String miperfil(
            //@ModelAttribute("usuario") Usuario usuario,
                           Model model, HttpSession session) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosAndActivoEquals(idusuario,1);
        model.addAttribute("listadirecciones", listadireccionescliente);

        Optional<Usuario> optional = usuarioRepository.findById(idusuario);
        if(optional.isPresent()){
            Usuario usuario = optional.get();
            model.addAttribute("usuario", usuario);

            System.out.println(usuario.getIdusuarios());
        }

        return "cliente/miPerfil";
    }

    @PostMapping("/cliente/miperfil")
    public String updatemiperfil(@ModelAttribute("usuario") @Valid Usuario usuario,
                                 BindingResult bindingResult,
                                 @RequestParam("pass2") String password2,
                                 HttpSession session,
                                 Model model) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");

        int idusuario=usuario.getIdusuarios();
        Optional<Usuario> usuarioopt = usuarioRepository.findById(idusuario);
        Usuario usuarioperfil = usuarioopt.get();

        if(bindingResult.hasFieldErrors("Telefono") || bindingResult.hasFieldErrors("contraseniaHash")){
            List<Distritos> listadistritos = distritosRepository.findAll();
            model.addAttribute("listadistritos", listadistritos);
            model.addAttribute("usuario",usuarioperfil);
            return "cliente/miPerfil";
        } else {
            if (usuario.getContraseniaHash().equals(password2)) {
                String contraxvalidarpatron = usuario.getContraseniaHash();
                boolean validarcontra = validarContrasenia(contraxvalidarpatron);
                if (validarcontra == true) {
                    String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());
                    sessionUser.setTelefono(usuario.getTelefono());
                    sessionUser.setContraseniaHash(contraseniahashbcrypt);
                    usuarioRepository.save(sessionUser);
                    return "redirect:/cliente/miperfil";
                }else{
                    List<Distritos> listadistritos = distritosRepository.findAll();
                    model.addAttribute("listadistritos", listadistritos);
                    model.addAttribute("errorpatroncontra", "La contraseña no cumple con los requisitos: mínimo 8 caracteres, un número y un caracter especial");
                    model.addAttribute("usuario",usuarioperfil);
                    return "cliente/miPerfil";
                }
            }else{
                List<Distritos> listadistritos = distritosRepository.findAll();
                model.addAttribute("listadistritos", listadistritos);
                model.addAttribute("usuario",usuarioperfil);
                return "cliente/miPerfil";
            }

        }
    }

    /** CRUD direcciones **/
    @GetMapping("/cliente/borrardireccion")
    public String borrardireccion(@RequestParam("iddireccion") int iddireccion,
                                  Model model) {

        Optional<Direcciones> direccionopt = direccionesRepository.findById(iddireccion);
        Direcciones direccionborrar = direccionopt.get();

        if(direccionborrar != null){
            direccionborrar.setActivo(0);
            direccionesRepository.save(direccionborrar);
        }

        return "redirect:/cliente/miperfil";
    }

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

        Direcciones direccioncrear = new Direcciones();
        direccioncrear.setDireccion(direccion);

        Optional<Distritos> distritoopt = distritosRepository.findById(iddistrito);
        if(distritoopt.isPresent()){

            Distritos distritonuevo = distritoopt.get();
            direccioncrear.setDistrito(distritonuevo);
            direccioncrear.setUsuariosIdusuarios(idusuario);
            direccioncrear.setActivo(1);
            direccionesRepository.save(direccioncrear);

        }
        return "redirect:/cliente/miperfil";
    }

}
