package com.financetechbackend.service;

import com.financetechbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class EmailService {
    @Autowired
    private JavaMailSender sender;

    public void welcome(User user){

        var emailDestino = user.getEmail();
        var assunto = "Confirmação de Cadastro";
        var message = "Olá, " + user.getNome() + "!" +
                "\n\nObrigado por se cadastrar no Plannic!";
        var email = new SimpleMailMessage();
        email.setTo(emailDestino);
        email.setSubject(assunto);
        email.setFrom("no-reply@plannic.com");
        email.setText(message);
        sender.send(email);
    }
}
