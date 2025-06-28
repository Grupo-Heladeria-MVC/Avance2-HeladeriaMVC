package com.utp.config;

import com.utp.model.Categoria;
import com.utp.model.Subcategoria;
import com.utp.repository.CategoriaRepository;
import com.utp.repository.SubcategoriaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CategoriaInitializer {
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private SubcategoriaRepository subcategoriaRepository;
    
    @PostConstruct
    public void init() {
        initializeCategorias();
    }
    
    private void initializeCategorias() {
        // Verificar si ya existen categorías
        if (categoriaRepository.count() > 0) {
            return; // Ya hay datos, no inicializar
        }
        
        // Crear categorías con sus subcategorías (las existentes + 1 nueva por categoría)
        createCategoriaWithSubcategorias("Helados", 
            Arrays.asList("Helados Artesanales", "Helados Gourmet", "Helados Veganos"));
            
        createCategoriaWithSubcategorias("Cremoladas", 
            Arrays.asList("Cremoladas Frutales", "Cremoladas Exóticas", "Cremoladas Naturales"));
            
        createCategoriaWithSubcategorias("Milshakes", 
            Arrays.asList("Milshakes Clásicos", "Milshakes Especiales", "Milshakes Proteicos"));
            
        createCategoriaWithSubcategorias("Yogures", 
            Arrays.asList("Yogures Gourmet", "Yogures Griegos", "Yogures Probióticos"));
    }
    
    private void createCategoriaWithSubcategorias(String nombreCategoria, List<String> nombresSubcategorias) {
        // Verificar si la categoría ya existe
        if (categoriaRepository.findByNombre(nombreCategoria) != null) {
            return;
        }
        
        // Crear la categoría
        Categoria categoria = new Categoria();
        categoria.setNombre(nombreCategoria);
        categoria.setSubcategorias(new ArrayList<>());
        
        // Guardar la categoría primero
        categoria = categoriaRepository.save(categoria);
        
        // Crear las subcategorías
        for (String nombreSubcategoria : nombresSubcategorias) {
            Subcategoria subcategoria = new Subcategoria();
            subcategoria.setNombre(nombreSubcategoria);
            subcategoria.setCategoria(categoria);
            
            subcategoriaRepository.save(subcategoria);
            categoria.getSubcategorias().add(subcategoria);
        }
        
        // Actualizar la categoría con las subcategorías
        categoriaRepository.save(categoria);
    }
}