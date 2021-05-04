package com.example.tarea4_grupo2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminRestauranteController {
    @GetMapping("/login")
    public String logIn(){
        return "adminrestaurante/login";
    }
    @GetMapping("/register")
    public String registerAdmin(){
        return "adminrestaurante/form";
    }
    @GetMapping("/perfil")
    public String perfilAdmin(){
        return "adminrestaurante/perfil";
    }
    @GetMapping("/correoconfirmar")
    public String confirmarCorreo(){
        return"adminrestaurante/correo";
    }
    @GetMapping("/registerRestaurante")
    public String registrarRestaurante(){
        return "adminrestaurante/registerrestaurante";
    }
    @PostMapping("/espera")
    public String esperaConfirmacion(){
        return "adminrestaurante/espera";
    }
    @GetMapping("/perfilrestaurante")
    public String perfilRestaurante(){
        return "adminrestaurante/perfilrestaurante";
    }
}