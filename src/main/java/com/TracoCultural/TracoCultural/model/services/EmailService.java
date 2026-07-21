package com.TracoCultural.TracoCultural.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {


    @Autowired
    private JavaMailSender mailSender;


    public void enviarEmailConfirmacao(String email, String token) {


        String link = "http://localhost:8080/api/v1/auth/confirmar-email?token=" + token;


        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(email);
        mensagem.setSubject("Confirmação de cadastro - Traço Cultural");

        mensagem.setText(
                "Olá!\n\n" +
                "Obrigado por se cadastrar no Traço Cultural.\n\n" +
                "Para confirmar seu email, clique no link abaixo:\n\n" +
                link +
                "\n\nCaso você não tenha criado essa conta, ignore este email."
        );


        mailSender.send(mensagem);
    }

}