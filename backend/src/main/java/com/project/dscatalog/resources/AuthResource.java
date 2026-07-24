package com.project.dscatalog.resources;

import com.project.dscatalog.dto.EmailDTO;
import com.project.dscatalog.dto.NewPasswordDTO;
import com.project.dscatalog.entities.User;
import com.project.dscatalog.repositories.UserRepository;
import com.project.dscatalog.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de autenticação e recuperação de senha.
 */
@RestController
@RequestMapping(value = "/auth")
public class AuthResource {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthResource(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    /**
     * Gera um ‘token’ temporário para recuperação de senha e dispara o envio por email.
     */
    @PostMapping(value = "/recover-token")
    public ResponseEntity<Void> createRecoverToken(@Valid @RequestBody EmailDTO emailDTO) {
        authService.createRecoverToken(emailDTO);
        return ResponseEntity.noContent().build();
    }

    /**
     * Atualiza a senha do usuário com base num ‘token’ de recuperação valido.
     */
    @PutMapping(value = "/new-password")
    public ResponseEntity<Void> saveNewPassword(@Valid @RequestBody NewPasswordDTO newPasswordDTO) {
        authService.saveNewPassword(newPasswordDTO);
        return ResponseEntity.noContent().build();
    }
}
