package com.utp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.utp.model.Movimiento;
import com.utp.repository.MovimientoRepository;
import com.utp.service.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/movimiento")
@CrossOrigin(origins = "*")
public class MovimientoController {

    private final MovimientoRepository repo;
    private final JwtService jwtService;

    public MovimientoController(MovimientoRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

   
    @GetMapping
    public List<Movimiento> listar(@RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

     

        return repo.findAll();
    }

  
    @PostMapping
    public Movimiento crear(@RequestBody Movimiento mov,
                            @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

       
        if (!"Administrador de Sistemas".equals(cargo)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para registrar movimientos"
            );
        }

 
        if (mov.getDescripcion() == null || mov.getDescripcion().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción es obligatoria");
        }

        return repo.save(mov);
    }

 
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id,
                         @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!"Administrador de Sistemas".equals(cargo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar movimientos");
        }

        repo.deleteById(id);
    }
}
