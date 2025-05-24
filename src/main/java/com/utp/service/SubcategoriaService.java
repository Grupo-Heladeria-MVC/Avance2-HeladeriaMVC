package com.utp.service;

import com.utp.model.*;
import com.utp.repository.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubcategoriaService {
    @Autowired
    private SubcategoriaRepository subcategoriaRepository;
    
    public List<Subcategoria> getAllSubcategorias(){
      return subcategoriaRepository.findAll();
  }
    
    public List<Subcategoria> findByCategoriaId(Integer categoriaId) {
        return subcategoriaRepository.findByCategoriaId(categoriaId);
    }
}

