package com.utp.controller;

import com.utp.model.Personal;
import com.utp.model.Cargo;
import com.utp.repository.PersonalRepository;
import com.utp.repository.CargoRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/personal")
@CrossOrigin(origins = "*")
public class PersonalController {

    private final PersonalRepository personalRepo;
    private final CargoRepository cargoRepo;

    public PersonalController(PersonalRepository personalRepo, CargoRepository cargoRepo) {
        this.personalRepo = personalRepo;
        this.cargoRepo = cargoRepo;
    }

  
    @GetMapping
    public List<Personal> listar() {
        return personalRepo.findAll();
    }

    
    @GetMapping("/{id}")
    public Personal obtener(@PathVariable Integer id) {
        return personalRepo.findById(id).orElse(null);
    }

 
    @PostMapping
    public Personal crear(@RequestBody Personal p) {

        if (p.getCargo() != null && p.getCargo().getIdCargo() != null) {
            Cargo cargo = cargoRepo.findById(p.getCargo().getIdCargo()).orElse(null);
            p.setCargo(cargo);
        }

        return personalRepo.save(p);
    }

 
    @PutMapping("/{id}")
    public Personal actualizar(@PathVariable Integer id, @RequestBody Personal nuevo) {
        return personalRepo.findById(id).map(p -> {
            p.setNombre(nuevo.getNombre());
            p.setApellPaterno(nuevo.getApellPaterno());
            p.setApellMaterno(nuevo.getApellMaterno());
            p.setEmail(nuevo.getEmail());
            p.setNroDocumento(nuevo.getNroDocumento());
            p.setFechaIngreso(nuevo.getFechaIngreso());
            p.setFechaNacimiento(nuevo.getFechaNacimiento());
            p.setIdTipoDocumento(nuevo.getIdTipoDocumento());

            if (nuevo.getCargo() != null && nuevo.getCargo().getIdCargo() != null) {
                Cargo cargo = cargoRepo.findById(nuevo.getCargo().getIdCargo()).orElse(null);
                p.setCargo(cargo);
            }

            return personalRepo.save(p);
        }).orElse(null);
    }

   
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        personalRepo.deleteById(id);
    }
}
