package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    // POST
    @PostMapping
    public ResponseEntity<Evento> publicarEvento(@RequestBody Evento evento) {
        Evento novo = eventoRepository.save(evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarEvento(@PathVariable String id) {
        try {
            Long eventoId = Long.parseLong(id);
            if (!eventoRepository.existsById(eventoId)) {
                return ResponseEntity.status(404).body(
                        Map.of("status", 404, "retorno", "Not Found", "message", "Evento não encontrado com o ID: " + id)
                );
            }
            eventoRepository.deleteById(eventoId);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "O id informado não é válido: " + id)
            );
        }
    }
}
