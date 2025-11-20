/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.utp.model;

/**
 *
 * @author Thiago
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "asistencia")
@Data
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Integer idAsistencia;

    @ManyToOne
    @JoinColumn(name = "id_personal")
    private Personal personal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_movimiento")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Movimiento movimiento;

    private String fecha;

    @Column(name = "ip_marcador")
    private String ipMarcador;

    @ManyToOne
    @JoinColumn(name = "id_autorizacion")
    private Autorizacion autorizacion;
}
