package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.config.security.JwtUtil;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
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


    @Autowired
    private EmailService emailService;



    // ==========================
    // CADASTRO
    // ==========================

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Usuario usuario) {


        if(usuario.getEmail() == null ||
                usuario.getSenha() == null ||
                usuario.getNome() == null){


            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Nome, email e senha são obrigatórios"
                    )
            );
        }



        String regex =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";


        if(!usuario.getSenha().matches(regex)){


            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "A senha deve conter no mínimo 8 caracteres, uma letra maiúscula, uma letra minúscula, um número e um caractere especial."
                    )
            );
        }



        if(usuarioRepository.findByEmail(usuario.getEmail()) != null){


            return ResponseEntity.status(409).body(
                    Map.of(
                            "message",
                            "Email já cadastrado"
                    )
            );
        }



        // criptografa senha
        usuario.setSenha(
                passwordEncoder.encode(usuario.getSenha())
        );



        // gera código de 6 números
        String codigo = String.valueOf(
                (int)(Math.random() * 900000) + 100000
        );


        usuario.setCodigoConfirmacao(codigo);

        usuario.setEmailVerificado(false);



        Usuario novo = usuarioRepository.save(usuario);



        // envia código por email
        emailService.enviarEmailConfirmacao(
                novo.getEmail(),
                codigo
        );



        return ResponseEntity.status(201).body(
                Map.of(
                        "message",
                        "Cadastro realizado. Verifique seu email com o código enviado."
                )
        );
    }







    // ==========================
    // VERIFICAR CÓDIGO EMAIL
    // ==========================

    @PostMapping("/verificar-email")
    public ResponseEntity<Object> verificarEmail(
            @RequestBody Map<String,String> body
    ){


        String email = body.get("email");
        String codigo = body.get("codigo");



        Usuario usuario =
                usuarioRepository.findByEmail(email);



        if(usuario == null){


            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Usuário não encontrado"
                    )
            );
        }



        if(usuario.getCodigoConfirmacao() == null ||
                !usuario.getCodigoConfirmacao().equals(codigo)){


            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Código inválido"
                    )
            );
        }




        usuario.setEmailVerificado(true);

        usuario.setCodigoConfirmacao(null);


        usuarioRepository.save(usuario);



        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Email confirmado com sucesso!"
                )
        );

    }







    // ==========================
    // LOGIN
    // ==========================


    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @RequestBody Map<String,String> body
    ){


        String email = body.get("email");
        String senha = body.get("senha");



        if(email == null || senha == null){


            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Email e senha são obrigatórios"
                    )
            );
        }




        if(estaBloqueado(email)){


            return ResponseEntity.status(429).body(
                    Map.of(
                            "message",
                            "Muitas tentativas. Aguarde alguns minutos."
                    )
            );
        }




        Usuario usuario =
                usuarioRepository.findByEmail(email);



        if(usuario == null ||
                !passwordEncoder.matches(senha, usuario.getSenha())){


            registrarTentativa(email);


            return ResponseEntity.status(401).body(
                    Map.of(
                            "message",
                            "Email ou senha inválidos"
                    )
            );
        }






        if(!usuario.isEmailVerificado()){


            return ResponseEntity.status(403).body(
                    Map.of(
                            "message",
                            "Confirme seu email antes de entrar."
                    )
            );
        }





        tentativasPorEmail.remove(email);



        String token =
                jwtUtil.gerarToken(usuario.getEmail());



        return ResponseEntity.ok(
                Map.of(
                        "token",
                        token,

                        "id",
                        usuario.getId(),

                        "nome",
                        usuario.getNome(),

                        "email",
                        usuario.getEmail(),

                        "isAdm",
                        usuario.getIsAdm()
                )
        );
    }







    private void registrarTentativa(String email){

        tentativasPorEmail
                .computeIfAbsent(email,k -> new ArrayList<>())
                .add(LocalDateTime.now());

    }





    private boolean estaBloqueado(String email){


        List<LocalDateTime> tentativas =
                tentativasPorEmail.get(email);



        if(tentativas == null){
            return false;
        }



        LocalDateTime janela =
                LocalDateTime.now()
                        .minusMinutes(JANELA_MINUTOS);



        tentativas.removeIf(
                t -> t.isBefore(janela)
        );



        return tentativas.size() >= MAX_TENTATIVAS;
    }

}