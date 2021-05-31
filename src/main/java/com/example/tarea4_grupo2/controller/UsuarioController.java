package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.*;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@Controller
public class UsuarioController {

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

    @GetMapping("/cliente/paginaprincipal")
    public String paginaprincipal(HttpSession session, Model model) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();
        
        List<Pedidos> pedidoscanceladosxrest = pedidosRepository.listapedidoscanceladosxrest(idusuario);
        model.addAttribute("listacancelados",pedidoscanceladosxrest);
        return "cliente/paginaPrincipal";
    }

    /** Registro cliente**/

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
                                 BindingResult bindingResult) {

        if(bindingResult.hasErrors()){
            List<Distritos> listadistritos = distritosRepository.findAll();
            model.addAttribute("listadistritos",listadistritos);
            return "cliente/registroCliente";
        }else{
            if (usuario.getContraseniaHash().equals(pass2)) {
                String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());

                usuario.setContraseniaHash(contraseniahashbcrypt);
                usuario.setRol("Cliente");
                usuario.setCuentaActiva(1);

                usuarioRepository.save(usuario);

                Usuario usuarionuevo = usuarioRepository.findByDni(usuario.getDni());

                int idusuarionuevo = usuarionuevo.getIdusuarios();

                Direcciones direccionactual = new Direcciones();
                direccionactual.setDireccion(direccion);
                Optional<Distritos> distritoopt = distritosRepository.findById(iddistrito);
                Distritos distritosactual = distritoopt.get();

                direccionactual.setDistrito(distritosactual);
                direccionactual.setUsuariosIdusuarios(idusuarionuevo);
                direccionactual.setActivo(1);
                direccionesRepository.save(direccionactual);

                return "cliente/confirmarCuenta";
            } else {
                List<Distritos> listadistritos = distritosRepository.findAll();
                model.addAttribute("listadistritos",listadistritos);
                return "cliente/registroCliente";
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
                                 @RequestParam(value = "idcategoriarest" ,defaultValue = "0") int idcategoriarest,
                                 @RequestParam(value = "preciopromedio", defaultValue = "0") int precio,
                                 @RequestParam(value = "direccion", defaultValue = "0") int direccionxenviar,
                                 @RequestParam(value = "calificacion", defaultValue = "0") int calificacion
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

        try {
            if (direccionxenviar == 0) {
                model.addAttribute("direccionseleccionada", listadireccionescliente.get(0).getDireccion());
                model.addAttribute("iddireccionxenviar", listadireccionescliente.get(0).getIddirecciones());
            } else {
                Optional<Direcciones> direccionopt = direccionesRepository.findById(direccionxenviar);
                if (direccionopt.isPresent()) {
                    Direcciones direccionseleccionada = direccionopt.get();
                    model.addAttribute("iddireccionxenviar", direccionxenviar);
                    model.addAttribute("direccionseleccionada", direccionseleccionada.getDireccion());
                }
            }
        }catch (Exception e){
        return "cliente/realizar_pedido_cliente";
    }

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
        try {
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
        }catch (Exception e){
            return "cliente/realizar_pedido_cliente";
        }

        try {
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
    }catch (Exception e){
        return "cliente/realizar_pedido_cliente";
    }
    }

    @GetMapping("/cliente/direccionxenviar")
    public String direccionxenviar(Model model,
                                   @RequestParam(value = "direccion", defaultValue = "0") int direccionxenviar,
                                   HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();

        Optional<Direcciones> direccionopt = Optional.ofNullable(direccionesRepository.findDireccionesByIddireccionesAndUsuariosIdusuariosEquals(direccionxenviar, idusuarioactual));
        try {
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
        }catch (Exception e){
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

        //TODO: verificar que ya exite un pedido guardado con el idrestaurante
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
                 pedidos.setRestaurante_idrestaurante(idrestaurante);
                 pedidos.setIdmetodopago(1);
                 pedidos.setIdrepartidor(11);
                 pedidos.setDireccionentrega(9);
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

             return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante;
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

            List<PedidoHasPlato> listaxpedido = pedidoHasPlatoRepository.findAllByPedidoIdpedidos(55);
            System.out.println(listaxpedido.get(0).getPlato().getIdplato());
            System.out.println(listaxpedido.get(0).getPlato().getNombre());
            System.out.println("*********************************+");

            List<PedidoHasPlato> listaplatosxpedido = pedidoHasPlatoRepository.findAll();
            System.out.println(listaplatosxpedido.get(1).getId().getPedidosidpedidos());
            System.out.println(listaplatosxpedido.get(1).getPlato().getNombre());

        }
        return "cliente/carrito_productos";
    }

    @GetMapping("/cliente/checkout")
    public String checkout(Model model, HttpSession session,
                           @RequestParam(value = "idmetodo" ,defaultValue = "0") int idmetodo){
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

        return "cliente/checkoutcarrito";
    }

    @GetMapping("/cliente/progresopedido")
    public String progresopedido(Model model, HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        //Pedidos pedidoencurso = pedidosRepository.findByIdclienteEquals(idusuario);
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
    public String calificarpedido(Model model, HttpSession session){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

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
    public String miperfil(Model model, HttpSession session) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosAndActivoEquals(idusuario,1);
        model.addAttribute("listadirecciones", listadireccionescliente);
        Optional<Usuario> optional = usuarioRepository.findById(idusuario);
        Usuario usuario = optional.get();
        model.addAttribute("usuario", usuario);
        System.out.println(usuario.getIdusuarios());
        return "cliente/miPerfil";
    }

    @PostMapping("/cliente/miperfil")
    public String updatemiperfil(Usuario usuarioRecibido) {
        System.out.println(usuarioRecibido.getIdusuarios());
        Optional<Usuario> optusuario = usuarioRepository.findById(usuarioRecibido.getIdusuarios());
        Usuario usuariodb = optusuario.get();

        usuariodb.setEmail(usuarioRecibido.getEmail());
        usuariodb.setTelefono(usuarioRecibido.getTelefono());
        System.out.println("contra es" + usuarioRecibido.getContraseniaHash() + "     ****");

        String contrarecibida = usuarioRecibido.getContraseniaHash();

        String contraseniahashbcrypt = BCrypt.hashpw(contrarecibida, BCrypt.gensalt());


        usuariodb.setContraseniaHash(contraseniahashbcrypt);
        usuarioRepository.save(usuariodb);

        return "redirect:/cliente/miperfil";
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
        Distritos distritonuevo = distritoopt.get();

        direccioncrear.setDistrito(distritonuevo);

        direccioncrear.setUsuariosIdusuarios(idusuario);
        direccioncrear.setActivo(1);
        direccionesRepository.save(direccioncrear);

        return "redirect:/cliente/miperfil";

    }

}
