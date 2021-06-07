package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Distritos;
import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.DistritosRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import com.example.tarea4_grupo2.service.SendMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class LoginController {
    @Autowired
    SendMailService sendMailService;
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
            if(Pattern.matches("^[a-z0-9]+@gmail.com",usuario.getEmail())){
                if(usuario.getContraseniaHash().equals(password2)) {
                    Usuario persona = usuarioRepository.findByEmailAndAndRol(usuario.getEmail(), "AdminRestaurante");
                    if(persona.getIdusuarios()==0){
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
                    else{
                        model.addAttribute("msg3","Correo ya existe");
                        model.addAttribute("listadistritos",distritosRepository.findAll());
                        return "AdminRestaurantes/register";
                    }
                }
                else {
                    model.addAttribute("msg","Contraseñas no son iguales");
                    model.addAttribute("listadistritos",distritosRepository.findAll());
                    return "AdminRestaurantes/register";
                }
            }
            else{
                System.out.println("hola");
                model.addAttribute("msg2","Ingrese un correo valido");
                model.addAttribute("listadistritos",distritosRepository.findAll());
                return "AdminRestaurantes/register";
            }
        }
    }

    @GetMapping(value = {"","/login"})
    public String loginForm(){
        return "login/login";
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
        } else if(rol.equals("Repartidor")) {
            return "redirect:/repartidor/home";
        }else if(rol.equals("Cliente")){
            return "redirect:/cliente/paginaprincipal";
        }
        else{
            System.out.println(rol);
            return "/login";
        }
    }
    @GetMapping("/olvidoContrasenia")
    public String olvidoContrasenia(RedirectAttributes attr) {
        return "login/olvidoContrasenia";
    }

    //envia el correo con el token
    @PostMapping("/recuperarContrasenia")
    public String recuperarContrasenia(@RequestParam("correo") String correoDestino, RedirectAttributes attr,
                                       @ModelAttribute("usuario") Usuario usuario) throws MalformedURLException {
        String emailPattern = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@" +
                "[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(correoDestino);

        if (matcher.find() == true) {
            Optional<Usuario> optionalUsuario = Optional.ofNullable(usuarioRepository.findByEmail(correoDestino));
            String subject;
            String mensaje;
            if (optionalUsuario.isPresent()) {
                SecureRandom random = new SecureRandom();
                byte bytes[] = new byte[20];
                random.nextBytes(bytes);
                String token = bytes.toString();
                subject = "Recuperacion de contraseña - Spicy";
                //TODO modificar direcion url despues de despliegue aws.
                //String direccion = "http://localhost:8090/cambiar1/";
                //Pegar aquí los datos del AWS;
                String aws = "ec2-user@ec2-3-84-20-210.compute-1.amazonaws.com";
                String direccion = "http://" + aws + ":8080/proyecto/cambiar1/";
                URL url = new URL(direccion + token);
                mensaje = "¡Hola!<br><br>Para reestablecer su contraseña haga click: <a href='" + direccion + token + "'>AQUÍ</a> <br><br>Atte. Equipo de Spicy :D</b>";
                attr.addFlashAttribute("msg", "¡Revisa tu correo para continuar el proceso! :D");
                optionalUsuario.get().setToken(token);
            } else {
                subject = "Invitacion de registro - Spicy";
                mensaje = "¡Hola!<br>No está registrado en Spicy :( <br><br>Atte. Equipo de Spicy :D</b>";
                attr.addFlashAttribute("msg2", "¡No estas registrado! :(");
            }
            sendMailService.sendMail(correoDestino, "saritaatanacioarenas@gmail.com", subject, mensaje);
            return "redirect:/login";
        } else {
            attr.addFlashAttribute("msg2", "¡Ingresa un formato email! :(");
            return "redirect:/login";
        }
    }

    //aquí se ingresa la contraseña
    @GetMapping(value = "/cambiar1/{token}") //formato que espero el usuario coloque en URL
    public String cambiar1(@PathVariable("token") String tokenObtenido, Model model, RedirectAttributes attr) {
        Usuario usuario = new Usuario();
        usuario.setToken(tokenObtenido);
        Optional<Usuario> usuarioToken = Optional.ofNullable(usuarioRepository.findByToken(usuario.getToken()));
        if (usuarioToken.isPresent()) {
            model.addAttribute("usuario", usuario);
            return "login/cambiar1";
        }else {
            attr.addFlashAttribute("msg2", "¡Error en el token o expirado! debes generar otro :(");
            return "redirect:/login";
        }
    }

    public  boolean validarContrasenia(String contrasenia1) {
        /*      https://mkyong.com/regular-expressions/how-to-validate-password-with-regular-expression/
                A!@#&()–a1
                A[{}]:;',?/*a1
                A~$^+=<>a1
                0123456789$abcdefgAB
                123Aa$Aa
         */
        Pattern pattern1 = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$");
        Matcher matcher1 = pattern1.matcher(contrasenia1);
        return matcher1.matches();
    }

    @PostMapping("/cambiarContrasenia")
    public String cambiarContrasenia(Usuario usuario, RedirectAttributes attr, @RequestParam("contrasenia") String contrasenia, @RequestParam("contrasenia2") String contrasenia2) {

        Optional<Usuario> usuarioToken = Optional.ofNullable(usuarioRepository.findByToken(usuario.getToken()));
        if (usuarioToken.isPresent()) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (contrasenia == "" || contrasenia2 == "") {
                attr.addFlashAttribute("msg2", "¡Contraseña no puede ser nula! Intenta nuevamente con el link enviado al correo :C");
            } else if (contrasenia.equals(contrasenia2)){
                Boolean validacionContrasenias = validarContrasenia(contrasenia);
                if (validacionContrasenias==true) {
                    String contraseniahashbcrypt = BCrypt.hashpw(contrasenia, BCrypt.gensalt());
                    usuarioToken.get().setContraseniaHash(contraseniahashbcrypt);
                    attr.addFlashAttribute("msg", "¡Contraseña cambiada! :D");
                    SecureRandom random = new SecureRandom();
                    byte bytes[] = new byte[20];
                    random.nextBytes(bytes);
                    String tokenNuevo = bytes.toString();
                    usuarioToken.get().setToken(tokenNuevo);
                    usuarioRepository.save(usuarioToken.get());
                } else {
                    attr.addFlashAttribute("msg2", "¡Debe tener al menos 8 caracteres, uno especial y una mayuscula");
                }
                return "redirect:/login";
            } else{
                attr.addFlashAttribute("msg2", "¡Las contraseñas no coinciden!");
                return "redirect:/login";
            }
            return "redirect:/login";
        } else {
            attr.addFlashAttribute("msg2", "¡Error en el token o expirado! debes generar otro :(");
            return "login/olvidoContrasenia";
        }
    }

}