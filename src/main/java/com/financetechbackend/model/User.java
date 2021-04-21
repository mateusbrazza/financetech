package com.financetechbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class User {

    @Id
    @Column(name = "idUser")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idUser;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "codVerify", length = 64)
    private String codVerify;

    @Column(name="active")
    private boolean active;

    @Column(name="resetPw")
    private String resetPw;
//
//    @OneToMany
//    List<NotasTutor> notasTutor;
}
