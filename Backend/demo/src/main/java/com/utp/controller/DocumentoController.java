package com.utp.controller;

import com.utp.model.Documento;
import com.utp.repository.DocumentoRepository;
import com.utp.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/documento")
@CrossOrigin(origins = "*")
public class DocumentoController {

    private final DocumentoRepository repo;
    private final JwtService jwtService;

    public DocumentoController(DocumentoRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

    @GetMapping
    public List<Documento> listar() {
        return repo.findAll();
    }

    @PostMapping
    public Documento crear(@RequestBody Documento d, @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para crear documentos");
        }

        return repo.save(d);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id, @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar documentos");
        }

        repo.deleteById(id);
    }
}
