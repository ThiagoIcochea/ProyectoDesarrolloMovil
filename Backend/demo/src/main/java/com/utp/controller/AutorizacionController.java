package com.utp.controller;

import com.utp.model.Autorizacion;
import com.utp.repository.AutorizacionRepository;
import com.utp.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/autorizacion")
@CrossOrigin(origins = "*")
public class AutorizacionController {

    private final AutorizacionRepository repo;
    private final JwtService jwtService;

    public AutorizacionController(AutorizacionRepository repo, JwtService jwtService) {
        this.repo = repo;
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
}
