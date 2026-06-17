package com.TracoCultural.TracoCultural.model.dto;

import com.TracoCultural.TracoCultural.model.entity.Usuario;

public class UsuarioDTO {

    private Long id;
    private String nome;
    private String email;
    private boolean isAdm;
    private String icone;
    private String corFundo;
    private String estado;

    public UsuarioDTO(Usuario u) {
        this.id       = u.getId();
        this.nome     = u.getNome();
        this.email    = u.getEmail();
        this.isAdm    = u.getIsAdm();
        this.icone    = u.getIcone();
        this.corFundo = u.getCorFundo();
        this.estado   = u.getEstado();
    }

    public Long getId()         { return id; }
    public String getNome()     { return nome; }
    public String getEmail()    { return email; }
    public boolean getIsAdm()   { return isAdm; }
    public String getIcone()    { return icone; }
    public String getCorFundo() { return corFundo; }
    public String getEstado()   { return estado; }
}
