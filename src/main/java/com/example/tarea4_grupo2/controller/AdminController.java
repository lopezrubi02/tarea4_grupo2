package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.service.*;
import com.example.tarea4_grupo2.dto.DeliveryReportes_DTO;
import com.example.tarea4_grupo2.dto.RepartidoresReportes_DTO;
import com.example.tarea4_grupo2.dto.RestauranteReportes_DTO;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
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
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/")
    public String redireccion(){
        return "redirect:/admin/gestionCuentas";
    }

    @GetMapping("/usuariosActuales")
    public String usuariosActuales(
            @RequestParam(name = "page", defaultValue = "1") String requestedPage,
            @RequestParam(name = "searchField", defaultValue = "") String searchField,
            @RequestParam(name = "rol", defaultValue = "") String rol,
            Model model
    ) {
        /**
         * Validaciones
         * ---
         *      + Si la pagina recibida es mayor a la maxima posible
         *
         */

        float numberOfUsersPerPage = 7;
        int page = Integer.parseInt(requestedPage);

        List<Usuario> usuarioList; // se define el contenido de la lista (la paginacion se hace a partir de esta)

        if (!searchField.equals("") && !rol.equals("")) {
            // si es que no estan vacios, se filtra por rol y nombre
            usuarioList = usuarioRepository.findAllByRolAndCuentaActivaAndNombre(rol, 1, searchField);
        } else if (!searchField.equals("")) {
            // si el nobre es el que no esta vacio, se filtra por nombre
            usuarioList = usuarioRepository.findAllByNombreAndCuentaActiva(searchField, 1);
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
                                 Model model) {

        System.out.println(rol);

        redirectAttributes.addAttribute("rol", rol);
        redirectAttributes.addAttribute("searchField", searchField);
        return "redirect:/admin/usuariosActuales";
    }

    @GetMapping("/user")
    public String paginaUsuario(
            Model model,
            @RequestParam("id") int id
    ) {
        Optional<Usuario> optional = usuarioRepository.findById(id);

        if (optional.isPresent()) {
            Usuario usuario = optional.get();

            switch (usuario.getRol()) {
                case "AdminSistema":
                    model.addAttribute("usuario", usuario);
                    return "adminsistema/datosAdmin";

                case "Repartidor":
                    model.addAttribute("usuario", usuario);

                    Repartidor repartidor = repartidorRepository.findRepartidorByIdusuariosEquals(id);
                    model.addAttribute("repartidor", repartidor);

                    return "adminsistema/datosRepartidor";

                case "Cliente":
                    model.addAttribute("usuario", usuario);

                    List<Direcciones> direccionesList = direccionesRepository.findAllByUsuariosIdusuariosEquals(id);
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
    }

    @GetMapping("/miCuenta")
    public String miCuenta(
            Model model,
            @ModelAttribute("usuario") @Valid Usuario usuarioRecibido,
            BindingResult bindingResult,
            HttpSession session){

        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
        int id = usuarioActual.getIdusuarios();

        Optional<Usuario> optional = usuarioRepository.findById(id);
        Usuario usuario = optional.get();
        model.addAttribute("usuario", usuario);

        return "adminsistema/miCuenta";
    }

    @PostMapping("/miCuenta")
    public String updateAdminInfo(
            @ModelAttribute("usuario") @Valid Usuario usuarioRecibido,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ){

        if(bindingResult.hasErrors()) {
            return "adminsistema/miCuenta";
        } else {
            // se obtiene el usuario en la base de datos para actualizar solo los campos que han cambiado
            Optional<Usuario> optionalUsuario = usuarioRepository.findById(usuarioRecibido.getIdusuarios());
            Usuario usuarioEnlabasededatos = optionalUsuario.get();

            usuarioEnlabasededatos.setNombre(usuarioRecibido.getNombre());
            usuarioEnlabasededatos.setEmail(usuarioRecibido.getEmail());
            usuarioEnlabasededatos.setDni(usuarioRecibido.getDni());
            usuarioEnlabasededatos.setTelefono(usuarioRecibido.getTelefono());
            usuarioEnlabasededatos.setFechaNacimiento(usuarioRecibido.getFechaNacimiento());
            usuarioEnlabasededatos.setSexo(usuarioRecibido.getSexo());

            usuarioRepository.save(usuarioEnlabasededatos);

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
            int page = Integer.parseInt(requestedPage);

            List<Usuario> usuarioList;

            if(!buscar.equals("")){
                String buscar2 = "%"+buscar+"%";
                usuarioList = usuarioRepository.buscarGestionCuentasNuevas(buscar2);
            } else if(rol.equals("Repartidor") || rol.equals("AdminRestaurante") ){
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

                if(buscar.equals("")){
                    attr.addFlashAttribute("msg","No hay nuevas Cuentas que aceptar");
                    return "redirect:/admin/gestionCuentas";
                }

                attr.addFlashAttribute("msg","No se encontraron resultados para su busqueda");
                return "redirect:/admin/nuevosUsuarios";
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
            model.addAttribute("rolSelected",rol);
            attr.addAttribute("searchField", buscar);

            //model.addAttribute("listaUsuariosNuevos",usuarioList);
            //return "adminsistema/nuevasCuentas";
            return "redirect:/admin/nuevosUsuarios";
        }
        //-------------------------------------------
    //----------------------------
    @GetMapping("/newuser")
    public String revisarCuenta(Model model,
            @RequestParam(value = "id") String idString, RedirectAttributes attr){
        try{
            int id = Integer.parseInt(idString);
            Optional<Usuario> optional = usuarioRepository.findById(id);
            if(optional.isPresent()){
                Usuario usuario = optional.get();
                if(usuario.getCuentaActiva()==2){

                    switch (usuario.getRol()){
                        case "AdminRestaurante":
                            model.addAttribute("usuario",usuario);

                            Restaurante restaurante = restauranteRepository.findRestauranteByUsuario_Idusuarios(id);
                            List<Categorias> categorias =  restaurante.getCategoriasrestList();

                            model.addAttribute("restaurante",restaurante);
                            model.addAttribute("categorias",categorias);
                            return "adminsistema/AceptarCuentaRestaurante";
                        case "Repartidor":
                            model.addAttribute("usuario",usuario);
                            Repartidor repartidor = repartidorRepository.findRepartidorByIdusuariosEquals(id);
                            List<Direcciones> listadirecciones = direccionesRepository.findAllByUsuariosIdusuariosEquals(id);
                            model.addAttribute("repartidor",repartidor);
                            model.addAttribute("lista",listadirecciones);
                            return "adminsistema/AceptarCuentaRepartidor";
                        default:
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
            if(usuario.getCuentaActiva()==2){
                    usuario.setCuentaActiva(1);
                    usuarioRepository.save(usuario);
                    if(usuario.getRol().equals("AdminRestaurante")){
                        String direccion = "http://localhost:8090/login";
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
                        String direccion = "http://localhost:8090/login";
                        String correoDestino = usuario.getEmail();
                        String subject = "SPYCYO - Cuenta Repartidor Aceptada";
                        String texto = "<p><strong>Bienvenido a SPYCYO - Cuenta Repartidor Aceptada</strong></p>\n" +
                                "<p>Tenemos el agrado de comunicarle que la cuenta de repartidor creada ha sido aprobada.</p>\n" +
                                "<p>Comienze a operar en la plataforma de SPYCYO ahora mismo, con el siguiente link.</p>\n" +
                                "<p>&nbsp;</p>\n" +
                                "<a href='"+direccion+"'>SPYCYO</a> <br><br>Atte. Equipo de Spicyo</b>";
                        sendMailService.sendMail(correoDestino,"saritaatanacioarenas@gmail.com",subject,texto);
                    }

                    attr.addFlashAttribute("msg1","Cuenta aceptada exitosamente");
                    return "redirect:/admin/nuevosUsuarios";
                }
            }
        attr.addFlashAttribute("msg1","Ha ocurrido un error,cuenta no aprobada");
        return "redirect:/admin/nuevosUsuarios";
    }

    @GetMapping("/correo")
    public String envioCorreo(@RequestParam(value="id") int id,
                              Model model){
        model.addAttribute("id",id);
        return "adminsistema/ADMIN_RazonDenegacionCuenta";
    }

    @PostMapping("/denegar")
    public String denegarCuenta(@RequestParam(value="id") int id,
                                @RequestParam(value = "message", defaultValue = "Sin razón") String message,
                                RedirectAttributes attr){
        Optional<Usuario> optional = usuarioRepository.findById(id);
        if(optional.isPresent()){
            Usuario usuario = optional.get();
            if(usuario.getRol().equals("Repartidor")){
                usuarioRepository.deleteById(id);
                //Envio de correo a usuario
                String correoDestino = usuario.getEmail();
                String subject = "SPYCYO - Cuenta Denegada";
                String texto = "<p><strong>Mensaje de SPYCYO - Cuenta Repartidor Denegada</strong></p>\n" +
                        "<p>La cuenta nueva de repartidor que ha registrado se le ha denegado la solicitud de aprobación.</p>\n" +
                        "<p>Motivo: '" +message +"'</p>\n" +
                        "<p>&nbsp;</p>\n" +
                        "<p>Nota: Si en el motivo de denegación se le indicaron puntos que mejorar o detallar, puede volver a intentar a registrar su cuenta</p>\n" +
                        "<p>&nbsp;</p>\n" +
                        "<br>Atte. Equipo de Spicyo</br>";
                sendMailService.sendMail(correoDestino,"saritaatanacioarenas@gmail.com",subject,texto);

                attr.addFlashAttribute("msg1","Cuenta denegada exitosamente");
                return "redirect:/admin/nuevosUsuarios";

            }else if(usuario.getRol().equals("AdminRestaurante")){
                Restaurante restaurante = restauranteRepository.findRestauranteByUsuario_Idusuarios(id);

                if(restaurante==null){
                    attr.addFlashAttribute("msg1","Ocurrio un error al denegar la cuenta");
                    System.out.println("Llegamos aqui");
                    return "redirect:/admin/gestionCuentas";
                }
                restauranteRepository.deleteById(restaurante.getIdrestaurante());
                usuario.setCuentaActiva(-1);
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


    @PostMapping("/agregarAdmin")
    public String agregarAdmin(@ModelAttribute("usuario") @Valid Usuario usuario, BindingResult bindingResult,
                               @RequestParam("password2") String password2,
                               @RequestParam(value = "contras",defaultValue = "") String contras,
                               Model model, RedirectAttributes attr){

        if(bindingResult.hasErrors()){
            return "adminsistema/agregarAdmin";
        }
        else {
            if(usuario.getContraseniaHash().equals(password2)) {
                String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());
                usuario.setContraseniaHash(contraseniahashbcrypt);
                usuarioRepository.save(usuario);
                attr.addFlashAttribute("msg", "Administrador creado exitosamente");

            }else {
                model.addAttribute("contras","Contraseña no coinciden");
                return "adminsistema/agregarAdmin";
            }
        }

        return "redirect:/admin/gestionCuentas";
    }


    //Reportes

    //Reportes

    @GetMapping("/reportes")
    public String reportesAdmin(){
        return "adminsistema/ADMIN_Reportes";
    }

    @GetMapping("/usuarioReportes")
    public String usuariosReportes(Model model){
        List<Usuario> usuarioList = usuarioRepository.usuarioreportes();
        model.addAttribute("listaUsuariosreporte",usuarioList);
        return "adminsistema/ADMIN_ReportesVistaUsuarios";
    }


    @GetMapping("/usuarioFiltro")
    public String usuarioFiltro(Model model,@RequestParam(value = "rolSelected" ,defaultValue = "Todos")String rol){
        List<Usuario> usuarioList;
        if(rol.equals("Repartidor") || rol.equals("AdminRestaurante") || rol.equals("Cliente")  ){
            usuarioList = usuarioRepository.findAllByRolAndCuentaActiva(rol,1);
        }else{
            usuarioList = usuarioRepository.usuarioreportes();
        }

        model.addAttribute("listaUsuariosreporte",usuarioList);

        return "adminsistema/ADMIN_ReportesVistaUsuarios";
    }


    @GetMapping("/RestaurantesReportes")
    public String restaurantesReportes(Model model){
        List<RestauranteReportes_DTO> reporteLista = restauranteRepository.reportesRestaurantes();
        model.addAttribute("reporteLista",reporteLista);

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

    @GetMapping("/RepartidorReportes")
    public String repartidorReportes(Model model){
        List<RepartidoresReportes_DTO>  reporteLista = repartidorRepository.reporteRepartidores();
        model.addAttribute("reporteLista",reporteLista);

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

    @GetMapping("/DeliveryReportes")
    public String deliveryReportes(Model model,
                                   @RequestParam(name = "page", defaultValue = "1") String requestedPage){
        System.out.println("errorr ***********************");
        float numberOfUsersPerPage = 8;
        int page = Integer.parseInt(requestedPage);

        String fechaPrimerPedido = pedidosRepository.primerPedido();
        List<DeliveryReportes_DTO> listaDeli = pedidosRepository.reportesDelivery2(fechaPrimerPedido);

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

        return "adminsistema/ADMIN_ReportesVistaDelivery";
    }


}
