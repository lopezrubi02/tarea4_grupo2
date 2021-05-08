package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.RepartidorComisionMensualDTO;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.RepartidorRepository;
import com.example.tarea4_grupo2.repository.RestauranteRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    RestauranteRepository restauranteRepository;

    @PostMapping("/Reporte1")
    public String buscaxRestauranteDistrito(@RequestParam("valorBuscado") String searchField,
                                      Model model) {

        List<Pedidos> listaPedidosxRestaurante = repartidorRepository.findPedidosByRestaurante(searchField);
        model.addAttribute("listaPedidosxDistrito", listaPedidosxRestaurante);

        List<Pedidos> listaPedidosxDistrito = repartidorRepository.findPedidosByDistrito(searchField);
        model.addAttribute("listaPedidosxDistrito", listaPedidosxDistrito);

        return "repartidor/reporte1";
    }


    @GetMapping("/home")
    public String homeRepartidor(@ModelAttribute("repartidor") Repartidor repartidor,Model model) {

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
