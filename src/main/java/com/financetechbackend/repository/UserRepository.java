package com.financetechbackend.repository;



import com.financetechbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String username);
    User findByIdUsuario(int id);
    User findByRecuperaSenha(String token);
    @Query("SELECT u FROM User u WHERE u.CodVerifica = ?1")
    User findByVerificationCode(String code);
}
