package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Pedidos;
import com.example.tarea4_grupo2.entity.Restaurante;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.RepartidorRepository;
import com.example.tarea4_grupo2.repository.RestauranteRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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




}
