package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.config.security.JwtUtil;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {


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
    public ResponseEntity<Object> register(
            @RequestBody Usuario usuario
    ){


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





        usuario.setSenha(
                passwordEncoder.encode(usuario.getSenha())
        );



        String codigo =
                String.valueOf(
                        (int)(Math.random() * 900000) + 100000
                );



        usuario.setCodigoConfirmacao(codigo);

        usuario.setEmailVerificado(false);



        Usuario novo =
                usuarioRepository.save(usuario);




        emailService.enviarEmailConfirmacao(
                novo.getEmail(),
                codigo
        );



        return ResponseEntity.status(201).body(
                Map.of(
                        "message",
                        "Cadastro realizado. Verifique seu email."
                )
        );

    }







    // ==========================
    // CONFIRMAR PELO EMAIL
    // ==========================

    @GetMapping("/confirmar-email/{codigo}")
    public ResponseEntity<Object> confirmarEmail(
            @PathVariable String codigo
    ){



        Usuario usuario =
                usuarioRepository.findByCodigoConfirmacao(codigo);



        if(usuario == null){


            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Link inválido ou expirado"
                    )
            );
        }




        usuario.setEmailVerificado(true);

        usuario.setCodigoConfirmacao(null);



        usuarioRepository.save(usuario);




        String token =
                jwtUtil.gerarToken(
                        usuario.getEmail()
                );




        return ResponseEntity.ok(
                Map.of(

                        "message",
                        "Email confirmado com sucesso",

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




        Usuario usuario =
                usuarioRepository.findByEmail(email);




        if(usuario == null ||
                !passwordEncoder.matches(
                        senha,
                        usuario.getSenha()
                )){


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





        String token =
                jwtUtil.gerarToken(
                        usuario.getEmail()
                );





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

}