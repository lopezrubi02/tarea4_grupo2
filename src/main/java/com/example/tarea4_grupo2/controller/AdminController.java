package com.example.tarea4_grupo2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/usuariosActuales")
    public String usuariosActuales(){
        return "adminsistema/usuariosActuales";
    }
}
