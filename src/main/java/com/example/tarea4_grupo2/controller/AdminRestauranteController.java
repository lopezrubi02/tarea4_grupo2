package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import com.example.tarea4_grupo2.service.SendMailService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/adminrest")
public class AdminRestauranteController {

    @Autowired
    PlatoRepository platoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    RestauranteRepository restauranteRepository;
    @Autowired
    CuponesRepository cuponesRepository;
    @Autowired
    PedidosRepository pedidosRepository;
    @Autowired
    SendMailService sendMailService;
    @Autowired
    CategoriasRepository categoriasRepository;
    @Autowired
    DistritosRepository distritosRepository;
    @Autowired
    DireccionesRepository direccionesRepository;
    @Autowired
    FotosPlatosRepository fotosPlatosRepository;

    @GetMapping("/login")
    public String loginAdmin(HttpSession session){

        /*Se obtiene Id de Usuario*/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int id = sessionUser.getIdusuarios();
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if (usuarioOptional.isPresent()){
            Usuario nuevoUsuario = usuarioOptional.get();
            System.out.println(id);

            if(nuevoUsuario.getCuentaActiva() == 3){
                return "AdminRestaurantes/sinRestaurante";

            }else if(nuevoUsuario.getCuentaActiva() == 2){

                return "AdminRestaurantes/espera";

            }else if(nuevoUsuario.getCuentaActiva() == 1){

                Optional<Restaurante> restauranteOpt = restauranteRepository.buscarRestaurantePorIdAdmin(id);

                if(restauranteOpt.isPresent()){
                    return "redirect:/adminrest/perfil";
                }else{
                    return "AdminRestaurantes/sinRestaurante";
                }

            }else{
                return "login/login";
            }
        }else{
            System.out.println("Revisar, esto no deberia pasar");
            return "redirect:/login";
        }
    }

    /************************RESTAURANTE************************/

