package com.utp.controller;

import com.utp.model.Rol;
import com.utp.repository.RolRepository;
import com.utp.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/rol")
@CrossOrigin(origins = "*")
public class RolController {

    private final RolRepository repo;
    private final JwtService jwtService;

    public RolController(RolRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

    private void validarAdmin(String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }

    @GetMapping
    public List<Rol> listar(@RequestHeader("Authorization") String auth) {
        validarAdmin(auth);
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Rol obtener(@PathVariable Integer id,
                       @RequestHeader("Authorization") String auth) {
        validarAdmin(auth);
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Rol crear(@RequestBody Rol r,
                     @RequestHeader("Authorization") String auth) {
        validarAdmin(auth);
        return repo.save(r);
    }

    @PutMapping("/{id}")
    public Rol actualizar(@PathVariable Integer id,
                          @RequestBody Rol nuevo,
                          @RequestHeader("Authorization") String auth) {
        validarAdmin(auth);
        return repo.findById(id).map(r -> {
            r.setDescripcion(nuevo.getDescripcion());
            r.setEstado(nuevo.getEstado());
            return repo.save(r);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id,
                         @RequestHeader("Authorization") String auth) {
        validarAdmin(auth);
        repo.deleteById(id);
    }
}
