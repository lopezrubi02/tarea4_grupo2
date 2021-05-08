package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Controller
@RequestMapping("/cliente")
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    DireccionesRepository direccionesRepository;

    @GetMapping("/nuevo")
    public String nuevoCliente(){
        return "cliente/registroCliente";
    }

    @PostMapping("/guardarNuevo")
    public String guardarCliente(@RequestParam("nombres") String nombres,
                                 @RequestParam("apellidos") String apellidos,
                                 @RequestParam("email") String email,
                                 @RequestParam("dni") String dni,
                                 @RequestParam("telefono") Integer telefono,
                                 @RequestParam("fechaNacimiento") String fechaNacimiento,
                                 @RequestParam("sexo") String sexo,
                                 @RequestParam("direccion") String direccion,
                                 @RequestParam("distrito") String distrito,
                                 @RequestParam("contraseniaHash") String contraseniaHash,
                                 @RequestParam("password2") String pass2,
                                Model model){

        System.out.println(nombres + apellidos + email + dni + telefono + fechaNacimiento);
        System.out.println(fechaNacimiento);
        if(contraseniaHash.equals(pass2)){
            Usuario usuario = new Usuario();
            usuario.setNombre(nombres);
            usuario.setApellidos(apellidos);
            usuario.setEmail(email);
            usuario.setTelefono(telefono);
            usuario.setSexo(sexo);
            usuario.setContraseniaHash(contraseniaHash);
            usuario.setRol("Cliente");
            usuario.setCuentaActiva(1);
            usuario.setDni(dni);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try{
                usuario.setFechaNacimiento(sdf.parse(fechaNacimiento));
                System.out.println(fechaNacimiento);
            }catch(ParseException e){
                e.printStackTrace();
                return "cliente/registroCliente";
            }
            usuarioRepository.save(usuario);

            Usuario usuarionuevo = usuarioRepository.findByDni(dni);

            int idusuarionuevo = usuarionuevo.getIdusuarios();

            Direcciones direccionactual = new Direcciones();
            direccionactual.setDireccion(direccion);
            direccionactual.setDistrito(distrito);
            direccionactual.setUsuariosIdusuarios(idusuarionuevo);

            direccionesRepository.save(direccionactual);

            return "cliente/confirmarCuenta";
        }else{
            return "cliente/registroCliente";
        }

    }

    @GetMapping("/reportes")
    public String reportesCliente(){
        return "cliente/reportes";
    }
}
