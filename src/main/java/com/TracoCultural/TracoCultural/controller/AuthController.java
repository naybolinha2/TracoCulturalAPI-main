package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.config.security.JwtUtil;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final int MAX_TENTATIVAS = 5;
    private static final int JANELA_MINUTOS = 10;

    private final Map<String, List<LocalDateTime>> tentativasPorEmail = new ConcurrentHashMap<>();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // POST /api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Usuario usuario) {

        // Verifica se os campos obrigatórios foram preenchidos
        if (usuario.getEmail() == null ||
                usuario.getSenha() == null ||
                usuario.getNome() == null) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", 400,
                            "retorno", "Bad Request",
                            "message", "Nome, email e senha são obrigatórios"
                    )
            );
        }

        // Validação de senha forte
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

        if (!usuario.getSenha().matches(regex)) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", 400,
                            "retorno", "Bad Request",
                            "message", "A senha deve conter no mínimo 8 caracteres, uma letra maiúscula, uma letra minúscula, um número e um caractere especial."
                    )
            );
        }

        // Verifica se o e-mail já existe
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(409).body(
                    Map.of(
                            "status", 409,
                            "retorno", "Conflict",
                            "message", "Email já cadastrado"
                    )
            );
        }

        // Criptografa a senha
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        Usuario novo = usuarioRepository.save(usuario);

        String token = jwtUtil.gerarToken(novo.getEmail());

        return ResponseEntity.status(201).body(
                Map.of(
                        "token", token,
                        "id", novo.getId(),
                        "nome", novo.getNome(),
                        "email", novo.getEmail(),
                        "isAdm", novo.getIsAdm()
                )
        );
    }

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String senha = body.get("senha");

        if (email == null || senha == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", 400,
                            "retorno", "Bad Request",
                            "message", "Email e senha são obrigatórios"
                    )
            );
        }

        if (estaBloqueado(email)) {
            return ResponseEntity.status(429).body(
                    Map.of(
                            "status", 429,
                            "retorno", "Too Many Requests",
                            "message", "Muitas tentativas. Tente novamente em alguns minutos."
                    )
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenha())) {
            registrarTentativa(email);

            return ResponseEntity.status(401).body(
                    Map.of(
                            "status", 401,
                            "retorno", "Unauthorized",
                            "message", "Email ou senha inválidos"
                    )
            );
        }

        tentativasPorEmail.remove(email);

        String token = jwtUtil.gerarToken(usuario.getEmail());

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "id", usuario.getId(),
                        "nome", usuario.getNome(),
                        "email", usuario.getEmail(),
                        "isAdm", usuario.getIsAdm()
                )
        );
    }

    private void registrarTentativa(String email) {
        tentativasPorEmail.computeIfAbsent(email, k -> new ArrayList<>()).add(LocalDateTime.now());
    }

    private boolean estaBloqueado(String email) {
        List<LocalDateTime> tentativas = tentativasPorEmail.get(email);

        if (tentativas == null) {
            return false;
        }

        LocalDateTime janela = LocalDateTime.now().minusMinutes(JANELA_MINUTOS);

        tentativas.removeIf(t -> t.isBefore(janela));

        return tentativas.size() >= MAX_TENTATIVAS;
    }
}