package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.DineroAhorrado_ClienteDTO;
import com.example.tarea4_grupo2.dto.TiempoMedio_ClienteDTO;
import com.example.tarea4_grupo2.dto.Top3Platos_ClientesDTO;
import com.example.tarea4_grupo2.dto.Top3Restaurantes_ClienteDTO;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @GetMapping("/cliente/paginaprincipal")
    public String paginaprincipal() {
        return "cliente/paginaPrincipal";
    }


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
    public String reportesCliente(Model model) {
        int idusuarios = 8;
        int anio = 2021;
        int mes = 05;
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
            System.out.println(dineroAhorrado_clienteDTO.getDiferencia());
            model.addAttribute("cliente", cliente);
            model.addAttribute("listaTop3Restaurantes",top3Restaurantes_clienteDTOS );
            model.addAttribute("listaTop3Platos", top3Platos_clientesDTOS);
            model.addAttribute("listaPromedioTiempo", tiempoMedio_clienteDTOS);
            model.addAttribute("diferencia", dineroAhorrado_clienteDTO);
            model.addAttribute("listaHistorialConsumo", pedidosRepository.obtenerHistorialConsumo(idusuarios, anio, mes));
            return "cliente/reportes";
        } else {
            return "redirect:/cliente/miperfil";
        }
    }

    @PostMapping("/cliente/recepcionCliente")
    public String recepcionCliente(@RequestParam("fechahorapedido") String fechahorapedido,
                                   Model model) {
        int idusuarios = 7;
        System.out.println(fechahorapedido);
        String[] fecha = fechahorapedido.split("-", 2);
        System.out.println(fecha);
        String a = fecha[0];
        String m = fecha[1];
        idusuarios = 8;
        int anio = Integer.parseInt(a);
        int mes = Integer.parseInt(m);
        System.out.println(anio);
        System.out.println(mes);
        System.out.println(idusuarios);
        System.out.println("**************************************");
        //string -> (fechahorapedido)
        //se divide en mes y año (haciendo un split -> arreglo de string)****
        ///sout del split
        //parseo a enteros
        Optional<Usuario> clienteopt = usuarioRepository.findById(idusuarios);
        if (clienteopt.isPresent()) {
            model.addAttribute("listaTop3Restaurantes", pedidosRepository.obtenerTop3Restaurantes(idusuarios, anio, mes));
            model.addAttribute("listaTop3Platos", pedidosRepository.obtenerTop3Platos(idusuarios, anio, mes));
            model.addAttribute("listaPromedioTiempo", pedidosRepository.obtenerTiemposPromedio(idusuarios, anio, mes));
            model.addAttribute("listaHistorialConsumo", pedidosRepository.obtenerHistorialConsumo(idusuarios, anio, mes));
            DineroAhorrado_ClienteDTO dineroAhorrado_clienteDTO = pedidosRepository.dineroAhorrado(idusuarios, anio, mes);
            //System.out.println(dineroAhorrado_clienteDTO.getDiferencia());
            System.out.println("gggggggggggggggggggggggggggg");
            model.addAttribute("diferencia", dineroAhorrado_clienteDTO);
            System.out.println("IDCliente: " + idusuarios + "Mes: " + mes + "Anio: " + anio);
        }
        return "cliente/reportes";
    }

    @GetMapping("/cliente/realizarpedido")
    public String realizarpedido(Model model) {

        int idusuarioactual = 8;

        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
        List<Categorias> listacategorias = categoriasRepository.findAll();
        List<Restaurante> listarestaurantes = restauranteRepository.findAll();
        model.addAttribute("listacategorias", listacategorias);
        model.addAttribute("listadirecciones", listadireccionescliente);
        model.addAttribute("listarestaurantes",listarestaurantes);
;

        return "cliente/realizar_pedido_cliente";
    }

    @PostMapping("/cliente/filtrarnombre")
    public String filtronombre(Model model,
                               @RequestParam(value = "searchField" ,defaultValue = "") String buscar){
        //TODO mandar a la vista los platos buscados
        System.out.println(buscar);
        List<Plato> listaplatos = platoRepository.buscarPlatoxNombre(buscar);
        List<Restaurante> listarestaurantes = restauranteRepository.buscarRestaurantexNombre(buscar);
        model.addAttribute("listarestaurantesbuscado",listarestaurantes);
        model.addAttribute("listaplatosbuscado",listaplatos);
        return "redirect:/cliente/realizarpedido";
    }


     @GetMapping("cliente/filtrocategoria")
     public String filtrosrestaurantes1(Model model,
     @RequestParam(value = "idcategoriarest" ,defaultValue = "0") int idcategoriarest
                                        ){
         System.out.println(idcategoriarest);
         System.out.println("*******************************");


         Optional<Categorias> catopt = categoriasRepository.findById(idcategoriarest);
         if(catopt.isPresent()){
             List<Restaurante> listarestauranteseleccionado = restauranteRepository.listarestxcategoria(idcategoriarest);
             int idusuarioactual = 8;

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
     public String restaurantexordenar(@RequestParam("idrestaurante") int idrestaurante, Model model){

         System.out.println(idrestaurante);
         System.out.println("**************************");

         Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);

        if(restopt.isPresent()){
            Restaurante rest = restopt.get();

            if (rest!=null){
                int cantreviews = restauranteRepository.cantreviews(idrestaurante);
                System.out.println(cantreviews);
                System.out.println("**************************");

                List<Plato> platosxrest = platoRepository.buscarPlatosPorIdRestauranteDisponilidadActivo(idrestaurante);

                model.addAttribute("restaurantexordenar",rest);
                model.addAttribute("cantreviews",cantreviews);
                model.addAttribute("platosxrest",platosxrest);
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
                              @RequestParam("idrestaurante") int idrestaurante){

         Optional<Plato> platoopt = platoRepository.findById(idplatopedir);
         Optional<Restaurante> restopt = restauranteRepository.findById(idrestaurante);

         if(platoopt.isPresent() && restopt.isPresent()){
             Plato platoseleccionado = platoopt.get();
             model.addAttribute("platoseleccionado",platoseleccionado);
             model.addAttribute("idrestaurante",idrestaurante);
             return "cliente/detalles_plato";
         }else{
            return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante;
         }
    }

    @PostMapping("/cliente/platopedido")
    public String platopedido(@RequestParam("cubierto") int cubiertos,
                              @RequestParam("cantidad") int cantidad,
                              @RequestParam("descripcion") String descripcion,
                              @RequestParam(value = "idrestaurante") int idrestaurante){

        System.out.println(cubiertos);
        System.out.println("**********************+");
        System.out.println(cantidad);
        System.out.println(descripcion);
        System.out.println(idrestaurante);
        System.out.println("gggggggggggggggggg");
        return "redirect:/cliente/restaurantexordenar?idrestaurante=" + idrestaurante;

    }

    /** Mi perfil **/

    @GetMapping("/cliente/miperfil")
    public String miperfil(Model model) {
        int idusuario = 8;
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
                                        @RequestParam("iddistrito") int iddistrito) {

        int idusuario = 8;

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
                              @RequestParam("contrasenia2") String contra2){


        int idusuario = 8;

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
