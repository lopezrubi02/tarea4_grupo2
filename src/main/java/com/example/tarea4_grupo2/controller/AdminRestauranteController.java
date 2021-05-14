package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.*;
import com.example.tarea4_grupo2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    CategoriasRepository categoriasRepository;

    @GetMapping("/login")
    public String loginAdmin(){
        return "AdminRestaurantes/login";
    }

    @GetMapping("/register")
    public String registerAdmin(@ModelAttribute("usuario") Usuario usuario){
        return "AdminRestaurantes/register";
    }

    @PostMapping("/categorias")
    public String esperaConfirmacion(@ModelAttribute("restaurante") Restaurante restaurante,@RequestParam("imagen") MultipartFile file,Model model) throws IOException {
        try {
            restaurante.setFoto(file.getBytes());
            System.out.println(file.getContentType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        restauranteRepository.save(restaurante);
        model.addAttribute("id",restaurante.getIdrestaurante());
        model.addAttribute("listacategorias",categoriasRepository.findAll());
        return "AdminRestaurantes/categorias";
    }
    @PostMapping("/estado")
    public String estadoAdmin(@RequestParam("correo") String correo) {
        //Se valida con el correo si en la bd aparece como usuario aceptado o en espera y tendría dos posibles salidas
        if(correo!=""){
            return "AdminRestaurantes/restaurante";
        }
        return "redirect:/login";
    }

    @GetMapping("/registerRestaurante")
    public String registerRestaurante(@ModelAttribute("restaurante")Restaurante restaurante, Model model){
        Usuario usuario = new Usuario();
        usuario.setIdusuarios(14);
        restaurante.setUsuario(usuario);
        model.addAttribute("restaurante",restaurante);
        return "AdminRestaurantes/registerRestaurante";
    }

    @GetMapping("/sinrestaurante")
    public String sinRestaurante(){
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
    @PostMapping("/guardaradmin")
    public String guardarAdmin(@ModelAttribute("usuario") @Valid Usuario usuario, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "AdminRestaurantes/register";
        }
        else {
            usuarioRepository.save(usuario);
        }
        return"AdminRestaurantes/correo";
    }

    @GetMapping("/imagen")
    public ResponseEntity<byte[]> imagenRestaurante(Model model) {
        Optional<Restaurante> optional = restauranteRepository.findById(6);
        if (optional.isPresent()) {
            byte[] imagen = optional.get().getFoto();
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType("image/png"));
            return new ResponseEntity<>(imagen,httpHeaders, HttpStatus.OK);
        }
        return null;
    }
    /************************PERFIL************************/

    @GetMapping("/perfil")
    public String perfilRestaurante(Model model){
        Integer id = 1;
        model.addAttribute("calificacion",pedidosRepository.calificacionPromedio(id));
        return "AdminRestaurantes/perfilrestaurante";
    }

    /************************PLATOS************************/

    @GetMapping("/menu")
    public String verMenu(Model model){
        Integer idrestaurante = 1;
        model.addAttribute("listaPlatos", platoRepository.buscarPlatosPorIdRestaurante(idrestaurante));
        return "AdminRestaurantes/menu";
    }

    @GetMapping("/crearPlato")
    public String crearPlato(@ModelAttribute("plato") Plato plato, Model model){
        model.addAttribute("plato",plato);
        return "AdminRestaurantes/newPlato";
    }

    @GetMapping("/editarPlato")
    public String editarPlato(Model model, @RequestParam("idplato") int id, @ModelAttribute("plato") Plato plato){

        Optional<Plato> optionalPlato = platoRepository.findById(id);

        if(optionalPlato.isPresent()){
            plato = optionalPlato.get();
            model.addAttribute("plato",plato);
            return "AdminRestaurantes/newPlato";
        }else{
            return "redirect:/menu";
        }
    }

    @PostMapping("/guardarPlato")
    public String guardarPlato(@ModelAttribute("plato") Plato plato, RedirectAttributes attr, Model model){
        if (plato.getIdplato() == 0) {
            attr.addFlashAttribute("msg", "Plato creado exitosamente");
            platoRepository.save(plato);
            return "redirect:/menu";
        } else {
            platoRepository.save(plato);
            attr.addFlashAttribute("msg", "Plato actualizado exitosamente");
            return "redirect:/menu";
        }
    }

    @GetMapping("/borrarPlato")
    public String borrarPlato(@RequestParam("idplato") int id, RedirectAttributes attr){

        Optional<Plato> optionalPlato = platoRepository.findById(id);

        if(optionalPlato.isPresent()){
            platoRepository.deleteById(id);
            attr.addFlashAttribute("msg", "Producto borrado exitosamente");
        }
        return "redirect:/menu";
    }

    /************************CUPONES************************/

    @GetMapping("/cupones")
    public String verCupones(Model model, @RequestParam(value = "idrestaurante", required = false) Integer idrestaurante){
        idrestaurante = 1;
        List<Cupones> listaCupones = cuponesRepository.buscarCuponesPorIdRestaurante(idrestaurante);
        List<String> listaDisponibilidad = new ArrayList<String>();

        for (Cupones i: listaCupones){
            Date inicio = i.getFechainicio();
            Date fin = i.getFechafin();
            Date ahora = Date.valueOf(LocalDate.now());

            if(inicio.compareTo(ahora) > 0){
                listaDisponibilidad.add("No");
            }else if(fin.compareTo(ahora) < 0){
                listaDisponibilidad.add("No");
            }else if((inicio.compareTo(ahora) < 0) && (fin.compareTo(ahora) > 0)){
                listaDisponibilidad.add("Si");
            }else{
                listaDisponibilidad.add("No");
            }

        }

        model.addAttribute("listaCupones", listaCupones);
        model.addAttribute("listaDisponibilidad", listaDisponibilidad);
        return "AdminRestaurantes/cupones";

    }

    @GetMapping("/crearCupon")
    public String crearCupon(@ModelAttribute("cupon") Cupones cupon, Model model){
        int idrestaurante = 1;
        Restaurante restaurante = new Restaurante();
        restaurante.setIdrestaurante(idrestaurante);
        cupon.setRestaurante(restaurante);
        model.addAttribute("cupon",cupon);
        List<Plato> listaPlatos = platoRepository.buscarPlatosPorIdRestaurante(idrestaurante);
        model.addAttribute("listaPlatos",listaPlatos);
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
            model.addAttribute("listaPlatos",listaPlatos);
            return "AdminRestaurantes/generarCupon";
        }else{
            return "redirect:/cupones";
        }
    }
    @GetMapping("/borrarCupon")
    public String borrarCupon(@RequestParam("idcupon") int id, RedirectAttributes attr){

        Optional<Cupones> optCupon = cuponesRepository.findById(id);

        if(optCupon.isPresent()){
            cuponesRepository.deleteById(id);
            attr.addFlashAttribute("msg", "Cupon borrado exitosamente");
            return "redirect:/cupones";
        }else{
            return "redirect:/cupones";
        }
    }

    @PostMapping("/guardarCupon")
    public String guardarCupon(@ModelAttribute("cupon") Cupones cupon, RedirectAttributes attr,
                               Model model){

        if (cupon.getIdcupones() == 0) {
            cuponesRepository.save(cupon);
            attr.addFlashAttribute("msg", "Cupon creado exitosamente");
            return "redirect:/cupones";

        } else {
            cuponesRepository.save(cupon);
            attr.addFlashAttribute("msg", "Cupon actualizado exitosamente");
            return "redirect:/cupones";
        }
    }

    /************************CALIFICACIONES************************/

    @GetMapping("/calificaciones")
    public String verCalificaciones(Model model){
        Integer id = 1;
        model.addAttribute("listaCalificacion",pedidosRepository.comentariosUsuarios(id));
        return "AdminRestaurantes/calificaciones";
    }

    @PostMapping("/buscarCalificaciones")
    public String buscarCalificaciones(@RequestParam("name") String name, Model model){
        Integer id = 1;
        model.addAttribute("listaCalificacion",pedidosRepository.buscarComentariosUsuarios(name,id));
        return "AdminRestaurantes/calificaciones";
    }

    /************************REPORTE************************/

    @GetMapping("/reporte")
    public String verReporte(Model model){
        Integer id = 1;
        model.addAttribute("listaPedidosPorFecha",pedidosRepository.listaPedidosReporteporFechamasantigua(id));
        model.addAttribute("listaGanancias",pedidosRepository.gananciaPorMes(id));
        model.addAttribute("platosTop5",pedidosRepository.platosMasVendidos(id));
        model.addAttribute("platosNoTop5",pedidosRepository.platosMenosVendidos(id));
        return "AdminRestaurantes/reporte";
    }

    @PostMapping("/buscarReporte")
    public String searchReporte(@RequestParam("name") String name, Model model) {
        Integer id = 1;
        model.addAttribute("listaPedidosPorFecha",pedidosRepository.buscarPorReporte(name,id));
        model.addAttribute("listaGanancias",pedidosRepository.gananciaPorMes(id));
        model.addAttribute("platosTop5",pedidosRepository.platosMasVendidos(id));
        model.addAttribute("platosNoTop5",pedidosRepository.platosMenosVendidos(id));
        return "AdminRestaurantes/reporte";
    }

    /************************PEDIDOS************************/

    @GetMapping("/pedidos")
    public String verPedidos(Model model){
        Integer id = 1;
        model.addAttribute("listaPedidos",pedidosRepository.listaPedidos(id));
        return "AdminRestaurantes/pedidos";
    }

    @GetMapping("/preparacion")
    public String pedidosPreparacion(Model model){
        model.addAttribute("listaAceptado",pedidosRepository.aceptadopedidos());
        model.addAttribute("listaPreparado",pedidosRepository.preparadopedidos());
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
        pedidosRepository.save(optional.get());
        return"redirect:/pedidos";
    }

    @GetMapping("/rechazarpedido")
    public String rechazarPedido(@RequestParam("id")int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("rechazado");
        pedidosRepository.save(optional.get());
        return"redirect:/pedidos";
    }

    @GetMapping("/preparadopedido")
    public String platoPreparado(@RequestParam("id") int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("preparado");
        pedidosRepository.save(optional.get());
        return "redirect:/preparacion";
    }

    @GetMapping("/entregadopedido")
    public String entregadoPedido(@RequestParam("id") int id){
        Optional<Pedidos> optional = pedidosRepository.findById(id);
        optional.get().setEstadorestaurante("entregado");
        pedidosRepository.save(optional.get());
        return"redirect:/preparacion";
    }

    @GetMapping("/cuentaAdmin")
    public String cuenta(@RequestParam("id") int id,Model model){
        model.addAttribute("restaurante",restauranteRepository.obtenerperfilRest(id));
        model.addAttribute("usuario",usuarioRepository.findById(id).get());
        return "AdminRestaurantes/cuenta";
    }
    @GetMapping("/borrarRestaurante")
    public String borrarRestaurante(@RequestParam("id")int id){
        restauranteRepository.deleteById(id);
        return "redirect:/sinrestaurante";
    }
    @PostMapping("/llenarcategoria")
    public String llenarcategorias(@ModelAttribute("restaurante") Restaurante restaurante,Model model){
        Optional<Restaurante> optional = restauranteRepository.findById(restaurante.getIdrestaurante());
        optional.get().setCategoriasrestList(restaurante.getCategoriasrestList());
        restauranteRepository.save(optional.get());
        model.addAttribute("id",optional.get().getIdrestaurante());
        return "AdminRestaurantes/espera";
    }
    @PostMapping("/guardaradminedit")
    public String editPerfilUsuario(@ModelAttribute("usuario") @Valid Usuario usuario, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "AdminRestaurantes/cuenta";
        }
        else {
            usuarioRepository.save(usuario);
        }
        return "redirect:/perfil";
    }
    @PostMapping("/guardarrestedit")
    public String editarPerfilRest(@ModelAttribute("restaurante") Restaurante restaurante,@RequestParam("imagen") MultipartFile file,Model model){
        try {
            restaurante.setFoto(file.getBytes());
            System.out.println(file.getContentType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        restauranteRepository.save(restaurante);
        model.addAttribute("id",restaurante.getIdrestaurante());
        model.addAttribute("listacategorias",categoriasRepository.findAll());
        return "AdminRestaurantes/categoriasedit";
    }
    @PostMapping("/llenarcategoriaedit")
    public String llenarcategoriasedit(@ModelAttribute("restaurante") Restaurante restaurante,Model model){
        Optional<Restaurante> optional = restauranteRepository.findById(restaurante.getIdrestaurante());
        optional.get().setCategoriasrestList(restaurante.getCategoriasrestList());
        restauranteRepository.save(optional.get());
        model.addAttribute("id",optional.get().getIdrestaurante());
        return "AdminRestaurantes/perfil";
    }
}
