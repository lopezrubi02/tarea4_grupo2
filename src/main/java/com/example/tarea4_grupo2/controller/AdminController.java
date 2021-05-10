package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.RepartidoresReportes_DTO;
import com.example.tarea4_grupo2.dto.RestauranteReportes_DTO;
import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Repartidor;
import com.example.tarea4_grupo2.entity.Restaurante;
import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.RepartidorRepository;
import com.example.tarea4_grupo2.repository.RestauranteRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
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

        // todo html para el post

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

            // TODO switch case
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

                    //Restaurante restaurante = restauranteRepository.findRestauranteByIdadminrestEquals(id);
                    //model.addAttribute("restaurante", restaurante);

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
            Model model){
        // TODO se harcodeo el id del actual usuario logeado
        int id = 1;

        Optional<Usuario> optional = usuarioRepository.findById(id);
        Usuario usuario = optional.get();
        model.addAttribute("usuario", usuario);

        return "adminsistema/miCuenta";
    }

    @PostMapping("/miCuenta")
    public String updateAdminInfo(
            Usuario usuarioRecibido,
            RedirectAttributes redirectAttributes
    ){

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

    //Gestion de Nuevas Cuentas

    @GetMapping("/gestionCuentas")
    public String gestionCuentas(){
        return "adminsistema/GestionCuentasPrincipal";
    }

    @GetMapping("/nuevosUsuarios")
    public String nuevosUsuarios(Model model,@RequestParam(value = "rolSelected" ,defaultValue = "Todos")String rol){
        List<Usuario> usuarioList;
        if(rol.equals("Repartidor") || rol.equals("AdminRestaurante") ){
            usuarioList = usuarioRepository.findAllByRolAndCuentaActiva(rol,0);
        }else{
            usuarioList = usuarioRepository.cuentasNuevas();
        }

        //usuarioList = usuarioRepository.findAllByCuentaActivaEquals(0);
        model.addAttribute("listaUsuariosNuevos",usuarioList);

        return "adminsistema/nuevasCuentas";
    }




    @PostMapping("/buscadorNuevos")
    public String buscarNuevos(@RequestParam(value = "searchField" ,defaultValue = "") String buscar,
                               @RequestParam(value = "rolSelected" ,defaultValue = "Todos")String rol,
                               Model model){
        List<Usuario> usuarioList;
        System.out.println("El rol es: " + rol);
        if(rol.equals("Repartidor") || rol.equals("AdminRestaurant")){
            usuarioList = usuarioRepository.findAllByRolAndNombreAndCuentaActiva(rol,buscar,0);
        }else{
            buscar = "%"+buscar+"%";
            usuarioList = usuarioRepository.buscarGestionCuentasNuevas(buscar);
        }
        model.addAttribute("listaUsuariosNuevos",usuarioList);
        model.addAttribute(rol,"rolSelected");
        return "adminsistema/nuevasCuentas";
    }

    @GetMapping("/newuser")
    public String revisarCuenta(Model model,
            @RequestParam(value = "id") int id){
        Optional<Usuario> optional = usuarioRepository.findById(id);
        if(optional.isPresent()){
            Usuario usuario = optional.get();
            if(usuario.getCuentaActiva()==0){

                switch (usuario.getRol()){
                    case "AdminRestaurante":
                        model.addAttribute("usuario",usuario);
                        //Restaurante restaurante = restauranteRepository.findRestauranteByIdadminrestEquals(id);
                        //model.addAttribute("restaurante",restaurante);
                        return "adminsistema/AceptarCuentaRestaurante";
                    case "Repartidor":
                        model.addAttribute("usuario",usuario);
                        Repartidor repartidor = repartidorRepository.findRepartidorByIdusuariosEquals(id);
                        List<Direcciones> listadirecciones = direccionesRepository.findAllByUsuariosIdusuariosEquals(id);
                        model.addAttribute("repartidor",repartidor);
                        model.addAttribute("lista",listadirecciones);
                        return "adminsistema/AceptarCuentaRepartidor";
                    default:
                        return "redirect:/admin/nuevasCuentas";
                }
            }
        }
        return "redirect:/admin/nuevasCuentas";
    }

    @GetMapping("/aceptar")
    public String aceptarCuenta(@RequestParam(value="id") int id,
                                RedirectAttributes attr){
        Optional<Usuario> optional = usuarioRepository.findById(id);
        if(optional.isPresent()){
            Usuario usuario = optional.get();
            if(usuario.getCuentaActiva()==0){
                    usuario.setCuentaActiva(1);
                    usuarioRepository.save(usuario);
                    attr.addFlashAttribute("msg","Cuenta aceptada exitosamente");
                    return "redirect:/admin/nuevosUsuarios";
                }
            }
        attr.addFlashAttribute("msg","Ha ocurrido un error,cuenta no aprobada");
        return "redirect:/admin/nuevosUsuarios";
    }




    @GetMapping("adminForm")
    public String adminForm(){
        return "adminsistema/agregarAdmin";
    }

    @PostMapping("/agregarAdmin")
    public String agregarAdmin(@RequestParam("nombres") String nombres,
                               @RequestParam("apellidos") String apellidos,
                               @RequestParam("email" ) String email,
                               @RequestParam("dni") String dni,
                               @RequestParam("telefono") String telefono,
                               @RequestParam("fechaNacimiento") String fechaNacimiento,
                               @RequestParam("sexo") String sexo,
                               @RequestParam("contraseniaHash") String contraseniaHash,
                               @RequestParam("password2") String pass2,
                               Model model, RedirectAttributes attr){
        System.out.println(nombres + apellidos + email + dni + telefono + fechaNacimiento);
        System.out.println(fechaNacimiento);
        if(contraseniaHash.equals(pass2)){
            Usuario usuario = new Usuario();
            usuario.setNombre(nombres);
            usuario.setApellidos(apellidos);
            usuario.setEmail(email);
            usuario.setTelefono(telefono);
            usuario.setSexo(sexo);
            usuario.setContraseniaHash(contraseniaHash);
            usuario.setRol("AdminSistema");
            usuario.setCuentaActiva(1);
            usuario.setDni(dni);
            LocalDateTime localDate = LocalDateTime.now();
            usuario.setUltimafechaingreso(localDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try{
                usuario.setFechaNacimiento(sdf.parse(fechaNacimiento));
                System.out.println(fechaNacimiento);
            } catch (ParseException e) {
                e.printStackTrace();
                attr.addFlashAttribute("msg","Fecha Incorrecta");
                return "adminsistema/agregarAdmin";
            }
            usuarioRepository.save(usuario);
            attr.addFlashAttribute("msg","Administrador creado exitosamente");
        }else{
            attr.addFlashAttribute("msg","Fallo al crear Administrador");
            return "adminsistema/agregarAdmin";
        }
        return "adminsistema/GestionCuentasPrincipal";
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


}
