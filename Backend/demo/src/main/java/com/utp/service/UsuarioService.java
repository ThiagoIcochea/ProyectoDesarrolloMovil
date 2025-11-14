package com.utp.service;

import com.utp.model.Usuario;
import com.utp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

  
    public List<Usuario> listar() {
        return repo.findAll();
    }

   
    public Usuario obtener(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public Usuario obtenerPorUsuario(String nombreUsuario) {
    return repo.findByUsuario(nombreUsuario);
    }  
    
    public Usuario registrar(Usuario u) {
        String hash = passwordEncoder.encode(u.getPassword());
        u.setPassword(hash);
        return repo.save(u);
    }

   
    public Usuario actualizar(Integer id, Usuario nuevo) {
        Optional<Usuario> optional = repo.findById(id);

        if (optional.isPresent()) {
            Usuario u = optional.get();

            u.setUsuario(nuevo.getUsuario());
            u.setEstado(nuevo.getEstado());
            u.setIdRol(nuevo.getIdRol());
            u.setPersonal(nuevo.getPersonal());

            if (nuevo.getPassword() != null && !nuevo.getPassword().isEmpty()) {
                u.setPassword(passwordEncoder.encode(nuevo.getPassword()));
            }

            return repo.save(u);
        }

        return null;
    }

 
    public void eliminar(Integer id) {
        repo.deleteById(id);
    }
    
    public Usuario login(String usuario, String passwordIngresada) {
    Usuario u = repo.findByUsuario(usuario);
    if (u != null && passwordEncoder.matches(passwordIngresada, u.getPassword())) {
        return u;
    }
    return null;
}

   
   public boolean validarLogin(String usuario, String passwordIngresada) {
    Usuario u = repo.findByUsuario(usuario);
    
    if (u == null) {
        System.out.println("⚠️ Usuario no encontrado: " + usuario);
        return false; 
    }

    System.out.println("✅ Usuario encontrado: " + u.getUsuario());
    System.out.println("🔐 Password en BD: " + u.getPassword());
    System.out.println("🔑 Password ingresada: " + passwordIngresada);

    boolean coincide = passwordEncoder.matches(passwordIngresada, u.getPassword());
    System.out.println("🔍 Coincide? " + coincide);

    return coincide;
}
    
 
}
