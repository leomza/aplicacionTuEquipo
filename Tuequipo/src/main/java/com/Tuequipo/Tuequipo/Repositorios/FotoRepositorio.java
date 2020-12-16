package com.Tuequipo.Tuequipo.Repositorios;


import com.Tuequipo.Tuequipo.entidades.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoRepositorio extends JpaRepository<Foto , String>{
    
}
