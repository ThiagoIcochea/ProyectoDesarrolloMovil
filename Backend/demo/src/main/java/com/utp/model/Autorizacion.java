/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.utp.model;

import jakarta.persistence.*;
import lombok.Data;
/**
 *
 * @author Thiago
 */
@Entity
@Table(name = "autorizacion")
@Data
public class Autorizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autorizacion")
    private Integer idAutorizacion;

    @ManyToOne
    @JoinColumn(name = "id_movimiento")
    private Movimiento movimiento;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuarioSolicita;

    @ManyToOne
    @JoinColumn(name = "id_user_autoriza")
    private Usuario usuarioAutoriza;

    private String descripcion;

    @Column(name = "fecha_solicitud")
    private String fechaSolicitud;

    @Column(name = "fecha_aprobacion")
    private String fechaAprobacion;

    private String estado;
}

