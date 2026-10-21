package com.project.dscatalog;

import com.project.dscatalog.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicacao Spring Boot.
 */
@SpringBootApplication
public class DscatalogApplication implements CommandLineRunner {

    @Autowired
    private S3Service s3Service;

    /**
     * Inicializa o contexto Spring e sobe a API.
     */
    public static void main(String[] args) {
        SpringApplication.run(DscatalogApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        s3Service.uploadFile("/home/samuel-freitas/workstation/file.txt");
    }
}

