package com.financetechbackend.service;


import com.financetechbackend.model.User;
import com.financetechbackend.repository.UserRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.bytebuddy.utility.RandomString;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class UserService {

//    @Autowired
//    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UserRepository repository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Configuration config;

    private static Logger logger = Logger.getLogger(UserService.class);


    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> getAll() {
        ModelMapper mapper = new ModelMapper();
        List<User> users = repository.findAll();

        if (!users.isEmpty()) {
            logger.info("Usuários recuperados");
            return users
                    .stream()
                    .map(usuario -> mapper.map(usuario, User.class))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<User> getUser(int id) {
        ModelMapper mapper = new ModelMapper();
        Optional<User> usuarios = Optional.ofNullable(this.repository.findByIdUsuario(id));

        if (!usuarios.isEmpty()) {
            logger.info("Usuário recuperado");
            return usuarios
                    .stream()
                    .map(usuario -> mapper.map(usuario, User.class))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public void save(User user, String url) throws IOException, MessagingException, TemplateException {
        var senha = user.getPassword();
        user.setPassword(passwordEncoder.encode(senha));
        ModelMapper mapper = new ModelMapper();
        String randomCode = RandomString.make(64);
        user.setCodVerify(randomCode);
        user.setAtivo(false);
        var usuarioSalvo = repository.save(mapper.map(user, User.class));

        sendVerificationEmail(user, url);

        MDC.put("user_id", usuarioSalvo.getIdUsuario());
        logger.info("Usuário salvo");
    }

    public void sendVerificationEmail(User user, String url)
            throws MessagingException, IOException, TemplateException {
        String toAddress = user.getEmail();
        String fromAddress = "plannic@plannic.com.br";
        String senderName = "Plannic";
        String subject = "Plannic - Verificação de email";

        Template template = config.getTemplate("emailVerificacao.ftl");
        String verifyURL = url + "/usuario/verify?code=" + user.getCodVerify();

        Map<String,Object> model =new HashMap<>();
        model.put("nome", user.getNome());
        model.put("url",verifyURL);

        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template,model);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        helper.setText(html, true);

        javaMailSender.send(message);

    }

    public boolean verify(String verificationCode) {
        User user = repository.findByVerificationCode(verificationCode);

        if (user == null || user.isAtivo()) {
            return false;
        } else {
            user.setCodVerify(null);
            user.setAtivo(true);
            repository.save(user);

            return true;
        }

    }
    public boolean update(User usuario) {
        Optional<User> usuarios = this.repository.findById(usuario.getIdUsuario());

        if (usuarios.isPresent()) {
            logger.info("Usuário atualizado");
            ModelMapper mapper = new ModelMapper();
            User user = new User(usuario.getIdUsuario(), usuario.getEmail(), usuarios.get().getPassword(), usuario.getNome(), usuario.getData(),usuarios.get().getCodVerify(),usuarios.get().isAtivo(),usuarios.get().getRecuperaSenha());
            repository.save(mapper.map(user, User.class));
            return true;
        }

        return false;
    }

    public boolean updatePassword(int id, String password) {
        User user = this.repository.findByIdUsuario(id);
        if (!password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
            ModelMapper mapper = new ModelMapper();
            var usuarioSalvo = repository.save(mapper.map(user, User.class));
            MDC.put("user_id", usuarioSalvo.getIdUsuario());
            logger.info("Senha atualizada");
            return true;
        }
        return false;
    }

    public boolean updateCodigoVerifica(User user, String url)
            throws IOException, MessagingException, TemplateException {
        User usuarios = this.repository.findByEmail(user.getEmail());

        if (usuarios.getCodVerify() != null && usuarios.isAtivo() ==false) {
            ModelMapper mapper = new ModelMapper();
            String randomCode = RandomString.make(64);
            usuarios.setCodVerify(randomCode);
            usuarios.setAtivo(false);
            var usuarioSalvo = repository.save(mapper.map(usuarios, User.class));
            sendVerificationEmail(usuarios, url);
            MDC.put("user_id", usuarioSalvo.getIdUsuario());
            logger.info("Senha atualizada");
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        Optional<User> usuarios = this.repository.findById(id);

        if (usuarios.isPresent()) {
            logger.info("Usuário deletado");
            this.repository.deleteById(id);
            return true;
        }

        return false;
    }

    public User findByEmail(String email) {
        return this.repository.findByEmail(email);
    }

    public void updateResetPasswordToken(String token, String email) throws MessagingException {
        User user = repository.findByEmail(email);
        if (user != null) {
            user.setRecuperaSenha(token);
            repository.save(user);
        } else {
            throw new MessagingException("Could not find any customer with the email " + email);
        }
    }

    public User getByResetPasswordToken(String token) {
        return repository.findByRecuperaSenha(token);
    }

    public void sendEmail(String recipientEmail, String link)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("contact@shopme.com", "Shopme Support");
        helper.setTo(recipientEmail);

        String subject = "Here's the link to reset your password";

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + link + "\">Change my password</a></p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        javaMailSender.send(message);
    }
}
