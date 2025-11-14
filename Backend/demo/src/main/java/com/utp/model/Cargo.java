package com.utp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cargo")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCargo;

    private String descripcion;

    private String estado = "ACTIVO";
}
