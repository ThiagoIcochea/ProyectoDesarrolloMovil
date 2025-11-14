package com.utp.service;

import com.utp.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Base64;

@Service
public class JwtService {

    private static final String SECRET_KEY_STRING = "claveSecretaSuperSeguraQueTieneAlMenos256Bits!!!";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(
            Base64.getEncoder().encode(SECRET_KEY_STRING.getBytes())
    );

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    public String generateToken(Usuario usuario) {
        // Validamos si Personal y Cargo existen
        String rol = "SinRol";
        if (usuario.getPersonal() != null && usuario.getPersonal().getCargo() != null) {
            rol = usuario.getPersonal().getCargo().getDescripcion();
        }

        return Jwts.builder()
                .setSubject(usuario.getUsuario())
                .claim("role", rol)
                .claim("id", usuario.getIdUsuario())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        String rol = extractAllClaims(token).get("role", String.class);
        return rol != null ? rol.trim() : "";
    }

    public Integer extractId(String token) {
        return extractAllClaims(token).get("id", Integer.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
