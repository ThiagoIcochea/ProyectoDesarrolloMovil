package com.utp.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.utp.model.Cargo;
import com.utp.model.Personal;
import com.utp.model.Usuario;
import com.utp.model.Rol;
import com.utp.model.Documento;

import com.utp.repository.CargoRepository;
import com.utp.repository.PersonalRepository;
import com.utp.repository.UsuarioRepository;
import com.utp.repository.RolRepository;
import com.utp.repository.DocumentoRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CargoRepository cargoRepo;

    @Autowired
    private PersonalRepository personalRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private RolRepository rolRepo;

    @Autowired
    private DocumentoRepository documentoRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        Documento docDni = documentoRepo.findByDescripcion("DNI");
        if (docDni == null) {
            docDni = new Documento();
            docDni.setDescripcion("DNI");
            documentoRepo.save(docDni);
            System.out.println("✔ Documento creado: DNI");
        }

        Rol rolAdmin = rolRepo.findByDescripcion("Administrador de Sistemas");
        if (rolAdmin == null) {
            rolAdmin = new Rol();
            rolAdmin.setDescripcion("Administrador de Sistemas");
            rolRepo.save(rolAdmin);
            System.out.println("✔ Rol creado: Administrador de Sistemas");
        }

        Rol rolEmpleado = rolRepo.findByDescripcion("Empleado");
        if (rolEmpleado == null) {
            rolEmpleado = new Rol();
            rolEmpleado.setDescripcion("Empleado");
            rolRepo.save(rolEmpleado);
            System.out.println("✔ Rol creado: Empleado");
        }

        Cargo cargoAdmin = cargoRepo.findByDescripcion("Administrador de Sistemas");
        if (cargoAdmin == null) {
            cargoAdmin = new Cargo();
            cargoAdmin.setDescripcion("Administrador de Sistemas");
            cargoRepo.save(cargoAdmin);
            System.out.println("✔ Cargo creado: Administrador de Sistemas");
        }

        Cargo cargoEmpleado = cargoRepo.findByDescripcion("Empleado de Planta");
        if (cargoEmpleado == null) {
            cargoEmpleado = new Cargo();
            cargoEmpleado.setDescripcion("Empleado de Planta");
            cargoRepo.save(cargoEmpleado);
            System.out.println("✔ Cargo creado: Empleado de Planta");
        }

        Personal personalAdmin = personalRepo.findByEmail("admin@system.com");
        if (personalAdmin == null) {
            personalAdmin = new Personal();
            personalAdmin.setNombre("Admin");
            personalAdmin.setApellPaterno("System");
            personalAdmin.setApellMaterno("Root");
            personalAdmin.setEmail("admin@system.com");
            personalAdmin.setNroDocumento("00000000");
            personalAdmin.setDocumento(docDni);
            personalAdmin.setFechaIngreso("2020-01-01");
            personalAdmin.setFechaNacimiento("1990-01-01");
            personalAdmin.setCargo(cargoAdmin);
            personalRepo.save(personalAdmin);
            System.out.println("✔ Personal admin creado");
        }

        Personal personalEmpleado = personalRepo.findByEmail("empleado@test.com");
        if (personalEmpleado == null) {
            personalEmpleado = new Personal();
            personalEmpleado.setNombre("Carlos");
            personalEmpleado.setApellPaterno("Empleado");
            personalEmpleado.setApellMaterno("Prueba");
            personalEmpleado.setEmail("empleado@test.com");
            personalEmpleado.setNroDocumento("11111111");
            personalEmpleado.setDocumento(docDni);
            personalEmpleado.setFechaIngreso("2022-01-01");
            personalEmpleado.setFechaNacimiento("2000-01-01");
            personalEmpleado.setCargo(cargoEmpleado);
            personalRepo.save(personalEmpleado);
            System.out.println("✔ Personal empleado creado");
        }

        Usuario userAdmin = usuarioRepo.findByUsuario("admin");
        if (userAdmin == null) {
            userAdmin = new Usuario();
            userAdmin.setUsuario("admin");
            userAdmin.setPassword(passwordEncoder.encode("admin123"));
            userAdmin.setEstado("ACTIVO");
            userAdmin.setRol(rolAdmin);
            userAdmin.setPersonal(personalAdmin);
            usuarioRepo.save(userAdmin);
            System.out.println("✔ Usuario administrador creado");
        }

        Usuario userEmpleado = usuarioRepo.findByUsuario("empleado");
        if (userEmpleado == null) {
            userEmpleado = new Usuario();
            userEmpleado.setUsuario("empleado");
            userEmpleado.setPassword(passwordEncoder.encode("123456"));
            userEmpleado.setEstado("ACTIVO");
            userEmpleado.setRol(rolEmpleado);
            userEmpleado.setPersonal(personalEmpleado);
            usuarioRepo.save(userEmpleado);
            System.out.println("✔ Usuario empleado creado");
        }

        System.out.println("✔ Datos iniciales cargados correctamente");
    }
}
