
package com.Tuequipo.Tuequipo.Controlador;

import com.Tuequipo.Tuequipo.Enumeracion.CantidadJugadores;
import com.Tuequipo.Tuequipo.Enumeracion.Dias;
import com.Tuequipo.Tuequipo.Enumeracion.Turno;
import com.Tuequipo.Tuequipo.Enumeracion.Zonas;
import com.Tuequipo.Tuequipo.Errores.ErrorServicio;
import com.Tuequipo.Tuequipo.entidades.Equipo;
import com.Tuequipo.Tuequipo.servicios.EquipoServicio;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/usuario")
public class UsuarioControlador {
    
    @Autowired
    private EquipoServicio equipoServicio;
    
    @GetMapping("/mostrarPerfil")
    public String mostrarPerfil(ModelMap modelo, @RequestParam String nombre){
        Equipo equipo = equipoServicio.buscarPorId(nombre);
        modelo.addAttribute("equipo", equipo);
        return "perfil.html";
    }
    
    @PostMapping("/actualizar-perfil")
    public String actualizar (ModelMap modelo, HttpSession session, MultipartFile archivo, @RequestParam String nombre, @RequestParam String clave1, @RequestParam String clave2, @RequestParam String numero1, @RequestParam String numero2, @RequestParam String descripcion, @RequestParam Turno turno, @RequestParam Zonas zona, @RequestParam Dias dia, @RequestParam CantidadJugadores cantidadJugadores){
            Equipo equipo = null;
        try {
            equipo = equipoServicio.buscarPorId(nombre);
            equipoServicio.modificarEquipo(archivo, nombre, descripcion, clave1, clave2, numero1, numero2, turno, zona, dia, cantidadJugadores);
            session.setAttribute("usuariosession", equipo);
            modelo.put("exito", "Modificacion exitosa");
            modelo.addAttribute("equipo", equipo);
            return "perfil.html";
        } catch (ErrorServicio ex) {
            System.out.println(ex.getMessage());
            session.setAttribute("usuariosession", equipo);
            return "perfil.html";
        }
        }
    
    @PostMapping("/eliminar-perfil")
    public String eliminar (ModelMap modelo, HttpSession session, @RequestParam String nombre){
        try {
            equipoServicio.eliminarEquipo(nombre);
            return "index.html";
        } catch (ErrorServicio ex) {
            System.out.println(ex.getMessage());
            return "perfil.html";
        }
        
    }
    
    @PostMapping("/disponibilidad")
    public String disponibilidad(ModelMap modelo, HttpSession session, @RequestParam String nombre, @RequestParam Boolean disponible){
        Equipo equipo = equipoServicio.buscarPorId(nombre);
        try {
            if(disponible){
                equipoServicio.habilitar(nombre);
            } else {
                equipoServicio.deshabilitar(nombre);
            }
            modelo.addAttribute("equipo", equipo);
            session.setAttribute("usuariosession", equipo);
            
            return "redirect:/buscador";
        } catch (ErrorServicio ex) {
            System.out.println(ex.getMessage());
            modelo.addAttribute("equipo", equipo);
            session.setAttribute("usuariosession", equipo);
            
            return "buscador.html";
        }
    }

}

