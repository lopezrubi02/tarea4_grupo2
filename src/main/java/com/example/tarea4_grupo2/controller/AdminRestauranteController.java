package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.ComentariosDto;
import com.example.tarea4_grupo2.dto.DatosDTO;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import jdk.nashorn.internal.objects.AccessorPropertyDescriptor;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/adminrest")
public class AdminRestauranteController {

    @Autowired
    PlatoRepository platoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    RestauranteRepository restauranteRepository;
    @Autowired
    CuponesRepository cuponesRepository;
    @Autowired
    PedidosRepository pedidosRepository;
    @Autowired
    CategoriasRepository categoriasRepository;
    @Autowired
    DistritosRepository distritosRepository;
    @Autowired
    DireccionesRepository direccionesRepository;

    @GetMapping("/login")
    public String loginAdmin(Model model, HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int id = sessionUser.getIdusuarios();
        System.out.println(id);
        if(sessionUser.getCuentaActiva() == 2){
            System.out.println("TRACE1");
            Optional<Restaurante> restauranteOpt = restauranteRepository.buscarRestaurantePorIdAdmin(id);
            System.out.println("TRACE2");
            if(restauranteOpt.isPresent()){

                return "AdminRestaurantes/espera";
            }else{
                System.out.println("ALLA");
                return "AdminRestaurantes/sinRestaurante";
            }
        }else if(sessionUser.getCuentaActiva() == 1){
            System.out.println("TRACE3");
            Optional<Restaurante> restauranteOpt = restauranteRepository.buscarRestaurantePorIdAdmin(id);
            if(restauranteOpt.isPresent()){
                return "AdminRestaurantes/perfilrestaurante";
            }else{
                return "AdminRestaurantes/sinRestaurante";
            }
        }else{
            return null;
        }
    }
/* Se encuentra en login controller
    @GetMapping("/register")
    public String registerAdmin(@ModelAttribute("usuario") Usuario usuario){
        return "AdminRestaurantes/register";
    }
*/
    @PostMapping("/categorias")
    public String esperaConfirmacion(@ModelAttribute("restaurante") @Valid Restaurante restaurante,BindingResult bindingResult,
                                     @RequestParam("imagen") MultipartFile file,
                                     Model model,
                                     HttpSession session) throws IOException {
        if(bindingResult.hasErrors()){
            Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
            restaurante.setUsuario(sessionUser);
            model.addAttribute("listadistritos",distritosRepository.findAll());
            model.addAttribute("restaurante",restaurante);
            return "AdminRestaurantes/registerRestaurante";
        }
        else {

            try {
                restaurante.setFoto(file.getBytes());
                restaurante.setFotocontenttype(file.getContentType());
                restaurante.setFotonombre(file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
            restauranteRepository.save(restaurante);
            model.addAttribute("id", restaurante.getIdrestaurante());
            model.addAttribute("listacategorias", categoriasRepository.findAll());
            return "AdminRestaurantes/categorias";
        }
    }
    @PostMapping("/estado")
    public String estadoAdmin(@RequestParam("correo") String correo) {
        //Se valida con el correo si en la bd aparece como usuario aceptado o en espera y tendría dos posibles salidas
        if(correo!=""){
            return "AdminRestaurantes/restaurante";
        }
        return "redirect:/login";
    }

    @GetMapping("/registerRestaurante")
    public String registerRestaurante(@ModelAttribute("restaurante")Restaurante restaurante, Model model,HttpSession session){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        restaurante.setUsuario(sessionUser);
        model.addAttribute("listadistritos",distritosRepository.findAll());
        model.addAttribute("restaurante",restaurante);
        return "AdminRestaurantes/registerRestaurante";
    }

    @GetMapping("/sinrestaurante")
    public String sinRestaurante(){
        return "AdminRestaurantes/sinRestaurante";
    }

    @PostMapping("/validarpersona")
    public String validarPersona(){
        return "AdminRestaurantes/restaurante";
    }

    @GetMapping("/correoconfirmar")
    public String correoConfirmar(){

        return "AdminRestaurantes/correo";
    }

    @GetMapping("/imagen")
    public ResponseEntity<byte[]> imagenRestaurante(@RequestParam("id")int id) {

        Optional<Restaurante> optional = restauranteRepository.findById(id);
        if (optional.isPresent()) {
            byte[] imagen = optional.get().getFoto();
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(optional.get().getFotocontenttype()));
            return new ResponseEntity<>(imagen,httpHeaders, HttpStatus.OK);
        }
        return null;
    }
    /************************PERFIL************************/

    @GetMapping("/perfil")
    public String perfilRestaurante(Model model){
        Integer id = 1;
        model.addAttribute("calificacion",pedidosRepository.calificacionPromedio(id));
        return "AdminRestaurantes/perfilrestaurante";
    }

    /************************PLATOS************************/

    @GetMapping("/menu")
    public String verMenu(Model model){
        Integer idrestaurante = 1;
        model.addAttribute("listaPlatos", platoRepository.buscarPlatosPorIdRestaurante(idrestaurante));
        return "AdminRestaurantes/menu";
    }

    @GetMapping("/crearPlato")
    public String crearPlato(@ModelAttribute("plato") Plato plato, Model model){
        model.addAttribute("plato",plato);
        return "AdminRestaurantes/newPlato";
    }

    @GetMapping("/editarPlato")
    public String editarPlato(Model model, @RequestParam("idplato") int id, @ModelAttribute("plato") Plato plato){

        Optional<Plato> optionalPlato = platoRepository.findById(id);

        if(optionalPlato.isPresent()){
            plato = optionalPlato.get();
            model.addAttribute("plato",plato);
            return "AdminRestaurantes/newPlato";
        }else{
            return "redirect:/menu";
        }
    }

    @PostMapping("/guardarPlato")
    public String guardarPlato(@ModelAttribute("plato") Plato plato, RedirectAttributes attr, Model model){
        if (plato.getIdplato() == 0) {
            attr.addFlashAttribute("msg", "Plato creado exitosamente");
            platoRepository.save(plato);
            return "redirect:/menu";
        } else {
            platoRepository.save(plato);
            attr.addFlashAttribute("msg", "Plato actualizado exitosamente");
            return "redirect:/menu";
        }
    }

    @GetMapping("/borrarPlato")
    public String borrarPlato(@RequestParam("idplato") int id, RedirectAttributes attr){

        Optional<Plato> optionalPlato = platoRepository.findById(id);

        if(optionalPlato.isPresent()){
            platoRepository.deleteById(id);
            attr.addFlashAttribute("msg", "Producto borrado exitosamente");
        }
        return "redirect:/menu";
    }

    /************************CUPONES************************/

    @GetMapping("/cupones")
    public String verCupones(Model model,@RequestParam(name = "page", defaultValue = "1") String requestedPage){
        float numberOfUsersPerPage = 7;
        int page = Integer.parseInt(requestedPage);
        int idrestaurante = 1;
        List<Cupones> listaCupones = cuponesRepository.buscarCuponesPorIdRestaurante(idrestaurante);
        List<String> listaDisponibilidad = new ArrayList<String>();
        int numberOfPages = (int) Math.ceil(listaCupones.size() / numberOfUsersPerPage);
        if (page > numberOfPages) {
            page = numberOfPages;
        } // validation

        int start = (int) numberOfUsersPerPage * (page - 1);
        int end = (int) (start + numberOfUsersPerPage);

        List<Cupones> listOfCuponesPage = listaCupones.subList(start, Math.min(end, listaCupones.size()));
        for (Cupones i: listOfCuponesPage){
            Date inicio = i.getFechainicio();
            Date fin = i.getFechafin();
            Date ahora = Date.valueOf(LocalDate.now());

            if(inicio.compareTo(ahora) > 0){
                listaDisponibilidad.add("No");
            }else if(fin.compareTo(ahora) < 0){
                listaDisponibilidad.add("No");
            }else if((inicio.compareTo(ahora) < 0) && (fin.compareTo(ahora) > 0)){
                listaDisponibilidad.add("Si");
            }else{
                listaDisponibilidad.add("No");
            }

        }
        model.addAttribute("listaCupones", listOfCuponesPage);
        model.addAttribute("listaDisponibilidad", listaDisponibilidad);
        model.addAttribute("currentPage", page);
        model.addAttribute("maxNumberOfPages", numberOfPages);
        return "AdminRestaurantes/cupones";
    }

    @GetMapping("/crearCupon")
    public String crearCupon(@ModelAttribute("cupon") Cupones cupon, Model model){
        int idrestaurante = 1;
        Restaurante restaurante = new Restaurante();
        restaurante.setIdrestaurante(idrestaurante);
        cupon.setRestaurante(restaurante);
        model.addAttribute("cupon",cupon);
        List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
        model.addAttribute("listaPlatos",listaPlatos);
        return "AdminRestaurantes/generarCupon";
    }

    @GetMapping("/editarCupon")
    public String editarCupon(@ModelAttribute("cupon") Cupones cupon, @RequestParam("idcupon") int id, Model model){
        Optional<Cupones> optCupon = cuponesRepository.findById(id);
        if(optCupon.isPresent()){
            cupon = optCupon.get();
            Restaurante restaurante = cupon.getRestaurante();
            int idrestaurante = restaurante.getIdrestaurante();
            model.addAttribute("cupon",cupon);
            List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
            model.addAttribute("listaPlatos",listaPlatos);
            return "AdminRestaurantes/generarCupon";
        }else{
            return "redirect:/cupones";
        }
    }
    @GetMapping("/borrarCupon")
    public String borrarCupon(@RequestParam("idcupon") int id, RedirectAttributes attr){

        Optional<Cupones> optCupon = cuponesRepository.findById(id);

        if(optCupon.isPresent()){
            cuponesRepository.deleteById(id);
            attr.addFlashAttribute("msg", "Cupon borrado exitosamente");
            return "redirect:/cupones";
        }else{
            return "redirect:/cupones";
        }
    }

    @PostMapping("/guardarCupon")
    public String guardarCupon(@ModelAttribute("cupon") Cupones cupon, RedirectAttributes attr,
                               Model model){

        if (cupon.getIdcupones() == 0) {
            cuponesRepository.save(cupon);
            attr.addFlashAttribute("msg", "Cupon creado exitosamente");
            return "redirect:/cupones";

        } else {
            cuponesRepository.save(cupon);
            attr.addFlashAttribute("msg", "Cupon actualizado exitosamente");
            return "redirect:/cupones";
        }
    }

    /************************CALIFICACIONES************************/

    @GetMapping("/calificaciones")
    public String verCalificaciones(Model model, @RequestParam(name = "page", defaultValue = "1") String requestedPage,
                                    @RequestParam(name = "searchfield", defaultValue = "") String searchField){
        float numberOfUsersPerPage = 7;
        int page = Integer.parseInt(requestedPage);
        //falta cambiar el id de acuerdo a la sesion pero por mientras se dejará ahi
        Integer id = 1;
        List<ComentariosDto> comentariosList;
        if(searchField.equals("")){
            comentariosList= pedidosRepository.comentariosUsuarios(id);
        }
        else{
            comentariosList= pedidosRepository.buscarComentariosUsuarios(searchField,id);
        }
        // si no se encuentra nada, se redirige a la lista general
        if(comentariosList.size() == 0){
            return "redirect:/adminrest/calificaciones";
        }

        int numberOfPages = (int) Math.ceil(comentariosList.size() / numberOfUsersPerPage);
        if (page > numberOfPages) {
            page = numberOfPages;
        } // validation

        int start = (int) numberOfUsersPerPage * (page - 1);
        int end = (int) (start + numberOfUsersPerPage);

        List<ComentariosDto> lisOfComentariosPage = comentariosList.subList(start, Math.min(end, comentariosList.size()));

        model.addAttribute("listaComentarios", lisOfComentariosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("maxNumberOfPages", numberOfPages);
        model.addAttribute("searchfield",searchField);
        return "AdminRestaurantes/calificaciones";
    }

    @PostMapping("/buscarCalificaciones")
    public String buscarCalificaciones(@RequestParam("searchfield") String searchField,
                                       RedirectAttributes attr){
        attr.addAttribute("searchfield",searchField);
        return "redirect:/adminrest/calificaciones";
    }

    /************************REPORTE************************/

    @GetMapping("/reporte")
    public String verReporte(Model model){
        Integer id = 1;
        model.addAttribute("listaPedidosPorFecha",pedidosRepository.listaPedidosReporteporFechamasantigua(id));
        model.addAttribute("listaGanancias",pedidosRepository.gananciaPorMes(id));
        model.addAttribute("platosTop5",pedidosRepository.platosMasVendidos(id));
        model.addAttribute("platosNoTop5",pedidosRepository.platosMenosVendidos(id));
        return "AdminRestaurantes/reporte";
    }

    @PostMapping("/buscarReporte")
    public String searchReporte(@RequestParam("name") String name, Model model) {
        Integer id = 1;
        model.addAttribute("listaPedidosPorFecha",pedidosRepository.buscarPorReporte(name,id));
        model.addAttribute("listaGanancias",pedidosRepository.gananciaPorMes(id));
        model.addAttribute("platosTop5",pedidosRepository.platosMasVendidos(id));
        model.addAttribute("platosNoTop5",pedidosRepository.platosMenosVendidos(id));
        return "AdminRestaurantes/reporte";
    }

    /************************PEDIDOS************************/

    @GetMapping("/pedidos")
    public String verPedidos(Model model){
        Integer id = 1;
        model.addAttribute("listaPedidos",pedidosRepository.listaPedidos(id));
        return "AdminRestaurantes/pedidos";
    }

    @GetMapping("/preparacion")
    public String pedidosPreparacion(Model model){
        model.addAttribute("listaAceptado",pedidosRepository.aceptadopedidos());
        model.addAttribute("listaPreparado",pedidosRepository.preparadopedidos());
        return"AdminRestaurantes/preparacion";
    }

    @GetMapping("/detallepedidos")
    public String detallePedidos(@RequestParam("id")int id,Model model){
        model.addAttribute("detalle",pedidosRepository.detallepedidos(id));
        return "AdminRestaurantes/detalle";
    }

    @GetMapping("/aceptarpedido")
    public String aceptarPedido(@RequestParam("id")int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("aceptado");
        pedidosRepository.save(optional.get());
        return"redirect:/pedidos";
    }

    @GetMapping("/rechazarpedido")
    public String rechazarPedido(@RequestParam("id")int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("rechazado");
        pedidosRepository.save(optional.get());
        return"redirect:/pedidos";
    }

    @GetMapping("/preparadopedido")
    public String platoPreparado(@RequestParam("id") int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("preparado");
        pedidosRepository.save(optional.get());
        return "redirect:/preparacion";
    }

    @GetMapping("/entregadopedido")
    public String entregadoPedido(@RequestParam("id") int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("entregado");
        pedidosRepository.save(optional.get());
        return"redirect:/preparacion";
    }

    @GetMapping("/cuentaAdmin")
    public String cuenta(@ModelAttribute("usuario") Usuario usuario, Model model,HttpSession session){
        Usuario user=(Usuario)session.getAttribute("usuarioLogueado");
        model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuariosIdusuariosAndActivoEquals(user.getIdusuarios(),1));
        model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(user.getIdusuarios()));
        model.addAttribute("usuario",usuarioRepository.findById(user.getIdusuarios()).get());
        model.addAttribute("datos",usuarioRepository.obtenerDatos(user.getIdusuarios()));
        return "AdminRestaurantes/cuenta";
    }
    @GetMapping("/borrarRestaurante")
    public String borrarRestaurante(HttpSession session){
        Usuario user= (Usuario) session.getAttribute("usuarioLogueado");
        int id=restauranteRepository.buscarRestaurantePorIdAdmin(user.getIdusuarios()).get().getIdrestaurante();
        restauranteRepository.deleteById(id);
        return "redirect:/adminrest/sinrestaurante";
    }
    @PostMapping("/llenarcategoria")
    public String llenarcategorias(@ModelAttribute("restaurante") Restaurante restaurante,Model model){
        Optional<Restaurante> optional = restauranteRepository.findById(restaurante.getIdrestaurante());
        optional.get().setCategoriasrestList(restaurante.getCategoriasrestList());
        restauranteRepository.save(optional.get());
        model.addAttribute("id",optional.get().getIdrestaurante());
        return "AdminRestaurantes/espera";
    }
    @PostMapping("/guardaradminedit")
    public String editPerfilUsuario(@ModelAttribute("usuario") @Valid Usuario usuario,
                                    BindingResult bindingResult,
                                    @RequestParam("pass2") String pass2,
                                    HttpSession session,
                                    Model model){
        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        System.out.println(usuario.getNombre());
        if(bindingResult.hasFieldErrors("email")||bindingResult.hasFieldErrors("telefono")|| bindingResult.hasFieldErrors("contraseniaHash")){
            model.addAttribute("datos",usuarioRepository.obtenerDatos(usuario.getIdusuarios()));
            model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuariosIdusuariosAndActivoEquals(usuario.getIdusuarios(),1));
            model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(usuario.getIdusuarios()));
            return "AdminRestaurantes/cuenta";
            }
        else {
            if(usuario.getContraseniaHash().equals(pass2)){
                user.setEmail(usuario.getEmail());
                user.setTelefono(usuario.getTelefono());
                user.setContraseniaHash(BCrypt.hashpw(usuario.getContraseniaHash(),BCrypt.gensalt()));
                usuarioRepository.save(user);
                return "redirect:/adminrest/perfil";
            }
            else{
                model.addAttribute("msg","Contraseñas no son iguales");
                model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuariosIdusuariosAndActivoEquals(user.getIdusuarios(),1));
                model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(user.getIdusuarios()));
                model.addAttribute("datos",usuarioRepository.obtenerDatos(user.getIdusuarios()));
                return "AdminRestaurantes/cuenta";
            }
        }
    }
    @PostMapping("/guardarrestedit")
    public String editarPerfilRest(@ModelAttribute("restaurante") Restaurante restaurante,@RequestParam("imagen") MultipartFile file,Model model){
        try {
            restaurante.setFoto(file.getBytes());
            System.out.println(file.getContentType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        restauranteRepository.save(restaurante);
        model.addAttribute("id",restaurante.getIdrestaurante());
        model.addAttribute("listacategorias",categoriasRepository.findAll());
        return "AdminRestaurantes/categoriasedit";
    }
    @PostMapping("/llenarcategoriaedit")
    public String llenarcategoriasedit(@ModelAttribute("restaurante") Restaurante restaurante,Model model){
        Optional<Restaurante> optional = restauranteRepository.findById(restaurante.getIdrestaurante());
        optional.get().setCategoriasrestList(restaurante.getCategoriasrestList());
        restauranteRepository.save(optional.get());
        model.addAttribute("id",optional.get().getIdrestaurante());
        return "AdminRestaurantes/perfil";
    }
    @GetMapping("/agregardireccion")
    public String agregardireccion(Model model) {

        List<Distritos> listadistritos = distritosRepository.findAll();
        model.addAttribute("listadistritos",listadistritos);

        return "AdminRestaurantes/agregardireccion";
    }
    @PostMapping("/guardardireccion")
    public String guardarnuevadireccion(@RequestParam("direccion") String direccion,
                                        @RequestParam("iddistrito") int iddistrito,
                                        HttpSession session) {

        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=user.getIdusuarios();
        Direcciones direccioncrear = new Direcciones();
        direccioncrear.setDireccion(direccion);
        //direccioncrear.setDistrito(distrito);
        Optional<Distritos> distritoopt = distritosRepository.findById(iddistrito);
        Distritos distritonuevo = distritoopt.get();
        direccioncrear.setDistrito(distritonuevo);
        direccioncrear.setUsuariosIdusuarios(idusuario);
        direccioncrear.setActivo(1);
        direccionesRepository.save(direccioncrear);
        return "redirect:/adminrest/perfil";
    }
    @GetMapping("/borrardireccion")
    public String borrarDireccion(@RequestParam("iddireccion") int iddireccion){
        Optional<Direcciones> direccionopt = direccionesRepository.findById(iddireccion);
        Direcciones direccionborrar = direccionopt.get();
        if(direccionborrar != null){
            direccionborrar.setActivo(0);
            direccionesRepository.save(direccionborrar);
        }
        return "redirect:/adminrest/perfil";
    }
}

