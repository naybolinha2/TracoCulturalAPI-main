package com.TracoCultural.TracoCultural.model.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {


    @Autowired
    private JavaMailSender mailSender;



    public void enviarEmailConfirmacao(String email, String codigo) {


        try {


            MimeMessage mensagem =
                    mailSender.createMimeMessage();



            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mensagem,
                            true,
                            "UTF-8"
                    );



            helper.setTo(email);


            helper.setSubject(
                    "Confirme seu cadastro - Traço Cultural"
            );



            // Depois que publicar o backend,
            // troque essa URL pela URL real da API
            String link =
                    "http://localhost:8080/api/v1/auth/confirmar-email/"
                    + codigo;



            String html =

                    "<html>" +
                    "<body style='font-family: Arial, sans-serif;'>" +


                    "<h2>Olá!</h2>" +


                    "<p>Recebemos um cadastro no <b>Traço Cultural</b>.</p>" +


                    "<p>Foi você quem criou essa conta?</p>" +


                    "<br>" +


                    "<a href='" + link + "' " +
                    "style='" +
                    "background-color:#6c63ff;" +
                    "color:white;" +
                    "padding:12px 25px;" +
                    "text-decoration:none;" +
                    "border-radius:8px;" +
                    "font-weight:bold;" +
                    "display:inline-block;" +
                    "'>" +

                    "SIM, FUI EU" +

                    "</a>" +


                    "<br><br>" +


                    "<p>Caso você não tenha criado essa conta, ignore este email.</p>" +


                    "<p>Equipe Traço Cultural</p>" +


                    "</body>" +
                    "</html>";



            helper.setText(
                    html,
                    true
            );



            mailSender.send(mensagem);



        } catch (MessagingException e) {


            throw new RuntimeException(
                    "Erro ao enviar email",
                    e
            );

        }

    }

}