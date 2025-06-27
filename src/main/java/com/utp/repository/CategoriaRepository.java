package com.utp.repository;

import com.utp.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends 
        JpaRepository<Categoria, Integer>{
    
     // Método para buscar por nombre (necesario para el inicializador)
    Categoria findByNombre(String nombre);
    
}
