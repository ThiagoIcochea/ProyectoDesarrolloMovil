package com.utp.controller;

import com.utp.model.Asistencia;
import com.utp.repository.AsistenciaRepository;
import com.utp.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/asistencia")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    private final AsistenciaRepository repo;
    private final JwtService jwtService;

    public AsistenciaController(AsistenciaRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

    @GetMapping
    public List<Asistencia> listar(@RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer userId = jwtService.extractId(token);

        if (cargo.equals("Administrador de Sistemas")) {
            return repo.findAll();
        }

        return repo.findAll().stream()
                .filter(a -> a.getPersonal().getIdPersonal().equals(userId))
                .toList();
    }

    @PostMapping
    public Asistencia crear(@RequestBody Asistencia asistencia, @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer userId = jwtService.extractId(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            if (!asistencia.getPersonal().getIdPersonal().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes marcar tu propia asistencia");
            }
        }

        return repo.save(asistencia);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id, @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar asistencias");
        }

        repo.deleteById(id);
    }
}
