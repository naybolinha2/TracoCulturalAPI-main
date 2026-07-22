package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.config.security.JwtUtil;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
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
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Value("${verificacao.codigo.validade-minutos}")
    private int validadeCodigoMinutos;

    // POST /api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getSenha() == null || usuario.getNome() == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Nome, email e senha são obrigatórios")
            );
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(409).body(
                    Map.of("status", 409, "retorno", "Conflict", "message", "Email já cadastrado")
            );
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setIsAdm(false);
        usuario.setEmailVerificado(false);

        String codigo = gerarCodigo();
        usuario.setCodigoVerificacao(codigo);
        usuario.setCodigoExpiracao(LocalDateTime.now().plusMinutes(validadeCodigoMinutos));

        Usuario novo = usuarioRepository.save(usuario);

        try {
            emailService.enviarEmailConfirmacao(novo.getEmail(), codigo);
        } catch (Exception e) {
            // Não derruba o cadastro se o envio falhar; a pessoa pode usar "reenviar código".
            System.err.println("Falha ao enviar email de verificação para " + novo.getEmail() + ": " + e.getMessage());
        }

        return ResponseEntity.status(201).body(Map.of(
                "id",    novo.getId(),
                "nome",  novo.getNome(),
                "email", novo.getEmail(),
                "message", "Cadastro realizado. Verifique seu email para ativar a conta."
        ));
    }

    // POST /api/v1/auth/verificar-codigo
    @PostMapping("/verificar-codigo")
    public ResponseEntity<Object> verificarCodigo(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String codigo = body.get("codigo");

        if (email == null || codigo == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "message", "Email e código são obrigatórios"));
        }

        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "message", "Usuário não encontrado"));
        }

        if (usuario.isEmailVerificado()) {
            String token = jwtUtil.gerarToken(usuario.getEmail());
            return ResponseEntity.ok(Map.of(
                    "token", token, "id", usuario.getId(), "nome", usuario.getNome(),
                    "email", usuario.getEmail(), "isAdm", usuario.getIsAdm()
            ));
        }

        if (usuario.getCodigoVerificacao() == null
                || usuario.getCodigoExpiracao() == null
                || usuario.getCodigoExpiracao().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body(
                    Map.of("status", 400, "message", "Código expirado. Solicite um novo código."));
        }

        if (!usuario.getCodigoVerificacao().equals(codigo)) {
            return ResponseEntity.status(401).body(
                    Map.of("status", 401, "message", "Código inválido."));
        }

        usuario.setEmailVerificado(true);
        usuario.setCodigoVerificacao(null);
        usuario.setCodigoExpiracao(null);
        usuarioRepository.save(usuario);

        String token = jwtUtil.gerarToken(usuario.getEmail());
        return ResponseEntity.ok(Map.of(
                "token", token, "id", usuario.getId(), "nome", usuario.getNome(),
                "email", usuario.getEmail(), "isAdm", usuario.getIsAdm()
        ));
    }

    // POST /api/v1/auth/reenviar-codigo
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<Object> reenviarCodigo(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "message", "Email é obrigatório"));
        }

        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "message", "Usuário não encontrado"));
        }

        if (usuario.isEmailVerificado()) {
            return ResponseEntity.status(400).body(
                    Map.of("status", 400, "message", "Este email já foi verificado."));
        }

        if (estaBloqueado("reenvio:" + email)) {
            return ResponseEntity.status(429).body(
                    Map.of("status", 429, "message", "Muitas tentativas. Tente novamente em alguns minutos."));
        }
        registrarTentativa("reenvio:" + email);

        String codigo = gerarCodigo();
        usuario.setCodigoVerificacao(codigo);
        usuario.setCodigoExpiracao(LocalDateTime.now().plusMinutes(validadeCodigoMinutos));
        usuarioRepository.save(usuario);

        try {
            emailService.enviarEmailConfirmacao(usuario.getEmail(), codigo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("status", 500, "message", "Não foi possível enviar o email. Tente novamente."));
        }

        return ResponseEntity.ok(Map.of("status", 200, "message", "Código reenviado com sucesso."));
    }

    private String gerarCodigo() {
        int numero = random.nextInt(1_000_000); // 0 a 999999
        return String.format("%06d", numero);
    }

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");

        if (email == null || senha == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Email e senha são obrigatórios")
            );
        }

        if (estaBloqueado(email)) {
            return ResponseEntity.status(429).body(
                    Map.of("status", 429, "retorno", "Too Many Requests",
                            "message", "Muitas tentativas. Tente novamente em alguns minutos.")
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenha())) {
            registrarTentativa(email);
            return ResponseEntity.status(401).body(
                    Map.of("status", 401, "retorno", "Unauthorized", "message", "Email ou senha inválidos")
            );
        }

        if (!usuario.isEmailVerificado()) {
            return ResponseEntity.status(403).body(
                    Map.of("status", 403, "retorno", "Forbidden",
                            "message", "Email ainda não verificado. Confira sua caixa de entrada.")
            );
        }

        tentativasPorEmail.remove(email);
        String token = jwtUtil.gerarToken(usuario.getEmail());

        return ResponseEntity.ok(Map.of(
                "token",  token,
                "id",     usuario.getId(),
                "nome",   usuario.getNome(),
                "email",  usuario.getEmail(),
                "isAdm",  usuario.getIsAdm()
        ));
    }

    private void registrarTentativa(String email) {
        tentativasPorEmail.computeIfAbsent(email, k -> new ArrayList<>()).add(LocalDateTime.now());
    }

    private boolean estaBloqueado(String email) {
        List<LocalDateTime> tentativas = tentativasPorEmail.get(email);
        if (tentativas == null) return false;
        LocalDateTime janela = LocalDateTime.now().minusMinutes(JANELA_MINUTOS);
        tentativas.removeIf(t -> t.isBefore(janela));
        return tentativas.size() >= MAX_TENTATIVAS;
    }
}