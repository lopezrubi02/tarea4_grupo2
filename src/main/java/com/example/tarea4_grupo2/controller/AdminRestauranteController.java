package com.example.avances.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminRestauranteController {
    @GetMapping("/login")
    public String loginAdmin(){
        return "AdminRestaurantes/login";
    }
    @GetMapping("/register")
    public String registerAdmin(){
        return "AdminRestaurantes/register";
    }
    @PostMapping("/espera")
    public String esperaConfirmacion(){
        return "AdminRestaurantes/espera";
    }
    @PostMapping("/estado")
    public String estadoAdmin(@RequestParam("correo") String correo) {
        //Se valida con el correo si en la bd aparece como usuario aceptado o en espera y tendría dos posibles salidas
        if(correo!=""){
            return "AdminRestaurantes/restaurante";
        }
        return "redirect:/login";
    }

    @PostMapping("/guardarCupon")
    public String guardarCupon(){

        return "AdminRestaurantes/cupones";
    }

    @PostMapping("/guardarPlato")
    public String guardarPlato(){

        return "AdminRestaurantes/menu";
    }

    @GetMapping("/crearCupon")
    public String crearCupon(){

        return "AdminRestaurantes/generarCupon";
    }

    @GetMapping("/crearPlato")
    public String crearPlato(){

        return "AdminRestaurantes/newPlato";
    }

    @GetMapping("/registerRestaurante")
    public String esperaRestaurante(){
        return "AdminRestaurantes/registerRestaurante";
    }
    @GetMapping("/estado2")
    public String estado(){
        return "AdminRestaurantes/restaurante";
    }
    @PostMapping("/validarpersona")
    public String validarPersona(){
        return "AdminRestaurantes/restaurante";
    }
    @GetMapping("/correoconfirmar")
    public String correoConfirmar(){
        return "AdminRestaurantes/correo";
    }
    @GetMapping("/perfil")
    public String prefilRestaurante(){
        return "AdminRestaurantes/perfilrestaurante";
    }
    @GetMapping("/menu")
    public String verMenu(){
        return "AdminRestaurantes/menu";
    }

    @GetMapping("/cupones")
    public String verCupones(){

        return "AdminRestaurantes/cupones";
    }

    @GetMapping("/pedidos")
    public String verPedidos(){

        return "AdminRestaurantes/pedidos";
    }

    @GetMapping("/prueba")
    public String verEjemplo(){
        return "html/recipe";
    }
}
