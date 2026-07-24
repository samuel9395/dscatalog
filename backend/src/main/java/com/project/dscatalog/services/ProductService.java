package com.project.dscatalog.services;


import com.project.dscatalog.dto.CategoryDTO;
import com.project.dscatalog.dto.ProductDTO;
import com.project.dscatalog.entities.Category;
import com.project.dscatalog.entities.Product;
import com.project.dscatalog.projections.ProductProjection;
import com.project.dscatalog.repositories.CategoryRepository;
import com.project.dscatalog.repositories.ProductRepository;
import com.project.dscatalog.services.exceptions.DatabaseException;
import com.project.dscatalog.services.exceptions.ResourceEntityNotFoundException;
import com.project.dscatalog.util.Utils;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Camada de servico para operacoes de produtos, filtros e mapeamento para DTO.
 */
@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Lista produtos de forma paginada sem filtros.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageable) {
        Page<Product> list = repository.findAll(pageable);
        return list.map(ProductDTO::new);
    }

    /**
     * Busca um produto por id e retorna o DTO com categorias.
     */
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> obj = repository.findById(id);
        Product entity = obj.orElseThrow(() -> new ResourceEntityNotFoundException("Entity not found with id: " + id));
        return new ProductDTO(entity, entity.getCategories());
    }

    /**
     * Insere um novo produto.
     */
    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return new ProductDTO(entity);
    }

    /**
     * Atualiza os dados de um produto existente.
     */
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            Product entity = repository.getReferenceById(id);
            entity.setName(dto.getName());
            copyDtoToEntity(dto, entity);
            return new ProductDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceEntityNotFoundException("Entity not found with id: " + id);
        }
    }

    /**
     * Remove um produto e trata erros de integridade referencial.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) throws DatabaseException {
        if (!repository.existsById(id)) {
            throw new ResourceEntityNotFoundException("Entity not found with id: " + id);
        }
        try {
            repository.deleteById(id);
            log.info("Delete Product with id: " + id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Referential integrity failure!");
        }
    }

    /**
     * Lista produtos com filtros e evita N+1 usando consulta em duas etapas.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(String name, String categoryId, Pageable pageable) {

        // Converte a String "1,2,3" em uma lista de IDs de categoria.
        // Se nenhum filtro for informado, utiliza uma lista vazia.
        List<Long> categoryIds = Arrays.asList();
        if (categoryId != null && !categoryId.isEmpty()) {
            categoryIds = Arrays.stream(categoryId.split(","))
                    .map(Long::parseLong)
                    .toList();
        }

        // Primeira consulta: busca apenas os dados necessários para a paginação
        // (projeção com os IDs dos produtos da página atual).
        Page<ProductProjection> page = repository.searchProducts(categoryIds, name, pageable);

        // Extrai os IDs retornados pela primeira consulta.
        List<Long> productsIds = page.map(ProductProjection::getId).toList();

        // Segunda consulta: carrega as entidades completas com suas categorias
        // usando JOIN FETCH, evitando o problema de N+1 consultas.
        // Lista desordenada vinda do banco.
        List<Product> entities = repository.searchProductsWithCategories(productsIds);

        // Reorganiza as entidades na mesma ordem da consulta paginada,
        // preservando a ordenação original dos resultados.
        // Aqui foi feito o Cast de List<Product>, pois o metodo usado é genérico.
        // Lista convertida em lista ordenada, com base na ordenação da página.
        entities = (List<Product>) Utils.replace(page.getContent(), entities);

        // Converte as entidades para DTOs.
        List<ProductDTO> dtos = entities.stream()
                .map(product -> new ProductDTO(product, product.getCategories()))
                .toList();

        // Reconstrói a página utilizando os DTOs, preservando as informações
        // de paginação e a quantidade total de registros.
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    /**
     * Copia os dados do DTO para a entidade de produto.
     */
    private void copyDtoToEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());

        entity.getCategories().clear();
        for (CategoryDTO catDTO : dto.getCategories()) {
            Category category = categoryRepository.getOne(catDTO.getId());
            entity.getCategories().add(category);
        }
    }

}
