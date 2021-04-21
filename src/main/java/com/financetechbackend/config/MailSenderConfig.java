package com.financetechbackend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailSenderConfig {

    @Bean
    public JavaMailSender getJavaMailSender() {
        var mailSender = new JavaMailSenderImpl();
        mailSender.setHost(System.getenv("SMTP_HOST"));
        mailSender.setPort(587);

        mailSender.setUsername(System.getenv("SMTP_EMAIL"));
        mailSender.setPassword(System.getenv("SMTP_PASSWORD"));

        var props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
