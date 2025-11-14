package com.utp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.utp.model.Cargo;
import com.utp.model.Personal;
import com.utp.model.Usuario;
import com.utp.repository.CargoRepository;
import com.utp.repository.PersonalRepository;
import com.utp.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CargoRepository cargoRepo;

    @Autowired
    private PersonalRepository personalRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // =============================
        // 1. CREAR CARGO
        // =============================
        Cargo cargo = cargoRepo.findByDescripcion("Administrador de Sistemas");
        if (cargo == null) {
            cargo = new Cargo();
            cargo.setDescripcion("Administrador de Sistemas");
            cargo.setEstado("ACTIVO");

            cargoRepo.save(cargo);
            System.out.println("✔ Cargo creado: Administrador de Sistemas");
        }

        // =============================
        // 2. CREAR PERSONAL
        // =============================
        Personal personal = personalRepo.findByEmail("admin@system.com");
        if (personal == null) {
            personal = new Personal();
            personal.setNombre("Admin");
            personal.setApellPaterno("System");
            personal.setApellMaterno("Root");
            personal.setEmail("admin@system.com");
            personal.setNroDocumento("00000000");
            personal.setIdTipoDocumento(1);
            personal.setFechaIngreso("2020-01-01");
            personal.setFechaNacimiento("1990-01-01");
            personal.setCargo(cargo);

            personalRepo.save(personal);
            System.out.println("✔ Personal administrador creado");
        }

        // =============================
        // 3. CREAR USUARIO ADMIN
        // =============================
        Usuario user = usuarioRepo.findByUsuario("admin");
        if (user == null) {
            user = new Usuario();
            user.setUsuario("admin");

            // 🔥 ENCRIPTAR CONTRASEÑA
            String hash = passwordEncoder.encode("admin123");
            user.setPassword(hash);

            user.setEstado("ACTIVO");
            user.setIdRol(1);
            user.setPersonal(personal);

            usuarioRepo.save(user);
            System.out.println("✔ Usuario administrador creado");
        }

        System.out.println("✔ Datos iniciales cargados correctamente");
    }
}