    @PostMapping("/categorias")
    public String esperaConfirmacion(@ModelAttribute("restaurante") @Valid Restaurante restaurante,BindingResult bindingResult,
                                     @RequestParam("imagen") MultipartFile file,
                                     @RequestParam("iddistrito") Integer iddistrito,
                                     @RequestParam("direccion_real") String direccion,
                                     Model model,
                                     HttpSession session) throws IOException {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        int correcto = 1;
        try {
            restaurante.setFoto(file.getBytes());
            restaurante.setFotocontenttype(file.getContentType());
            restaurante.setFotonombre(file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Direccion: ");
        System.out.println(direccion);

        if(bindingResult.hasFieldErrors("nombre")||bindingResult.hasFieldErrors("ruc")){

            restaurante.setUsuario(sessionUser);
            model.addAttribute("listadistritos",distritosRepository.findAll());
            model.addAttribute("restaurante",restaurante);
            model.addAttribute("direction",direccion);

            if(file.isEmpty()){
                correcto = 0;
                model.addAttribute("msgSinFoto", "Adjunte una imagen.");
            }else{
                if (!((file.getContentType().contains("jpeg")) || (file.getContentType().contains("jpg")) || (file.getContentType().contains("png")) ||
                        (file.getContentType().contains("JPEG")) || (file.getContentType().contains("JPG")) || (file.getContentType().contains("PNG")))) {
                    correcto = 0;
                    model.addAttribute("msgFotoInvalida", "La foto no contiene el formato adecuado. Se aceptan solo archivos .jpeg, .jpg, .png");
                }
            }

            if(!(restauranteRepository.findRestauranteByRuc(restaurante.getRuc()).isPresent())){
                if (!(restaurante.getRuc().startsWith("20") || restaurante.getRuc().startsWith("10"))) {
                    correcto = 0;
                    model.addAttribute("msgrucerror","No es un RUC valido.");
                }else{
                    if(!(validarRUC(restaurante.getRuc()))){
                        correcto = 0;
                        model.addAttribute("msgrucerror","No es un RUC registrado.");
                    }
                }
            }else{
                correcto = 0;
                model.addAttribute("msgrucerror","Ya existe un restaurante registrado con este RUC.");
            }
            if(direccion.equals("")){
                correcto = 0;
                model.addAttribute("direccionNula","La direccion no puede ser nula.");
            }

            return "AdminRestaurantes/registerRestaurante";

        }else{

            if(file.isEmpty()){
                correcto = 0;
                model.addAttribute("msgSinFoto", "Adjunte una imagen.");
            }else{
                if (!((file.getContentType().contains("jpeg")) || (file.getContentType().contains("jpg")) || (file.getContentType().contains("png")) ||
                        (file.getContentType().contains("JPEG")) || (file.getContentType().contains("JPG")) || (file.getContentType().contains("PNG")))) {
                    correcto = 0;
                    model.addAttribute("msgFotoInvalida", "La foto no contiene el formato adecuado. Se aceptan solo archivos .jpeg, .jpg, .png");
                }
            }

            if(!(restauranteRepository.findRestauranteByRuc(restaurante.getRuc()).isPresent())){
                if (!(restaurante.getRuc().startsWith("20") || restaurante.getRuc().startsWith("10"))) {
                    correcto = 0;
                    model.addAttribute("msgrucerror","No es un RUC valido.");
                }else{
                    if(!(validarRUC(restaurante.getRuc()))){
                        correcto = 0;
                        model.addAttribute("msgrucerror","No es un RUC registrado.");
                    }
                }
            }else{
                correcto = 0;
                model.addAttribute("msgrucerror","Ya existe un restaurante registrado con este RUC.");
            }

            if(direccion.equals("")){
                correcto = 0;
                model.addAttribute("direccionNula","La direccion no puede ser nula.");
            }

            if(correcto == 1){
                try {
                    restaurante.setFoto(file.getBytes());
                    restaurante.setFotocontenttype(file.getContentType());
                    restaurante.setFotonombre(file.getOriginalFilename());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                direccion = direccion.split(",")[0];
                restaurante.setDireccion(direccion);
                Distritos distrito = distritosRepository.findById(iddistrito).get();
                restaurante.setDistrito(distrito);
                Optional<Restaurante> OptRestaurante = restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios());
                if(!(OptRestaurante.isPresent())){
                    restauranteRepository.save(restaurante);
                }else{
                    restauranteRepository.deleteById(OptRestaurante.get().getIdrestaurante());
                    restauranteRepository.save(restaurante);
                }
                sessionUser.setCuentaActiva(2);
                usuarioRepository.save(sessionUser);
                model.addAttribute("id", restaurante.getIdrestaurante());
                model.addAttribute("listacategorias", categoriasRepository.findAll());
                return "AdminRestaurantes/categorias";
            }else{
                restaurante.setUsuario(sessionUser);
                model.addAttribute("direction",direccion);
                model.addAttribute("listadistritos",distritosRepository.findAll());
                model.addAttribute("restaurante",restaurante);
                return "AdminRestaurantes/registerRestaurante";
            }
        }
    }

    @GetMapping("/registerRestaurante")
    public String registerRestaurante(@ModelAttribute("restaurante")Restaurante restaurante, Model model,HttpSession session){
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        restaurante.setUsuario(sessionUser);
        model.addAttribute("listadistritos",distritosRepository.findAll());
        model.addAttribute("restaurante",restaurante);
        String direction=null;
        model.addAttribute("direction",direction);
        return "AdminRestaurantes/registerRestaurante";
    }

    @GetMapping("/borrarRestaurante")
    public String borrarRestaurante(HttpSession session){
        Usuario user= (Usuario) session.getAttribute("usuarioLogueado");
        int id=restauranteRepository.buscarRestaurantePorIdAdmin(user.getIdusuarios()).get().getIdrestaurante();
        restauranteRepository.deleteById(id);
        user.setCuentaActiva(3);
        usuarioRepository.save(user);
        return "redirect:/adminrest/sinrestaurante";
    }

    @PostMapping("/llenarcategoria")
    public String llenarcategorias(@ModelAttribute("restaurante") Restaurante restaurante,Model model){
        Optional<Restaurante> optional = restauranteRepository.findById(restaurante.getIdrestaurante());
        optional.get().setCategoriasrestList(restaurante.getCategoriasrestList());
        restauranteRepository.save(optional.get());
        model.addAttribute("id",optional.get().getIdrestaurante());
        return "AdminRestaurantes/espera";
    }

    @GetMapping("/sinrestaurante")
    public String sinRestaurante(){
        return "AdminRestaurantes/sinRestaurante";
    }

    public boolean validarRUC(String dni){
        Boolean rucValido = false;

        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        try{

            String urlString = "https://api.ateneaperu.com/api/Sunat/Ruc?sNroDocumento="+dni;

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

            if((!jsonObj.get("nombre_o_razon_social").equals("")) && (!jsonObj.get("nombre_o_razon_social").equals(null))){
                System.out.println("RUC valido");
                rucValido = true;
            }

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rucValido;
    }

    @PostMapping("/guardarrestedit")
    public String editPerfilUsuario(@ModelAttribute("restaurante") Restaurante restaurante,
                                    HttpSession session,
                                    @RequestParam("imagen") MultipartFile file,
                                    Model model){
        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        Restaurante rest = restauranteRepository.buscarRestaurantePorIdAdmin(user.getIdusuarios()).get();
        try {
            rest.setFoto(file.getBytes());
            rest.setFotocontenttype(file.getContentType());
            rest.setFotonombre(file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        restauranteRepository.save(rest);
        return "redirect:/adminrest/cuentaAdmin";
    }

    @GetMapping("/categoriaedit")
    public String llenarcategoriasedit(@ModelAttribute("restaurante") Restaurante restaurante,Model model,HttpSession session){
        Usuario usuario= (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("restaurante",restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()));
        model.addAttribute("listacategorias",categoriasRepository.findAll());
        model.addAttribute("numero",restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getCategoriasrestList().size());
        System.out.println(restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getCategoriasrestList().size());
        return "AdminRestaurantes/categoriasedit";
    }

    @PostMapping("/guardarcategoriaedit")
    public String nuevascategorias(@ModelAttribute("restaurante") Restaurante restaurante, HttpSession session){
        Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado");
        Restaurante rest= restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get();
        rest.setCategoriasrestList(restaurante.getCategoriasrestList());
        restauranteRepository.save(rest);
        return "redirect:/adminrest/cuentaAdmin";
    }

    /************************FOTOS************************/

    @GetMapping("/imagen")
    public ResponseEntity<byte[]> imagenRestaurante(HttpSession session) {

        /**Se obtiene Id de Restaurante**/
        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        Integer id_rest=restauranteRepository.buscarRestaurantePorIdAdmin(user.getIdusuarios()).get().getIdrestaurante();
        /********************************/

        Optional<Restaurante> optional = restauranteRepository.findById(id_rest);
        if (optional.isPresent()) {
            byte[] imagen = optional.get().getFoto();
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(optional.get().getFotocontenttype()));
            return new ResponseEntity<>(imagen,httpHeaders, HttpStatus.OK);
        }
        return null;
    }

    @GetMapping("/imagenplato")
    public ResponseEntity<byte[]> imagenPlato(HttpSession session, @RequestParam("idplato") Integer idplato) {

        Optional<FotosPlatos> optFoto = fotosPlatosRepository.encontrarIdPlato(idplato);
        if (optFoto.isPresent()) {
            byte[] imagen = optFoto.get().getFoto();
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(optFoto.get().getFotocontenttype()));
            return new ResponseEntity<>(imagen,httpHeaders, HttpStatus.OK);
        }
        return null;
    }

    /************************PERFIL************************/

    @GetMapping("/perfil")
    public String perfilRestaurante(Model model, HttpSession session){

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
        Restaurante restaurante = restauranteRepository.findById(idrestaurante).get();
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(sessionUser.getIdusuarios());
        if(usuarioOptional.isPresent()){
            Usuario nuevoUsuario = usuarioOptional.get();
            if(nuevoUsuario.getCuentaActiva()==1){
                /********************************/
                BigDecimal calificacion = pedidosRepository.calificacionPromedio(idrestaurante);
                if(calificacion != null){
                    MathContext m = new MathContext(3);
                    calificacion = calificacion.round(m);
                    model.addAttribute("calificacionpromedio",calificacion);
                }else{
                    model.addAttribute("calificacionpromedio","No hay calificaciones");
                }
                Integer cantidadcalificaciones = restauranteRepository.obtenerCantidadCalificaciones(idrestaurante);
                model.addAttribute("cantidadcalificaciones",cantidadcalificaciones);
                model.addAttribute("nombrerestaurante", restaurante.getNombre());
                return "AdminRestaurantes/perfilrestaurante";
            }
            else{
                return "redirect:/adminrest/login";
            }
        }else{
            System.out.println("Revisar, esto no deberia pasar");
            return "redirect:/login";
        }
    }

    /************************PLATOS************************/

    @GetMapping("/menu")
    public String verMenu(Model model, HttpSession session, RedirectAttributes attr){

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(sessionUser.getIdusuarios());
        if(usuarioOptional.isPresent()) {
            Usuario usuarioNuevo = usuarioOptional.get();
            if(usuarioNuevo.getCuentaActiva()==1){
                Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
                List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
                if(listaPlatos.isEmpty()){
                    attr.addFlashAttribute("sinPlatosCreados","No tienes ningún plato registrado. Crea uno aquí.");
                    return "redirect:/adminrest/perfil";
                }
                /********************************/
                model.addAttribute("iddelrestaurante", idrestaurante);
                model.addAttribute("listaPlatos", listaPlatos);
                model.addAttribute("cantidadPlatos", listaPlatos.size());
                return "AdminRestaurantes/menu";
            }
            else{
                return "redirect:/adminrest/login";
            }
        }else {
            return "redirect:/login";
        }
    }

    @GetMapping("/crearPlato")
    public String crearPlato(@ModelAttribute("plato") Plato plato, Model model, HttpSession session) {

        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        Integer idrestaurante = restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(sessionUser.getIdusuarios());
        if (usuarioOptional.isPresent()) {
            Usuario usuarioNuevo = usuarioOptional.get();
            if (usuarioNuevo.getCuentaActiva() == 1) {
                model.addAttribute("plato", plato);
                model.addAttribute("iddelrestaurante", idrestaurante);
                return "AdminRestaurantes/newPlato";
            }else{
                return "redirect:/adminrest/login";
            }
        }else {
            return "redirect:/login";
        }
    }

    @GetMapping("/editarPlato")
    public String editarPlato(Model model, @RequestParam("idplato") String idString, @ModelAttribute("plato") Plato plato) throws IOException{

        try{
            int id = Integer.parseInt(idString);
            Optional<Plato> optionalPlato = platoRepository.findById(id);
            if(optionalPlato.isPresent()){
                plato = optionalPlato.get();
                model.addAttribute("plato",plato);
                return "AdminRestaurantes/newPlato";
            }else{
                return "redirect:/adminrest/menu";
            }
        }catch (NumberFormatException e) {
            e.printStackTrace();
            return "redirect:/adminrest/menu";
        }
    }

    @PostMapping("/guardarPlato")
    public String guardarPlato (@ModelAttribute("plato") @Valid Plato plato, BindingResult bindingResult, Model model, RedirectAttributes attr,
                               @RequestParam( name="imagen", required = false) MultipartFile file, HttpSession session) throws IOException {

        if(bindingResult.hasErrors()) {

            /**Se obtiene Id de Restaurante**/
            Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            /********************************/
            model.addAttribute("plato",plato);
            model.addAttribute("iddelrestaurante", idrestaurante);

            if(file.isEmpty()){
                model.addAttribute("msgFotoInvalida", "Adjunte una imagen.");
            }else{
                if(!( (file.getContentType().contains("jpeg")) || (file.getContentType().contains("jpg")) || (file.getContentType().contains("png")) ||
                        (file.getContentType().contains("JPEG")) || (file.getContentType().contains("JPG")) || (file.getContentType().contains("PNG")))) {
                    model.addAttribute("msgFotoInvalida", "La foto no contiene el formato adecuado. Se aceptan solo archivos .jpeg, .jpg, .png");
                    return "AdminRestaurantes/newPlato";
                }
            }

            return "AdminRestaurantes/newPlato";

        }else {
            /**Se obtiene Id de Restaurante**/
            Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            /********************************/

            if (plato.getIdplato() == 0) {

                if (!(file.isEmpty())) {
                    System.out.println("Hay archivo");
                    if(!( (file.getContentType().contains("jpeg")) || (file.getContentType().contains("jpg")) || (file.getContentType().contains("png")) ||
                            (file.getContentType().contains("JPEG")) || (file.getContentType().contains("JPG")) || (file.getContentType().contains("PNG")))) {
                        model.addAttribute("msgFotoInvalida", "La foto no contiene el formato adecuado. Se aceptan solo archivos .jpeg, .jpg, .png");
                        model.addAttribute("plato",plato);
                        model.addAttribute("iddelrestaurante", idrestaurante);
                        return "AdminRestaurantes/newPlato";
                    }

                    String nombreplato = plato.getNombre();
                    platoRepository.save(plato);
                    Optional<Plato> platoguardado = platoRepository.buscarPlato(nombreplato);

                    if (platoguardado.isPresent()) {
                        System.out.println("Plato guardado, se intenta guardar foto");
                        Plato plato1 = platoguardado.get();
                        FotosPlatos fotosPlatos = new FotosPlatos();
                        fotosPlatos.setIdplato(plato1);
                        try {
                            System.out.println("Intentando");
                            fotosPlatos.setFoto(file.getBytes());
                            fotosPlatos.setFotocontenttype(file.getContentType());
                            fotosPlatos.setFotonombre(file.getOriginalFilename());
                        }catch (MaxUploadSizeExceededException exc){
                            System.out.println("Excepcion de tamano");
                            model.addAttribute("plato",plato);
                            model.addAttribute("iddelrestaurante", idrestaurante);
                            model.addAttribute("msgFotoInvalida", "El archivo es muy grande.");
                            return "AdminRestaurantes/newPlato";
                        }catch(IOException e) {
                                System.out.println("ARCHIVO NO COMPATIBLE");
                                e.printStackTrace();
                                platoRepository.deleteById(plato1.getIdplato());
                                model.addAttribute("plato",plato);
                                model.addAttribute("iddelrestaurante", idrestaurante);
                                model.addAttribute("msgFotoInvalida", "La foto no contiene el formato adecuado. Elija otra imagen.");
                                return "AdminRestaurantes/newPlato";
                        }
                        System.out.println("GUARDANDO FOTOSPLATOS");
                        fotosPlatosRepository.save(fotosPlatos);
                        attr.addFlashAttribute("msg1", "Plato creado exitosamente");

                    }else{

                        model.addAttribute("plato",plato);
                        model.addAttribute("iddelrestaurante", idrestaurante);
                        model.addAttribute("msgFotoInvalida", "La foto no contiene el formato adecuado. Elija otra imagen.");
                        return "AdminRestaurantes/newPlato";

                    }

                    return "redirect:/adminrest/menu";

                }else{

                    model.addAttribute("plato",plato);
                    model.addAttribute("iddelrestaurante", idrestaurante);
                    model.addAttribute("msgFotoInvalida", "Adjunte una imagen.");
                    return "AdminRestaurantes/newPlato";

                }

        }else{

                platoRepository.save(plato);

                if (!(file.isEmpty())) {

                    Optional<FotosPlatos> optFoto = fotosPlatosRepository.encontrarIdPlato(plato.getIdplato());

                    if (optFoto.isPresent()) {

                        FotosPlatos fotosPlatos = optFoto.get();
                        try {
                            fotosPlatos.setFoto(file.getBytes());
                            fotosPlatos.setFotocontenttype(file.getContentType());
                            fotosPlatos.setFotonombre(file.getOriginalFilename());
                        } catch (IOException e) {
                            e.printStackTrace();
                            model.addAttribute("plato",plato);
                            model.addAttribute("iddelrestaurante", idrestaurante);
                            model.addAttribute("msgFotoInvalida", "La foto no contiene el formato adecuado. Elija otra imagen.");
                            return "AdminRestaurantes/newPlato";
                        }
                        fotosPlatosRepository.save(fotosPlatos);

                    }else{

                        FotosPlatos fotosPlatos = new FotosPlatos();
                        fotosPlatos.setIdplato(plato);
                        try {
                            fotosPlatos.setFoto(file.getBytes());
                            fotosPlatos.setFotocontenttype(file.getContentType());
                            fotosPlatos.setFotonombre(file.getOriginalFilename());
                        } catch (IOException e) {
                            e.printStackTrace();
                            model.addAttribute("plato",plato);
                            model.addAttribute("iddelrestaurante", idrestaurante);
                            model.addAttribute("msgFotoInvalida", "La foto no contiene el formato adecuado. Elija otra imagen.");
                            return "AdminRestaurantes/newPlato";
                        }
                        fotosPlatosRepository.save(fotosPlatos);
                    }
                }
                attr.addFlashAttribute("msg2", "Plato actualizado exitosamente");
                return "redirect:/adminrest/menu";
            }
        }
    }

    @GetMapping("/borrarPlato")
    public String borrarPlato(@RequestParam("idplato") int id, RedirectAttributes attr){

        Optional<Plato> optionalPlato = platoRepository.findById(id);

        if(optionalPlato.isPresent()){
            Plato platoBorrar = optionalPlato.get();
            if(platoBorrar.getIdplato() != 0){
                platoBorrar.setActivo(0);
                platoRepository.save(platoBorrar);
                attr.addFlashAttribute("msg", "Producto borrado exitosamente");
            }
            return "redirect:/adminrest/menu";
        }else{
            return "redirect:/adminrest/menu";
        }
    }

    /************************CUPONES************************/

    @GetMapping("/cupones")
    public String verCupones(Model model,@RequestParam(name = "page", defaultValue = "1") String requestedPage,HttpSession session, RedirectAttributes attr){
        Usuario usuario= (Usuario) session.getAttribute("usuarioLogueado");

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuario.getIdusuarios());
        if(usuarioOptional.isPresent()) {
            Usuario usuarioNuevo = usuarioOptional.get();
            if(usuarioNuevo.getCuentaActiva()==1){
                float numberOfUsersPerPage = 7;
                int page = Integer.parseInt(requestedPage);
                int idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getIdrestaurante();
                List<Cupones> listaCupones = cuponesRepository.buscarCuponesPorIdRestaurante(idrestaurante);

                if(!(listaCupones.isEmpty())) {

                    List<String> listaDisponibilidad = new ArrayList<String>();
                    int numberOfPages = (int) Math.ceil(listaCupones.size() / numberOfUsersPerPage);
                    if (page > numberOfPages) {
                        page = numberOfPages;
                    } // validation

                    int start = (int) numberOfUsersPerPage * (page - 1);
                    int end = (int) (start + numberOfUsersPerPage);

                    List<Cupones> listOfCuponesPage = listaCupones.subList(start, Math.min(end, listaCupones.size()));
                    System.out.println("TRACE3");
                    for (Cupones i : listOfCuponesPage) {
                        Date inicio = i.getFechainicio();
                        Date fin = i.getFechafin();
                        Date ahora = Date.valueOf(LocalDate.now());

                        if (inicio.compareTo(ahora) > 0) {
                            listaDisponibilidad.add("No");
                        } else if (fin.compareTo(ahora) < 0) {
                            listaDisponibilidad.add("No");
                        } else if ((inicio.compareTo(ahora) < 0) && (fin.compareTo(ahora) > 0)) {
                            listaDisponibilidad.add("Si");
                        } else {
                            listaDisponibilidad.add("No");
                        }
                    }
                    model.addAttribute("listaCupones", listOfCuponesPage);
                    model.addAttribute("listaDisponibilidad", listaDisponibilidad);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("maxNumberOfPages", numberOfPages);
                    return "AdminRestaurantes/cupones";
                }
                attr.addFlashAttribute("NoHayCupones","No tienes cupones activos. Crea uno");
                return "redirect:/adminrest/perfil";
            }
            else{
                return "redirect:/adminrest/login";
            }
        }else {
            return "redirect:/login";
        }
    }

    @GetMapping("/crearCupon")
    public String crearCupon(@ModelAttribute("cupon") Cupones cupon, Model model,HttpSession session, RedirectAttributes attr){
        Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado");
        int idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getIdrestaurante();
        Restaurante restaurante = new Restaurante();
        restaurante.setIdrestaurante(idrestaurante);
        cupon.setRestaurante(restaurante);
        model.addAttribute("cupon",cupon);
        List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
        if(listaPlatos.isEmpty()){
            attr.addFlashAttribute("sinPlatos","No puedes crear un cupon si no tienes ningun plato registrado.");
            return "redirect:/adminrest/perfil";
        }
        model.addAttribute("listaPlatos",listaPlatos);
        model.addAttribute("index",0);
        return "AdminRestaurantes/generarCupon";
    }

    @GetMapping("/editarCupon")
    public String editarCupon(@ModelAttribute("cupon") Cupones cupon, @RequestParam("idcupon") int id, Model model){
        Optional<Cupones> optCupon = cuponesRepository.findById(id);
        if(optCupon.isPresent()){
            cupon = optCupon.get();
            Restaurante restaurante = cupon.getRestaurante();
            int idrestaurante = restaurante.getIdrestaurante();
            model.addAttribute("cupon",cupon);
            List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
            model.addAttribute("index",0);
            model.addAttribute("listaPlatos",listaPlatos);
            return "AdminRestaurantes/generarCupon";
        }else{
            return "redirect:/adminrest/cupones";
        }
    }

    @GetMapping("/borrarCupon")
    public String borrarCupon(@RequestParam("idcupon") int id, RedirectAttributes attr){

        Optional<Cupones> optCupon = cuponesRepository.findById(id);

        if(optCupon.isPresent()){
            cuponesRepository.deleteById(id);
            attr.addFlashAttribute("msg3", "Cupon borrado exitosamente");
            return "redirect:/adminrest/cupones";
        }else{
            return "redirect:/adminrest/cupones";
        }
    }

    @PostMapping("/guardarCupon")
    public String guardarCupon(@ModelAttribute("cupon") @Valid Cupones cupon, BindingResult bindingResult, RedirectAttributes attr,
                               Model model, HttpSession session,@RequestParam("index") int index){

        if(bindingResult.hasErrors()){
            /**Se obtiene Id de Restaurante**/
            Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            /********************************/
            model.addAttribute("cupon",cupon);
            List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
            model.addAttribute("listaPlatos",listaPlatos);
            if(cupon.getValordescuento() > cupon.getPlato().getPrecio()){
                model.addAttribute("msg3", "El precio del descuento no puede ser mayor al precio del plato");

            }
            Date inicio = cupon.getFechainicio();
            Date fin = cupon.getFechafin();
            Date ahora = Date.valueOf(LocalDate.now());
            if( (inicio != null) && (fin != null)) {
                if (fin.compareTo(inicio) < 0) {
                    model.addAttribute("msg4", "La fecha de fin no puede ser antes que la fecha de inicio del cupon.");
                }
            }
            model.addAttribute("index",index);
            return "AdminRestaurantes/generarCupon";

        }else {

            if (cupon.getIdcupones() == 0) {

                if(cupon.getValordescuento() > cupon.getPlato().getPrecio()){
                    model.addAttribute("msg3", "El precio del descuento no puede ser mayor al precio del plato");
                    /**Se obtiene Id de Restaurante**/
                    Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
                    Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
                    /********************************/
                    model.addAttribute("cupon",cupon);
                    List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
                    model.addAttribute("listaPlatos",listaPlatos);
                    Date inicio = cupon.getFechainicio();
                    Date fin = cupon.getFechafin();
                    if(fin.compareTo(inicio) < 0){
                        model.addAttribute("msg4", "La fecha de fin no puede ser antes que la fecha de inicio del cupon.");
                    }
                    model.addAttribute("index",index);
                    return "AdminRestaurantes/generarCupon";
                }
                Date inicio = cupon.getFechainicio();
                Date fin = cupon.getFechafin();
                if(fin.compareTo(inicio) < 0){
                    /**Se obtiene Id de Restaurante**/
                    Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
                    Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
                    /********************************/
                    model.addAttribute("cupon",cupon);
                    List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
                    model.addAttribute("listaPlatos",listaPlatos);
                    model.addAttribute("msg4", "La fecha de fin no puede ser antes que la fecha de inicio del cupon.");
                    model.addAttribute("index",index);
                    return "AdminRestaurantes/generarCupon";
                }

                cuponesRepository.save(cupon);
                attr.addFlashAttribute("msg1", "Cupon creado exitosamente");
                return "redirect:/adminrest/cupones";

            } else {

                cuponesRepository.save(cupon);
                attr.addFlashAttribute("msg2", "Cupon actualizado exitosamente");
                return "redirect:/adminrest/cupones";
            }
        }
    }

    /************************CALIFICACIONES************************/

    @GetMapping("/calificaciones")
    public String verCalificaciones(Model model, @RequestParam(name = "page", defaultValue = "1") String requestedPage,
                                    @RequestParam(name = "searchfield", defaultValue = "") String searchField,HttpSession session, RedirectAttributes attr){
        Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado");
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuario.getIdusuarios());
        if(usuarioOptional.isPresent()) {
            Usuario usuarioNuevo = usuarioOptional.get();
            if (usuarioNuevo.getCuentaActiva() == 1) {
                float numberOfUsersPerPage = 7;
                int page = Integer.parseInt(requestedPage);
                int idrestaurante = restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getIdrestaurante();
                List<ComentariosDto> comentariosList;
                if (searchField.equals("")) {
                    comentariosList = pedidosRepository.comentariosUsuarios(idrestaurante);
                } else {
                    comentariosList = pedidosRepository.buscarComentariosUsuarios(searchField, idrestaurante);
                }

                if (!(comentariosList.isEmpty())) {
                    int numberOfPages = (int) Math.ceil(comentariosList.size() / numberOfUsersPerPage);
                    if (page > numberOfPages) {
                        page = numberOfPages;
                    } // validation

                    int start = (int) numberOfUsersPerPage * (page - 1);
                    int end = (int) (start + numberOfUsersPerPage);

                    List<ComentariosDto> lisOfComentariosPage = comentariosList.subList(start, Math.min(end, comentariosList.size()));

                    model.addAttribute("listaComentarios", lisOfComentariosPage);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("maxNumberOfPages", numberOfPages);
                    model.addAttribute("searchfield", searchField);
                    return "AdminRestaurantes/calificaciones";
                } else {
                    attr.addFlashAttribute("sinCalificaciones","No tienes ninguna calificacion hasta la fecha.");
                    return "redirect:/adminrest/perfil";
                }
            }else {
                return "redirect:/adminrest/login";
            }
        }else{
            return "redirect:/login";
        }
    }

    @PostMapping("/buscarCalificaciones")
    public String buscarCalificaciones(@RequestParam("searchfield") String searchField,
                                       RedirectAttributes attr){
        attr.addAttribute("searchfield",searchField);
        return "redirect:/adminrest/calificaciones";
    }

    /************************EXCEL************************/

    private static CellStyle createVHCenterStyle(final Workbook wb) {
        CellStyle style = wb.createCellStyle (); // objeto de estilo
        style.setVerticalAlignment (VerticalAlignment.CENTER); // vertical
        style.setAlignment (HorizontalAlignment.CENTER); // horizontal
        style.setWrapText (true); // Especifica el salto de línea automático cuando no se puede mostrar el contenido de la celda
        // agregar borde
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createHeadStyle(final Workbook wb) {
        CellStyle style = createVHCenterStyle(wb);
        final Font font = wb.createFont();
        font.setFontName ("Songti");
        font.setFontHeight((short) 150);
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    @GetMapping("/excelexportar")
    public ResponseEntity<InputStreamResource> exportAllData(HttpSession session) throws Exception {

        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        int id=restauranteRepository.buscarRestaurantePorIdAdmin(user.getIdusuarios()).get().getIdrestaurante();

        ByteArrayInputStream stream2 = exportReporte(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reportes.xls");
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(stream2));
    }

    public ByteArrayInputStream exportReporte(int id) throws IOException {
        Workbook workbook = new HSSFWorkbook();
        CellStyle headStyle = createHeadStyle(workbook);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(!pedidosRepository.listaPedidosReporteporFechamasantigua(id).isEmpty()) {
            String[] columnsPedido = {"N° PEDIDO", "FECHA DEL PEDIDO", "NOMBRE DEL CLIENTE", "MONTO DEL PEDIDO", "NOMBRE DEL PLATO", "METODO DE PAGO", "DISTRITO DEL PEDIDO"};
            Sheet sheet1 = workbook.createSheet("Pedidos");
            Row row1 = sheet1.createRow(0);
            for (int i = 0; i < columnsPedido.length; i++) {
                Cell cell = row1.createCell(i);
                cell.setCellValue(columnsPedido[i]);
                cell.setCellStyle(headStyle);
            }
            List<PedidosReporteDto> listaPedidos = pedidosRepository.listaPedidosReporteporFechamasantigua(id);
            int initRow = 1;
            for (PedidosReporteDto pedido : listaPedidos) {
                row1 = sheet1.createRow(initRow);
                row1.createCell(0).setCellValue(pedido.getnumeropedido());
                row1.createCell(1).setCellValue(String.valueOf(pedido.getfechahorapedido()));
                row1.createCell(2).setCellValue(pedido.getnombre());
                row1.createCell(3).setCellValue(pedido.getmontototal());
                row1.createCell(4).setCellValue(pedido.getnombreplato());
                row1.createCell(5).setCellValue(pedido.getmetodo());
                row1.createCell(6).setCellValue(pedido.getdistrito());
                initRow++;
            }
            sheet1.autoSizeColumn(0);
            sheet1.autoSizeColumn(1);
            sheet1.autoSizeColumn(2);
            sheet1.autoSizeColumn(3);
            sheet1.autoSizeColumn(4);
            sheet1.autoSizeColumn(5);
            sheet1.autoSizeColumn(6);
        }
        if(!pedidosRepository.gananciaPorMes(id).isEmpty()) {
            String[] columnsMes = {"MES", "AÑO", "  GANANCIAS  "};
            Sheet sheet2 = workbook.createSheet("IngresoMensual");
            Row row2 = sheet2.createRow(0);
            for (int i = 0; i < columnsMes.length; i++) {
                Cell cell = row2.createCell(i);
                cell.setCellValue(columnsMes[i]);
                cell.setCellStyle(headStyle);
            }

            List<PedidosGananciaMesDto> listaGanancia = pedidosRepository.gananciaPorMes(id);
            int initRow2 = 1;
            for (PedidosGananciaMesDto mes : listaGanancia) {
                row2 = sheet2.createRow(initRow2);
                row2.createCell(0).setCellValue(mes.getmes());
                row2.createCell(1).setCellValue(mes.getanio());
                row2.createCell(2).setCellValue(mes.getganancia());
                initRow2++;
            }
            sheet2.autoSizeColumn(0);
            sheet2.autoSizeColumn(1);
            sheet2.autoSizeColumn(2);
        }
        if(!pedidosRepository.platosMasVendidos(id).isEmpty()) {
            String[] columnsPlatosMas = {"NOMBRE", "CANTIDAD", "GANANCIAS"};
            Sheet sheet3 = workbook.createSheet("PlatosMasVendidos");
            Row row3 = sheet3.createRow(0);
            for (int i = 0; i < columnsPlatosMas.length; i++) {
                Cell cell = row3.createCell(i);
                cell.setCellValue(columnsPlatosMas[i]);
                cell.setCellStyle(headStyle);
            }

            List<PedidosTop5Dto> listaPlatosMas = pedidosRepository.platosMasVendidos(id);
            int initRow3 = 1;
            for (PedidosTop5Dto plato : listaPlatosMas) {
                row3 = sheet3.createRow(initRow3);
                row3.createCell(0).setCellValue(plato.getnombreplato());
                row3.createCell(1).setCellValue(plato.getcantidad());
                row3.createCell(2).setCellValue(plato.getganancia());
                initRow3++;
            }

            sheet3.autoSizeColumn(0);
            sheet3.autoSizeColumn(1);
            sheet3.autoSizeColumn(2);
        }
        if(!pedidosRepository.platosMenosVendidos(id).isEmpty()) {
            String[] columnsPlatosMenos = {"NOMBRE", "CANTIDAD", "GANANCIAS"};
            Sheet sheet4 = workbook.createSheet("PlatosMenosVendidos");
            Row row4 = sheet4.createRow(0);
            for (int i = 0; i < columnsPlatosMenos.length; i++) {
                Cell cell = row4.createCell(i);
                cell.setCellValue(columnsPlatosMenos[i]);
                cell.setCellStyle(headStyle);
            }
            List<PedidosTop5Dto> listaPlatosMenos = pedidosRepository.platosMenosVendidos(id);
            int initRow4 = 1;
            for (PedidosTop5Dto plato : listaPlatosMenos) {
                row4 = sheet4.createRow(initRow4);
                row4.createCell(0).setCellValue(plato.getnombreplato());
                row4.createCell(1).setCellValue(plato.getcantidad());
                row4.createCell(2).setCellValue(plato.getganancia());
                initRow4++;
            }

            sheet4.autoSizeColumn(0);
            sheet4.autoSizeColumn(1);
            sheet4.autoSizeColumn(2);
        }
        workbook.write(stream);
        workbook.close();
        return new ByteArrayInputStream(stream.toByteArray());
    }

    /************************REPORTE************************/

    @GetMapping("/reporte")
    public String verReporte (Model model,HttpSession session,@RequestParam(name = "page", defaultValue = "1") String requestedPage,
                             @RequestParam(name = "page2", defaultValue = "1") String requestedPage2,
                             @RequestParam(name="name",defaultValue = "") String name,
                             RedirectAttributes attr) throws NumberFormatException,IllegalArgumentException {
        Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado");
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuario.getIdusuarios());
        int page;
        int page2;
            if(usuario.getCuentaActiva()==1){
                float numberOfUsersPerPage = 7;
                try {
                    page = Integer.parseInt(requestedPage);
                }catch(Exception e) {
                    return "redirect:/adminrest/reporte";
                }
                try {
                    page2 = Integer.parseInt(requestedPage2);
                }catch(Exception e) {
                    return "redirect:/adminrest/reporte";
                }
                int idrestaurante= restauranteRepository.buscarRestaurantePorIdAdmin(usuario.getIdusuarios()).get().getIdrestaurante();
                List<PedidosReporteDto> pedidosReporte;
                if(name.equals("")){
                    System.out.println("Trace1");
                    pedidosReporte = pedidosRepository.listaPedidosReporteporFechamasantigua(idrestaurante);
                    if(pedidosReporte.isEmpty()){
                        attr.addFlashAttribute("registroVacio","No tienes ningun reporte de pedidos hasta la fecha.");
                        return "redirect:/adminrest/perfil";
                    }
                }
                else{
                    System.out.println("Trace2");
                    System.out.println(name);
                    pedidosReporte=pedidosRepository.buscarPorReporte(name,idrestaurante);
                }
                List<PedidosGananciaMesDto> listaGanancias = pedidosRepository.gananciaPorMes(idrestaurante);
                if(!(pedidosReporte.isEmpty())){
                    int numberOfPages = (int) Math.ceil(pedidosReporte.size() / numberOfUsersPerPage);
                    int numberOfPages2 = (int) Math.ceil(listaGanancias.size() / numberOfUsersPerPage);
                    if (page > numberOfPages) {
                        page = numberOfPages;
                    } // validation
                    if (page2 > numberOfPages2) {
                        page2 = numberOfPages2;
                    }
                    int start = (int) numberOfUsersPerPage * (page - 1);
                    int end = (int) (start + numberOfUsersPerPage);
                    int start2 = (int) numberOfUsersPerPage * (page2 - 1);
                    int end2 = (int) (start2 + numberOfUsersPerPage);
                    List<PedidosReporteDto> lisOfPedidosReportePage = pedidosReporte.subList(start, Math.min(end, pedidosReporte.size()));
                    List<PedidosGananciaMesDto> lisOfGananciasPage = listaGanancias.subList(start2, Math.min(end2, listaGanancias.size()));
                    model.addAttribute("listaPedidosPorFecha", lisOfPedidosReportePage);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("maxNumberOfPages", numberOfPages);
                    model.addAttribute("listaGanancias",lisOfGananciasPage);
                    model.addAttribute("currentPage2", page2);
                    model.addAttribute("maxNumberOfPages2", numberOfPages2);
                    model.addAttribute("platosTop5",pedidosRepository.platosMasVendidos(idrestaurante));
                    model.addAttribute("platosNoTop5",pedidosRepository.platosMenosVendidos(idrestaurante));
                    model.addAttribute("name",name);
                    return "AdminRestaurantes/reporte";
                }else{
                    attr.addFlashAttribute("registroVacio","No se han encontrado registros");
                    return "redirect:/adminrest/reporte";
                }
            }else{
                return "redirect:/login";
            }
    }

    @PostMapping("/buscarReporte")
    public String searchReporte(RedirectAttributes attr,@RequestParam("name") String name, Model model, HttpSession session) {

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if(sessionUser.getCuentaActiva()==1){
            Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
            System.out.println(pedidosRepository.buscarPorReporte(name,idrestaurante));
            System.out.println("Trace3");
            //model.addAttribute("listaPedidosPorFecha",pedidosRepository.buscarPorReporte(name,idrestaurante));
            attr.addAttribute("name",name);
            return "redirect:/adminrest/reporte";
        }
        else{
            return "redirect:/adminrest/login";
        }
    }

    /************************PEDIDOS************************/

    @GetMapping("/pedidos")
    public String verPedidos(Model model, @RequestParam(name = "page", defaultValue = "1") String requestedPage,
                             HttpSession session, RedirectAttributes attr) {
        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(sessionUser.getIdusuarios());
        if(usuarioOptional.isPresent()) {
            Usuario usuarioNuevo = usuarioOptional.get();
            if (usuarioNuevo.getCuentaActiva() == 1) {
                Integer idrestaurante = restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
                /********************************/
                float numberOfUsersPerPage = 7;
                int page = Integer.parseInt(requestedPage);
                List<PedidosAdminRestDto> listaPedidos = pedidosRepository.listaPedidos(idrestaurante);
                if (!(listaPedidos.isEmpty())) {
                    int numberOfPages = (int) Math.ceil(listaPedidos.size() / numberOfUsersPerPage);
                    if (page > numberOfPages) {
                        page = numberOfPages;
                    }

                    int start = (int) numberOfUsersPerPage * (page - 1);
                    int end = (int) (start + numberOfUsersPerPage);

                    List<PedidosAdminRestDto> lisOfPedidosPage = listaPedidos.subList(start, Math.min(end, listaPedidos.size()));

                    model.addAttribute("listaPedidos", lisOfPedidosPage);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("maxNumberOfPages", numberOfPages);
                    return "AdminRestaurantes/pedidos";
                } else {
                    if(!pedidosRepository.aceptadopedidos(idrestaurante).isEmpty()){
                        model.addAttribute("listaAceptado",pedidosRepository.aceptadopedidos(idrestaurante));
                        model.addAttribute("valid",listaPedidos.isEmpty());
                        return "AdminRestaurantes/preparacion";
                    }
                    else {
                        attr.addFlashAttribute("nopedidos","No tiene pedidos pendientes");
                        return "redirect:/adminrest/perfil";
                    }
                }
            }else {
                return "redirect:/adminrest/login";
            }
        }else{
            return "redirect:/login";
        }
    }

    @GetMapping("/preparacion")
    public String pedidosPreparacion(Model model, HttpSession session){

        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
        /********************************/

        model.addAttribute("listaAceptado",pedidosRepository.aceptadopedidos(idrestaurante));
        return"AdminRestaurantes/preparacion";
    }

    @GetMapping("/detallepedidos")
    public String detallePedidos(@RequestParam("id")int id,Model model){
        model.addAttribute("detalle",pedidosRepository.detallepedidos(id));
        return "AdminRestaurantes/detalle";
    }

    @GetMapping("/aceptarpedido")
    public String aceptarPedido(@RequestParam("id")int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("aceptado");
        optional.get().setEstadorepartidor("pendiente"); //Para que le aparezca al repartidor
        pedidosRepository.save(optional.get());
        return"redirect:/adminrest/pedidos";
    }

    @GetMapping("/rechazarpedido")
    public String rechazarPedido(@RequestParam("id")int id, HttpSession session){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("rechazado");
        pedidosRepository.save(optional.get());
        /**Se obtiene Id de Restaurante**/
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        Integer idrestaurante=restauranteRepository.buscarRestaurantePorIdAdmin(sessionUser.getIdusuarios()).get().getIdrestaurante();
        /********************************/
        /* Envio de correo de confirmacion */
        String subject = "Pedido rechazado";
        String aws = "g-spicyo.publicvm.com";
        String direccionurl = "http://" + aws + ":8080/login";
        String mensaje = "¡Hola! Tu pedido ha sido rechazado por el administrador de restaurante. Por favor, intenta con un nuevo pedido.<br><br>" +
                "Ahora es parte de Spicyo. Para ingresar a su cuenta haga click: <a href='" + direccionurl + "'>AQUÍ</a> <br><br>Atte. Equipo de Spicy :D</b>";
        String correoDestino = sessionUser.getEmail();
        sendMailService.sendMail(correoDestino, "saritaatanacioarenas@gmail.com", subject, mensaje);
        return"redirect:/adminrest/pedidos";
    }

    @GetMapping("/preparadopedido")
    public String platoPreparado(@RequestParam("id") int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("preparado");
        pedidosRepository.save(optional.get());
        return "redirect:/adminrest/pedidos";
    }

    /************************CUENTA************************/

    @GetMapping("/cuentaAdmin")
    public String cuenta(@ModelAttribute("restaurante") Restaurante restaurante, @ModelAttribute("usuario") Usuario usuario, Model model,HttpSession session){

        Usuario user=(Usuario)session.getAttribute("usuarioLogueado");
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(user.getIdusuarios());
        Restaurante rest= restauranteRepository.obtenerperfilRest(user.getIdusuarios());
        if(usuarioOptional.isPresent()) {
            Usuario usuarioNuevo = usuarioOptional.get();
            if(usuarioNuevo.getCuentaActiva()==1){
                model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuarioAndActivoEquals(user,1));
                model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(user.getIdusuarios()));
                model.addAttribute("usuario",usuarioRepository.findById(user.getIdusuarios()).get());
                model.addAttribute("datos",usuarioRepository.obtenerDatos(user.getIdusuarios()));
                model.addAttribute("listadistritos",distritosRepository.findAll());
                model.addAttribute("ruc",restauranteRepository.buscarRuc(user.getIdusuarios()));
                model.addAttribute("direccion_rest",rest.getDireccion()+", "+rest.getDistrito().getNombredistrito());
                return "AdminRestaurantes/cuenta";
            }else{
                return "redirect:/login";
            }
        }else{
            return "redirect:/login";
        }
    }

