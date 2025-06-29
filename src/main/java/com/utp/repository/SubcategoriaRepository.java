package com.utp.repository;

import com.utp.model.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubcategoriaRepository extends
        JpaRepository<Subcategoria, Integer> {

    //BUSCAR POR EL ID_CATEGORIA
    List<Subcategoria> findByCategoriaId(Integer categoriaId);
    
      // Método para buscar por nombre (necesario para el inicializador)
    Subcategoria findByNombre(String nombre);
}

