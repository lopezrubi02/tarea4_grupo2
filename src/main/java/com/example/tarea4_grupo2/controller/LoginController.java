package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {
    @Autowired
    UsuarioRepository usuarioRepository;
    @GetMapping("/eleccion")
    public String eleccionRegister(){
        return "adminsistema/eleccion";
    }
    @GetMapping("/registerAdminRestaurante")
    public String registerAdminRest(){
        return "redirect:/adminrest/register";
    }


    @GetMapping("/login")
    public String loginForm(
    ){
        return "adminsistema/login";
    }

    @GetMapping("/redirectByRol")
    public String redirectByRol(Authentication auth, HttpSession session){
        String rol="";
        for(GrantedAuthority role:auth.getAuthorities()){
            rol= role.getAuthority();
            break;
        }
        Usuario usuarioLogueado= usuarioRepository.findByEmail(auth.getName());
        session.setAttribute("usuarioLogueado",usuarioLogueado);
        if (rol.equals("AdminRestaurante")){

            return "redirect:/adminrest/login";
        }
        else{
            System.out.println(rol);
            return "adminsistema/login";
        }
    }
}