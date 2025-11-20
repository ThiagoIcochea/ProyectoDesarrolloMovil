package com.utp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "movimiento")
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "abre_desc")
    private String abreDesc;

    private String estado = "ACTIVO";
}
