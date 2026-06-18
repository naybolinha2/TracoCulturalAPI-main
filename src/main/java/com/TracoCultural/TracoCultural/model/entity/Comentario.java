package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Comentario")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String texto;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    @Column(name = "idUsuarioFk")
    private Long idUsuarioFk;

    @Column(name = "nomeUsuario", length = 100)
    private String nomeUsuario;

    @Column(name = "idEventoFk")
    private Long idEventoFk;

    @PrePersist
    public void prePersist() { this.dataCriacao = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public Long getIdUsuarioFk() { return idUsuarioFk; }
    public void setIdUsuarioFk(Long idUsuarioFk) { this.idUsuarioFk = idUsuarioFk; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public Long getIdEventoFk() { return idEventoFk; }
    public void setIdEventoFk(Long idEventoFk) { this.idEventoFk = idEventoFk; }
}
