package com.project.dscatalog.services;

import com.project.dscatalog.dto.EmailDTO;
import com.project.dscatalog.dto.NewPasswordDTO;
import com.project.dscatalog.entities.PasswordRecover;
import com.project.dscatalog.entities.User;
import com.project.dscatalog.repositories.PasswordRecoverRepository;
import com.project.dscatalog.repositories.UserRepository;
import com.project.dscatalog.services.exceptions.ResourceEntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Regras de autenticacao auxiliares, como recuperacao e redefinicao de senha.
 */
@Service
@Log4j2
public class AuthService {

    @Value("${email.password-recover.token.minutes}")
    private Long tokenMinutes;

    @Value("${email.remetente}")
    private String remetente;

    @Value("${email.to}")
    private String to;

    @Value("${email.title}")
    private String title;

    @Value("${email.message}")
    private String message;

    @Value("${email.link}")
    private String recoverUri;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordRecoverRepository passwordRecoverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Gera e persiste um token temporario para recuperacao de senha.
     * Em seguida monta a mensagem e solicita o envio por email.
     */
    @Transactional
    public void createRecoverToken(EmailDTO body) {

        User user = userRepository.findByEmail(body.getEmail());
        if (user == null) {
            throw new ResourceEntityNotFoundException("Email nao encontrado!");
        }

        String token = UUID.randomUUID().toString();

        PasswordRecover entity = new PasswordRecover();
        entity.setEmail(body.getEmail());
        entity.setToken(token);
        entity.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));
        entity = passwordRecoverRepository.save(entity);

        String msg = remetente + "\n\n"
                + message + "\n\n" + recoverUri + token
                + "\n\nToken válido por " + tokenMinutes + " minutos.";

        emailService.sendEmail(to, title, msg);
    }

    /**
     * Valida o token de recuperacao e salva a nova senha criptografada.
     */
    @Transactional
    public void saveNewPassword(NewPasswordDTO body) {
        List<PasswordRecover> result = passwordRecoverRepository.searchValidTokens(body.getToken(), Instant.now());
        if (result.isEmpty()) {
            throw new ResourceEntityNotFoundException("Token nao encontrado!");
        }

        User user = userRepository.findByEmail(result.get(0).getEmail());
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        user = userRepository.save(user);
    }

    /**
     * Busca por email a entidade do usuário autenticado.
     * @return
     */
    protected User authenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            String username = jwtPrincipal.getClaim("username");
            return userRepository.findByEmail(username);
        } catch (Exception e) {
            throw new UsernameNotFoundException("Invalid user");
        }
    }
}
