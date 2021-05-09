package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.repository.PedidosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/reportesCliente")
public class ReportesClienteController {
    @Autowired
    PedidosRepository pedidosRepository;

    @GetMapping(value = {"","/"})
    public String reportesCliente(Model model, @RequestParam("idcliente") int idcliente,
                                               @RequestParam("anio") int anio,
                                               @RequestParam("mes") int mes){
        model.addAttribute("listaTop3Restaurantes", pedidosRepository.obtenerTop3Restaurantes(idcliente, anio, mes));
        return "cliente/reportes";
    }
}
