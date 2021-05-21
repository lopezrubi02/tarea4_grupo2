package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import jdk.nashorn.internal.runtime.regexp.joni.NodeOptInfo;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/cliente/paginaprincipal")
    public String paginaprincipal() {

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
            System.out.println("hay algun error");
            List<Distritos> listadistritos = distritosRepository.findAll();
            model.addAttribute("listadistritos",listadistritos);
            System.out.println(bindingResult.getFieldErrors());
            System.out.println(pass2);
            System.out.println(usuario.getContraseniaHash());
            return "cliente/registroCliente";
        }else{
            System.out.println("mo hay error de binding");
            System.out.println(pass2);
            System.out.println(usuario.getContraseniaHash());
            System.out.println("#####################################33");
            if (usuario.getContraseniaHash().equals(pass2)) {
                System.out.println(pass2);
                System.out.println(usuario.getContraseniaHash());
                String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());


                usuario.setContraseniaHash(contraseniahashbcrypt);
                usuario.setRol("Cliente");
                usuario.setCuentaActiva(1);

                usuarioRepository.save(usuario);
                System.out.println("guarda");

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
    public String realizarpedido(Model model, HttpSession session) {

        //int idusuarioactual = 7;
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();

        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
        List<Categorias> listacategorias = categoriasRepository.findAll();
        List<Restaurante> listarestaurantes = restauranteRepository.findAll();
        Direcciones direccionseleccionada = listadireccionescliente.get(1);
        model.addAttribute("listacategorias", listacategorias);
        model.addAttribute("listadirecciones", listadireccionescliente);
        model.addAttribute("listarestaurantes",listarestaurantes);
        model.addAttribute("direccionseleccionada",direccionseleccionada);

        return "cliente/realizar_pedido_cliente";
    }

    @PostMapping("/cliente/filtrarnombre")
    public String filtronombre(Model model,
                               @RequestParam(value = "searchField" ,defaultValue = "") String buscar){
        //TODO mandar a la vista los platos buscados
    //    System.out.println(buscar);
        List<Plato> listaplatos = platoRepository.buscarPlatoxNombre(buscar);
        List<Restaurante> listarestaurantes = restauranteRepository.buscarRestaurantexNombre(buscar);
        model.addAttribute("listarestaurantesbuscado",listarestaurantes);
        model.addAttribute("listaplatosbuscado",listaplatos);
        return "redirect:/cliente/realizarpedido";
    }

    @GetMapping("/cliente/direccionxenviar")
    public String direccionxenviar(Model model,
                                   @RequestParam(value = "direccionxenviar", defaultValue = "0") int direccionxenviar,
                                   HttpSession session){
        //int idusuarioactual= 7;
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuarioactual=sessionUser.getIdusuarios();

        Optional<Direcciones> direccionopt = direccionesRepository.findById(direccionxenviar);
        if(direccionopt.isPresent()){
            Direcciones direccionseleccionada = direccionopt.get();
            List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
            List<Categorias> listacategorias = categoriasRepository.findAll();
            List<Restaurante> listarestaurantes = restauranteRepository.findAll();
            model.addAttribute("listacategorias", listacategorias);
            model.addAttribute("listadirecciones", listadireccionescliente);
            model.addAttribute("listarestaurantes",listarestaurantes);
            model.addAttribute("iddireccionxenviar",direccionxenviar);
            System.out.println(direccionxenviar);
            model.addAttribute("direccionseleccionada",direccionseleccionada);
            return "cliente/realizar_pedido_cliente";
        }else{
            return "redirect:/cliente/realizarpedido";

        }
    }


     @GetMapping("/cliente/filtrocategoria")
     public String filtrosrestaurantes1(Model model,
     @RequestParam(value = "idcategoriarest" ,defaultValue = "0") int idcategoriarest,
                                        HttpSession session
                                        ){
  //       System.out.println(idcategoriarest);
    //     System.out.println("*******************************");


         Optional<Categorias> catopt = categoriasRepository.findById(idcategoriarest);
         if(catopt.isPresent()){
             List<Restaurante> listarestauranteseleccionado = restauranteRepository.listarestxcategoria(idcategoriarest);
             //int idusuarioactual = 7;

             Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
             int idusuarioactual=sessionUser.getIdusuarios();

             List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
             List<Categorias> listacategorias = categoriasRepository.findAll();
             List<Restaurante> listarestaurantes = restauranteRepository.findAll();
             model.addAttribute("listacategorias", listacategorias);
             model.addAttribute("listadirecciones", listadireccionescliente);


             if(idcategoriarest!=0){
                 model.addAttribute("listarestaurantes",listarestauranteseleccionado);
             }else{
                 model.addAttribute("listarestaurantes",listarestaurantes);
             }

             return "cliente/realizar_pedido_cliente";
         }else{
             return "redirect:/cliente/realizarpedido";
         }

     }


        /** restaurante a ordenar **/

     @GetMapping("/cliente/restaurantexordenar")
     public String restaurantexordenar(@RequestParam("idrestaurante") int idrestaurante, Model model,
                                       @RequestParam("direccionxenviar") int iddireccionxenviar
                                     ){

     //    System.out.println(idrestaurante);
       //  System.out.println("**************************");

         Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);
        //Optional<Direcciones> direccionopt )
        if(restopt.isPresent()){
            Restaurante rest = restopt.get();

            if (rest!=null){
                int cantreviews = restauranteRepository.cantreviews(idrestaurante);
       //         System.out.println(cantreviews);
         //       System.out.println("**************************");

                List<Plato> platosxrest = platoRepository.buscarPlatosPorIdRestauranteDisponilidadActivo(idrestaurante);

                model.addAttribute("restaurantexordenar",rest);
                model.addAttribute("cantreviews",cantreviews);
                model.addAttribute("platosxrest",platosxrest);
                model.addAttribute("iddireccionxenviar",iddireccionxenviar);
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
                              @RequestParam("direccionxenviar") int iddireccionxpedir){

         Optional<Plato> platoopt = platoRepository.findById(idplatopedir);
         Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);

         if(platoopt.isPresent() && restopt.isPresent()){
             Plato platoseleccionado = platoopt.get();
             model.addAttribute("platoseleccionado",platoseleccionado);
             model.addAttribute("idrestaurante",idrestaurante);
             model.addAttribute("iddireccionxpedir",iddireccionxpedir);
             return "cliente/detalles_plato";
         }else{
            return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante;
         }
    }

    @PostMapping("/cliente/platopedido")
    public String platopedido(@RequestParam("cubierto") boolean cubiertos,
                              @RequestParam("cantidad") int cantidad,
                              @RequestParam("descripcion") String descripcion,
                              @RequestParam(value = "idrestaurante") int idrestaurante,
                              @RequestParam("idplato") int idplato,
                              @RequestParam("direccionxpedir") int iddireccionxpedir,
                              HttpSession session,
                              Model model){

         //int idusuario = 7;

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

         Optional<Restaurante> restauranteopt = restauranteRepository.findById(idrestaurante);
         Optional<Plato> platoopt = platoRepository.findById(idplato);

         if(platoopt.isPresent() && restauranteopt.isPresent()){

             Plato platoelegido = platoopt.get();
             Optional<Usuario> usuarioopt = usuarioRepository.findById(idusuario);
             Usuario usuarioactual = usuarioopt.get();

             int idcliente = usuarioactual.getIdusuarios();
             System.out.println("idcliente");
             System.out.println(idcliente);
             System.out.println(cubiertos);
             System.out.println("**********************+");
             System.out.println(cantidad);
             System.out.println(descripcion);
             System.out.println("idrestaurante: ");
             System.out.println(idrestaurante);
             System.out.println("gggggggggggggggggg");
            // List<PedidoHasPlato> pedidoHasPlatoList =  pedidoHasPlatoRepository.findAll();
             //System.out.println(pedidoHasPlatoList.get(1).getPlato().getIdplato());
            // System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa");
             System.out.println("idplato:");
             System.out.println(idplato);

             System.out.println(iddireccionxpedir);
             Pedidos pedidos = new Pedidos();
             pedidos.setIdcliente(idcliente);
             pedidos.setRestaurante_idrestaurante(idrestaurante);
             pedidos.setIdmetodopago(1);
             pedidos.setIdrepartidor(11);
             pedidos.setDireccionentrega(iddireccionxpedir);

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

             return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante + "&direccionxenviar=" + iddireccionxpedir;
         }else{
             return "redirect:/cliente/realizarpedido";
         }

    }

    @GetMapping("/cliente/carritoproductos")
    public String carritoproductos(Model model, HttpSession session){


        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();


        return "cliente/carrito_productos";
    }

    /** Mi perfil **/

    @GetMapping("/cliente/miperfil")
    public String miperfil(Model model, HttpSession session) {
        //int idusuario = 7;

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

        //int idusuario = 7;

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        Direcciones direccioncrear = new Direcciones();
        direccioncrear.setDireccion(direccion);
        //direccioncrear.setDistrito(distrito);

        Optional<Distritos> distritoopt = distritosRepository.findById(iddistrito);
        Distritos distritonuevo = distritoopt.get();

        direccioncrear.setDistrito(distritonuevo);

        direccioncrear.setUsuariosIdusuarios(idusuario);
        direccioncrear.setActivo(1);
        direccionesRepository.save(direccioncrear);

        return "redirect:/cliente/miperfil";

    }

    @GetMapping("/cliente/olvidecontrasenia")
    public String olvidecontrasenia()
    {
        return "cliente/recuperarContra1";
    }

    @GetMapping("/cliente/recuperarcontrasenia")
    public String recuperarcontra(){
        return "cliente/recuperarContra2";
    }

    @PostMapping("/cliente/guardarnuevacontra")
    public String nuevacontra(@RequestParam("contrasenia1") String contra1,
                              @RequestParam("contrasenia2") String contra2,
                              HttpSession session){


        //int idusuario = 7;

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int idusuario=sessionUser.getIdusuarios();

        Optional<Usuario> usarioopt = usuarioRepository.findById(idusuario);

        Usuario usuariodb = usarioopt.get();

        if(usuariodb.getIdusuarios()!=null){
            if(contra1.equals(contra2)){


                System.out.println(contra1);
                String contraseniahashbcrypt = BCrypt.hashpw(contra1, BCrypt.gensalt());

                usuariodb.setContraseniaHash(contraseniahashbcrypt);
                usuarioRepository.save(usuariodb);

            }
        }


        return "cliente/confirmarRecu";
    }

}
