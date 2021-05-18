package com.example.tarea4_grupo2.service;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class SendMailService {

    @Autowired
    JavaMailSender javaMailSender;

    @PostMapping("/sendMail")
    public void sendMail(String correoDestino, String correoOrigen, String subject, String bodyMensaje) {
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "utf-8");
            messageHelper.setFrom(correoOrigen);
            messageHelper.setTo(correoDestino);
            messageHelper.setSubject(subject);
            messageHelper.setText(bodyMensaje, true);
        };
        javaMailSender.send(mimeMessagePreparator);
    }
}
