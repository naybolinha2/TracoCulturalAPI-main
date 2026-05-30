package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UsuarioServices {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Register normal — isAdm sempre false por segurança
    public Usuario save(Usuario usuario) {
        usuario.setIsAdm(false);  // ← ninguém vira adm pelo register
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    // Criação de admin — uso interno apenas
    public Usuario saveAdmin(Usuario usuario) {
        usuario.setIsAdm(true);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public Usuario update(Long id, Usuario usuario) {
        Usuario existente = findById(id);
        existente.setNome(usuario.getNome());
        existente.setEmail(usuario.getEmail());
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        if (usuario.getFotoPerfil() != null) {
            existente.setFotoPerfil(usuario.getFotoPerfil());
        }
        return usuarioRepository.save(existente);
    }

    public ResponseEntity<Object> deleteById(String id) {
        try {
            Long longId = Long.parseLong(id);
            if (!usuarioRepository.existsById(longId)) {
                return ResponseEntity.status(404).body(
                        Map.of("status", 404, "retorno", "Not Found",
                               "message", "Usuário não encontrado com o ID: " + id)
                );
            }
            usuarioRepository.deleteById(longId);
            return ResponseEntity.ok(
                    Map.of("status", 200, "retorno", "OK",
                           "message", "Usuário deletado com o ID: " + id)
            );
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request",
                           "message", "Caminho inválido")
            );
        }
    }
}