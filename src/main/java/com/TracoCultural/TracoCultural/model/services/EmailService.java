package com.TracoCultural.TracoCultural.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {


    @Autowired
    private JavaMailSender mailSender;


    public void enviarEmailConfirmacao(String email, String codigo) {


        SimpleMailMessage mensagem = new SimpleMailMessage();


        mensagem.setTo(email);

        mensagem.setSubject("Código de confirmação - Traço Cultural");


        mensagem.setText(
                "Olá!\n\n" +
                "Obrigado por se cadastrar no Traço Cultural.\n\n" +
                "Seu código de confirmação é:\n\n" +
                codigo +
                "\n\nDigite esse código no aplicativo para confirmar seu email.\n\n" +
                "Caso você não tenha criado essa conta, ignore este email."
        );


        mailSender.send(mensagem);

    }

}