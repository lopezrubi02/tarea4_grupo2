package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.RepartidorComisionMensualDTO;
import com.example.tarea4_grupo2.service.*;
import com.example.tarea4_grupo2.dto.DeliveryReportes_DTO;
import com.example.tarea4_grupo2.dto.RepartidoresReportes_DTO;
import com.example.tarea4_grupo2.dto.RestauranteReportes_DTO;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    RepartidorRepository repartidorRepository;

    @Autowired
    DireccionesRepository direccionesRepository;

    @Autowired
    RestauranteRepository restauranteRepository;

    @Autowired
    PedidosRepository pedidosRepository;

    @Autowired
    SendMailService sendMailService;

    @Autowired
    DistritosRepository distritosRepository;

    @Autowired
    Environment environment;

    public List<String> getIpAndProt() throws UnknownHostException {
        // Port
        String puerto = environment.getProperty("server.port");

        // Local address
        String ip = InetAddress.getLocalHost().getHostAddress();
        String namehost = InetAddress.getLocalHost().getHostName();

        List<String> lista = new ArrayList<String>();
        lista.add(0,ip);
        lista.add(1,puerto);
        lista.add(2,namehost);
        // Remote address
        //InetAddress.getLoopbackAddress().getHostAddress();
        //InetAddress.getLoopbackAddress().getHostName();
        return lista;
    }

    ////////////////////////////////////////// API - Validacion DNI ///////////////////////
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

    @GetMapping(value ={"/","/*"})
    public String redireccion(){
        return "redirect:/admin/gestionCuentas";
    }

    @GetMapping("/usuariosActuales")
    public String usuariosActuales(
            @RequestParam(name = "page", defaultValue = "1") String requestedPage,
            @RequestParam(name = "searchField", defaultValue = "") String searchField,
            @RequestParam(name = "rol", defaultValue = "") String rol,
            Model model,
            RedirectAttributes attributes

    ) {
        /**
         * Validaciones
         * ---
         *      + Si la pagina recibida es mayor a la maxima posible
         *
         */

        float numberOfUsersPerPage = 7;
        int page;
        try{
            page = Integer.parseInt(requestedPage);
        }catch (Exception e){
            page = 1;
        }


        List<Usuario> usuarioList; // se define el contenido de la lista (la paginacion se hace a partir de esta)
        String buscar = "%"+searchField+"%";

        if (!searchField.equals("") && !rol.equals("")) {
            // si es que no estan vacios, se filtra por rol y nombre
            //usuarioList = usuarioRepository.findAllByRolAndCuentaActivaAndNombre(rol, 1, searchField);

            usuarioList = usuarioRepository.cuentasActualesRol(buscar,rol);
        } else if (!searchField.equals("")) {
            // si el nobre es el que no esta vacio, se filtra por nombre
            //usuarioList = usuarioRepository.findAllByNombreAndCuentaActiva(searchField, 1);
            usuarioList = usuarioRepository.cuentasActuales(buscar);
        } else if (!rol.equals("")) {
            // viceversa
            usuarioList = usuarioRepository.findAllByRolAndCuentaActiva(rol, 1);
        } else {
            // si todos los campos estan vacios, se muestran todos por defecto
            usuarioList = usuarioRepository.findAllByCuentaActivaEquals(1);
        }

        // si no se encuentra nada, se redirige a la lista general
        if(usuarioList.size() == 0){
            return "redirect:/admin/usuariosActuales";
        }

        int numberOfPages = (int) Math.ceil(usuarioList.size() / numberOfUsersPerPage);
        if (page > numberOfPages) {
            page = numberOfPages;
        } // validation

        int start = (int) numberOfUsersPerPage * (page - 1);
        int end = (int) (start + numberOfUsersPerPage);

        List<Usuario> lisOfUsersPage = usuarioList.subList(start, Math.min(end, usuarioList.size()));

        model.addAttribute("lisOfUsersPage", lisOfUsersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("maxNumberOfPages", numberOfPages);
        model.addAttribute("rol", rol);
        model.addAttribute("searchField", searchField);

        return "adminsistema/usuariosActuales";
    }

    @PostMapping("/buscadorUsuarios")
    public String buscarEmployee(@RequestParam(value = "searchField", defaultValue = "") String searchField,
                                 @RequestParam(value = "rol") String rol,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 HttpSession session) {

        System.out.println(rol);

        redirectAttributes.addAttribute("rol", rol);
        redirectAttributes.addAttribute("searchField", searchField);
        return "redirect:/admin/usuariosActuales";
    }

    @GetMapping("/user")
    public String paginaUsuario(
            Model model,
            @RequestParam("id") String idString,
            HttpSession session
    ) {
        try{
            int id = Integer.parseInt(idString);
        Optional<Usuario> optional = usuarioRepository.findById(id);

        if (optional.isPresent() && optional.get().getCuentaActiva() == 1) {
            Usuario usuario = optional.get();

            switch (usuario.getRol()) {
                case "AdminSistema":

                    // se obtiene el ID del usuaio logueado
                    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
                    int idUsuarioactual = usuarioActual.getIdusuarios();

                    // se verifica si el super-admin (cuyo ID = 1)
                    if(idUsuarioactual == 1){
                        // pasar a vista editar el usuario seleccionado
                        model.addAttribute("usuario", usuario);
                        return "adminsistema/editaradmin";

                    } else {
                        // solo listar la data
                        model.addAttribute("usuario", usuario);
                        return "adminsistema/datosAdmin";
                    }

                case "Repartidor":
                    model.addAttribute("usuario", usuario);

                    Repartidor repartidor = repartidorRepository.findRepartidorByIdusuariosEquals(id);
                    model.addAttribute("repartidor", repartidor);

                    return "adminsistema/datosRepartidor";

                case "Cliente":
                    model.addAttribute("usuario", usuario);

                    List<Direcciones> direccionesList = direccionesRepository.findAllByUsuario_Idusuarios(id);
                    model.addAttribute("direccionesList", direccionesList);

                    return "adminsistema/datosCliente";

                case "AdminRestaurante":
                    model.addAttribute("usuario", usuario);

                    Restaurante restaurante = restauranteRepository.findRestauranteByUsuario_Idusuarios(id);

                    model.addAttribute("restaurante", restaurante);

                    return "adminsistema/datosRestaurante";

                default:
                    return "redirect:/admin/usuariosActuales";
            }

        } else {
            return "redirect:/admin/usuariosActuales";
        }
        }catch (NumberFormatException e){
            return "redirect:/admin/usuariosActuales";
        }
    }

    @PostMapping("/miCuenta")
    public String updateMiCuenta(
            @ModelAttribute("usuario") @Valid Usuario usuarioRecibido,
            BindingResult bindingResult,
            RedirectAttributes attr,
            Model model
    ) throws IOException {

            // 1. Validacion de email unico
            Boolean emailValido = false;
            Optional<Usuario> usuario = Optional.ofNullable(usuarioRepository.findByEmailEquals(usuarioRecibido.getEmail()));
            if(!usuario.isPresent()) {
                // CREAR NUEVO: si no hay un usuario con el mismo correo
                emailValido = true;
            } else {
                if(usuario.get().getIdusuarios() == usuarioRecibido.getIdusuarios()) {
                    // EDICION: si el usuario que se ha extraido, es el que se esta editando
                    emailValido = true;
                } else{
                    model.addAttribute("emailUnico", "Ya existe un usuario registrado con el mismo correo");
                }
            }

            // 2. Validacion de dni
            boolean dniValido = validarDNI(usuarioRecibido.getDni());
            if(dniValido == false){
                model.addAttribute("dniValido", "Ingrese un DNI valido");
            }

            if(emailValido == true && dniValido == true){
                usuarioRepository.save(usuarioRecibido);
                attr.addFlashAttribute("msg", "Usuario actualizado correctamente");
                return "redirect:/admin/usuariosActuales";
            } else{
                return "adminsistema/miCuenta";
            }

    }

    @PostMapping("/editarAdmin")
    public String updateAdminUser(
            @ModelAttribute("usuario") @Valid Usuario usuarioRecibido,
            BindingResult bindingResult,
            RedirectAttributes attr,
            Model model
    ) throws IOException {


            // 1. Validacion de email unico
            Boolean emailValido = false;
            Optional<Usuario> usuario = Optional.ofNullable(usuarioRepository.findByEmailEquals(usuarioRecibido.getEmail()));
            if(!usuario.isPresent()) {
                // CREAR NUEVO: si no hay un usuario con el mismo correo
                emailValido = true;
            } else {
                if(usuario.get().getIdusuarios() == usuarioRecibido.getIdusuarios()) {
                    // EDICION: si el usuario que se ha extraido, es el que se esta editando
                    emailValido = true;
                } else{
                    model.addAttribute("emailUnico", "Ya existe un usuario registrado con el mismo correo");
                }
            }

            // 2. Validacion de dni
            boolean dniValido = validarDNI(usuarioRecibido.getDni());
            if (dniValido == false){
                model.addAttribute("dniValido", "Ingrese un DNI valido");
            }

            // Actualizar los datos
            if( emailValido == true && dniValido == true){
                usuarioRepository.save(usuarioRecibido);
                attr.addFlashAttribute("msg", "Usuario actualizado correctamente");
                return "redirect:/admin/usuariosActuales";
            } else{
                return "adminsistema/editaradmin";
            }


    }

    @GetMapping("/miCuenta")
    public String miCuenta(
            Model model,
            HttpSession session){

        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        int id = usuarioActual.getIdusuarios();

        Optional<Usuario> optional = usuarioRepository.findById(id);
        Usuario usuario = optional.get();
        model.addAttribute("usuario", usuario);

        return "adminsistema/miCuenta";
    }

    @GetMapping("/delete")
    public String borrarAdmin(@RequestParam("id") int id, RedirectAttributes attr, HttpSession session) {

        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        int idUsuarioactual = usuarioActual.getIdusuarios();
        if (idUsuarioactual == 1){
            Optional<Usuario> optional = usuarioRepository.findById(id);

            if (optional.isPresent()) {

                if(optional.get().getRol().equals("AdminSistema")){
                    // solo puede eliminar usuarios admin
                    usuarioRepository.deleteById(id);
                }
            }

            attr.addFlashAttribute("msg", "Usuario eliminado correctamente");
            return "redirect:/admin/usuariosActuales";
        } else{
            return "redirect:/admin/usuariosActuales";
        }
    }

//  Imagenes
@GetMapping("/imagerestaurante/{id}")
public ResponseEntity<byte[]> mostrarImagenRest(@PathVariable("id") int id){
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

    @GetMapping("/imagenrepartidor/{id}")
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

    //Gestion de Nuevas Cuentas
    //--------------------------------------------

//--------------------------
//-------------------------------------------------k
    @GetMapping("/gestionCuentas")
    public String gestionCuentas(HttpSession session,Model model){
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        int id = usuarioActual.getIdusuarios();
        model.addAttribute("idAdmin",id);

        return "adminsistema/GestionCuentasPrincipal";
    }

        @GetMapping("/nuevosUsuarios")
        public String nuevosUsuarios(Model model,@RequestParam(value = "rolSelected" ,defaultValue = "Todos")String rol,
                                     @RequestParam(value = "searchField" ,defaultValue = "") String buscar,
                                     @RequestParam(name = "page", defaultValue = "1") String requestedPage,
                                     RedirectAttributes attr){

            float numberOfUsersPerPage = 4;
            int page;
            try{
                page = Integer.parseInt(requestedPage);
            }catch (Exception e){
                page = 1;
            }


            List<Usuario> usuarioList;

            if(!buscar.equals("")){
                String buscar2 = "%"+buscar+"%";
                usuarioList = usuarioRepository.buscarGestionCuentasNuevas(buscar2);
            } else if(rol.equals("Repartidor")){
                usuarioList = usuarioRepository.findAllByRolAndCuentaActiva(rol,-1);
            } else if(rol.equals("AdminRestaurante")){
                usuarioList = usuarioRepository.findAllByRolAndCuentaActiva(rol,2);
            }else{
                usuarioList = usuarioRepository.cuentasNuevas();
            }
            model.addAttribute("rolSelected",rol);

            String nombreRol;
            if(rol.equals("Repartidor")){
                nombreRol = "Repartidores";
            }else if(rol.equals("AdminRestaurante")){
                nombreRol = "Restaurantes";
            }else{
                nombreRol = "Todos";
            }
            model.addAttribute("nombreRol",nombreRol);

            //todo agregado para lograr paginacion
            if(usuarioList.size() == 0){

                if(buscar.equals("") && rol.equals("Todos")){
                    attr.addFlashAttribute("msg","No hay nuevas Cuentas que aceptar");
                    return "redirect:/admin/gestionCuentas";
                }else if(buscar.equals("") && (rol.equals("Repartidor") || rol.equals("AdminRestaurante"))){
                    attr.addFlashAttribute("msg","No se encontraron resultados para su búsqueda");
                    model.addAttribute("rolSelected","Todos");
                    return "redirect:/admin/nuevosUsuarios";
                }else{
                    attr.addFlashAttribute("msg","No se encontraron resultados para su búsqueda");
                    return "redirect:/admin/nuevosUsuarios";
                }


            }


            int numberOfPages = (int) Math.ceil(usuarioList.size() / numberOfUsersPerPage);
            if (page > numberOfPages) {
                page = numberOfPages;
            } // validation

            int start = (int) numberOfUsersPerPage * (page - 1);
            int end = (int) (start + numberOfUsersPerPage);

            List<Usuario> lisOfUsersPage = usuarioList.subList(start, Math.min(end, usuarioList.size()));

            model.addAttribute("listaUsuariosNuevos", lisOfUsersPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("maxNumberOfPages", numberOfPages);
            model.addAttribute("searchField", buscar);

            //model.addAttribute("listaUsuariosNuevos",usuarioList);
            return "adminsistema/nuevasCuentas";
        }




        @PostMapping("/buscadorNuevos")
        public String buscarNuevos(@RequestParam(value = "searchField" ,defaultValue = "") String buscar,
                                   @RequestParam(value = "rolSelected" ,defaultValue = "Todos")String rol,
                                   RedirectAttributes attr,
                                   Model model){
            System.out.println("El rol es: " + rol);
            attr.addAttribute("rolSelected",rol);
            attr.addAttribute("searchField", buscar);

            //model.addAttribute("listaUsuariosNuevos",usuarioList);
            //return "adminsistema/nuevasCuentas";
            return "redirect:/admin/nuevosUsuarios";
        }
        //-------------------------------------------
    //----------------------------
    @GetMapping("/newuser")
    public String revisarCuenta(Model model,
            @RequestParam(value = "id",defaultValue = "") String idString, RedirectAttributes attr){
        try{
            int id = Integer.parseInt(idString);
            Optional<Usuario> optional = usuarioRepository.findById(id);
            List<Distritos> distritos = distritosRepository.findAll();
            if(optional.isPresent()){
                Usuario usuario = optional.get();
                if( (usuario.getCuentaActiva() == 2) || (usuario.getCuentaActiva() == -1) ){

                    switch (usuario.getRol()){
                        case "AdminRestaurante":
                            model.addAttribute("usuario",usuario);
                            Restaurante restaurante;
                            List<Categorias> categorias;
                            try{
                                restaurante = restauranteRepository.findRestauranteByUsuario_Idusuarios(id);
                                categorias =  restaurante.getCategoriasrestList();
                            }catch (Exception e){
                                System.out.println(e.getMessage());
                                attr.addFlashAttribute("msg","Error al obtener datos del restaurante");
                                return "redirect:/admin/nuevosUsuarios";
                            }
                            model.addAttribute("distritos",distritos);
                            model.addAttribute("restaurante",restaurante);
                            model.addAttribute("categorias",categorias);
                            return "adminsistema/AceptarCuentaRestaurante";
                        case "Repartidor":
                            Repartidor repartidor;
                            List<Direcciones> listadirecciones;
                            try {
                                repartidor = repartidorRepository.findRepartidorByIdusuariosEquals(id);
                                listadirecciones = direccionesRepository.findAllByUsuario_Idusuarios(id);
                            }catch (Exception e){
                                System.out.println(e.getMessage());
                                attr.addFlashAttribute("msg","Error al obtener datos del repartidor");
                                return "redirect:/admin/nuevosUsuarios";
                            }
                            model.addAttribute("usuario",usuario);
                            model.addAttribute("distritos",distritos);
                            model.addAttribute("repartidor",repartidor);
                            model.addAttribute("lista",listadirecciones);
                            return "adminsistema/AceptarCuentaRepartidor";
                        default:
                            attr.addFlashAttribute("msg","Error al obtener el rol del usuario");
                            return "redirect:/admin/nuevosUsuarios";
                    }
                }
            }else{
                attr.addFlashAttribute("msg","Ocurrió un error con el ID");
                return "redirect:/admin/nuevosUsuarios";
            }
        }catch (NumberFormatException e){
            System.out.println(e.getMessage());
            System.out.println("error");
            attr.addFlashAttribute("msg","Ocurrio un error con el ID");
            return "redirect:/admin/nuevosUsuarios";
        }

        return "redirect:/admin/nuevosUsuarios";
    }

    @GetMapping("/aceptar")
    public String aceptarCuenta(@RequestParam(value="id") int id,
                                RedirectAttributes attr){
        Optional<Usuario> optional = usuarioRepository.findById(id);
        if(optional.isPresent()){
            Usuario usuario = optional.get();
            if(usuario.getCuentaActiva()==2 || usuario.getCuentaActiva()==-1){
                usuario.setCuentaActiva(1);
                usuarioRepository.save(usuario);

                //String direccion = "http://localhost:8090/login";
                //TODO modificar direcion url despues de despliegue aws.
                //Pegar aquí los datos del AWS;
                //String aws = "ec2-user@ec2-3-84-20-210.compute-1.amazonaws.com";
                String direccion="http://g-spicyo.publicvm.com:8080/";

                if(usuario.getRol().equals("AdminRestaurante")){

                        //String direccion = "http://" + aws + ":8081/login";
                        String correoDestino = usuario.getEmail();
                        String subject = "SPYCYO - Restaurante agregado";
                        String texto = "<p><strong>Bienvenido a SPYCYO - Restaurante agregado</strong></p>\n" +
                                "<p>Tenemos el agrado de comunicarle que el restaurante asociado a su cuenta ha sido aprobado.</p>\n" +
                                "<p>Comienze a operar en la plataforma de SPYCYO ahora mismo, con el siguiente link.</p>\n" +
                                "<p>&nbsp;</p>\n" +
                                "<a href='"+direccion+"'>SPYCYO</a> <br><br>Atte. Equipo de Spicyo</b>";

                        sendMailService.sendMail(correoDestino,"saritaatanacioarenas@gmail.com",subject,texto);
                    }
                    if(usuario.getRol().equals("Repartidor")){
                        //String direccion = "http://" + aws + ":8090/login";
                        String correoDestino = usuario.getEmail();
                        String subject = "SPYCYO - Cuenta Repartidor Aceptada";
                        String texto = "<p><strong>Bienvenido a SPYCYO - Cuenta Repartidor Aceptada</strong></p>\n" +
                                "<p>Tenemos el agrado de comunicarle que la cuenta de repartidor creada ha sido aprobada.</p>\n" +
                                "<p>Comienze a operar en la plataforma de SPYCYO ahora mismo, con el siguiente link.</p>\n" +
                                "<p>&nbsp;</p>\n" +
                                "<a href='"+direccion+"'>SPYCYO</a> <br><br>Atte. Equipo de Spicyo</b>";
                        sendMailService.sendMail(correoDestino,"saritaatanacioarenas@gmail.com",subject,texto);
                    }

                    attr.addFlashAttribute("msg","Cuenta aceptada exitosamente");
                    return "redirect:/admin/nuevosUsuarios";
                }
            }
        attr.addFlashAttribute("msg","Ha ocurrido un error,cuenta no aprobada");
        return "redirect:/admin/nuevosUsuarios";
    }

    @GetMapping("/correo")
    public String envioCorreo(@RequestParam(value="id") int id,
                              Model model){
        Optional<Usuario> optional = usuarioRepository.findById(id);
        if(optional.isPresent()) {
            Usuario usuario = optional.get();
            if ((usuario.getRol().equals("Repartidor") && usuario.getCuentaActiva() == -1) || (usuario.getRol().equals("AdminRestaurante") && usuario.getCuentaActiva() == 2)) {
                model.addAttribute("id", id);
                return "adminsistema/ADMIN_RazonDenegacionCuenta";
            }
        }
        return "redirect:/admin/nuevosUsuarios";
    }

    @PostMapping("/denegar")
    public String denegarCuenta(@RequestParam(value="id") int id,
                                @RequestParam(value = "message", defaultValue = "Sin razón") String message,
                                RedirectAttributes attr){
        Optional<Usuario> optional = usuarioRepository.findById(id);
        if(optional.isPresent()){
            Usuario usuario = optional.get();
                if(usuario.getRol().equals("Repartidor") && usuario.getCuentaActiva()==-1){
                    try {
                        List<Direcciones> direccionLista = direccionesRepository.findAllByUsuario_Idusuarios(id);
                        Repartidor repartidor = repartidorRepository.findRepartidorByIdusuariosEquals(id);

                        for(Direcciones direccion: direccionLista){
                            direccionesRepository.deleteById(direccion.getIddirecciones());
                        }
                        repartidorRepository.deleteById(repartidor.getIdrepartidor());

                        usuarioRepository.deleteById(id);
                        //Envio de correo a usuario
                        String correoDestino = usuario.getEmail();
                        String subject = "SPYCYO - Cuenta Denegada";
                        String texto = "<p><strong>Mensaje de SPYCYO - Cuenta Repartidor Denegada</strong></p>\n" +
                                "<p>La cuenta nueva de repartidor que ha registrado se le ha denegado la solicitud de aprobación.</p>\n" +
                                "<p>Motivo: '" + message + "'</p>\n" +
                                "<p>&nbsp;</p>\n" +
                                "<p>Nota: Si en el motivo de denegación se le indicaron puntos que mejorar o detallar, puede volver a intentar a registrar su cuenta</p>\n" +
                                "<p>&nbsp;</p>\n" +
                                "<br>Atte. Equipo de Spicyo</br>";
                        sendMailService.sendMail(correoDestino, "saritaatanacioarenas@gmail.com", subject, texto);

                        attr.addFlashAttribute("msg", "Cuenta denegada exitosamente");
                        return "redirect:/admin/nuevosUsuarios";
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                        attr.addFlashAttribute("msg", "Ocurrió un error al denegar la cuenta");
                        return "redirect:/admin/nuevosUsuarios";
                    }
                }else if(usuario.getRol().equals("AdminRestaurante") && usuario.getCuentaActiva()==2){
                    Restaurante restaurante = restauranteRepository.findRestauranteByUsuario_Idusuarios(id);

                    if(restaurante==null){
                        attr.addFlashAttribute("msg1","Ocurrio un error al denegar la cuenta");
                        System.out.println("Llegamos aqui");
                        return "redirect:/admin/gestionCuentas";
                    }
                    restauranteRepository.deleteById(restaurante.getIdrestaurante());
                    usuario.setCuentaActiva(3);
                    usuarioRepository.save(usuario);

                    //Envio de correo a usuario
                    String correoDestino = usuario.getEmail();
                    String subject = "SPYCYO - Cuenta de Restaurante Denegada";
                    String texto = "<p><strong>Mensaje de SPYCYO - Cuenta de Restaurante Denegada</strong></p>\n" +
                            "<p>El restaurante asociado a la cuenta que ha registrado se le ha denegado la solicitud de aprobación.</p>\n" +
                            "<p>Motivo: '" +message +"'</p>\n" +
                            "<p>&nbsp;</p>\n" +
                            "<p>Nota: Si en el motivo de denegación se le indicaron puntos que mejorar o detallar, puede volver a intentar a registrar su cuenta</p>\n" +
                            "<p>&nbsp;</p>\n" +
                            "<br>Atte. Equipo de Spicyo</br>";

                    sendMailService.sendMail(correoDestino,"saritaatanacioarenas@gmail.com",subject,texto);

                    attr.addFlashAttribute("msg","Cuenta denegada exitosamente");
                    return "redirect:/admin/nuevosUsuarios";
                }
            attr.addFlashAttribute("msg","Ocurrio un error al denegar la cuenta");
            return "redirect:/admin/nuevosUsuarios";
        }
        attr.addFlashAttribute("msg","Ocurrio un error al denegar la cuenta");
        return "redirect:/admin/nuevosUsuarios";
    }




    @GetMapping("/adminForm")
    public String adminForm(Model model,HttpSession session){
        model.addAttribute("usuario", new Usuario());
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        int idAdmin = usuario.getIdusuarios();
        if(idAdmin!=1){
            return "redirect:/admin/gestionCuentas";
        }
        return "adminsistema/agregarAdmin";
    }

    public  boolean validarContrasenia(String contrasenia1) {
        /*      https://mkyong.com/regular-expressions/how-to-validate-password-with-regular-expression/
                A!@#&()–a1
                A[{}]:;',?/*a1
                A~$^+=<>a1
                0123456789$abcdefgAB
                123Aa$Aa
         */
        Pattern pattern1 = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$");
        Matcher matcher1 = pattern1.matcher(contrasenia1);
        return matcher1.matches();
    }

    @PostMapping("/agregarAdmin")
    public String agregarAdmin(@ModelAttribute("usuario") @Valid Usuario usuario, BindingResult bindingResult,
                               @RequestParam("password2") String password2,
                               Model model, RedirectAttributes attr){



            // 1. Validacion de contraseñas
            Boolean contraseniasValidas = false; // para la validacion final
            Boolean validacionContrasenias = validarContrasenia(password2);
            if(usuario.getContraseniaHash().equals(password2) && validacionContrasenias==true) {

                String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());
                usuario.setContraseniaHash(contraseniahashbcrypt);
                contraseniasValidas = true;

            }else {

                if (!usuario.getContraseniaHash().equals(password2)){
                    model.addAttribute("contras","Las contraseñas no coinciden");
                }
                if (validacionContrasenias == false){
                    model.addAttribute("contras2","La contraseña no cumple con los requisitos minimos");
                }
            }

            // 2. Validacion de correo unico
            Boolean correoUnico = false; // para la validacion final
            Optional<Usuario> usuarioValidacion = Optional.ofNullable(usuarioRepository.findByEmailEquals(usuario.getEmail()));
            if(!usuarioValidacion.isPresent()){
                correoUnico = true;

            }else{
                model.addAttribute("emailUnico", "Ya existe un usuario registrado con el mismo correo");
            }

            // 3. Validacion de dni
            boolean dniValido = validarDNI(usuario.getDni());
            if(dniValido == false){
                model.addAttribute("dniValido", "Ingrese un DNI valido");
            }

            // 4. Validacion de sexo
            Boolean sexoValido = false;
            if(usuario.getSexo().equals("Masculino") || usuario.getSexo().equals("Femenino")){
                sexoValido = true;
            } else{
                model.addAttribute("sexoValido", "Ingrese un sexo valido");
            }

            // Guardar usuario
            if(contraseniasValidas == true && correoUnico == true && dniValido == true && sexoValido == true){
                usuarioRepository.save(usuario);
                attr.addFlashAttribute("msg", "Administrador creado exitosamente");
            } else{
                return "adminsistema/agregarAdmin";
            }


        return "redirect:/admin/gestionCuentas";
    }



    //Reportes

    /**excel**/
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

    @GetMapping("/reportes")
    public String reportesAdmin(){
        return "adminsistema/ADMIN_Reportes";
    }

    @GetMapping("/usuarioReportes")
    public String usuariosReportes(Model model,RedirectAttributes attr,
                                   @RequestParam(value ="searchField",defaultValue = "") String searchField,
                                   @RequestParam(value = "rol" ,defaultValue = "Todos")String rol,
                                   @RequestParam(value = "page",defaultValue = "1") String requestedPage){

        float numberOfUsersPerPage = 8;
        int page;
        try {
            page = Integer.parseInt(requestedPage);
        }catch (Exception e){
            page = 1;
        }

        System.out.println("Busqueda: "+searchField);
        System.out.println("Rol: "+rol );

        List<Usuario> usuarioList;
        String buscar = "%" + searchField + "%";

        if(!searchField.equals("") && (rol.equals("Repartidor") || rol.equals("AdminRestaurante") || rol.equals("Cliente"))) {
            usuarioList = usuarioRepository.cuentasActualesRol(buscar,rol);

        }else if(!searchField.equals("")){
            usuarioList = usuarioRepository.cuentasActuales(buscar);

        }else if(rol.equals("Repartidor") || rol.equals("AdminRestaurante") || rol.equals("Cliente")){
            usuarioList = usuarioRepository.findAllByRolAndCuentaActiva(rol,1);

        }else{
            usuarioList = usuarioRepository.usuarioreportes();
        }

        String nombreRol;
        if(rol.equals("Repartidor")){
            nombreRol = "Repartidores";
        }else if(rol.equals("AdminRestaurante")){
            nombreRol = "Restaurantes";
        }else if(rol.equals("Cliente")){
            nombreRol = "Clientes";
        }else{
            nombreRol = "Todos";
        }

        int numberOfPages = (int) Math.ceil(usuarioList.size() / numberOfUsersPerPage);
        if (page > numberOfPages) {
            page = numberOfPages;
        } // validation

        int start = (int) numberOfUsersPerPage * (page - 1);
        int end = (int) (start + numberOfUsersPerPage);

        List<Usuario> lisOfUsersPage = usuarioList.subList(start, Math.min(end, usuarioList.size()));

        model.addAttribute("currentPage", page);
        model.addAttribute("maxNumberOfPages", numberOfPages);
        model.addAttribute("listaUsuariosreporte", lisOfUsersPage);

        model.addAttribute("nombreRol",nombreRol);

        model.addAttribute("rol",rol);
        model.addAttribute("searchField", searchField);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("formatoFecha",format);

        return "adminsistema/ADMIN_ReportesVistaUsuarios";
    }

    @PostMapping("/buscadorReportesUsuarios")
    public String buscadorReportesUsuarios(@RequestParam(value ="searchField",defaultValue = "") String searchField,
                                           @RequestParam(value = "rol",defaultValue = "Todos") String rol,
                                           RedirectAttributes attr,Model model){
        attr.addAttribute("rol",rol);
        attr.addAttribute("searchField", searchField);
        return "redirect:/admin/usuarioReportes";
    }


    @GetMapping("/RestaurantesReportes")
    public String restaurantesReportes(Model model,RedirectAttributes attr,
                                       @RequestParam(value = "page",defaultValue = "1") String requestedPage,
                                       @RequestParam(value ="searchField",defaultValue = "") String searchField){
        List<RestauranteReportes_DTO> reporteLista;
        if(!searchField.equals("")){
            String buscar = "%" + searchField + "%";
            reporteLista = restauranteRepository.reportesRestaurantes2(buscar);

            if(reporteLista.size()==0){
                reporteLista = restauranteRepository.reportesRestaurantes();
                model.addAttribute("msg","No se encontraron resultados para su búsqueda");
            }

        }else{
            reporteLista = restauranteRepository.reportesRestaurantes();
        }

        float numberOfUsersPerPage = 5;
        int page;
        try {
            page = Integer.parseInt(requestedPage);
        }catch (Exception e){
            page = 1;
        }

        int numberOfPages = (int) Math.ceil(reporteLista.size() / numberOfUsersPerPage);
        if (page > numberOfPages) {
            page = numberOfPages;
        } // validation

        int start = (int) numberOfUsersPerPage * (page - 1);
        int end = (int) (start + numberOfUsersPerPage);

        List<RestauranteReportes_DTO> lisOfUsersPage = reporteLista.subList(start, Math.min(end, reporteLista.size()));

        model.addAttribute("currentPage", page);
        model.addAttribute("maxNumberOfPages", numberOfPages);

        model.addAttribute("reporteLista",lisOfUsersPage);


        double max = 0;
        int indicemayor = 0;
        for (int i = 0; i < reporteLista.size(); i++) {
            if (reporteLista.get(i).getVentastotales() > max) {
                max = reporteLista.get(i).getVentastotales();
                indicemayor = i;
            }
        }
        double min = max;
        int indicemenor = indicemayor;
        for (int i = 0; i < reporteLista.size(); i++) {
            if (reporteLista.get(i).getVentastotales() < min) {
                min = reporteLista.get(i).getVentastotales();
                indicemenor = i;
            }
        }
        RestauranteReportes_DTO mayor = reporteLista.get(indicemayor);
        RestauranteReportes_DTO menor = reporteLista.get(indicemenor);

        model.addAttribute("mayorrest",mayor);
        model.addAttribute("menorrest",menor);
        //t.stream().mapToDouble(i -> i).max().getAsDouble()
        return "adminsistema/ADMIN_ReportesVistaRestaurantes";
    }

    @PostMapping("/RestaurantesBuscador")
    public String restaurantesReportes2(Model model,RedirectAttributes attr,
                                        @RequestParam(value ="searchField",defaultValue = "") String searchField){

        attr.addAttribute("searchField", searchField);
        return "redirect:/admin/RestaurantesReportes";
    }

    @GetMapping("/RepartidorReportes")
    public String repartidorReportes(Model model,
                                     @RequestParam(value = "page",defaultValue = "1") String requestedPage,
                                     @RequestParam(value ="searchField",defaultValue = "") String searchField){


        List<RepartidoresReportes_DTO> reporteLista;
        if(!searchField.equals("")){
            String buscar = "%" + searchField + "%";
            reporteLista = repartidorRepository.reporteRepartidores2(buscar);
            if(reporteLista.size()==0){
                reporteLista = repartidorRepository.reporteRepartidores();
                model.addAttribute("msg","No se encontraron resultados para su búsqueda");
            }
        }else{
            reporteLista = repartidorRepository.reporteRepartidores();
        }



        float numberOfUsersPerPage = 5;
        int page;
        try {
            page = Integer.parseInt(requestedPage);
        }catch (Exception e){
            page = 1;
        }

        int numberOfPages = (int) Math.ceil(reporteLista.size() / numberOfUsersPerPage);
        if (page > numberOfPages) {
            page = numberOfPages;
        } // validation

        int start = (int) numberOfUsersPerPage * (page - 1);
        int end = (int) (start + numberOfUsersPerPage);

        List<RepartidoresReportes_DTO> lisOfUsersPage = reporteLista.subList(start, Math.min(end, reporteLista.size()));

        model.addAttribute("currentPage", page);
        model.addAttribute("maxNumberOfPages", numberOfPages);

        model.addAttribute("reporteLista",lisOfUsersPage);




        int max = 0;
        int indicemayor = 0;
        for (int i = 0; i < reporteLista.size(); i++) {
            if (reporteLista.get(i).getPedidos() > max) {
                max = reporteLista.get(i).getPedidos();
                indicemayor = i;
            }
        }
        int min = max;
        int indicemenor = indicemayor;
        for (int i = 0; i < reporteLista.size(); i++) {
            if (reporteLista.get(i).getPedidos() < min) {
                min = reporteLista.get(i).getPedidos();
                indicemenor = i;
            }
        }
        RepartidoresReportes_DTO mayor = reporteLista.get(indicemayor);
        RepartidoresReportes_DTO menor = reporteLista.get(indicemenor);

        model.addAttribute("mayorrep",mayor);
        model.addAttribute("menorrep",menor);

        return "adminsistema/ADMIN_ReportesVistaRepartidor";
    }

    @PostMapping("/RepartidorBuscador")
    public String repartidorReportes2(Model model,RedirectAttributes attr,
                                        @RequestParam(value ="searchField",defaultValue = "") String searchField){

        attr.addAttribute("searchField", searchField);
        return "redirect:/admin/RepartidorReportes";
    }

    @GetMapping("/DeliveryReportes")
    public String deliveryReportes(@RequestParam(name= "searchField",defaultValue = "") String buscar,
                                   @RequestParam (value="filtro",defaultValue = "Todos") String filtro,
                                   @RequestParam(name = "page", defaultValue = "1") String requestedPage,
                                   Model model){

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


        System.out.println("errorr ***********************");
        float numberOfUsersPerPage = 8;
        int page;
        try {
            page = Integer.parseInt(requestedPage);
        }catch (Exception e){
            page = 1;
        }
        List<DeliveryReportes_DTO> listaDeli;
        if(buscar.equals("") && filtro.equals("Habiles")){
            listaDeli = pedidosRepository.reportesDelivery();
        }else{
            String fechaPrimerPedido = pedidosRepository.primerPedido();
            listaDeli = pedidosRepository.reportesDelivery2(fechaPrimerPedido);
        }

        String nombreFiltro;
        if(filtro.equals("Habiles")){
            nombreFiltro = "Fechas con Pedidos";
        }else{
            nombreFiltro = "Todas las Fechas";
        }
        model.addAttribute("nombreFiltro",nombreFiltro);


        int numberOfPages = (int) Math.ceil(listaDeli.size() / numberOfUsersPerPage);
        if (page > numberOfPages) {
            page = numberOfPages;
        } // validation

        int start = (int) numberOfUsersPerPage * (page - 1);
        int end = (int) (start + numberOfUsersPerPage);

        System.out.println(listaDeli.get(0).getPedidos());
        System.out.println("******************************");
        System.out.println(listaDeli.get(0).getComision());
        System.out.println(listaDeli);
        List<DeliveryReportes_DTO> lisOfUsersPage = listaDeli.subList(start, Math.min(end, listaDeli.size()));
        System.out.println(lisOfUsersPage);
        System.out.println(listaDeli.get(0));
        System.out.println(listaDeli);
        System.out.println("***********************************");
        //model.addAttribute("lisOfUsersPage", lisOfUsersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("maxNumberOfPages", numberOfPages);
        model.addAttribute("listadeli", lisOfUsersPage);

        model.addAttribute("filtro",filtro);

        //buscar por fecha
        model.addAttribute("fechaseleccionada",fechamostrar);

        return "adminsistema/ADMIN_ReportesVistaDelivery";
    }

    @GetMapping("/buscadorDeli")
    public String buscadorFalso(){
        return "redirect:/admin/DeliveryReportes";
    }


    @PostMapping("/buscadorDeli")
    public String buscadorDelivery(@RequestParam("fechahorapedido") String fechahorapedido,
                                   RedirectAttributes attr,Model model){


        float numberOfUsersPerPage = 31;
        int page = 1;

        String[] fecha = fechahorapedido.split("-", 2);

        LocalDate dateactual = LocalDate.now();
        String fechaactual1 = String.valueOf(dateactual);

        List<DeliveryReportes_DTO> listaDeli;

        try {
            String a = fecha[0];
            String m = fecha[1];
            int anio = Integer.parseInt(a);
            int mes = Integer.parseInt(m);

            listaDeli = pedidosRepository.reportesDeliveryFecha(anio,mes);
            if(listaDeli.isEmpty()){
                attr.addFlashAttribute("msg","No se encontraron resultados");
                return "redirect:/admin/DeliveryReportes";
            }

            String nombreFiltro = "Mes";

            int numberOfPages = (int) Math.ceil(listaDeli.size() / numberOfUsersPerPage);
            if (page > numberOfPages) {
                page = numberOfPages;
            } // validation

            int start = (int) numberOfUsersPerPage * (page - 1);
            int end = (int) (start + numberOfUsersPerPage);

            model.addAttribute("currentPage", page);
            model.addAttribute("maxNumberOfPages", numberOfPages);
            model.addAttribute("listadeli", listaDeli);

            model.addAttribute("filtro","Todos");
            model.addAttribute("nombreFiltro",nombreFiltro);

            //buscar por fecha
            model.addAttribute("fechaseleccionada",fechahorapedido);

            return "adminsistema/ADMIN_ReportesVistaDelivery";

        }catch (Exception e){
            attr.addFlashAttribute("msg","Ocurrio un error - Lista no valida");
            System.out.println(e);
            return "redirect:/admin/DeliveryReportes";

        }

    }
    //todo:Adaptar para exportar excel

    @GetMapping("/exportarUsuarios")
    public ResponseEntity<InputStreamResource> exportUsuarios() throws Exception {
        ByteArrayInputStream stream2 = exportAllData1();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reportesUsuarios.xls");

        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(stream2));
    }

    public ByteArrayInputStream exportAllData1() throws IOException {
        String[] columns = { "USUARIO", "ROL", "CORREO","TELEFONO","ULTIMA FECHA DE INGRESO" };

        Workbook workbook = new HSSFWorkbook();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CellStyle headStyle = createHeadStyle(workbook);

        Sheet sheet = workbook.createSheet("Usuarios");
        Row row = sheet.createRow(0);
        System.out.println(columns.length);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headStyle);
        }

        List<Usuario> usuarioList = usuarioRepository.usuarioreportes();

        int initRow = 1;
        for (Usuario usuario: usuarioList) {
            row = sheet.createRow(initRow);
            row.createCell(0).setCellValue(usuario.getNombre()+ " " +usuario.getApellidos());
            row.createCell(1).setCellValue(usuario.getRol());
            row.createCell(2).setCellValue(usuario.getEmail());
            row.createCell(3).setCellValue(Integer.toString(usuario.getTelefono()));
            if(usuario.getUltimafechaingreso()!=null){
                row.createCell(4).setCellValue(String.valueOf(usuario.getUltimafechaingreso()).replace('T',' '));
            }
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

    @GetMapping("/exportarDelivery")
    public ResponseEntity<InputStreamResource> exportDelivery() throws Exception {
        ByteArrayInputStream stream2 = exportAllData2();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reportesDelivery.xls");

        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(stream2));
    }

    public ByteArrayInputStream exportAllData2() throws IOException {
        String[] columns = { "FECHA","PEDIDOS COMPLETADOS","COMISION SISTEMA" };

        Workbook workbook = new HSSFWorkbook();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CellStyle headStyle = createHeadStyle(workbook);

        Sheet sheet = workbook.createSheet("Delivery");
        Row row = sheet.createRow(0);
        System.out.println(columns.length);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headStyle);
        }
        String primerpedido = pedidosRepository.primerPedido();
        List<DeliveryReportes_DTO> deliveryReportes = pedidosRepository.reportesDelivery2(primerpedido);

        int initRow = 1;
        for (DeliveryReportes_DTO delivery : deliveryReportes) {
            row = sheet.createRow(initRow);
            row.createCell(0).setCellValue(String.valueOf(delivery.getFecha()));
            row.createCell(1).setCellValue(delivery.getPedidos());
            row.createCell(2).setCellValue(delivery.getComision());
            initRow++;

        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        workbook.write(stream);
        workbook.close();
        return new ByteArrayInputStream(stream.toByteArray());
    }

    @GetMapping("/exportarRestaurante")
    public ResponseEntity<InputStreamResource> exportRestaurante() throws Exception {
        ByteArrayInputStream stream2 = exportAllData3();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reportesRestaurantes.xls");

        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(stream2));
    }

    public ByteArrayInputStream exportAllData3() throws IOException {
        String[] columns = { "RESTAURANTE","ENCARGADO","PEDIDOS COMPLETADOS","VENTAS TOTALES S/." };

        Workbook workbook = new HSSFWorkbook();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CellStyle headStyle = createHeadStyle(workbook);


        Sheet sheet = workbook.createSheet("Restaurantes");
        Row row = sheet.createRow(0);
        System.out.println(columns.length);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headStyle);
        }

        List<RestauranteReportes_DTO> restaurantesList = restauranteRepository.reportesRestaurantes();
        int initRow = 1;
        for (RestauranteReportes_DTO restaurante : restaurantesList) {
            row = sheet.createRow(initRow);
            row.createCell(0).setCellValue(restaurante.getRestnombre());
            row.createCell(1).setCellValue(restaurante.getNombre()+" "+restaurante.getApellidos());
            row.createCell(2).setCellValue(restaurante.getPedidos());
            row.createCell(3).setCellValue(restaurante.getVentastotales());
            initRow++;

        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        workbook.write(stream);
        workbook.close();
        return new ByteArrayInputStream(stream.toByteArray());
    }

    @GetMapping("/exportarRepartidor")
    public ResponseEntity<InputStreamResource> exportRepartidor() throws Exception {
        ByteArrayInputStream stream2 = exportAllData4();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reportesRepartidor.xls");

        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(stream2));
    }

    public ByteArrayInputStream exportAllData4() throws IOException {
        String[] columns = { "REPARTIDOR","DNI","MOVILIDAD","PEDIDOS REALIZADOS","COMISION S/." };

        Workbook workbook = new HSSFWorkbook();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CellStyle headStyle = createHeadStyle(workbook);

        Sheet sheet = workbook.createSheet("Repartidor");
        Row row = sheet.createRow(0);
        System.out.println(columns.length);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headStyle);
        }

        List<RepartidoresReportes_DTO> repartidorList = repartidorRepository.reporteRepartidores();
        int initRow = 1;
        for (RepartidoresReportes_DTO repartidor : repartidorList) {
            row = sheet.createRow(initRow);
            row.createCell(0).setCellValue(repartidor.getNombre()+" "+repartidor.getApellidos());
            row.createCell(1).setCellValue(repartidor.getDni());
            row.createCell(2).setCellValue(repartidor.getMovilidad());
            row.createCell(3).setCellValue(repartidor.getPedidos());
            row.createCell(4).setCellValue(repartidor.getComision());
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
}
