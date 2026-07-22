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



            String html =

                    "<html>" +
                    "<body style='font-family: Arial, sans-serif;'>" +


                    "<h2>Olá!</h2>" +


                    "<p>Recebemos um cadastro no <b>Traço Cultural</b>.</p>" +


                    "<p>Foi você quem criou essa conta? Digite o código abaixo no app para confirmar:</p>" +


                    "<br>" +


                    "<div style='" +
                    "background-color:#6c63ff;" +
                    "color:white;" +
                    "padding:16px 30px;" +
                    "border-radius:8px;" +
                    "font-weight:bold;" +
                    "font-size:28px;" +
                    "letter-spacing:6px;" +
                    "display:inline-block;" +
                    "'>" +

                    codigo +

                    "</div>" +


                    "<br><br>" +


                    "<p>Esse código é válido por 15 minutos.</p>" +


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