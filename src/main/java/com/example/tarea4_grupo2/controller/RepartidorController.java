package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.PedidosDisponiblesDTO;
import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.dto.PlatosPorPedidoDTO;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/repartidor")
public class RepartidorController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    RepartidorRepository repartidorRepository;

    @Autowired
    DireccionesRepository direccionesRepository;

    @Autowired
    PedidosRepository pedidosRepository;

    @Autowired
    RestauranteRepository restauranteRepository;

    @GetMapping("/VerDetalles")
    public String verDetalles (Model model, @RequestParam("id") int id){
        Optional<Pedidos> optionalPedidos = pedidosRepository.findById(id);

        if (optionalPedidos.isPresent()) {
            Pedidos pedido = optionalPedidos.get();
            List <PlatosPorPedidoDTO> listaPlatosPorPedidoDTO= repartidorRepository.findListaPlatosPorPedido(id);
            Optional<Restaurante> restauranteOptional = restauranteRepository.findById(pedido.getRestaurante_idrestaurante());
            if (restauranteOptional.isPresent()){
                model.addAttribute("listaPlatosPorPedidoDTO", listaPlatosPorPedidoDTO);
                model.addAttribute("pedido", pedido);
                Restaurante restaurante = restauranteOptional.get();
                model.addAttribute("restaurante", restaurante);
                return "repartidor/repartidor_detalles_pedido";
            }else {
                return "redirect:/repartidor";
            }
        }else{return "redirect:/repartidor";}
    }

    @GetMapping("/PedidosDisponibles")
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
    @GetMapping("/AceptaPedido")
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

    //El repartidor recoge el pedido del restaurante y el estado cambia a "por entregar".
    @GetMapping("/ConfirmaRecojo")
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
            attr.addFlashAttribute("Este pedido ya no está disponible :(");
            return "redirect:/repartidor";
        }

    }

    //El repartidor entrega el pedido al cliente
    @GetMapping("/ConfirmaEntrega")
    public String confirmaEntrega(RedirectAttributes attr, @RequestParam("idpedido") int idPedidoElegido, Model model) {
        Optional<Pedidos> pedidoElegido = pedidosRepository.findById(idPedidoElegido);

        if (pedidoElegido.isPresent()) {
            Pedidos pedido = pedidoElegido.get();
            pedido.setEstadorepartidor("2"); //Estado de entregado al cliente
            attr.addFlashAttribute("msg", "Se registró la entrega del pedido al cliente");
            return "redirect:/repartidor";
        } else {
            attr.addFlashAttribute("msg", "Este pedido ya no está disponible :(");
            return "redirect:/repartidor";
        }
    }

    //Filtra por Restaurante o Distrito
    @PostMapping("/Buscador")
    public String buscador(@RequestParam("valorBuscado") String searchField,
                           Model model, RedirectAttributes attr) {

        List<Pedidos> listaPedidosxRestaurante = repartidorRepository.findPedidosByRestaurante(searchField);
        List<Pedidos> listaPedidosxDistrito = repartidorRepository.findPedidosByDistrito(searchField);
        if (listaPedidosxRestaurante.isEmpty() && listaPedidosxDistrito.isEmpty()) {
            attr.addFlashAttribute("msg", "No hay resultados asociados a la búsqueda.");
            return "redirect:/repartidor";
        }else{
        model.addAttribute("listaPedidosxRestaurante", listaPedidosxRestaurante);
        model.addAttribute("listaPedidosxDistrito", listaPedidosxDistrito);
        return"repartidor/repartidor_resultado_buscador";
        }

    }

    @GetMapping("/Reportes")
    public String reportes(Model model, RedirectAttributes attr){
        List<PedidosReporteDto> listaReporte1 = repartidorRepository.findPedidosPorRepartidor();
        if (listaReporte1.isEmpty()) {
            attr.addFlashAttribute("msg", "No hay resultados para mostrar.");
            return "redirect:/repartidor";
        }else{
            model.addAttribute("listaReporte1", listaReporte1);
            return "repartidor/repartidor_reportes";
        }

    }


    @GetMapping(value={"/home", "", "/"})
    public String homeRepartidor(@ModelAttribute("repartidor") Repartidor repartidor,Model model, RedirectAttributes attr) {

        int id=10;

        Optional<Usuario> optional = usuarioRepository.findById(id);

        if (optional.isPresent()) {
            Usuario usuario = optional.get();
            model.addAttribute("usuario", usuario);

            Repartidor repartidor2 = repartidorRepository.findRepartidorByIdusuariosEquals(id);
            model.addAttribute("repartidor", repartidor2);
        }
        return "repartidor/repartidor_principal";
    }

    @PostMapping("/save_principal")
    public String guardarHomeRepartidor(
            Repartidor repartidorRecibido) {

        Repartidor optionalRepartidor = repartidorRepository.findRepartidorByIdusuariosEquals(repartidorRecibido.getIdusuarios());
        Repartidor repartidorEnlabasededatos = optionalRepartidor;

        //repartidorEnlabasededatos.setMovilidad(optionalRepartidor.getMovilidad());
        repartidorEnlabasededatos.setDisponibilidad(repartidorRecibido.isDisponibilidad());
        repartidorEnlabasededatos.setDistritoactual(repartidorRecibido.getDistritoactual());

        repartidorRepository.save(repartidorEnlabasededatos);

        return "redirect:/repartidor/home";
    }

    @GetMapping("/perfil")
    public String perfilRepartidor(@ModelAttribute("repartidor") Repartidor repartidor, Model model) {

        int id=10;

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

    @PostMapping("/save_perfil")
    public String guardarPerfilRepartidor(@ModelAttribute("usuario") Usuario usuario,
                                        @RequestParam("idusuario") int idusuario,@RequestParam("telefono") int telefono,
                                          @RequestParam("direccion") String direccion,@RequestParam("password") String password
    ) {

        Optional<Usuario> usuario1= usuarioRepository.findById(idusuario);

        if(usuario1.isPresent()){
            usuario=usuario1.get();
            usuario.setTelefono(telefono);
            usuario.setContraseniaHash(password);
            usuarioRepository.save(usuario);

            Direcciones dnueva = direccionesRepository.findByUsuariosIdusuarios(usuario.getIdusuarios());
            dnueva.setDireccion(direccion);
            direccionesRepository.save(dnueva);
        }

        return "redirect:/repartidor/perfil";
    }

    @GetMapping("/new1")
    public String nuevoRepartidor1(@ModelAttribute("repartidor") Repartidor repartidor) {
        return "repartidor/registro_parte1";
    }

    @PostMapping("/save1")
    public String guardarRepartidor1(@RequestParam("movilidad") String movilidad, Model model) {

        Repartidor repartidor=new Repartidor();
        repartidor.setMovilidad(movilidad);
        model.addAttribute("repartidor",repartidor);

        return "redirect:/repartidor/new2";
    }

    @GetMapping("/new2")
    public String nuevoRepartidor2(@ModelAttribute("repartidor") Repartidor repartidor) {
        return "repartidor/registro_parte2";
    }

    @PostMapping("/save2")
    public String guardarRepartidor2(Repartidor repartidor,@RequestParam("placa") String placa,
                                     @RequestParam("licencia") String licencia      ) {

        repartidor.setPlaca(placa);
        repartidor.setLicencia(licencia);
        repartidorRepository.save(repartidor);

        return "repartidor/new3";
    }

    @GetMapping("/new3")
    public String nuevoRepartidor3(@ModelAttribute("repartidor") Usuario repartidor) {
        return "repartidor/registro_parte3";
    }

    //@GetMapping("/reportes")
    //public String repartidorReportes(Model model) {
        //hardcodeado por ahora
       // int id=10;
      //  List<RepartidorComisionMensualDTO> listaComisionMensual = repartidorRepository.obtenerComisionPorMes(id);


}
