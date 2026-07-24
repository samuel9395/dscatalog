package com.project.dscatalog.resources;

import com.project.dscatalog.dto.UserDTO;
import com.project.dscatalog.dto.UserInsertDTO;
import com.project.dscatalog.dto.UserUpdateDTO;
import com.project.dscatalog.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST para gerenciamento de usuários.
 */
@RestController
@RequestMapping(value = "/users")
public class UserResource {

    @Autowired
    private UserService service;

    /**
     * Lista usuários de forma paginada.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDTO>> findAll(Pageable pageable) {
        Page<UserDTO> categories = service.findAllPaged(pageable);
        return ResponseEntity.ok().body(categories);
    }

    /**
     * Busca um usuário pelo identificador.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping(value = "/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
        UserDTO dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    /**
     * Busca 'usuário' autenticado.
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
    @GetMapping(value = "/profile")
    public ResponseEntity<UserDTO> findConnectedUser() {
        UserDTO dto = service.findConnectedUser();
        return ResponseEntity.ok().body(dto);
    }

    /**
     * Cria um usuário.
     */
    @PostMapping
    public ResponseEntity<UserDTO> insert(@Valid @RequestBody UserInsertDTO dto) {
        UserDTO newDTO = service.insert(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDTO);
    }

    /**
     * Atualiza os dados de um usuário existente.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        UserDTO newDto = service.update(id, dto);
        return ResponseEntity.ok().body(newDto);
    }

    /**
     * Remove um usuário pelo identificador.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<UserDTO> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
