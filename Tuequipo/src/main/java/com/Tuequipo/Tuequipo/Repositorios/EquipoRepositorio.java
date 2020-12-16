
package com.Tuequipo.Tuequipo.Repositorios;

import com.Tuequipo.Tuequipo.entidades.Equipo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface EquipoRepositorio extends JpaRepository <Equipo,String>{
 
    @Query ("SELECT c FROM Equipo c  WHERE c.nombre = :nombre")
    public Equipo buscarEquipoPorNombre(@Param("nombre") String nombre);
    
    @Query ("SELECT c FROM Equipo c WHERE c.mail= :mail")
    public Equipo buscarEquipoPorMail(@Param("mail") String mail);
    
    @Query ("SELECT c FROM Equipo c WHERE c.disponible = 1")
    public List<Equipo> buscarEquipoDisponible();
    
}