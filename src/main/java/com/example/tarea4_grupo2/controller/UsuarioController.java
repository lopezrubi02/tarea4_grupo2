package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @GetMapping("/nuevo")
    public String nuevoCliente(){
        return "cliente/registroCliente";
    }

    @PostMapping("/guardarNuevo")
    public String guardarCliente(Usuario usuario){
        return "redirect:/cliente/nuevo";
    }

    @GetMapping("/reportes")
    public String reportesCliente(){
        return "cliente/reportes";
    }
}
