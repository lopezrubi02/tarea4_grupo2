package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Distritos;
import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.oauth.CustomOAuth2User;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.DistritosRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import com.example.tarea4_grupo2.service.SendMailService;
import org.json.JSONObject;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
        String direction=null;
        model.addAttribute("direction",direction);
        System.out.println(direction);
        return "AdminRestaurantes/register";
    }

    @PostMapping("/guardaradminrest")
    public String guardarAdmin(@ModelAttribute("usuario") @Valid Usuario usuario, BindingResult bindingResult,
                               @RequestParam("password2") String password2,
                               @RequestParam("iddistrito") int iddistrito,
                               @RequestParam("direccion_real") String direccion,
                               Model model){
        int correcto=1;
        Optional<Usuario> persona = usuarioRepository.findByEmailAndAndRol(usuario.getEmail(), "AdminRestaurante");
        if(bindingResult.hasErrors()){
            correcto=0;
            model.addAttribute("listadistritos",distritosRepository.findAll());
        }
        if(!usuario.getContraseniaHash().equals(password2)){
            correcto=0;
            model.addAttribute("msgcontra","Contrase??as no son iguales");
        }else{
            if(!Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()???[{}]:;',?/*~$^+=<>]).{8,20}$",usuario.getContraseniaHash())){
                correcto=0;
                model.addAttribute("msgcontra", "Contrase??as no cumple con los requisitos");
            }
        }
        if(persona.isPresent()){
            correcto=0;
            model.addAttribute("msgcorreo","Correo ya existe");
        }
        if(!validarDNI(usuario.getDni())){
            correcto=0;
            model.addAttribute("msgdni","DNI no existe");
        }
        if(!(usuario.getSexo().equals("Masculino"))&&!(usuario.getSexo().equals("Femenino"))){
            System.out.println(usuario.getSexo());
            correcto=0;
            model.addAttribute("msgsex","Sexo no existe");
        }
        if(direccion.equals("")||direccion==null){
            correcto=0;
            model.addAttribute("msgdir","Direccion no puede ser nula");
        }
        if(correcto==0) {
            model.addAttribute("direction",direccion);
            model.addAttribute("listadistritos",distritosRepository.findAll());
            return "AdminRestaurantes/register";
        }
        else{
            String contraseniahashbcrypt = BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt());
            usuario.setContraseniaHash(contraseniahashbcrypt);
            usuarioRepository.save(usuario);
            Usuario usuarionuevo = usuarioRepository.findByEmail(usuario.getEmail());
            Direcciones direccionactual = new Direcciones();
            direccion = direccion.split(",")[0];
            direccionactual.setDireccion(direccion);
            Distritos distritosactual = distritosRepository.findById(iddistrito).get();
            direccionactual.setDistrito(distritosactual);
            //direccionactual.setUsuariosIdusuarios(usuarionuevo.getIdusuarios());
            direccionactual.setUsuario(usuarionuevo);
            direccionactual.setActivo(1);
            direccionesRepository.save(direccionactual);
            /* Envio de correo de confirmacion */
            String subject = "Creacion de cuenta";
            String aws = "g-spicyo.publicvm.com";
            String direccionurl = "http://" + aws + ":8080/login";
            String mensaje = "??Hola! Tu cuenta de administrador de restaurante ha sido creada exitosamente. Registra tu restaurante!<br><br>" +
                    "Ahora es parte de Spicyo. Para ingresar a su cuenta haga click: <a href='" + direccionurl + "'>AQU??</a> <br><br>Atte. Equipo de Spicy :D</b>";
            String correoDestino = usuario.getEmail();
            sendMailService.sendMail(correoDestino, "saritaatanacioarenas@gmail.com", subject, mensaje);
            return "AdminRestaurantes/correo";
        }
    }

    @GetMapping(value = {"","/login"})
    public String loginForm(HttpSession session,Authentication auth){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if(sessionUser != null){
            System.out.println("sesion iniciada");
            String nombreocorreo = auth.getName();
            System.out.println(nombreocorreo);
            boolean escorreo = isValid(nombreocorreo);
            if(!escorreo){
                System.out.println("inicio con google");
                return "redirect:/redirectByRol";
            }else{
                System.out.println("inicio con db");
                return "redirect:/redirectByRolDB";
            }
        }else{
            return "login/login";
        }
    }

    @GetMapping("/redirectByRolDB")
    public String redirectByRolDB(Authentication auth, HttpSession session){
        String rol="";
        //setear la ??ltima fecha y hora de ingreso
        for(GrantedAuthority role:auth.getAuthorities()){
            rol= role.getAuthority();
            break;
        }

        String nombreocorreo = auth.getName();
        boolean escorreo = isValid(nombreocorreo);
        if(!escorreo){
            System.out.println("inicio con google");
            return "redirect:/redirectByRol";
        }else{
            Usuario usuarioLogueado= usuarioRepository.findByEmail(auth.getName());
            session.setAttribute("usuarioLogueado",usuarioLogueado);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZoneId zoneId = ZoneId.of("America/Lima");
            LocalDateTime datetime1 = LocalDateTime.now(zoneId);
            String formatDateTime = datetime1.format(format);

            System.out.println(formatDateTime);
            System.out.println(datetime1);

            if (rol.equals("AdminRestaurante")){
                usuarioLogueado.setUltimafechaingreso(datetime1);
                usuarioRepository.save(usuarioLogueado);
                return "redirect:/adminrest/login";
            }else if(rol.equals("AdminSistema")){
                usuarioLogueado.setUltimafechaingreso(datetime1);
                usuarioRepository.save(usuarioLogueado);
                return "redirect:/admin/gestionCuentas";
            } else if(rol.equals("Repartidor")) {
                usuarioLogueado.setUltimafechaingreso(datetime1);
                usuarioRepository.save(usuarioLogueado);
                return "redirect:/repartidor/home";
            }else if(rol.equals("Cliente")){
                usuarioLogueado.setUltimafechaingreso(datetime1);
                usuarioRepository.save(usuarioLogueado);
                return "redirect:/cliente/paginaprincipal";
            }else{
                System.out.println(rol);
                return "/login";
            }
        }

    }

    @GetMapping("/redirectByRol")
    public String redirectByRol(Authentication auth, HttpSession session){
        System.out.println("******TRACER 10**************");
        String rol="";
        for(GrantedAuthority role:auth.getAuthorities()){
            rol= role.getAuthority();
            System.out.println(rol);
            break;
        }

        System.out.println(auth.getName());
        System.out.println("correo logeado por api google");
        try{
            CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
            System.out.println(oauthUser.getEmail());

            Usuario usuarioLogueado= usuarioRepository.getUserByUsername(oauthUser.getEmail());
            session.setAttribute("usuarioLogueado",usuarioLogueado);
            String rol_log = usuarioLogueado.getRol();
            System.out.println("ROL OBTENIDO DEL USUARIO OBTENIDO");
            System.out.println(rol_log);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZoneId zoneId = ZoneId.of("America/Lima");
            LocalDateTime datetime1 = LocalDateTime.now(zoneId);
            String formatDateTime = datetime1.format(format);

            System.out.println(formatDateTime);
            System.out.println(datetime1);

            if (rol_log.equalsIgnoreCase("AdminRestaurante")){
                usuarioLogueado.setUltimafechaingreso(datetime1);
                usuarioRepository.save(usuarioLogueado);
                return "redirect:/adminrest/login";
            }else if(rol_log.equalsIgnoreCase("AdminSistema")){
                usuarioLogueado.setUltimafechaingreso(datetime1);
                usuarioRepository.save(usuarioLogueado);
                return "redirect:/admin/gestionCuentas";
            } else if(rol_log.equalsIgnoreCase("Repartidor")) {
                usuarioLogueado.setUltimafechaingreso(datetime1);

                int repactivado = usuarioLogueado.getCuentaActiva();
                if(repactivado == -1){
                    session.invalidate();
                    return "redirect:/login";
                } else if(repactivado == 1){
                    usuarioRepository.save(usuarioLogueado);
                    return "redirect:/repartidor/home";
                }else{
                    session.invalidate();
                    return "redirect:/login";
                }

            }else if(rol_log.equalsIgnoreCase("Cliente")){
                usuarioLogueado.setUltimafechaingreso(datetime1);
                usuarioRepository.save(usuarioLogueado);
                return "redirect:/cliente/paginaprincipal";
            }else{
                return "login/login";
            }
        }catch(ClassCastException e){
            System.out.println(e.getMessage());
            return "redirect:/login";
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
                subject = "Recuperacion de contrase??a - Spicy";
                // no cambien esto
                //String direccion = "http://localhost:8090/cambiar1/";
                //Pegar aqu?? los datos del AWS;
                String aws = "g-spicyo.publicvm.com";
                String direccion = "http://" + aws + ":8080/cambiar1/";
                URL url = new URL(direccion + token);
                mensaje = "??Hola!<br><br>Para reestablecer su contrase??a haga click: <a href='" + direccion + token + "'>AQU??</a> <br><br>Atte. Equipo de Spicy :D</b>";
                attr.addFlashAttribute("msg", "??Revisa tu correo para continuar el proceso! :D");
                optionalUsuario.get().setToken(token);
            } else {
                subject = "Invitacion de registro - Spicy";
                mensaje = "??Hola!<br>No est?? registrado en Spicy :( <br><br>Atte. Equipo de Spicy :D</b>";
                attr.addFlashAttribute("msg2", "??No estas registrado! :(");
            }
            sendMailService.sendMail(correoDestino, "saritaatanacioarenas@gmail.com", subject, mensaje);
            return "redirect:/login";
        } else {
            attr.addFlashAttribute("msg2", "??Ingresa un formato email! :(");
            return "redirect:/login";
        }
    }

    //aqu?? se ingresa la contrase??a
    @GetMapping(value = "/cambiar1/{token}") //formato que espero el usuario coloque en URL
    public String cambiar1(@PathVariable("token") String tokenObtenido, Model model, RedirectAttributes attr) {
        Usuario usuario = new Usuario();
        usuario.setToken(tokenObtenido);
        Optional<Usuario> usuarioToken = Optional.ofNullable(usuarioRepository.findByToken(usuario.getToken()));
        if (usuarioToken.isPresent()) {
            model.addAttribute("usuario", usuario);
            return "login/cambiar1";
        }else {
            attr.addFlashAttribute("msg2", "??Error en el token o expirado! debes generar otro :(");
            return "redirect:/login";
        }
    }

    public  boolean validarContrasenia(String contrasenia1) {
        /*      https://mkyong.com/regular-expressions/how-to-validate-password-with-regular-expression/
                A!@#&()???a1
                A[{}]:;',?/*a1
                A~$^+=<>a1
                0123456789$abcdefgAB
                123Aa$Aa
         */
        System.out.println(contrasenia1);
        Pattern pattern1 = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[%!@#&()???[{}]:;',?/*~$^+=<>]).{8,20}$");
        Matcher matcher1 = pattern1.matcher(contrasenia1);
        System.out.println(matcher1.matches());
        return matcher1.matches();
    }

    @PostMapping("/cambiarContrasenia")
    public String cambiarContrasenia(Usuario usuario, RedirectAttributes attr, @RequestParam("contrasenia") String contrasenia, @RequestParam("contrasenia2") String contrasenia2) {

        Optional<Usuario> usuarioToken = Optional.ofNullable(usuarioRepository.findByToken(usuario.getToken()));
        if (usuarioToken.isPresent()) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (contrasenia == "" || contrasenia2 == "") {
                attr.addFlashAttribute("msg2", "??Contrase??a no puede ser nula! Intenta nuevamente con el link enviado al correo :C");
            } else if (contrasenia.equals(contrasenia2)){
                Boolean validacionContrasenias = validarContrasenia(contrasenia);
                if (validacionContrasenias==true) {
                    String contraseniahashbcrypt = BCrypt.hashpw(contrasenia, BCrypt.gensalt());
                    usuarioToken.get().setContraseniaHash(contraseniahashbcrypt);
                    attr.addFlashAttribute("msg", "??Contrase??a cambiada! :D");
                    SecureRandom random = new SecureRandom();
                    byte bytes[] = new byte[20];
                    random.nextBytes(bytes);
                    String tokenNuevo = bytes.toString();
                    usuarioToken.get().setToken(tokenNuevo);
                    usuarioRepository.save(usuarioToken.get());
                } else {
                    attr.addFlashAttribute("msg2", "??Debe tener al menos 8 caracteres, uno especial y una mayuscula");
                }
                return "redirect:/login";
            } else{
                attr.addFlashAttribute("msg2", "??Las contrase??as no coinciden!");
                return "redirect:/login";
            }
            return "redirect:/login";
        } else {
            attr.addFlashAttribute("msg2", "??Error en el token o expirado! debes generar otro :(");
            return "login/olvidoContrasenia";
        }
    }

    ////////////////////////////////////////// API - Validacion DNI ///////////////////////
    public boolean validarDNI(String dni){
        Boolean dniValido = false;

        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        try{

            // reemplazar DNI
            String urlString = "https://api.ateneaperu.com/api/reniec/dni?sNroDocumento="+dni;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if(status > 299){
                System.out.println("EROR PAPU");
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                System.out.println(connection.getResponseMessage());
                System.out.println(connection.getResponseCode());
                System.out.println(connection.getErrorStream());
                reader.close();
            } else {
                System.out.println("/GET");
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            }
            System.out.println(responseContent.toString());
            JSONObject jsonObj = new JSONObject(responseContent.toString());
            //System.out.println(jsonObj.get("nombres"));

            // Validar si existe documento
            if(!jsonObj.get("nombres").equals("")){
                System.out.println("DNI valido");
                dniValido = true;
            }else{
                System.out.println("NO SE ENCONTRO POR LA API");
            }

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dniValido;
    }

    public boolean isValid(String email) {
        String emailREGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailREGEX );
        if (email == null){
            return false;
        }
        return pattern .matcher(email).matches();
    }
}