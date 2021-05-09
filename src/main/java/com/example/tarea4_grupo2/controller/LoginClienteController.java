package com.example.tarea4_grupo2.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginClienteController {
    @GetMapping("/loginForm")
    public String loginForm() {
        return "cliente/loginForm";
    }

}
