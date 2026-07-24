package com.project.dscatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Ponto de entrada da aplicacao Spring Boot.
 */
public class DscatalogApplication {

	/**
	 * Inicializa o contexto Spring e sobe a API.
	 */
	public static void main(String[] args) {
		SpringApplication.run(DscatalogApplication.class, args);
	}

}
