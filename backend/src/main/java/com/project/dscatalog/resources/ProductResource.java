package com.project.dscatalog.resources;

import com.project.dscatalog.dto.ProductDTO;
import com.project.dscatalog.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST para consulta e manutencao de produtos.
 */
@RestController
@RequestMapping(value = "/products")
public class ProductResource {

    @Autowired
    private ProductService service;

    /**
     * Lista produtos com suporte a filtro por nome e categorias.
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> findAll(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "categoryId", defaultValue = "") String categoryId,
            Pageable pageable) {
        Page<ProductDTO> projection = service.findAllPaged(name, categoryId, pageable);
        return ResponseEntity.ok().body(projection);
    }

    /**
     * Busca um produto pelo identificador.
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> findById(@PathVariable Long id) {
        ProductDTO dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    /**
     * Cadastra um novo produto.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @PostMapping
    public ResponseEntity<ProductDTO> insert(@Valid @RequestBody ProductDTO dto) {
        dto = service.insert(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Atualiza um produto existente.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        dto = service.update(id, dto);
        return ResponseEntity.ok().body(dto);
    }

    /**
     * Exclui um produto pelo identificador.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
