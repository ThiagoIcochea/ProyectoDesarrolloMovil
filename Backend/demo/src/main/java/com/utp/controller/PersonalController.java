/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.utp.controller;

import com.utp.model.Personal;
import com.utp.repository.PersonalRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/personal")
@CrossOrigin(origins = "*")
public class PersonalController {

    private final PersonalRepository repo;

    public PersonalController(PersonalRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Personal> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Personal obtener(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Personal crear(@RequestBody Personal p) {
        return repo.save(p);
    }

    @PutMapping("/{id}")
    public Personal actualizar(@PathVariable Integer id, @RequestBody Personal nuevo) {
        return repo.findById(id).map(p -> {
            p.setNombre(nuevo.getNombre());
            p.setApellPaterno(nuevo.getApellPaterno());
            p.setApellMaterno(nuevo.getApellMaterno());
            p.setEmail(nuevo.getEmail());
            return repo.save(p);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
