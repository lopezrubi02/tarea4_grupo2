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

        Optional<Repartidor> repartidor2 = repartidorRepository.findById(id);
        if(repartidor2.isPresent()) {
            repartidor=repartidor2.get();
            model.addAttribute("repartidor", repartidor);

        }
        return "repartidor/repartidor_principal";
    }

    @PostMapping("/save_home")
    public String guardarHomeRepartidor(Repartidor datosRepartidor,
                                        @RequestParam("movilidad") String movilidad,
                                        @RequestParam("disponibilidad") String disponibilidad) {

        //disponible - true | ocupado-false
        if(disponibilidad.equalsIgnoreCase("disponible")){
            boolean disponibilidadB = true;
            datosRepartidor.setDisponibilidad(disponibilidadB);
        }else{
            boolean disponibilidadB = false;
            datosRepartidor.setDisponibilidad(disponibilidadB);
        }

        datosRepartidor.setMovilidad(movilidad);
        repartidorRepository.save(datosRepartidor);

        return "repartidor/home";
    }



    @GetMapping("/new1")
    public String nuevoRepartidor1(@ModelAttribute("repartidor") Repartidor repartidor) {
        return "repartidor/registro_parte1";
    }

    @GetMapping("/perfil")
    public String perfilRepartidor(@ModelAttribute("repartidor") Repartidor repartidor, Model model) {

        int id=10;
        id = repartidor.getIdrepartidor();

        Repartidor repartidor2 = repartidorRepository.findRepartidorByUsuariosIdusuariosEquals(id);

        model.addAttribute("repartidor",repartidor2);



        return "repartidor/repartidor_perfil";
    }

    @PostMapping("/save_perfil")
    public String guardarPerfilRepartidor(@ModelAttribute("repartidor") Usuario repartidor,
                                        @RequestParam("direccion") String direccion) {


        usuarioRepository.save(repartidor);

        return "repartidor/home";
    }


    @PostMapping("/save1")
    public String guardarRepartidor1(Repartidor repartidor,@RequestParam("movilidad") String movilidad) {

        repartidor.setMovilidad(movilidad);
        repartidorRepository.save(repartidor);

        return "repartidor/new2";
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

    @GetMapping("/reportes")
    public String repartidorReportes(Model model) {
        //hardcodeado por ahora
        int id=10;
        List<RepartidorComisionMensualDTO> listaComisionMensual = repartidorRepository.obtenerComisionPorMes(id);

        model.addAttribute("listaComisionMensual",listaComisionMensual);

        return "repartidor/repartidor_reportes";
    }

}
