package com.utp.controller;

import com.utp.model.Usuario;
import com.utp.service.UsuarioService;
import com.utp.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;


import com.utp.model.Usuario;
import com.utp.service.UsuarioService;
import com.utp.service.JwtService;




import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService service;
    private final JwtService jwtService;

    public UsuarioController(UsuarioService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @GetMapping
    public List<Usuario> listar(@RequestHeader("Authorization") String tokenHeader) {
    System.out.println("Token recibido: " + tokenHeader);
    String token = tokenHeader.replace("Bearer ", "");
    System.out.println("Token limpio: " + token);
    
    if(!jwtService.validateToken(token)){
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
    }

    String role = jwtService.extractRole(token);
    System.out.println("Rol extraído: " + role);

    if (!role.equals("Administrador de Sistemas")) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para listar usuarios");
    }

    return service.listar();
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Integer id, @RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String role = jwtService.extractRole(token);
        Integer userId = jwtService.extractId(token);

        if (!role.equals("Administrador de Sistemas") && !id.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver los datos de otro usuario");
        }

        return service.obtener(id);
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario u, @RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String role = jwtService.extractRole(token);

        if (!role.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para crear usuarios");
        }

        return service.registrar(u);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Integer id, @RequestBody Usuario nuevo,
                              @RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String role = jwtService.extractRole(token);

        if (!role.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para actualizar usuarios");
        }

        return service.actualizar(id, nuevo);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id, @RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String role = jwtService.extractRole(token);

        if (!role.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar usuarios");
        }

        service.eliminar(id);
    }

    @PostMapping("/login")
   public String login(@RequestBody Usuario u) {
    Usuario usuario = service.login(u.getUsuario(), u.getPassword());
    if (usuario != null) {
        return jwtService.generateToken(usuario);
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
}
}
