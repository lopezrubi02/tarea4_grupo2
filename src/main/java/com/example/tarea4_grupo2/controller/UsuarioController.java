package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Categorias;
import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.CategoriasRepository;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.PlatosRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/cliente")
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PlatosRepository platosRepository;

    @Autowired
    CategoriasRepository categoriasRepository;

    @Autowired
    DireccionesRepository direccionesRepository;


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
    public String realizarPedido(Model model){

        int idusuario = 7;

        model.addAttribute("listaPlatos",platosRepository.findAll());
        model.addAttribute("listaCategorias",categoriasRepository.findAll());
        List<Direcciones> direccionUsuario = direccionesRepository.direccionesporusuario(idusuario);
        model.addAttribute("direccionesUsuario",direccionUsuario);
        System.out.println(platosRepository.findAll());
        System.out.println(direccionUsuario);
        return "cliente/realizar_pedido_cliente";
    }
}
