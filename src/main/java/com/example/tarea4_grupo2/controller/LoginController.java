package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Distritos;
import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.DistritosRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class LoginController {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    DistritosRepository distritosRepository;
    @Autowired
    DireccionesRepository direccionesRepository;
    @GetMapping("/eleccion")
    public String eleccionRegister(){
        return "adminsistema/eleccion";
    }
    @GetMapping("/registerAdminRestaurante")
    public String registerAdmin(@ModelAttribute("usuario") Usuario usuario, Model model){
        model.addAttribute("listadistritos",distritosRepository.findAll());
        return "AdminRestaurantes/register";
    }
    @PostMapping("/guardaradminrest")
    public String guardarAdmin(@ModelAttribute("usuario") @Valid Usuario usuario, BindingResult bindingResult,
                               @RequestParam("password2") String password2,
                               @RequestParam("iddistrito") int iddistrito,
                               @RequestParam("direccion") String direccion,
                               Model model){
        if(bindingResult.hasErrors()){
            model.addAttribute("listadistritos",distritosRepository.findAll());
            return "AdminRestaurantes/register";
        }
        else {
            if(usuario.getContraseniaHash().equals(password2)) {
                String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());
                usuario.setContraseniaHash(contraseniahashbcrypt);
                usuarioRepository.save(usuario);
                Usuario usuarionuevo = usuarioRepository.findByDni(usuario.getDni());
                Direcciones direccionactual = new Direcciones();
                direccionactual.setDireccion(direccion);
                Distritos distritosactual= distritosRepository.findById(iddistrito).get();
                direccionactual.setDistrito(distritosactual);
                direccionactual.setUsuariosIdusuarios(usuarionuevo.getIdusuarios());
                direccionactual.setActivo(1);
                direccionesRepository.save(direccionactual);
                return"AdminRestaurantes/correo";
            }
            else {
                model.addAttribute("msg","Contraseñas no son iguales");
                model.addAttribute("listadistritos",distritosRepository.findAll());
                return "AdminRestaurantes/register";
            }
        }
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
        }else if(rol.equals("AdminSistema")){
            System.out.println("El rol es : "+rol);
            return "redirect:/admin/gestionCuentas";
        } else{
            System.out.println(rol);
            return "adminsistema/login";
        }
    }
}