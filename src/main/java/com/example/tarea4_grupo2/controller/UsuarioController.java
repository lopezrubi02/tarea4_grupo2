package com.example.tarea4_grupo2.controller;

import com.example.tarea4_grupo2.dto.TiempoMedio_ClienteDTO;
import com.example.tarea4_grupo2.dto.Top3Platos_ClientesDTO;
import com.example.tarea4_grupo2.dto.Top3Restaurantes_ClienteDTO;
import com.example.tarea4_grupo2.entity.Categorias;
import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Pedidos;
import com.example.tarea4_grupo2.entity.Usuario;
import com.example.tarea4_grupo2.repository.CategoriasRepository;
import com.example.tarea4_grupo2.repository.DireccionesRepository;
import com.example.tarea4_grupo2.repository.PedidosRepository;
import com.example.tarea4_grupo2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @GetMapping("/paginaprincipal")
    public String paginaprincipal() {
        return "cliente/paginaPrincipal";
    }


    @GetMapping("/nuevo")
    public String nuevoCliente() {
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
                                 Model model) {

        System.out.println(nombres + apellidos + email + dni + telefono + fechaNacimiento);
        System.out.println(fechaNacimiento);
        if (contraseniaHash.equals(pass2)) {
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
            try {
                usuario.setFechaNacimiento(sdf.parse(fechaNacimiento));
                System.out.println(fechaNacimiento);
            } catch (ParseException e) {
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
        } else {
            return "cliente/registroCliente";
        }

    }

    @GetMapping("/reportes")
    public String reportesCliente(Model model, @RequestParam("idusuarios") int idusuarios,
                                  @RequestParam("anio") int anio,
                                  @RequestParam("mes") int mes, @ModelAttribute("cliente") Usuario cliente) {
        idusuarios = 7;
        Optional<Usuario> optUsuario = usuarioRepository.findById(idusuarios);
        if (optUsuario.isPresent()) {
            cliente = optUsuario.get();

            List<Top3Restaurantes_ClienteDTO> top3Restaurantes_clienteDTOS = pedidosRepository.obtenerTop3Restaurantes(idusuarios, anio, mes);
            List<Top3Platos_ClientesDTO> top3Platos_clientesDTOS = pedidosRepository.obtenerTop3Platos(idusuarios, anio, mes);
            List<TiempoMedio_ClienteDTO> tiempoMedio_clienteDTOS = pedidosRepository.obtenerTiemposPromedio(idusuarios, anio, mes);

            for(Top3Restaurantes_ClienteDTO t : top3Restaurantes_clienteDTOS){
                System.out.println(t.getRestaurante());
            }
            model.addAttribute("cliente", cliente);
            model.addAttribute("listaTop3Restaurantes",top3Restaurantes_clienteDTOS );
            model.addAttribute("listaTop3Platos", top3Platos_clientesDTOS);
            model.addAttribute("listaPromedioTiempo", tiempoMedio_clienteDTOS);
            model.addAttribute("listaHistorialConsumo", pedidosRepository.obtenerHistorialConsumo(idusuarios, anio, mes));
            return "cliente/reportes";
        } else {
            return "redirect:/cliente/miperfil";
        }
    }

    @PostMapping("/recepcionCliente")
    public String recepcionCliente(@ModelAttribute("cliente") @Valid Usuario cliente, @RequestParam("idusuarios") int idusuarios,
                                   @RequestParam("anio") int anio,
                                   @RequestParam("mes") int mes, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("listaTop3Restaurantes", pedidosRepository.obtenerTop3Restaurantes(idusuarios, anio, mes));
            model.addAttribute("listaTop3Platos", pedidosRepository.obtenerTop3Platos(idusuarios, anio, mes));
            model.addAttribute("listaPromedioTiempo", pedidosRepository.obtenerTiemposPromedio(idusuarios, anio, mes));
            model.addAttribute("listaHistorialConsumo", pedidosRepository.obtenerHistorialConsumo(idusuarios, anio, mes));
            System.out.println("IDCliente: " + idusuarios + "Mes: " + mes + "Anio: " + anio);
        }
        return "cliente/reportes";
    }

    @GetMapping("/realizarpedido")
    public String realizarpedido(Model model) {

        int idusuarioactual = 7;

        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuarioactual);
        List<Categorias> listacategorias = categoriasRepository.findAll();
        model.addAttribute("listacategorias", listacategorias);
        model.addAttribute("listadirecciones", listadireccionescliente);


        return "cliente/realizar_pedido_cliente";
    }

    @GetMapping("/miperfil")
    public String miperfil(Model model) {
        int idusuario = 7;
        List<Direcciones> listadireccionescliente = direccionesRepository.findAllByUsuariosIdusuariosEquals(idusuario);
        model.addAttribute("listadirecciones", listadireccionescliente);
        Optional<Usuario> optional = usuarioRepository.findById(idusuario);
        Usuario usuario = optional.get();
        model.addAttribute("usuario", usuario);
        System.out.println(usuario.getIdusuarios());
        return "cliente/miPerfil";
    }

    @PostMapping("/miperfil")
    public String updatemiperfil(Usuario usuarioRecibido) {
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
                                  Model model) {

        direccionesRepository.deleteById(iddireccion);

        return "redirect:/cliente/miperfil";
    }

    @GetMapping("/agregardireccion")
    public String agregardireccion() {

        return "cliente/registrarNuevaDireccion";
    }

    @PostMapping("/guardarnuevadireccion")
    public String guardarnuevadireccion(@RequestParam("direccion") String direccion,
                                        @RequestParam("distrito") String distrito) {

        int idusuario = 7;

        Direcciones direccioncrear = new Direcciones();
        direccioncrear.setDireccion(direccion);
        direccioncrear.setDistrito(distrito);
        direccioncrear.setUsuariosIdusuarios(idusuario);

        direccionesRepository.save(direccioncrear);

        return "redirect:/cliente/miperfil";

    }

}
