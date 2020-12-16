package com.Tuequipo.Tuequipo.servicios;
import com.Tuequipo.Tuequipo.Servicios.FotoServicio;
import com.Tuequipo.Tuequipo.Enumeracion.CantidadJugadores;
import com.Tuequipo.Tuequipo.Enumeracion.Categoria;
import com.Tuequipo.Tuequipo.Enumeracion.Dias;
import com.Tuequipo.Tuequipo.Enumeracion.Turno;
import com.Tuequipo.Tuequipo.Enumeracion.Zonas;
import com.Tuequipo.Tuequipo.Errores.ErrorServicio;
import com.Tuequipo.Tuequipo.Repositorios.EquipoRepositorio;
import com.Tuequipo.Tuequipo.entidades.Equipo;
import com.Tuequipo.Tuequipo.entidades.Foto;
import com.Tuequipo.Tuequipo.enumeracion.Tipo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EquipoServicio implements UserDetailsService {

    @Autowired
    private EquipoRepositorio equipoRepositorio;

    @Autowired
    private FotoServicio fotoServicio;
    
    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    public void cargaEquipo(MultipartFile archivo, String nombre, String mail, String descripcion, String clave1, String clave2, String telefono1, String telefono2, Turno turno, Zonas zona, Dias dia, Tipo tipo, Categoria categoria, CantidadJugadores cantidadJugadores) throws ErrorServicio {
        
        validar(nombre, clave1, clave2, telefono1, telefono2);
        
        Equipo equipo = new Equipo();
        
        equipo.setNombre(nombre);
        equipo.setMail(mail);
        equipo.setDescripcion(descripcion);
        String encriptada = new BCryptPasswordEncoder().encode(clave1);
        equipo.setClave(encriptada);
        equipo.setNumero1(telefono1);
        equipo.setNumero2(telefono2);
        equipo.setTurno(turno);
        equipo.setZona(zona);
        equipo.setDia(dia);
        equipo.setTipo(tipo);
        equipo.setCategoria(categoria);
        equipo.setCantidadJugadores(cantidadJugadores);
        equipo.setDisponible(Boolean.TRUE);
        equipo.setAlta(new Date());
        
        if(!archivo.isEmpty()){
            Foto foto = fotoServicio.guardar(archivo);
            equipo.setFoto(foto);
        }
        String subject = "Inscripcion de tu equipo";

        String content = "Gracias por registrarse " + equipo.getNombre() + "!";        
        sendEmail(mail, subject, content);


        equipoRepositorio.save(equipo);
    }

    @Transactional
    public void modificarEquipo(MultipartFile archivo, String nombre, String descripcion, String clave1, String clave2, String telefono1, String telefono2, Turno turno, Zonas zona, Dias dia, CantidadJugadores cantidadJugadores) throws ErrorServicio {
        
        revalidar( clave1, clave2, telefono1, telefono2);
        
        Optional<Equipo> respuesta = equipoRepositorio.findById(nombre);
        if (respuesta.isPresent()) {
            
            Equipo equipo = respuesta.get();
            
            equipo.setDescripcion(descripcion);
            String encriptada = new BCryptPasswordEncoder().encode(clave1);
            equipo.setClave(encriptada);
            equipo.setNumero1(telefono1);
            equipo.setNumero2(telefono2);
            equipo.setTurno(turno);
            equipo.setZona(zona);
            equipo.setDia(dia);
            equipo.setCantidadJugadores(cantidadJugadores);
           
            if(!archivo.isEmpty()){
                String idFoto = null;
                
                if(equipo.getFoto() != null){
                    idFoto = equipo.getFoto().getId();
                }

                Foto foto = fotoServicio.actualizar(idFoto, archivo);
                equipo.setFoto(foto);
            }
            
            equipoRepositorio.save(equipo);
        } else {
            throw new ErrorServicio("No se encontro el equipo solicitado");
        }
    }

    @Transactional
    public void habilitar(String nombre) throws ErrorServicio {

        Optional<Equipo> respuesta = equipoRepositorio.findById(nombre);

        if (respuesta.isPresent()) {
            Equipo equipo = respuesta.get();
            
            equipo.setBaja(null);
            equipo.setDisponible(Boolean.TRUE);
            
            equipoRepositorio.save(equipo);
        } else {
            throw new ErrorServicio("No se encontro el equipo solicitado");
        }
    }

    @Transactional
    public void deshabilitar(String nombre) throws ErrorServicio {
        Optional<Equipo> respuesta = equipoRepositorio.findById(nombre);
        if (respuesta.isPresent()) {
            Equipo equipo = respuesta.get();
            equipo.setBaja(new Date());
            equipo.setDisponible(Boolean.FALSE);
            equipoRepositorio.save(equipo);
        }
    }
    
    @Transactional
    public void eliminarEquipo(String nombre) throws ErrorServicio {
        Optional<Equipo> respuesta = equipoRepositorio.findById(nombre);
        if (respuesta.isPresent()) {
            Equipo equipo = respuesta.get();
            String subject = "Se elimino tu equipo";
            String content = "Tu equipo fue eliminado, esperamos que pronto vuelvas a registrarte :(";        
            sendEmail(equipo.getMail(), subject, content);
            equipoRepositorio.delete(equipo);
        }
    }
    
    @Transactional
    public void renovarPass(String nombre) throws ErrorServicio {

        String clave = "tuequipo1234";

        Optional<Equipo> respuesta = equipoRepositorio.findById(nombre);

        if (respuesta.isPresent()) {
            Equipo equipo = respuesta.get();

            String encriptada = new BCryptPasswordEncoder().encode(clave);
            equipo.setClave(encriptada);

            String subject = "Recuperación de contraseña";
            String content = "Tu contraseña provisoria es " + clave;        
            sendEmail(equipo.getMail(), subject, content);

            equipoRepositorio.save(equipo);    
        } else {
            throw new ErrorServicio("No se encontro el equipo solicitado");
        }
    }
    
    public Equipo buscarPorId(String nombre) {
        Optional<Equipo> respuesta = equipoRepositorio.findById(nombre);
        return respuesta.get();
    }
    
    public List<Equipo> buscarDisponibles(){
        return equipoRepositorio.buscarEquipoDisponible();
    }

    private void validar(String nombre,  String clave1, String clave2, String telefono1, String telefono2) throws ErrorServicio { 
        Optional<Equipo> respuesta = equipoRepositorio.findById(nombre);
        if (respuesta.isPresent()){
            throw new ErrorServicio("Ese nombre se encuentra en uso");
        }
        
        if ( clave1.length() <= 6) {
            throw new ErrorServicio("La clave debe tener más de 6 caracteres");
        }
        if (!clave2.equals(clave1)) {
            throw new ErrorServicio("Las claves deben coincidir");
        }
        if (telefono2.equals(telefono1)) {
            throw new ErrorServicio("Los numeros de telefono deben ser distintos");
        }
    }
    
    private void revalidar(  String clave1, String clave2, String telefono1, String telefono2) throws ErrorServicio {
        if ( clave1.length() <= 6) {
            throw new ErrorServicio("La clave debe tener más de 6 caracteres");
        }
        if (!clave2.equals(clave1)) {
            throw new ErrorServicio("Las claves deben coincidir");
        }
        if (telefono2.equals(telefono1)) {
            throw new ErrorServicio("Los numeros de telefono deben ser distintos");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String nombre) throws UsernameNotFoundException {
        Equipo equipo = equipoRepositorio.buscarEquipoPorNombre(nombre);
        if (equipo != null) {

            List<GrantedAuthority> permisos = new ArrayList<>();

            GrantedAuthority p1 = new SimpleGrantedAuthority("ROLE_USUARIO_REGISTRADO");
            permisos.add(p1);

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("usuariosession", equipo);

            User user = new User(equipo.getNombre(), equipo.getClave(), permisos);
            return user;
        } else {
            return null;
        }
    }

    public void sendEmail(String mail, String subject, String content) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(mail);
        email.setSubject(subject);
        email.setText(content);
        mailSender.send(email);
    }
}
