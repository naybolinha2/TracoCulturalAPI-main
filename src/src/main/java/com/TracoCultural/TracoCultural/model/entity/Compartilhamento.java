package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="Compartilhamento")
public class Compartilhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idUsuarioFk;
    private Long idEventoFk;




    //              -------------------------------- Getter e Setter --------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUsuarioFk() {
        return idUsuarioFk;
    }

    public void setIdUsuarioFk(Long idUsuarioFk) {
        this.idUsuarioFk = idUsuarioFk;
    }

    public Long getIdEventoFk() {
        return idEventoFk;
    }

    public void setIdEventoFk(Long idEventoFk) {
        this.idEventoFk = idEventoFk;
    }


}
