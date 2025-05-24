package com.utp.repository;

import com.utp.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TamanoRepository extends JpaRepository<Tamano, Integer> {
    Tamano findByNombre(String nombre);
}
