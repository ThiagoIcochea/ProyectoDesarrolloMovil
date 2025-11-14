/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.utp.controller;

/**
 *
 * @author Thiago
 */


import com.utp.model.Cargo;
import com.utp.repository.CargoRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cargo")
@CrossOrigin(origins = "*")
public class CargoController {

    private final CargoRepository repo;

    public CargoController(CargoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Cargo> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Cargo obtener(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Cargo crear(@RequestBody Cargo c) {
        return repo.save(c);
    }

    @PutMapping("/{id}")
    public Cargo actualizar(@PathVariable Integer id, @RequestBody Cargo nuevo) {
        return repo.findById(id).map(c -> {
            c.setDescripcion(nuevo.getDescripcion());
            c.setEstado(nuevo.getEstado());
            return repo.save(c);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
    
    
}
