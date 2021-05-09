package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.entity.Categorias;
import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.CategoriasRepository;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.PedidosRepository;
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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cliente")
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    DireccionesRepository direccionesRepository;

    @Autowired
    CategoriasRepository categoriasRepository;

    @Autowired
    PedidosRepository pedidosRepository;

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
    public String reportesCliente(Model model, @RequestParam("idcliente") int idcliente,
                                  @RequestParam("anio") int anio,
                                  @RequestParam("mes") int mes){
        model.addAttribute("listaTop3Restaurantes", pedidosRepository.obtenerTop3Restaurantes(idcliente, anio, mes));
        model.addAttribute("listaPromedioTiempo", pedidosRepository.obtenerTiemposPromedio(idcliente, anio, mes));
        return "cliente/reportes";
    }

    @GetMapping("/realizarpedido")
    public String realizarpedido(Model model){

        int idusuarioactual = 7;

        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
        List<Categorias> listacategorias = categoriasRepository.findAll();
        model.addAttribute("listacategorias", listacategorias);
        model.addAttribute("listadirecciones",listadireccionescliente);


        return "cliente/realizar_pedido_cliente";
    }

    @GetMapping("/miperfil")
    public String miperfil(Model model){
        int idusuario = 7;
        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuario);
        model.addAttribute("listadirecciones",listadireccionescliente);
        Optional<Usuario> optional = usuarioRepository.findById(idusuario);
        Usuario usuario = optional.get();
        model.addAttribute("usuario", usuario);
        System.out.println(usuario.getIdusuarios());
        return "cliente/miPerfil";
    }

    @PostMapping("/miperfil")
    public String updatemiperfil(Usuario usuarioRecibido){
        System.out.println(usuarioRecibido.getIdusuarios());
        Optional<Usuario> optusuario = usuarioRepository.findById(usuarioRecibido.getIdusuarios());
        Usuario usuariodb = optusuario.get();

        usuariodb.setEmail(usuarioRecibido.getEmail());
        usuariodb.setTelefono(usuarioRecibido.getTelefono());
        System.out.println("contra es" + usuarioRecibido.getContraseniaHash() + "     ****");
        usuariodb.setContraseniaHash(usuarioRecibido.getContraseniaHash());
        usuarioRepository.save(usuariodb);

        return "redirect:/cliente/miperfil";
    }


    @GetMapping("/borrardireccion")
    public String borrardireccion(@RequestParam("iddireccion") int iddireccion,
                                  Model model){

        direccionesRepository.deleteById(iddireccion);

        return "redirect:/cliente/miperfil";
    }

    @GetMapping("/agregardireccion")
    public String agregardireccion(){

        return "cliente/registrarNuevaDireccion";
    }

    @PostMapping("/guardarnuevadireccion")
    public String guardarnuevadireccion(@RequestParam("direccion") String direccion,
                                        @RequestParam("distrito") String distrito){

        int idusuario = 7;

        Direcciones direccioncrear = new Direcciones();
        direccioncrear.setDireccion(direccion);
        direccioncrear.setDistrito(distrito);
        direccioncrear.setUsuariosIdusuarios(idusuario);

        direccionesRepository.save(direccioncrear);

        return "redirect:/cliente/miperfil";

    }

}
