package com.utp.controller;

import com.utp.model.Usuario;
import com.utp.service.UsuarioService;
import com.utp.service.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {
        try {
            
            Usuario usuario = usuarioService.obtenerPorUsuario(loginRequest.getUsuario());

            if (usuario == null || !usuarioService.validarLogin(usuario.getUsuario(), loginRequest.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
            }

            
            String token = jwtService.generateToken(usuario);

            return ResponseEntity.ok(token);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

  
    @GetMapping("/validar")
    public ResponseEntity<?> validarToken(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            boolean valido = jwtService.validateToken(token);

            if (valido) {
                String usuario = jwtService.extractUsername(token);
                return ResponseEntity.ok("Token válido para el usuario: " + usuario);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error al validar el token: " + e.getMessage());
        }
    }
}
