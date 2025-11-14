package com.utp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
@Entity
@Data
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    private Integer idRol;

    @ManyToOne
    @JoinColumn(name = "id_personal")
    private Personal personal;

    private String usuario;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String estado = "ACTIVO";
    private LocalDate fechaCreacion = LocalDate.now();
}
