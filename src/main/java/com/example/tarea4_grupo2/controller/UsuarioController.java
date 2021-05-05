package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cliente")
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @GetMapping("/nuevo")
    public String nuevoUsuario(){
        return "cliente/registroCliente";
    }

    @PostMapping("/guardarNuevo")
    public String guardarUsuario(Usuario usuario,
                                 @RequestParam(name= "password2") String pass2){

        if(usuario.getContraseniaHash().equals(pass2)){
            usuario.setRol("Cliente");
           // usuarioRepository.nuevoCliente(usuario.getNombre(),usuario.getApellidos(),usuario.getEmail(),
             //                   usuario.getContraseniaHash(),usuario.getTelefono(),usuario.getFechaNacimiento(),usuario.getSexo(),
               //             usuario.getDni(),usuario.getRol());

            usuarioRepository.save(usuario);
        }else{
            System.out.println("***** form");
            return "cliente/registroCliente";
        }
        System.out.println("registro");
        return "redirect:/cliente/confirmarCuenta";
    }

    @GetMapping("/realizarPedido")
    public String realizarPedido(){
        return "cliente/realizar_pedido_cliente";
    }
}
