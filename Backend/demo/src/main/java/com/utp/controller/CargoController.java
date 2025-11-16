package com.utp.controller;

import com.utp.model.Cargo;
import com.utp.repository.CargoRepository;

import com.utp.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cargo")
@CrossOrigin(origins = "*")
public class CargoController {
    
    private final CargoRepository repo;
    private final JwtService jwtService;

    public CargoController(CargoRepository repo,JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
       
    }

    @GetMapping
    public List<Cargo> listar(@RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
       Integer userId = jwtService.extractId(token);
       
        if (cargo.equals("Administrador de Sistemas")) {
            return repo.findAll();
        }

        
       return repo.findAll().stream()
        .filter(a -> a.getDescripcion().equalsIgnoreCase(cargo))
        .toList();
    }

    @GetMapping("/{id}")
    public Cargo obtener(@PathVariable Integer id,
                         @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        Cargo c = repo.findById(id).orElse(null);

        if (cargo.equals("Administrador de Sistemas")) {
            return c;
        }
        else{
         return repo.findAll().stream()
        .filter(a -> a.getDescripcion().equalsIgnoreCase(cargo))
        .findFirst().orElse(null);
        }
    }

    @PostMapping
    public Cargo crear(@RequestBody Cargo c,
                       @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para crear cargos");
        }

        return repo.save(c);
    }

    @PutMapping("/{id}")
    public Cargo actualizar(@PathVariable Integer id, @RequestBody Cargo nuevo,
                            @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para actualizar cargos");
        }

        return repo.findById(id).map(c -> {
            c.setDescripcion(nuevo.getDescripcion());
            c.setEstado(nuevo.getEstado());
            return repo.save(c);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id,
                         @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para eliminar cargos");
        }

        repo.deleteById(id);
    }
}
