package com.TracoCultural.TracoCultural.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Favorito")
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idUsuarioFk;
    private Long idEventoFk;




    //              -------------------------------- Getter e Setter --------------------------------


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
