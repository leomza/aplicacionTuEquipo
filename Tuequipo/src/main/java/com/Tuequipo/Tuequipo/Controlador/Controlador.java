package com.Tuequipo.Tuequipo.Controlador;

import com.Tuequipo.Tuequipo.Enumeracion.CantidadJugadores;
import com.Tuequipo.Tuequipo.Enumeracion.Categoria;
import com.Tuequipo.Tuequipo.Enumeracion.Dias;
import com.Tuequipo.Tuequipo.Enumeracion.Turno;
import com.Tuequipo.Tuequipo.Enumeracion.Zonas;
import com.Tuequipo.Tuequipo.Errores.ErrorServicio;
import com.Tuequipo.Tuequipo.Repositorios.FotoRepositorio;
import com.Tuequipo.Tuequipo.entidades.Equipo;
import com.Tuequipo.Tuequipo.entidades.Foto;
import com.Tuequipo.Tuequipo.enumeracion.Tipo;
import com.Tuequipo.Tuequipo.servicios.EquipoServicio;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class Controlador {

    @Autowired
    private EquipoServicio equipoServicio;

    @Autowired
    private FotoRepositorio fotoRepositorio;

    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/Registro")
    public String registro() {
        return "Registro.html";
    }

    @PostMapping("/registrar")
    public String cargaEquipo(ModelMap modelo, MultipartFile archivo, @RequestParam String nombre, @RequestParam String mail, @RequestParam String descripcion, @RequestParam String clave1, @RequestParam String clave2, @RequestParam String telefono1, @RequestParam String telefono2, @RequestParam Turno turno, @RequestParam Zonas zona, @RequestParam Dias dia, @RequestParam Tipo tipo, @RequestParam Categoria categoria, @RequestParam CantidadJugadores cantidadJugadores) {
        try {
            equipoServicio.cargaEquipo(archivo, nombre, mail, descripcion, clave1, clave2, telefono1, telefono2, turno, zona, dia, tipo, categoria, cantidadJugadores);
        } catch (ErrorServicio ex) {
            modelo.put("error", ex.getMessage());
            modelo.put("nombre", nombre);
            modelo.put("mail", mail);
            modelo.put("descripcion", descripcion);
            modelo.put("clave1", clave1);
            modelo.put("clave2", clave2);
            modelo.put("telefono1", telefono1);
            modelo.put("telefono2", telefono2);
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
            return "Registro.html";
        }
        return "RegistroExitoso.html";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, @RequestParam(required = false) String logout, ModelMap model) {
        if (error != null) {
            model.put("error", "Usuario o clave incorrectos");
        }
        if (logout != null) {
            model.put("logout", "Ha salido correctamente");
        }
        return "login.html";
    }

    @PreAuthorize("hasAnyRole('ROLE_USUARIO_REGISTRADO')")
    @GetMapping("/buscador")
    public String buscador(ModelMap modelo) {
        
        List<Equipo> equipos = equipoServicio.buscarDisponibles();
        
        modelo.put("equipos",equipos);

        for (Zonas o : Zonas.values()) {
            modelo.put(o.toString(), equipos.stream()
                .filter(c -> c.getZona().toString().equals(o.toString()))
                .count());
        }
        
        for (CantidadJugadores o : CantidadJugadores.values()) {
            modelo.put(o.toString(), equipos.stream()
                .filter(c -> c.getCantidadJugadores().toString().equals(o.toString()))
                .count());
        }
        
        for (Categoria o : Categoria.values()) {
            modelo.put(o.toString(), equipos.stream()
                .filter(c -> c.getCategoria().toString().equals(o.toString()))
                .count());
        }
        
        for (Dias o : Dias.values()) {
            modelo.put(o.toString(), equipos.stream()
                .filter(c -> c.getDia().toString().equals(o.toString()))
                .count());
        }
        
        for (Tipo o : Tipo.values()) {
            modelo.put(o.toString(), equipos.stream()
                .filter(c -> c.getTipo().toString().equals(o.toString()))
                .count());
        }
        
        for (Turno o : Turno.values()) {
            modelo.put(o.toString(), equipos.stream()
                .filter(c -> c.getTurno().toString().equals(o.toString()))
                .count());
        }

        return "buscador.html";
    }

    @GetMapping("/cargar/{id}")
    public ResponseEntity<byte[]> cargarfoto(@PathVariable String id) {
        Foto foto = fotoRepositorio.getOne(id);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(foto.getContenido(), headers, HttpStatus.OK);
    }
    
    @PostMapping("/recuperarPass")
    public String reestablecerPass(ModelMap modelo, @RequestParam String name){
        try {
            equipoServicio.renovarPass(name);
            return "redirect:/login";
        } catch (ErrorServicio ex) {
            modelo.put("error", ex.getMessage());
            return "login.html";
        }
    }

}
