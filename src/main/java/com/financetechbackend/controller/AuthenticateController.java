package com.financetechbackend.controller;



import com.financetechbackend.model.AuthRequest;
import com.financetechbackend.model.User;
import com.financetechbackend.service.UserService;
import com.financetechbackend.util.JwtUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController
@RequestMapping()
public class AuthenticateController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;



    @GetMapping("/")
    public String welcome() {
        return "test aplication";
    }

    private static Logger logger = Logger.getLogger(AuthenticateController.class);

    @PostMapping("authenticate")
    @ApiOperation(value = "Realiza o login do usuario gerando o token")
    public ResponseEntity<?> generateToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {

            logger.info("Autenticando o usuário.");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            User user = userService.findByEmail(authRequest.getEmail());

            if( user.isActive() == true){

            String token = (jwtUtil.generateToken(authRequest.getEmail()));
            HashMap jsonMessage= new HashMap();
            jsonMessage.put("token", token);
            jsonMessage.put("idUser", user.getIdUser());
            jsonMessage.put("name", user.getName());
            return new ResponseEntity<>(jsonMessage, HttpStatus.ACCEPTED);

            }

            return new ResponseEntity<>("Erro ao realizar login, por favor verificar se email esta confirmado",
                    HttpStatus.UNAUTHORIZED);

        } catch (Exception ex) {
            logger.error("Erro na autenticação do usuário");
            throw new Exception("Erro na autenticação do usuário.");

        }

    }
}