    @PostMapping("/guardaradminedit")
    public String editPerfilUsuario(@ModelAttribute("usuario") @Valid Usuario usuario,
                                    BindingResult bindingResult,
                                    @RequestParam("pass2") String pass2,
                                    @RequestParam("telefono") String telefono,
                                    HttpSession session,
                                    RedirectAttributes attr,
                                    Model model){
        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        Restaurante rest= restauranteRepository.obtenerperfilRest(user.getIdusuarios());
        int correcto=1;
        int flg_contra=0;

        if(bindingResult.hasFieldErrors("telefono")){
            correcto=0;
        }
        System.out.println("contraseña");
        System.out.println(usuario.getContraseniaHash());
        if(usuario.getContraseniaHash()!=null&&(!(usuario.getContraseniaHash().equals("")))){
            System.out.println("Entre");
            flg_contra=1;
            if(!Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",usuario.getContraseniaHash())){
                correcto=0;
                model.addAttribute("msgpasserror", "Contraseñas no cumple con los requisitos");

            }
            if(usuario.getContraseniaHash().equals(pass2)){
                correcto=0;
                model.addAttribute("msgpasserror2","Contraseñas no osn iguales");
            }
        }
        if(correcto==0){
            model.addAttribute("direccion_rest",rest.getDireccion()+", "+rest.getDistrito().getNombredistrito());
            model.addAttribute("datos",usuarioRepository.obtenerDatos(usuario.getIdusuarios()));
            model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1));
            model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(usuario.getIdusuarios()));
            model.addAttribute("ruc",restauranteRepository.buscarRuc(usuario.getIdusuarios()));
            model.addAttribute("listadistritos",distritosRepository.findAll());
            model.addAttribute("telefono",telefono);
            return "AdminRestaurantes/cuenta";
        }
        else{
            if(flg_contra==0){
                user.setTelefono(usuario.getTelefono());
                usuarioRepository.save(user);
                attr.addFlashAttribute("suceso","Perfil actualizado exitosamente");
                return "redirect:/adminrest/cuentaAdmin";
            }
            else{
                user.setTelefono(usuario.getTelefono());
                user.setContraseniaHash(BCrypt.hashpw(usuario.getContraseniaHash(), BCrypt.gensalt()));
                usuarioRepository.save(user);
                attr.addFlashAttribute("suceso","Perfil actualizado exitosamente");
                return "redirect:/adminrest/cuentaAdmin";
            }
        }
