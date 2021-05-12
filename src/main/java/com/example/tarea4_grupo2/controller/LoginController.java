package com.example.tarea4_grupo2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/loginAdmin")
    public String loginForm(
            @RequestParam(value = "error", required = false) String error,
            Model model
    ){
        return "adminsistema/login";
    }

    @GetMapping("/loginCliente")
    public String loginFormCliente(
            @RequestParam(value = "error", required = false) String error,
            Model model
    )
    {
        return "cliente/loginForm";
    }
}