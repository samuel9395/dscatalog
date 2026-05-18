package com.project.dscatalog.repositories;

import com.project.dscatalog.entities.Product;
import com.project.dscatalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class ProductRepositoryTests {

    private long existingId;
    private long nonExistingId;
    private long countTotalProducts;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        countTotalProducts = 25L;
        nonExistingId = 26L;
    }

    @Test
    public void saveShouldIdPersistWithAutoIncrementWhenIdIsNull() {
        Product product = Factory.createProduct();
        product.setId(null);

        product = repository.save(product);

        Assertions.assertNotNull(product.getId());
        Assertions.assertEquals(countTotalProducts + 1, product.getId());
    }

    @Test
    public void deleteShouldDeleteObjectWhenIdExists() {

        repository.deleteById(existingId);

        Optional <Product> result = repository.findById(existingId);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnNotEmptyForIdThatNotExist() {

        Optional <Product> result = repository.findById(existingId);
        Assertions.assertEquals(result.isPresent(), true);
        System.out.println("Result: " + result.get().getName());
    }

    @Test
    public void shouldReturnEmptyForIdThatNotExist() {

        Optional <Product> result = repository.findById(nonExistingId);
        Assertions.assertTrue(result.isEmpty());
    }

}