/*

        if(bindingResult.hasFieldErrors("telefono")|| bindingResult.hasFieldErrors("contraseniaHash")){
            if (!BCrypt.checkpw(usuario.getContraseniaHash(),user.getContraseniaHash())) {
                model.addAttribute("msg", "Contraseña incorrecta");
            }
            if(!usuario.getContraseniaHash().equals(pass2)){
                model.addAttribute("msg2","Contraseñas no son iguales");
            }
            if(!Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",usuario.getContraseniaHash())){
                model.addAttribute("msgpasserror", "Contraseñas no cumple con los requisitos");
            }
            model.addAttribute("direccion_rest",rest.getDireccion()+", "+rest.getDistrito().getNombredistrito());
            model.addAttribute("datos",usuarioRepository.obtenerDatos(usuario.getIdusuarios()));
            model.addAttribute("listadirecciones",direccionesRepository.findAllByUsuarioAndActivoEquals(usuario,1));
            model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(usuario.getIdusuarios()));
            model.addAttribute("ruc",restauranteRepository.buscarRuc(usuario.getIdusuarios()));
            model.addAttribute("listadistritos",distritosRepository.findAll());
            model.addAttribute("telefono",telefono);
            return "AdminRestaurantes/cuenta";
            }
        else {
            if (BCrypt.checkpw(usuario.getContraseniaHash(),user.getContraseniaHash())) {
                if(usuario.getContraseniaHash().equals(pass2) && usuario.getContraseniaHash()!=null && usuario.getContraseniaHash()!="" && Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",usuario.getContraseniaHash())){
                    user.setTelefono(usuario.getTelefono());
                    usuarioRepository.save(user);
                    attr.addFlashAttribute("suceso","Perfil actualizado exitosamente");
                    return "redirect:/adminrest/cuentaAdmin";
                }
                else{
                    if (!BCrypt.checkpw(usuario.getContraseniaHash(),user.getContraseniaHash())) {
                        model.addAttribute("msg", "Contraseña incorrecta");
                    }
                    if(!usuario.getContraseniaHash().equals(pass2)){
                        model.addAttribute("msg2","Contraseñas no son iguales");
                    }
                    model.addAttribute("direccion_rest",rest.getDireccion()+", "+rest.getDistrito().getNombredistrito());
                    model.addAttribute("msgpasserror", "Contraseñas no cumple con los requisitos");
                    model.addAttribute("listadirecciones", direccionesRepository.findAllByUsuarioAndActivoEquals(user, 1));
                    model.addAttribute("restaurante", restauranteRepository.obtenerperfilRest(user.getIdusuarios()));
                    model.addAttribute("datos", usuarioRepository.obtenerDatos(user.getIdusuarios()));
                    model.addAttribute("ruc", restauranteRepository.buscarRuc(usuario.getIdusuarios()));
                    model.addAttribute("listadistritos", distritosRepository.findAll());
                    return "AdminRestaurantes/cuenta";
                }
            } else {
                if(!usuario.getContraseniaHash().equals(pass2)){
                    model.addAttribute("msg2","Contraseñas no son iguales");
                }
                if(!Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",usuario.getContraseniaHash())){
                    model.addAttribute("msgpasserror", "Contraseñas no cumple con los requisitos");
                }
                model.addAttribute("direccion_rest",rest.getDireccion()+", "+rest.getDistrito().getNombredistrito());
                model.addAttribute("msg", "Contraseña incorrecta");
                model.addAttribute("listadirecciones", direccionesRepository.findAllByUsuarioAndActivoEquals(user, 1));
                model.addAttribute("restaurante", restauranteRepository.obtenerperfilRest(user.getIdusuarios()));
                model.addAttribute("datos", usuarioRepository.obtenerDatos(user.getIdusuarios()));
                model.addAttribute("ruc", restauranteRepository.buscarRuc(usuario.getIdusuarios()));
                model.addAttribute("listadistritos", distritosRepository.findAll());
                return "AdminRestaurantes/cuenta";
            }
        }
 */
    }

    @GetMapping("/editardireccion")
    public String agregardireccion(Model model) {
        List<Distritos> listadistritos = distritosRepository.findAll();
        model.addAttribute("listadistritos",listadistritos);
        String direction=null;
        model.addAttribute("direction",direction);
        System.out.println(direction);
        return "AdminRestaurantes/agregardireccion";
    }

    @PostMapping("/guardardireccion")
    public String guardarnuevadireccion(@RequestParam("iddistrito") Integer iddistrito,
                                        @RequestParam("direccion_real") String direccion,
                                        HttpSession session) {

        Usuario user=(Usuario) session.getAttribute("usuarioLogueado");
        Direcciones direccion_user = direccionesRepository.findByUsuario(user);
        String dir = direccion.split(",")[0].trim();
        direccion_user.setDireccion(dir);
        System.out.println(iddistrito);
        direccion_user.setDistrito(distritosRepository.findById(iddistrito).get());
        direccionesRepository.save(direccion_user);
        return "redirect:/adminrest/cuentaAdmin";
    }
    @GetMapping("/errores")
    public String hackeandoURL(@RequestParam("id") Integer id,RedirectAttributes attr){
        if(id==1) {
            attr.addFlashAttribute("msgpage","No existe la página solicitada");
            return "redirect:/adminrest/reporte";
        }
        else{
            return "redirect:/adminrest/perfil";
        }
    }
}