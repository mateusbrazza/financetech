package com.financetechbackend.controller;


import com.financetechbackend.model.User;
import com.financetechbackend.service.EmailService;
import com.financetechbackend.service.UserService;
import freemarker.template.TemplateException;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private EmailService emailService;

    @Autowired
    //TODO voltar a parte do email
    public UserController(UserService userService,
                          EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    @ApiOperation(value = "Realiza o cadastro do usuario")
    public ResponseEntity<User> save(@RequestBody User user, HttpServletRequest request)
            throws IOException, MessagingException, TemplateException {
        try {
            MDC.put("name", user.getName());
            MDC.put("fluxo", "POST save");
           emailService.welcome(user);
            userService.save(user,getSiteURL(request));
        }  finally{
            MDC.clear();
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    @ApiOperation(value = "Realiza a verificação do email do usuario assim que o mesmo se cadastra")
    public ResponseEntity<?> verifyUser(@Param("code") String code) {
        if (userService.verify(code)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("https://plannic.herokuapp.com/login"))
                    .build();
        } else {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .location(URI.create("https://plannic.herokuapp.com/email"))
                    .build();
        }
    }


    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }


    @PutMapping
    @ApiOperation(value = "Atualiza dados cadastrados do usuario")
    public ResponseEntity update(@RequestBody User user) {
        try {
            MDC.put("user_id", user.getIdUser());
            MDC.put("name", user.getName());
            MDC.put("fluxo", "PUT update");
            if(userService.update(user)) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }finally{
            MDC.clear();
        }
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }

    @PutMapping("/redefinicao")
    @ApiOperation(value = "Realiza a redefinicao de senha do usuario")
    public ResponseEntity updatePassword( @RequestBody User user) {
        try {
//            MDC.put("user_id", usuario.getIdUsuario());
//            MDC.put("name", usuario.getNome());
            MDC.put("fluxo", "PUT update");
            if(userService.updatePassword(user.getIdUser(), user.getPassword())) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }finally{
            MDC.clear();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/atualizaverificaemail")
    @ApiOperation(value = "Envia novamente o email para o usuario caso ele nao tenha recebido o email de verificacao")
    public ResponseEntity<User> updateCodeVerifica(@RequestBody User user, HttpServletRequest request)
            throws IOException, MessagingException, TemplateException {
        try {
//            MDC.put("name", usuario.getNome());
//            MDC.put("fluxo", "POST save");
//            emailService.welcome(usuario);
            if(userService.updateCodigoVerifica(user,getSiteURL(request))) {
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        }finally{
            MDC.clear();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Realiza a delecao do usuario pelo id")
    public ResponseEntity delete(@PathVariable("id") int id) {
        try {
//            MDC.put("user_id", usuario.getIdUsuario());
//            MDC.put("name", usuario.getNome());
            MDC.put("fluxo", "DELETE delete");
            if (userService.delete(id)) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }finally{
            MDC.clear();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    @ApiOperation(value = "Realiza a busca de todos os usuarios cadastrados")
    public ResponseEntity getAll() {
        try{
            MDC.put("fluxo", "GET usuarios");
            return new ResponseEntity<>(userService.getAll(), HttpStatus.OK);
        }finally {
            MDC.clear();
        }
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Realiza a busca de acordo com o id informado")
    public ResponseEntity getUser(@PathVariable("id") int id) {
        try{
            MDC.put("fluxo", "GET usuarios");
            return new ResponseEntity<>(userService.getUser(id), HttpStatus.OK);
        }finally {
            MDC.clear();
        }
    }





}
