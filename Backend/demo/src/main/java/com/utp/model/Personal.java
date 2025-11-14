package com.utp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "personal")
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personal")
    private Integer idPersonal;

    @ManyToOne
    @JoinColumn(name = "id_cargo")
    private Cargo cargo;

    private Integer idTipoDocumento;
    private String nombre;
    private String apellPaterno;
    private String apellMaterno;
    private String nroDocumento;
    private  String  fechaNacimiento;
    private  String  fechaIngreso;
    private String email;
}
