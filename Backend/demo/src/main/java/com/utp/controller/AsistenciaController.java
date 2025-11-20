package com.utp.controller;

import com.utp.model.Asistencia;
import com.utp.repository.AsistenciaRepository;
import com.utp.repository.MovimientoRepository;
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
    private final MovimientoRepository movimientoRepo;
    private final JwtService jwtService;

    public AsistenciaController(AsistenciaRepository repo, MovimientoRepository movimientoRepo, JwtService jwtService) {
        this.repo = repo;
        this.movimientoRepo = movimientoRepo;
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
            if (asistencia.getPersonal() == null || asistencia.getPersonal().getIdPersonal() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta información del personal");
            }
            if (!asistencia.getPersonal().getIdPersonal().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes marcar tu propia asistencia");
            }
        }

        // Cargar el movimiento desde la BD para asegurar persistencia correcta
        if (asistencia.getMovimiento() != null && asistencia.getMovimiento().getIdMovimiento() != null) {
            var movimiento = movimientoRepo.findById(asistencia.getMovimiento().getIdMovimiento())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Movimiento no encontrado"));
            asistencia.setMovimiento(movimiento);
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
    
    @DeleteMapping("/limpiar-datos-prueba")
    public String limpiarDatosPrueba(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        Integer userId = jwtService.extractId(token);
        
        // Eliminar todas las asistencias con movimiento null del usuario actual
        var asistencias = repo.findAll().stream()
                .filter(a -> a.getPersonal().getIdPersonal().equals(userId))
                .filter(a -> a.getMovimiento() == null)
                .toList();
        
        repo.deleteAll(asistencias);
        
        return "Eliminadas " + asistencias.size() + " asistencias con movimiento null";
    }
}
