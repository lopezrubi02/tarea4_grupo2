package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.dto.PedidosDisponiblesDTO;
import com.example.tarea4_grupo2.dto.PedidosReporteDTOs;
import com.example.tarea4_grupo2.dto.PlatosPorPedidoDTO;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

@Controller
public class RepartidorController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    RepartidorRepository repartidorRepository;

    @Autowired
    DireccionesRepository direccionesRepository;

    @Autowired
    DistritosRepository distritosRepository;

    @Autowired
    PedidosRepository pedidosRepository;

    @Autowired
    RestauranteRepository restauranteRepository;





    @GetMapping("/repartidor/VerDetalles")
    public String verDetalles(Model model, @RequestParam("id") int id, RedirectAttributes attr) {
        Optional<Pedidos> pedidoElegido = pedidosRepository.findById(id);

        if (pedidoElegido.isPresent()) {
            Pedidos pedido = pedidoElegido.get();
            pedido.setEstadorepartidor("aceptado"); //Estado de esperando recojo del restaurante
            model.addAttribute("pedido", pedido);

            Restaurante restaurante = restauranteRepository.findRestauranteById(pedido.getRestaurante_idrestaurante());
            model.addAttribute("restaurante", restaurante);

            Direcciones direccion = direccionesRepository.findDireccionById(pedido.getDireccionentrega());
            model.addAttribute("direccion", direccion);

            List<PlatosPorPedidoDTO> listaPlatosPorPedidoDTO = repartidorRepository.findListaPlatosPorPedido(pedido.getIdpedidos());
            model.addAttribute("listaPlatosPorPedidoDTO", listaPlatosPorPedidoDTO);

            return "repartidor/repartidor_detalles_pedido";
        } else {
            attr.addFlashAttribute("msg", "Este pedido ya no está disponible :(");
            return "redirect:/repartidor";
        }
    }

    @GetMapping("/repartidor/PedidosDisponibles")
    public String pedidosDisponibles(RedirectAttributes attr, Model model) {
        List<PedidosDisponiblesDTO> listaPedidos = repartidorRepository.findListaPedidosDisponibles();
        if (listaPedidos.isEmpty()) {
            attr.addFlashAttribute("msg", "No hay pedidos disponibles para mostrar.");
            return "redirect:/repartidor";
        } else {
            model.addAttribute("listaPedidosDisponibles", listaPedidos);
            return "repartidor/repartidor_pedidos_disponibles";
        }
    }

    //El repartidor acepta el pedido del restaurante y se cambia el estado a "esperando recojo del restaurante"
    @GetMapping("/repartidor/AceptaPedido")
    public String aceptaPedidoPorElRepartidor(RedirectAttributes attr, @RequestParam("idpedido") int idPedidoElegido, Model model) {
        Optional<Pedidos> pedidoElegido = pedidosRepository.findById(idPedidoElegido);

        if (pedidoElegido.isPresent()) {
            Pedidos pedido = pedidoElegido.get();
            pedido.setEstadorepartidor("0"); //Estado de esperando recojo del restaurante

            List<PlatosPorPedidoDTO> listaPlatosPorPedidoDTO = repartidorRepository.findListaPlatosPorPedido(idPedidoElegido);
            Optional<Restaurante> restauranteOptional = restauranteRepository.findById(pedido.getRestaurante_idrestaurante());
            if (restauranteOptional.isPresent()) {
                model.addAttribute("listaPlatosPorPedidoDTO", listaPlatosPorPedidoDTO);
                model.addAttribute("pedido", pedido);
                Restaurante restaurante = restauranteOptional.get();
                model.addAttribute("restaurante", restaurante);
                return "repartidor/repartidor_recojo_de_producto";
            } else {
                attr.addFlashAttribute("msg", "Este pedido ya no está disponible :(");
                return "redirect:/repartidor";
            }
        } else {
            return "redirect:/repartidor";
        }
    }

    //pendiente, aceptado, recogido, entregado
    //El repartidor recoge el pedido del restaurante y el estado cambia a "por entregar".
    @GetMapping("/repartidor/ConfirmaRecojo")
    public String confirmaRecojo(RedirectAttributes attr, @RequestParam("idpedido") int idPedidoElegido, Model model) {
        Optional<Pedidos> pedidoElegido = pedidosRepository.findById(idPedidoElegido);

        if (pedidoElegido.isPresent()) {
            Pedidos pedido = pedidoElegido.get();
            pedido.setEstadorepartidor("1"); //Estado de esperando recojo del restaurante
            Optional<Usuario> usuarioOptional = usuarioRepository.findById(pedido.getIdcliente());
            List<PlatosPorPedidoDTO> listaPlatosPorPedidoDTO = repartidorRepository.findListaPlatosPorPedido(idPedidoElegido);
            Optional<Restaurante> restauranteOptional = restauranteRepository.findById(pedido.getRestaurante_idrestaurante());
            if (restauranteOptional.isPresent() && usuarioOptional.isPresent()) {
                Usuario usuario = usuarioOptional.get();
                model.addAttribute("usuario", usuario);
                model.addAttribute("listaPlatosPorPedidoDTO", listaPlatosPorPedidoDTO);
                model.addAttribute("pedido", pedido);
                Restaurante restaurante = restauranteOptional.get();
                model.addAttribute("restaurante", restaurante);
                return "repartidor/repartidor_pedido_en_progreso";
            } else {
                attr.addFlashAttribute("msg", "Este pedido ya no está disponible :(");
                return "redirect:/repartidor";
            }
        } else {
            return "redirect:/repartidor";
        }

    }

    //El repartidor entrega el pedido al cliente
    @GetMapping("/repartidor/ConfirmaEntrega")
    public String confirmaEntrega(RedirectAttributes attr, @RequestParam("idpedido") int idPedidoElegido, Model model) {
        Optional<Pedidos> pedidoElegido = pedidosRepository.findById(idPedidoElegido);

        if (pedidoElegido.isPresent()) {
            Pedidos pedido = pedidoElegido.get();
            pedido.setEstadorepartidor("entregado"); //Estado de esperando ser entregado al cliente
            model.addAttribute("pedido", pedido);
            attr.addFlashAttribute("msgVerde", "Se registró la entrega del pedido. ¡Gracias!");
        } else {
            attr.addFlashAttribute("msg", "Este pedido ya no está disponible :(");
        }
        return "redirect:/repartidor";
    }

    //Filtra por Restaurante o Distrito
    @PostMapping("/repartidor/Buscador")
    public String buscador(@RequestParam("valorBuscado") String searchField,
                           Model model, RedirectAttributes attr,HttpSession session) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int id=sessionUser.getIdusuarios();

        //List<PedidosReporteDTO> listaPedidosxRestaurante = repartidorRepository.findPedidosByRestaurante(searchField);
        //List<PedidosReporteDTO> listaPedidosxDistrito = repartidorRepository.findPedidosByDistrito(searchField);
        List <PedidosReporteDTOs> ListaFindReporte = repartidorRepository.findReporte(searchField, id);
        if (ListaFindReporte.isEmpty()) {
            attr.addFlashAttribute("msg", "No hay resultados asociados a la búsqueda.");
            return "redirect:/repartidor";
        }else{
            //model.addAttribute("listaPedidosxRestaurante", listaPedidosxRestaurante);
            //model.addAttribute("listaPedidosxDistrito", listaPedidosxDistrito);
            model.addAttribute("ListaFindReporte", ListaFindReporte);
            return "repartidor/repartidor_resultado_buscador";
        }

    }

    @GetMapping("/repartidor/Reportes")
    public String reportes(Model model, RedirectAttributes attr,HttpSession session){

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int id = sessionUser.getIdusuarios();
        List<PedidosReporteDTOs> listaReporte1 = repartidorRepository.findPedidosPorRepartidor(id);
        List<RepartidorComisionMensualDTO> listaComisionMensual = repartidorRepository.obtenerComisionPorMes(id);
        if (listaReporte1.isEmpty()) {
            attr.addFlashAttribute("msg", "No hay resultados para mostrar.");
            return "redirect:/repartidor";
        }else{
            //Lista1
            model.addAttribute("listaReporte1", listaReporte1);
            //Lista2
            model.addAttribute("listaComisionMensual", listaComisionMensual);
            return "repartidor/repartidor_reportes";
        }

    }


    @GetMapping(value={"/repartidor/home", "/repartidor", "/repartidor"})
    public String homeRepartidor(@ModelAttribute("repartidor") Repartidor repartidor,
                                 Model model, RedirectAttributes attr,HttpSession session) {


        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int id= sessionUser.getIdusuarios();

        Optional<Usuario> optional = usuarioRepository.findById(id);
        if (optional.isPresent()) {
            Usuario usuario = optional.get();
            model.addAttribute("usuario", usuario);

            Repartidor repartidor2 = repartidorRepository.findRepartidorByIdusuariosEquals(id);
            model.addAttribute("repartidor", repartidor2);
            model.addAttribute("listadistritos",distritosRepository.findAll());
        }
        return "repartidor/repartidor_principal";
    }

    @PostMapping("/repartidor/save_principal")
    public String guardarHomeRepartidor(Repartidor repartidorRecibido) {

        Repartidor optionalRepartidor = repartidorRepository.findRepartidorByIdusuariosEquals(repartidorRecibido.getIdusuarios());
        Repartidor repartidorEnlabasededatos = optionalRepartidor;

        //repartidorEnlabasededatos.setMovilidad(optionalRepartidor.getMovilidad());
        repartidorEnlabasededatos.setDisponibilidad(repartidorRecibido.isDisponibilidad());
       // Distritos nuevo=distritosRepository.findById(repartidorRecibido.getDistritos())
        repartidorEnlabasededatos.setDistritos(repartidorRecibido.getDistritos());

        repartidorRepository.save(repartidorEnlabasededatos);

        return "redirect:/repartidor/home";
    }

    @GetMapping("/repartidor/perfil")
    public String perfilRepartidor(@ModelAttribute("repartidor") Repartidor repartidor, Model model,
                                   HttpSession session) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int id = sessionUser.getIdusuarios();


        Optional<Usuario> optional = usuarioRepository.findById(id);

        if (optional.isPresent()) {
            Usuario usuario = optional.get();
            model.addAttribute("usuario", usuario);

            Repartidor repartidor2 = repartidorRepository.findRepartidorByIdusuariosEquals(id);
            model.addAttribute("repartidor", repartidor2);

            Direcciones direcciones = direccionesRepository.findByUsuariosIdusuarios(id);
            model.addAttribute("direcciones", direcciones);
        }

        return "repartidor/repartidor_perfil";
    }

    @PostMapping("/repartidor/save_perfil")
    public String guardarPerfilRepartidor(@ModelAttribute("usuario") @Valid Usuario usuario,
                                          BindingResult bindingResult,
                                          @RequestParam("password2") String password2,
                                          @RequestParam("direccion") String direccion,
                                          //HttpSession session,
                                          Model model) {
        //Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        int id=usuario.getIdusuarios();
        Optional<Usuario> optional = usuarioRepository.findById(id);

        Usuario user = optional.get();


        if(bindingResult.hasFieldErrors("telefono")|| bindingResult.hasFieldErrors("contraseniaHash")){

            Usuario usuario2 = optional.get();
            model.addAttribute("usuario", usuario2);

            Repartidor repartidor2 = repartidorRepository.findRepartidorByIdusuariosEquals(id);
            model.addAttribute("repartidor", repartidor2);

            Direcciones direcciones2 = direccionesRepository.findByUsuariosIdusuarios(id);
            model.addAttribute("direcciones", direcciones2);
            return "repartidor/repartidor_perfil";
        }
        else {
            if(usuario.getContraseniaHash().equals(password2)){

                user.setTelefono(usuario.getTelefono());
                user.setContraseniaHash(BCrypt.hashpw(usuario.getContraseniaHash(),BCrypt.gensalt()));
                usuarioRepository.save(user);
                Direcciones dnueva = direccionesRepository.findByUsuariosIdusuarios(usuario.getIdusuarios());
                dnueva.setDireccion(direccion);
                direccionesRepository.save(dnueva);
                return "redirect:/repartidor/perfil";
            }
            else{
                if(password2.isEmpty()){
                    user.setTelefono(usuario.getTelefono());
                    Direcciones dnueva = direccionesRepository.findByUsuariosIdusuarios(usuario.getIdusuarios());
                    dnueva.setDireccion(direccion);
                    direccionesRepository.save(dnueva);
                    usuarioRepository.save(user);
                    return "redirect:/repartidor/perfil";
                }else{
                    model.addAttribute("msg","Contraseñas no son iguales");
                    Usuario usuario2 = optional.get();
                    model.addAttribute("usuario", usuario2);

                    Repartidor repartidor2 = repartidorRepository.findRepartidorByIdusuariosEquals(id);
                    model.addAttribute("repartidor", repartidor2);
                    Direcciones direcciones2 = direccionesRepository.findByUsuariosIdusuarios(id);
                    model.addAttribute("direcciones", direcciones2);

                    return "repartidor/repartidor_perfil";
                }

            }
        }

    }




    @GetMapping("/new2")
    public String nuevoRepartidor2(@ModelAttribute("repartidor") Repartidor repartidor,
                                   @RequestParam("id") int id, Model model) {
        Repartidor repartidor1=repartidorRepository.findRepartidorByIdusuariosEquals(id);
        model.addAttribute("repartidor",repartidor1);
        return "repartidor/registro_parte2";
    }

    @PostMapping("/save2")
    public String guardarRepartidor2(Repartidor repartidor,@RequestParam("placa") String placa,
                                     @RequestParam("licencia") String licencia  ,
                                     @RequestParam("idusuarios") String id) {

        int id1=Integer.parseInt(id);
        Repartidor repartidor1=repartidorRepository.findRepartidorByIdusuariosEquals(id1);
        repartidor1.setPlaca(placa);
        repartidor1.setLicencia(licencia);
        repartidorRepository.save(repartidor1);

        return "redirect:/repartidor/home";
    }

    @GetMapping("/new3")
    public String nuevoRepartidor3(@ModelAttribute("usuario") Usuario usuario,
                                   BindingResult bindingResult, Model model) {
        model.addAttribute("listadistritos", distritosRepository.findAll());
        return "repartidor/registro_parte3";
    }

    @PostMapping("/save3")
    public String guardarRepartidor3(@ModelAttribute("usuario") @Valid Usuario usuario,
                                     BindingResult bindingResult,
                                     @RequestParam("direccion") String direccion,
                                     @RequestParam("distrito") Distritos distrito,
                                     @RequestParam("password2") String pass2,
                                     @RequestParam("movilidad") String movilidad,
                                     @RequestParam("archivo") MultipartFile file,
                                     Model model, RedirectAttributes attributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("listadistritos", distritosRepository.findAll());
            return "repartidor/registro_parte3";
        }

        if (usuario.getContraseniaHash().equals(pass2)) {
            Usuario usuario2 = new Usuario();
            usuario2.setNombre(usuario.getNombre());
            usuario2.setApellidos(usuario.getApellidos());
            usuario2.setEmail(usuario.getEmail());
            usuario2.setTelefono(usuario.getTelefono());
            usuario2.setSexo(usuario.getSexo());

            String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());
            System.out.println(contraseniahashbcrypt);

            usuario2.setContraseniaHash(contraseniahashbcrypt);
            usuario2.setRol("Repartidor");
            usuario2.setCuentaActiva(2);
            usuario2.setDni(usuario.getDni());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            usuario2.setFechaNacimiento(usuario.getFechaNacimiento());
            usuarioRepository.save(usuario2);
                //System.out.println(fechaNacimiento);

            Direcciones direccionactual = new Direcciones();
            direccionactual.setDireccion(direccion);
            direccionactual.setDistrito(distrito);
            direccionactual.setUsuariosIdusuarios(usuario2.getIdusuarios());

            direccionesRepository.save(direccionactual);

            if (file.isEmpty()) {
                model.addAttribute("msg", "Debe subir un archivo");
                return "repartidor/registro_parte3";
            }
            String fileName = file.getOriginalFilename();
            if (fileName.contains("..")) {
                model.addAttribute("msg", "No se permiten '..' en el archivo");
                return "repartidor/registro_parte3";
            }
            try {
                Repartidor repartidor= new Repartidor();
                repartidor.setIdusuarios(usuario2.getIdusuarios());
                repartidor.setFoto(file.getBytes());
                repartidor.setFotonombre(fileName);
                repartidor.setFotocontenttype(file.getContentType());
                repartidor.setDistritos(distrito);
                repartidor.setDisponibilidad(false);
                repartidor.setMovilidad(movilidad);
                repartidorRepository.save(repartidor);

                if(movilidad.equalsIgnoreCase("bicicleta")){
                    return "redirect:/repartidor/home";
                }else{
                    //form de placa y licencia
                    attributes.addAttribute("id",usuario2.getIdusuarios());
                    return "redirect:/repartidor/new2";
                }


            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("msg", "ocurrió un error al subir el archivo");
                return "repartidor/registro_parte3";
            }

            //usuarioRepository.save(usuario);
            //Usuario usuarionuevo = usuarioRepository.findByDni(usuario.getDni());
            //int idusuarionuevo = usuarionuevo.getIdusuarios();

            //return "redirect:/repartidor/new1";
        } else {
            return "repartidor/registro_parte3";
        }





    }

}
