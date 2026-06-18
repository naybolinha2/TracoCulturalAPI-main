package com.TracoCultural.TracoCultural.model.services;

import com.TracoCultural.TracoCultural.model.Repository.ComentarioRepository;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Comentario;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Comentario> listarPorEvento(Long eventoId) {
        return comentarioRepository.findByIdEventoFkOrderByDataCriacaoDesc(eventoId);
    }

    public Comentario salvar(Long eventoId, String texto, Authentication auth) {
        if (texto == null || texto.isBlank() || texto.length() > 500)
            throw new RuntimeException("Texto inválido");
        Usuario usuario = usuarioRepository.findByEmail(auth.getName());
        Comentario c = new Comentario();
        c.setTexto(texto.trim());
        c.setIdEventoFk(eventoId);
        c.setIdUsuarioFk(usuario.getId());
        c.setNomeUsuario(usuario.getNome());
        return comentarioRepository.save(c);
    }

    public void deletar(Long comentarioId, Authentication auth) {
        Comentario c = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));
        Usuario usuario = usuarioRepository.findByEmail(auth.getName());
        if (!c.getIdUsuarioFk().equals(usuario.getId()) && !usuario.getIsAdm())
            throw new RuntimeException("SEM_PERMISSAO");
        comentarioRepository.deleteById(comentarioId);
    }
}
