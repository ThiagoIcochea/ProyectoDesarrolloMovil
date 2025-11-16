package com.utp.controller;

import com.utp.model.Usuario;
import com.utp.service.UsuarioService;
import com.utp.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

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

        String token = tokenHeader.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer idUsuarioToken = jwtService.extractId(token);

        if (cargo.equals("Administrador de Sistemas")) {
            return service.listar();
        }

       
        Usuario miUsuario = service.obtener(idUsuarioToken);
        return List.of(miUsuario);
    }


 
    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Integer id,
                           @RequestHeader("Authorization") String tokenHeader) {

        String token = tokenHeader.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer idUsuarioToken = jwtService.extractId(token);

        if (cargo.equals("Administrador de Sistemas")) {
            return service.obtener(id);
        }

        
        return service.obtener(idUsuarioToken);
    }


   
    @PostMapping
    public Usuario crear(@RequestBody Usuario u,
                         @RequestHeader("Authorization") String tokenHeader) {

        String token = tokenHeader.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
         
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden crear usuarios");
        }

        return service.registrar(u);
    }


    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Integer id,
                              @RequestBody Usuario nuevo,
                              @RequestHeader("Authorization") String tokenHeader) {

        String token = tokenHeader.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer idUsuarioToken = jwtService.extractId(token);

        if (cargo.equals("Administrador de Sistemas")) {
            return service.actualizar(id, nuevo);
        }

        
        if (!idUsuarioToken.equals(id)) {
            
            return service.actualizar(idUsuarioToken, nuevo);
        }

        return service.actualizar(idUsuarioToken, nuevo);
    }


  
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id,
                         @RequestHeader("Authorization") String tokenHeader) {

        String token = tokenHeader.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden eliminar usuarios");
        }

        service.eliminar(id);
    }


   
    @PostMapping("/login")
    public String login(@RequestBody Usuario u) {
        Usuario usuario = service.login(u.getUsuario(), u.getPassword());
        if (usuario != null) {
            return jwtService.generateToken(usuario);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas");
    }
}
