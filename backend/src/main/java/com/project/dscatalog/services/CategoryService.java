package com.project.dscatalog.services;

import com.project.dscatalog.dto.CategoryDTO;
import com.project.dscatalog.entities.Category;
import com.project.dscatalog.repositories.CategoryRepository;
import com.project.dscatalog.services.exceptions.DatabaseException;
import com.project.dscatalog.services.exceptions.ResourceEntityNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    @Autowired
    private CategoryRepository repository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        List<Category> list = repository.findAll();
        return list.stream().map(CategoryDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Optional<Category> obj = repository.findById(id);
        Category entity = obj.orElseThrow(() -> new ResourceEntityNotFoundException("Entity not found with id: " + id));
        return new CategoryDTO(entity);
    }

    @Transactional
    public CategoryDTO insert(CategoryDTO dto) {
        Category entity = new Category();
        entity.setName(dto.getName());
        entity = repository.save(entity);
        return new CategoryDTO(entity);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        try {
            Category entity = repository.getReferenceById(id);
            entity.setName(dto.getName());
            entity = repository.save(entity);
            return new CategoryDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceEntityNotFoundException("Entity not found with id: " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) throws DatabaseException {
        if (!repository.existsById(id)) {
            throw new ResourceEntityNotFoundException("Entity not found with id: " + id);
        }
        try {
            repository.deleteById(id);
            log.info("Delete category with id: " + id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Referential integrity failure!");
        }
    }
}
