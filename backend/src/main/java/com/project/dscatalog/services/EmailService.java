package com.project.dscatalog.services;

import com.project.dscatalog.services.exceptions.EmailException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service responsavel por encapsular o envio de emails da aplicacao.
 * A anotacao @Log4j2 registra eventos de inicio e sucesso do envio.
 */
@Service
@Log4j2
public class EmailService {

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Autowired
    private JavaMailSender emailSender;

    /**
     * Envia um email simples e converte falhas tecnicas em excecao de dominio.
     */
    public void sendEmail(String to, String subject, String msg) {
        try{
            log.info("Enviando email...");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(msg);
            emailSender.send(message);
            log.info("Email enviado com sucesso!");
        }
        catch (MailException e){
            throw new EmailException("Failed to send email");
        }
    }
}
