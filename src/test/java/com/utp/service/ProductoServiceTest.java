package com.utp.service;


import com.utp.model.*;
import com.utp.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SubcategoriaRepository subcategoriaRepository;

    @Mock
    private LocalRepository localRepository;

    @Mock
    private TamanoRepository tamanoRepository;

    @Mock
    private PrecioTamanoRepository precioTamanoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto1;
    private Producto producto2;
    private Subcategoria subcategoria;
    private Tamano tamano;
    private PrecioTamano precioTamano;

    @BeforeEach
    void setUp() {
        // Configuración de datos de prueba
        subcategoria = new Subcategoria();
        subcategoria.setId(1);
        subcategoria.setNombre("Helados Artesanales");

        producto1 = new Producto();
        producto1.setId(1);
        producto1.setNombreProducto("Helado de Vainilla");
        producto1.setDescripcion("Delicioso helado de vainilla natural");
        producto1.setPrecio(15.50);
        producto1.setStockOpenPlaza(20.0);
        producto1.setStockUDEP(15.0);
        producto1.setStockActual(35.0);
        producto1.setDisponiblePorTemporada(true);
        producto1.setSubcategoria(subcategoria);
        producto1.setCreatedAt(LocalDateTime.now());

        producto2 = new Producto();
        producto2.setId(2);
        producto2.setNombreProducto("Helado de Chocolate");
        producto2.setDescripcion("Rico helado de chocolate belga");
        producto2.setPrecio(18.00);
        producto2.setStockOpenPlaza(10.5);
        producto2.setStockUDEP(25.0);
        producto2.setStockActual(35.0);
        producto2.setDisponiblePorTemporada(true);
        producto2.setSubcategoria(subcategoria);
        producto2.setCreatedAt(LocalDateTime.now().minusDays(1));

        tamano = new Tamano();
        tamano.setId(1);
        tamano.setNombre("Mediano");

        precioTamano = new PrecioTamano();
        precioTamano.setId(1);
        precioTamano.setProducto(producto1);
        precioTamano.setTamano(tamano);
        precioTamano.setPrecio(20.00);
    }

    @Test
    @DisplayName("Test guardar producto con subcategoría")
    void saveProductoConSubcategoriaTest() {
        // Given
        Integer subcategoriaId = 1;
        when(subcategoriaRepository.findById(subcategoriaId)).thenReturn(Optional.of(subcategoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto1);

        // When
        Producto result = productoService.saveProducto(producto1, subcategoriaId);

        // Then
        assertNotNull(result);
        assertEquals("Helado de Vainilla", result.getNombreProducto());
        assertEquals(35, result.getStockActual()); // stockOpenPlaza + stockUDEP
        assertEquals(subcategoria, result.getSubcategoria());
        verify(subcategoriaRepository).findById(subcategoriaId);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("Test guardar producto con subcategoría inexistente")
    void saveProductoConSubcategoriaInexistenteTest() {
        // Given
        Integer subcategoriaId = 999;
        when(subcategoriaRepository.findById(subcategoriaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            productoService.saveProducto(producto1, subcategoriaId);
        });
        verify(subcategoriaRepository).findById(subcategoriaId);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Test guardar producto con precio negativo")
    void saveProductoConPrecioNegativoTest() {
        // Given
        Integer subcategoriaId = 1;
        producto1.setPrecio(-10.0);
        when(subcategoriaRepository.findById(subcategoriaId)).thenReturn(Optional.of(subcategoria));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.saveProducto(producto1, subcategoriaId);
        });
        verify(subcategoriaRepository).findById(subcategoriaId);
        verify(productoRepository, never()).save(any(Producto.class));
    }


    @Test
    @DisplayName("Test obtener producto por ID existente")
    void getProductoByIdExistenteTest() {
        // Given
        Integer id = 1;
        when(productoRepository.findById(id)).thenReturn(Optional.of(producto1));

        // When
        Producto result = productoService.getProductoById(id);

        // Then
        assertNotNull(result);
        assertEquals("Helado de Vainilla", result.getNombreProducto());
        assertEquals(15.50, result.getPrecio());
        verify(productoRepository).findById(id);
    }

    @Test
    @DisplayName("Test obtener producto por ID inexistente")
    void getProductoByIdInexistenteTest() {
        // Given
        Integer id = 999;
        when(productoRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            productoService.getProductoById(id);
        });
        verify(productoRepository).findById(id);
    }

    @Test
    @DisplayName("Test asignar precio tamaño")
    void asignarPrecioTamanoTest() {
        // Given
        Integer productoId = 1;
        Integer tamanoId = 1;
        Double precio = 20.00;
        
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(producto1));
        when(tamanoRepository.findById(tamanoId)).thenReturn(Optional.of(tamano));
        when(precioTamanoRepository.save(any(PrecioTamano.class))).thenReturn(precioTamano);

        // When
        PrecioTamano result = productoService.asignarPrecioTamano(productoId, tamanoId, precio);

        // Then
        assertNotNull(result);
        assertEquals(precio, result.getPrecio());
        assertEquals(producto1, result.getProducto());
        assertEquals(tamano, result.getTamano());
        verify(productoRepository).findById(productoId);
        verify(tamanoRepository).findById(tamanoId);
        verify(precioTamanoRepository).save(any(PrecioTamano.class));
    }

    @Test
    @DisplayName("Test actualizar producto existente")
    void updateProductoExistenteTest() {
        // Given
        Integer id = 1;
        Producto updatedProducto = new Producto();
        updatedProducto.setNombreProducto("Helado de Vainilla Premium");
        updatedProducto.setDescripcion("Helado premium de vainilla");
        updatedProducto.setPrecio(25.00);
        updatedProducto.setStockOpenPlaza(30.0);
        updatedProducto.setStockUDEP(20.0);
        updatedProducto.setDisponiblePorTemporada(true);
        updatedProducto.setSubcategoria(subcategoria);

        when(productoRepository.findById(id)).thenReturn(Optional.of(producto1));
        when(productoRepository.findByNombreProducto("Helado de Vainilla Premium")).thenReturn(null);
        when(productoRepository.save(any(Producto.class))).thenReturn(updatedProducto);

        // When
        Producto result = productoService.updateProducto(id, updatedProducto);

        // Then
        assertNotNull(result);
        assertEquals("Helado de Vainilla Premium", result.getNombreProducto());
        assertEquals(25.00, result.getPrecio());
        verify(productoRepository).findById(id);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("Test actualizar producto con nombre duplicado")
    void updateProductoConNombreDuplicadoTest() {
        // Given
        Integer id = 1;
        Producto updatedProducto = new Producto();
        updatedProducto.setNombreProducto("Helado de Chocolate");
        updatedProducto.setPrecio(20.00);
        updatedProducto.setStockOpenPlaza(10.0);
        updatedProducto.setStockUDEP(15.0);

        when(productoRepository.findById(id)).thenReturn(Optional.of(producto1));
        when(productoRepository.findByNombreProducto("Helado de Chocolate")).thenReturn(producto2);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.updateProducto(id, updatedProducto);
        });
        verify(productoRepository).findById(id);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Test eliminar producto")
    void deleteProductoTest() {
        // Given
        Integer id = 1;
        doNothing().when(productoRepository).deleteById(id);

        // When
        productoService.deleteProducto(id);

        // Then
        verify(productoRepository).deleteById(id);
    }

    @Test
    @DisplayName("Test obtener últimos productos")
    void getLatestProductosTest() {
        // Given
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoRepository.findTop6ByOrderByCreatedAtDesc()).thenReturn(productos);

        // When
        List<Producto> result = productoService.getLatestProductos();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productoRepository).findTop6ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Test obtener todos los productos paginados")
    void getAllProductosPaginadosTest() {
        // Given
        List<Producto> productos = Arrays.asList(producto1, producto2);
        Page<Producto> page = new PageImpl<>(productos);
        Pageable pageable = PageRequest.of(0, 10);
        when(productoRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);

        // When
        Page<Producto> result = productoService.getAllProductos(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(productoRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    @DisplayName("Test obtener productos disponibles")
    void getAllProductosDisponiblesTest() {
        // Given
        List<Producto> productos = Arrays.asList(producto1, producto2);
        Page<Producto> page = new PageImpl<>(productos);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(productoRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);
        when(productoRepository.findAll()).thenReturn(productos);

        // When
        Page<Producto> result = productoService.getAllProductosDisponibles(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(productoRepository).findAllByOrderByCreatedAtDesc(pageable);
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("Test buscar todos los productos")
    void findAllTest() {
        // Given
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoRepository.findAll()).thenReturn(productos);

        // When
        List<Producto> result = productoService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("Test buscar productos por subcategoría")
    void findBySubcategoriaIdTest() {
        // Given
        Integer subcategoriaId = 1;
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoRepository.findBySubcategoriaId(subcategoriaId)).thenReturn(productos);

        // When
        List<Producto> result = productoService.findBySubcategoriaId(subcategoriaId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productoRepository).findBySubcategoriaId(subcategoriaId);
    }

    @Test
    @DisplayName("Test obtener producto por nombre")
    void getProductoByNombreTest() {
        // Given
        String nombreProducto = "Helado de Vainilla";
        when(productoRepository.findByNombreProducto(nombreProducto)).thenReturn(producto1);

        // When
        Producto result = productoService.getProductoByNombre(nombreProducto);

        // Then
        assertNotNull(result);
        assertEquals(nombreProducto, result.getNombreProducto());
        verify(productoRepository).findByNombreProducto(nombreProducto);
    }

    @Test
    @DisplayName("Test buscar productos por nombre")
    void buscarProductosTest() {
        // Given
        String nombre = "Helado";
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoRepository.findByNombreContainingIgnoreCase(nombre)).thenReturn(productos);

        // When
        List<Producto> result = productoService.buscarProductos(nombre);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productoRepository).findByNombreContainingIgnoreCase(nombre);
    }

    @Test
    @DisplayName("Test obtener producto opcional")
    void getOptionalProductoTest() {
        // Given
        Integer id = 1;
        when(productoRepository.findById(id)).thenReturn(Optional.of(producto1));

        // When
        Optional<Producto> result = productoService.get(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Helado de Vainilla", result.get().getNombreProducto());
        verify(productoRepository).findById(id);
    }

    @Test
    @DisplayName("Test obtener producto opcional inexistente")
    void getOptionalProductoInexistenteTest() {
        // Given
        Integer id = 999;
        when(productoRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<Producto> result = productoService.get(id);

        // Then
        assertFalse(result.isPresent());
        verify(productoRepository).findById(id);
    }
}