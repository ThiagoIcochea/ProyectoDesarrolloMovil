package com.utp.controller;

import com.utp.model.Autorizacion;
import com.utp.model.Usuario;
import com.utp.repository.AutorizacionRepository;
import com.utp.repository.UsuarioRepository;
import com.utp.service.JwtService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/autorizacion")
@CrossOrigin(origins = "*")
public class AutorizacionController {

    private final AutorizacionRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final JwtService jwtService;

    public AutorizacionController(AutorizacionRepository repo, UsuarioRepository usuarioRepo, JwtService jwtService) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
        this.jwtService = jwtService;
    }

    @GetMapping
    public List<Autorizacion> listar(@RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer userId = jwtService.extractId(token);

        if (cargo.equals("Administrador de Sistemas")) {
            return repo.findAll();
        }

        return repo.findAll().stream()
                .filter(a -> a.getUsuarioSolicita().getIdUsuario().equals(userId))
                .toList();
    }

    @PostMapping
    public Autorizacion crear(@RequestBody Autorizacion a, @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer userId = jwtService.extractId(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            if (!a.getUsuarioSolicita().getIdUsuario().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes crear autorizaciones para ti mismo");
            }
        }

        return repo.save(a);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id, @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar autorizaciones");
        }

        repo.deleteById(id);
    }
    
    @PutMapping("/{id}/aprobar")
    public Autorizacion aprobar(@PathVariable Integer id, @RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer userId = jwtService.extractId(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el Administrador de Sistemas puede aprobar autorizaciones");
        }

        Optional<Autorizacion> optionalAutorizacion = repo.findById(id);
        if (optionalAutorizacion.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Autorización no encontrada");
        }
        Autorizacion autorizacion = optionalAutorizacion.get();

        Optional<Usuario> optionalUserAutoriza = usuarioRepo.findById(userId);
        if (optionalUserAutoriza.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario que autoriza no encontrado.");
        }

        autorizacion.setEstado("APROBADO");
        autorizacion.setFechaAprobacion(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        autorizacion.setUsuarioAutoriza(optionalUserAutoriza.get());

        return repo.save(autorizacion);
    }

    @PutMapping("/{id}/rechazar")
    public Autorizacion rechazar(@PathVariable Integer id, @RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer userId = jwtService.extractId(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el Administrador de Sistemas puede rechazar autorizaciones");
        }

        Optional<Autorizacion> optionalAutorizacion = repo.findById(id);
        if (optionalAutorizacion.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Autorización no encontrada");
        }
        Autorizacion autorizacion = optionalAutorizacion.get();
        
        Optional<Usuario> optionalUserAutoriza = usuarioRepo.findById(userId);
        if (optionalUserAutoriza.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario que autoriza no encontrado.");
        }

        autorizacion.setEstado("RECHAZADO");
        autorizacion.setFechaAprobacion(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        autorizacion.setUsuarioAutoriza(optionalUserAutoriza.get());

        return repo.save(autorizacion);
    }
    
}
