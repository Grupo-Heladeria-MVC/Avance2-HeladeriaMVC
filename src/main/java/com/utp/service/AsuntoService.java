package com.utp.heladeriaBreezemvc.service;

import com.utp.heladeriaBreezemvc.model.Asunto;
import com.utp.heladeriaBreezemvc.repository.AsuntoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AsuntoService {
    @Autowired
    private AsuntoRepository asuntoRepository;

    // Método para obtener todos los asuntos
    public List<Asunto> getAllAsuntos() {
        return asuntoRepository.findAll();
    }

    // Método para obtener un asunto por ID
    public Optional<Asunto> getAsuntoById(Integer id) {
        return asuntoRepository.findById(id);
    }
}
