package com.utp.repository;

import com.utp.model.Asunto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsuntoRepository extends 
        JpaRepository<Asunto, Integer> {
    
}