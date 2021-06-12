package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.ComentariosDto;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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
    @Autowired
    FotosPlatosRepository fotosPlatosRepository;

    @GetMapping("/login")
    public String loginAdmin(HttpSession session){

        /*Se obtiene Id de Usuario*/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int id = sessionUser.getIdusuarios();
        System.out.println(id);

        if(sessionUser.getCuentaActiva() == 2){
            Optional<Restaurante> restauranteOpt = restauranteRepository.buscarRestaurantePorIdAdmin(id);

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
                return "redirect:/adminrest/perfil";
            }else{
                return "AdminRestaurantes/sinRestaurante";
            }

        }else{
            //TODO Retornar una vista de rechazado//
            return null;
        }
    }

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

    //TODO se usa esto?
    @PostMapping("/validarpersona")
    public String validarPersona(){
        return "AdminRestaurantes/restaurante";
    }

    @GetMapping("/correoconfirmar")
    public String correoConfirmar(){

        return "AdminRestaurantes/correo";
    }

    /************************FOTOS************************/

    @GetMapping("/imagen")
    public ResponseEntity<byte[]> imagenRestaurante(HttpSession session) {

        /**Se obtiene Id de Restaurante**/
        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        Integer id_rest=restauranteRepository.buscarRestaurantePorIdAdmin(user.getIdusuarios()).get().getIdrestaurante();
        /********************************/

        Optional<Restaurante> optional = restauranteRepository.findById(id_rest);
        if (optional.isPresent()) {
            byte[] imagen = optional.get().getFoto();
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(optional.get().getFotocontenttype()));
            return new ResponseEntity<>(imagen,httpHeaders, HttpStatus.OK);
        }
        return null;
    }

    @GetMapping("/imagenplato")
    public ResponseEntity<byte[]> imagenPlato(HttpSession session, @RequestParam("idplato") Integer idplato) {

        Optional<FotosPlatos> optFoto = fotosPlatosRepository.encontrarIdPlato(idplato);
        if (optFoto.isPresent()) {
            byte[] imagen = optFoto.get().getFoto();
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(optFoto.get().getFotocontenttype()));
            return new ResponseEntity<>(imagen,httpHeaders, HttpStatus.OK);
        }
        return null;
    }

    /************************PERFIL************************/

    @GetMapping("/perfil")
    public String perfilRestaurante(Model model, HttpSession session){

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if(sessionUser.getCuentaActiva()==1){
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            /********************************/
            BigDecimal calificacion = pedidosRepository.calificacionPromedio(idrestaurante);
            MathContext m = new MathContext(3);
            calificacion = calificacion.round(m);
            Restaurante restaurante = restauranteRepository.findById(idrestaurante).get();
            restaurante.setCalificacionpromedio(calificacion.floatValue());
            restauranteRepository.save(restaurante);
            Integer cantidadcalificaciones = restauranteRepository.obtenerCantidadCalificaciones(idrestaurante);
            model.addAttribute("calificacionpromedio",calificacion);
            model.addAttribute("cantidadcalificaciones",cantidadcalificaciones);
            model.addAttribute("nombrerestaurante", restaurante.getNombre());
            return "AdminRestaurantes/perfilrestaurante";
        }
        else{
            return "redirect:/adminrest/login";
        }
    }

    /************************PLATOS************************/

    @GetMapping("/menu")
    public String verMenu(Model model, HttpSession session){

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if(sessionUser.getCuentaActiva()==1){
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            /********************************/

            model.addAttribute("iddelrestaurante", idrestaurante);
            model.addAttribute("listaPlatos", platoRepository.buscarPlatosPorIdRestaurante(idrestaurante));
            return "AdminRestaurantes/menu";
        }
        else{
            return "redirect:/adminrest/login";
        }
    }

    @GetMapping("/crearPlato")
    public String crearPlato(@ModelAttribute("plato") Plato plato, Model model, HttpSession session){

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
        /********************************/

        model.addAttribute("plato",plato);
        model.addAttribute("iddelrestaurante", idrestaurante);
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
            return "redirect:/adminrest/menu";
        }
    }

    @PostMapping("/guardarPlato")
    public String guardarPlato(@ModelAttribute("plato") @Valid Plato plato, BindingResult bindingResult, RedirectAttributes attr, Model model,
                               @RequestParam( name="imagen", required = false) MultipartFile file, HttpSession session) throws IOException{

        if(bindingResult.hasErrors()) {

            /**Se obtiene Id de Restaurante**/
            Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            /********************************/
            model.addAttribute("plato",plato);
            model.addAttribute("iddelrestaurante", idrestaurante);
            return "AdminRestaurantes/newPlato";

        }else{

            String nombreplato = plato.getNombre();

            if (plato.getIdplato() == 0) {

                platoRepository.save(plato);
                Optional<Plato> platoguardado = platoRepository.buscarPlato(nombreplato);

                if (platoguardado.isPresent()) {

                    Plato plato1 = platoguardado.get();
                    FotosPlatos fotosPlatos = new FotosPlatos();
                    fotosPlatos.setIdplato(plato1);

                    try {
                        fotosPlatos.setFoto(file.getBytes());
                        fotosPlatos.setFotocontenttype(file.getContentType());
                        fotosPlatos.setFotonombre(file.getOriginalFilename());
                    } catch (IOException e) {
                        e.printStackTrace();
                        plato1.setActivo(0);
                        platoRepository.save(plato1);
                    }

                    fotosPlatosRepository.save(fotosPlatos);
                    attr.addFlashAttribute("msg1", "Plato creado exitosamente");
                }

                return "redirect:/adminrest/menu";

            }else{

                platoRepository.save(plato);

                if (!(file.isEmpty())) {

                    Optional<FotosPlatos> optFoto = fotosPlatosRepository.encontrarIdPlato(plato.getIdplato());

                    if (optFoto.isPresent()) {

                        FotosPlatos fotosPlatos = optFoto.get();
                        try {
                            fotosPlatos.setFoto(file.getBytes());
                            fotosPlatos.setFotocontenttype(file.getContentType());
                            fotosPlatos.setFotonombre(file.getOriginalFilename());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fotosPlatosRepository.save(fotosPlatos);

                    }else{

                        FotosPlatos fotosPlatos = new FotosPlatos();
                        fotosPlatos.setIdplato(plato);
                        try {
                            fotosPlatos.setFoto(file.getBytes());
                            fotosPlatos.setFotocontenttype(file.getContentType());
                            fotosPlatos.setFotonombre(file.getOriginalFilename());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fotosPlatosRepository.save(fotosPlatos);
                    }
                }
                attr.addFlashAttribute("msg2", "Plato actualizado exitosamente");
                return "redirect:/adminrest/menu";
            }
        }
    }

    @GetMapping("/borrarPlato")
    public String borrarPlato(@RequestParam("idplato") int id, RedirectAttributes attr){

        Optional<Plato> optionalPlato = platoRepository.findById(id);

        if(optionalPlato.isPresent()){
            Plato platoBorrar = optionalPlato.get();
            if(platoBorrar.getIdplato() != 0){
                platoBorrar.setActivo(0);
                platoRepository.save(platoBorrar);
                attr.addFlashAttribute("msg", "Producto borrado exitosamente");
            }
            return "redirect:/adminrest/menu";
        }else{
            return "redirect:/adminrest/menu";
        }
    }

    /************************CUPONES************************/

    @GetMapping("/cupones")
    public String verCupones(Model model,@RequestParam(name = "page", defaultValue = "1") String requestedPage,HttpSession session){
        Usuario usuario= (Usuario) session.getAttribute("usuarioLogueado");
        if(usuario.getCuentaActiva()==1){
            float numberOfUsersPerPage = 7;
            int page = Integer.parseInt(requestedPage);
            int idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getIdrestaurante();
            List<Cupones> listaCupones = cuponesRepository.buscarCuponesPorIdRestaurante(idrestaurante);

            if(!(listaCupones.isEmpty())) {

                List<String> listaDisponibilidad = new ArrayList<String>();
                int numberOfPages = (int) Math.ceil(listaCupones.size() / numberOfUsersPerPage);
                if (page > numberOfPages) {
                    page = numberOfPages;
                } // validation

                int start = (int) numberOfUsersPerPage * (page - 1);
                int end = (int) (start + numberOfUsersPerPage);

                List<Cupones> listOfCuponesPage = listaCupones.subList(start, Math.min(end, listaCupones.size()));
                System.out.println("TRACE3");
                for (Cupones i : listOfCuponesPage) {
                    Date inicio = i.getFechainicio();
                    Date fin = i.getFechafin();
                    Date ahora = Date.valueOf(LocalDate.now());

                    if (inicio.compareTo(ahora) > 0) {
                        listaDisponibilidad.add("No");
                    } else if (fin.compareTo(ahora) < 0) {
                        listaDisponibilidad.add("No");
                    } else if ((inicio.compareTo(ahora) < 0) && (fin.compareTo(ahora) > 0)) {
                        listaDisponibilidad.add("Si");
                    } else {
                        listaDisponibilidad.add("No");
                    }
                }
                model.addAttribute("listaCupones", listOfCuponesPage);
                model.addAttribute("listaDisponibilidad", listaDisponibilidad);
                model.addAttribute("currentPage", page);
                model.addAttribute("maxNumberOfPages", numberOfPages);
                return "AdminRestaurantes/cupones";
            }
            return "AdminRestaurantes/sincupones";
        }
        else{
            return "redirect:/adminrest/login";
        }
    }

    @GetMapping("/crearCupon")
    public String crearCupon(@ModelAttribute("cupon") Cupones cupon, Model model,HttpSession session){
        Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado");
        int idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getIdrestaurante();
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
            return "redirect:/adminrest/cupones";
        }
    }
    @GetMapping("/borrarCupon")
    public String borrarCupon(@RequestParam("idcupon") int id, RedirectAttributes attr){

        Optional<Cupones> optCupon = cuponesRepository.findById(id);

        if(optCupon.isPresent()){
            cuponesRepository.deleteById(id);
            attr.addFlashAttribute("msg3", "Cupon borrado exitosamente");
            return "redirect:/adminrest/cupones";
        }else{
            return "redirect:/adminrest/cupones";
        }
    }

    @PostMapping("/guardarCupon")
    public String guardarCupon(@ModelAttribute("cupon") @Valid Cupones cupon, BindingResult bindingResult, RedirectAttributes attr,
                               Model model, HttpSession session){

        if(bindingResult.hasErrors()){

            /**Se obtiene Id de Restaurante**/
            Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            /********************************/
            model.addAttribute("cupon",cupon);
            List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
            model.addAttribute("listaPlatos",listaPlatos);
            return "AdminRestaurantes/generarCupon";

        }else {

            if (cupon.getIdcupones() == 0) {

                cuponesRepository.save(cupon);
                attr.addFlashAttribute("msg1", "Cupon creado exitosamente");
                return "redirect:/adminrest/cupones";

            } else {

                cuponesRepository.save(cupon);
                attr.addFlashAttribute("msg2", "Cupon actualizado exitosamente");
                return "redirect:/adminrest/cupones";
            }
        }
    }

    /************************CALIFICACIONES************************/

    @GetMapping("/calificaciones")
    public String verCalificaciones(Model model, @RequestParam(name = "page", defaultValue = "1") String requestedPage,
                                    @RequestParam(name = "searchfield", defaultValue = "") String searchField,HttpSession session){
        Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado");
        if(usuario.getCuentaActiva()==1){
            float numberOfUsersPerPage = 7;
            int page = Integer.parseInt(requestedPage);
            int idrestaurante= restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getIdrestaurante();
            List<ComentariosDto> comentariosList;
            if(searchField.equals("")){
                comentariosList= pedidosRepository.comentariosUsuarios(idrestaurante);
            }
            else{
                comentariosList= pedidosRepository.buscarComentariosUsuarios(searchField,idrestaurante);
            }

            if(!(comentariosList.isEmpty())){
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
            }else{
                return "AdminRestaurantes/sincalificaciones";
            }
        }else{
            return"redirect:/adminrest/login";
        }
    }

    @PostMapping("/buscarCalificaciones")
    public String buscarCalificaciones(@RequestParam("searchfield") String searchField,
                                       RedirectAttributes attr){
        attr.addAttribute("searchfield",searchField);
        return "redirect:/adminrest/calificaciones";
    }

    /************************REPORTE************************/

    @GetMapping("/reporte")
    public String verReporte(Model model,HttpSession session){
        Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado");
        if(usuario.getCuentaActiva()==1){
            int idrestaurante= restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getIdrestaurante();
            model.addAttribute("listaPedidosPorFecha",pedidosRepository.listaPedidosReporteporFechamasantigua(idrestaurante));
            model.addAttribute("listaGanancias",pedidosRepository.gananciaPorMes(idrestaurante));
            model.addAttribute("platosTop5",pedidosRepository.platosMasVendidos(idrestaurante));
            model.addAttribute("platosNoTop5",pedidosRepository.platosMenosVendidos(idrestaurante));
            return "AdminRestaurantes/reporte";
        }else{
            return "redirect:/adminrest/login";
        }
    }

    @PostMapping("/buscarReporte")
    public String searchReporte(@RequestParam("name") String name, Model model, HttpSession session) {

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if(sessionUser.getCuentaActiva()==1){
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            model.addAttribute("listaPedidosPorFecha",pedidosRepository.buscarPorReporte(name,idrestaurante));
            model.addAttribute("listaGanancias",pedidosRepository.gananciaPorMes(idrestaurante));
            model.addAttribute("platosTop5",pedidosRepository.platosMasVendidos(idrestaurante));
            model.addAttribute("platosNoTop5",pedidosRepository.platosMenosVendidos(idrestaurante));
            return "AdminRestaurantes/reporte";
        }
        else{
            return "redirect:/adminrest/login";
        }
    }

    /************************PEDIDOS************************/

    @GetMapping("/pedidos")
    public String verPedidos(Model model, HttpSession session){

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if(sessionUser.getCuentaActiva()==1){
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            /********************************/
            model.addAttribute("listaPedidos",pedidosRepository.listaPedidos(idrestaurante));
            return "AdminRestaurantes/pedidos";
        }else{
            return "redirect:/adminrest/login";
        }
    }

    @GetMapping("/preparacion")
    public String pedidosPreparacion(Model model, HttpSession session){

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
        /********************************/

        model.addAttribute("listaAceptado",pedidosRepository.aceptadopedidos(idrestaurante));
        model.addAttribute("listaPreparado",pedidosRepository.preparadopedidos(idrestaurante));
        return"AdminRestaurantes/preparacion";
    }

    @GetMapping("/detallepedidos")
    public String detallePedidos(@RequestParam("id")int id,Model model){
        System.out.println("Trace1");
        model.addAttribute("detalle",pedidosRepository.detallepedidos(id));
        System.out.println("Trace2");
        return "AdminRestaurantes/detalle";
    }

    @GetMapping("/aceptarpedido")
    public String aceptarPedido(@RequestParam("id")int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("aceptado");
        optional.get().setEstadorepartidor("pendiente"); //Para que le aparezca al repartidor
        pedidosRepository.save(optional.get());
        return"redirect:/adminrest/pedidos";
    }

    @GetMapping("/rechazarpedido")
    public String rechazarPedido(@RequestParam("id")int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("rechazado");
        pedidosRepository.save(optional.get());
        return"redirect:/adminrest/pedidos";
    }

    @GetMapping("/preparadopedido")
    public String platoPreparado(@RequestParam("id") int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("preparado");
        pedidosRepository.save(optional.get());
        return "redirect:/adminrest/preparacion";
    }

    @GetMapping("/entregadopedido")
    public String entregadoPedido(@RequestParam("id") int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("entregado");
        pedidosRepository.save(optional.get());
        return"redirect:/adminrest/preparacion";
    }

    /************************CUENTA************************/

    @GetMapping("/cuentaAdmin")
    public String cuenta(@ModelAttribute("restaurante") Restaurante restaurante, @ModelAttribute("usuario") Usuario usuario, Model model,HttpSession session){
        Usuario user=(Usuario)session.getAttribute("usuarioLogueado");
        if(user.getCuentaActiva()==1){
        model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuarioAndActivoEquals(user,1));
        model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(user.getIdusuarios()));
        model.addAttribute("usuario",usuarioRepository.findById(user.getIdusuarios()).get());
        model.addAttribute("datos",usuarioRepository.obtenerDatos(user.getIdusuarios()));
        model.addAttribute("listadistritos",distritosRepository.findAll());
        model.addAttribute("ruc",restauranteRepository.buscarRuc(user.getIdusuarios()));
        return "AdminRestaurantes/cuenta";
        }
        else{
            return "redirect:/adminrest/login";
        }
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
                                    @RequestParam("pass") String pass,
                                    HttpSession session,
                                    Model model){
        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        System.out.println(usuario.getNombre());
        if(bindingResult.hasFieldErrors("email")||bindingResult.hasFieldErrors("telefono")|| bindingResult.hasFieldErrors("contraseniaHash")){
            model.addAttribute("datos",usuarioRepository.obtenerDatos(usuario.getIdusuarios()));
            model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1));
            model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(usuario.getIdusuarios()));
            model.addAttribute("ruc",restauranteRepository.buscarRuc(usuario.getIdusuarios()));
            model.addAttribute("listadistritos",distritosRepository.findAll());
            return "AdminRestaurantes/cuenta";
            }
        else {
            if(Pattern.matches("^[a-z0-9_]+@gmail.com",usuario.getEmail())) {
                Integer id = usuarioRepository.verificarEmail(usuario.getEmail(), "AdminRestaurante");
                if(id==0 || usuario.getIdusuarios().equals(user.getIdusuarios())) {

                    if (user.getContraseniaHash().equals(BCrypt.hashpw(usuario.getContraseniaHash(),BCrypt.gensalt()))) {
                        user.setEmail(usuario.getEmail());
                        user.setTelefono(usuario.getTelefono());
                        usuarioRepository.save(user);
                        if(pass.equals(pass2) && pass!=null && pass!="" && Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",pass)){
                            user.setContraseniaHash(BCrypt.hashpw(pass,BCrypt.gensalt()));
                            return "redirect:/adminrest/cuentaAdmin";
                        }
                        else{
                            model.addAttribute("msgpasserror", "Contraseñas no cumple con los requisitos");
                            model.addAttribute("listadirecciones", direccionesRepository.findAllByUsuarioAndActivoEquals(user, 1));
                            model.addAttribute("restaurante", restauranteRepository.obtenerperfilRest(user.getIdusuarios()));
                            model.addAttribute("datos", usuarioRepository.obtenerDatos(user.getIdusuarios()));
                            model.addAttribute("ruc", restauranteRepository.buscarRuc(usuario.getIdusuarios()));
                            model.addAttribute("listadistritos", distritosRepository.findAll());
                            return "AdminRestaurantes/cuenta";
                        }
                    } else {
                        model.addAttribute("msg", "Contraseñas no son iguales");
                        model.addAttribute("listadirecciones", direccionesRepository.findAllByUsuarioAndActivoEquals(user, 1));
                        model.addAttribute("restaurante", restauranteRepository.obtenerperfilRest(user.getIdusuarios()));
                        model.addAttribute("datos", usuarioRepository.obtenerDatos(user.getIdusuarios()));
                        model.addAttribute("ruc", restauranteRepository.buscarRuc(usuario.getIdusuarios()));
                        model.addAttribute("listadistritos", distritosRepository.findAll());
                        return "AdminRestaurantes/cuenta";
                    }
                }
                else{
                    model.addAttribute("datos",usuarioRepository.obtenerDatos(usuario.getIdusuarios()));
                    model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1));
                    model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(usuario.getIdusuarios()));
                    model.addAttribute("ruc",restauranteRepository.buscarRuc(usuario.getIdusuarios()));
                    model.addAttribute("listadistritos",distritosRepository.findAll());
                    model.addAttribute("mensajemail2","Correo ya existe");
                    return "AdminRestaurantes/cuenta";
                }
            }
            else{
                model.addAttribute("datos",usuarioRepository.obtenerDatos(usuario.getIdusuarios()));
                model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1));
                model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(usuario.getIdusuarios()));
                model.addAttribute("ruc",restauranteRepository.buscarRuc(usuario.getIdusuarios()));
                model.addAttribute("listadistritos",distritosRepository.findAll());
                model.addAttribute("mensajemail","Ingrese un correo valido");
                return "AdminRestaurantes/cuenta";
            }
        }
    }
    @PostMapping("/guardarrestedit")
    public String editPerfilUsuario(@ModelAttribute("restaurante") @Valid Restaurante restaurante,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    @RequestParam("imagen") MultipartFile file,
                                    Model model){
        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        if(bindingResult.hasFieldErrors("nombre")||bindingResult.hasFieldErrors("direccion")){
            model.addAttribute("datos",usuarioRepository.obtenerDatos(user.getIdusuarios()));
            model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuarioAndActivoEquals(user,1));
            model.addAttribute("ruc",restauranteRepository.buscarRuc(user.getIdusuarios()));
            model.addAttribute("listadistritos",distritosRepository.findAll());
            model.addAttribute("usuario",user);
            return "AdminRestaurantes/cuentarest";
        }
            Restaurante rest = restauranteRepository.buscarRestaurantePorIdAdmin(user.getIdusuarios()).get();
            try {
                rest.setFoto(file.getBytes());
                rest.setFotocontenttype(file.getContentType());
                rest.setFotonombre(file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
            rest.setNombre(restaurante.getNombre());
            rest.setDireccion(restaurante.getDireccion());
            restauranteRepository.save(rest);
            return "redirect:/adminrest/cuentaAdmin";
    }
    @GetMapping("/categoriaedit")
    public String llenarcategoriasedit(@ModelAttribute("restaurante") Restaurante restaurante,Model model,HttpSession session){
        Usuario usuario= (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("restaurante",restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()));
        model.addAttribute("listacategorias",categoriasRepository.findAll());
        return "AdminRestaurantes/categoriasedit";
    }
    @PostMapping("/guardarcategoriaedit")
    public String nuevascategorias(@ModelAttribute("restaurante") Restaurante restaurante, HttpSession session){
        Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado");
        Restaurante rest= restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get();
        rest.setCategoriasrestList(restaurante.getCategoriasrestList());
        restauranteRepository.save(rest);
        return "redirect:/adminrest/cuentaAdmin";
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
        Optional<Usuario> usuarioxguardar = usuarioRepository.findById(idusuario);
        Usuario usuario2 = usuarioxguardar.get();
        Direcciones direccioncrear = new Direcciones();
        direccioncrear.setDireccion(direccion);
        //direccioncrear.setDistrito(distrito);
        Optional<Distritos> distritoopt = distritosRepository.findById(iddistrito);
        Distritos distritonuevo = distritoopt.get();
        direccioncrear.setDistrito(distritonuevo);
        direccioncrear.setUsuario(usuario2);
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

