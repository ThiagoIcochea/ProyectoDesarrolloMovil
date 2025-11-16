package com.utp.controller;

import com.utp.model.Personal;
import com.utp.model.Usuario;
import com.utp.model.Cargo;
import com.utp.repository.PersonalRepository;
import com.utp.repository.CargoRepository;
import com.utp.repository.UsuarioRepository;
import com.utp.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/personal")
@CrossOrigin(origins = "*")
public class PersonalController {

    private final PersonalRepository personalRepo;
    private final UsuarioRepository usuarioRepo;
    private final CargoRepository cargoRepo;
    private final JwtService jwtService;

    public PersonalController(PersonalRepository personalRepo,
                              CargoRepository cargoRepo, UsuarioRepository usuarioRepo,
                              JwtService jwtService) {
        this.personalRepo = personalRepo;
        this.cargoRepo = cargoRepo;
        this.jwtService = jwtService;
        this.usuarioRepo = usuarioRepo;
    }

        @GetMapping
    public List<Personal> listar(@RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer idUsuario = jwtService.extractId(token);

        
        if (cargo.equals("Administrador de Sistemas")) {
            return personalRepo.findAll();
        }

       
        System.out.println("IDUSUARIO:"+idUsuario);
        Usuario usuario = usuarioRepo.findAll().stream()
                .filter(u -> u.getIdUsuario().equals(idUsuario))
                .findFirst()
                .orElse(null);

        if (usuario == null || usuario.getPersonal() == null) {
            return List.of();
        }

   
        Integer idPersonal = usuario.getPersonal().getIdPersonal();
        System.out.println("IDPERSONAL:"+idPersonal);
        
        return personalRepo.findAll().stream()
                .filter(per -> per.getIdPersonal().equals(idPersonal))
                .toList();
    }


    @GetMapping("/{id}")
    public Personal obtener(@PathVariable Integer id,
                            @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer userId = jwtService.extractId(token);

        Personal p = personalRepo.findById(id).orElse(null);
        if (p == null) return null;

       
        if (cargo.equals("Administrador de Sistemas")) {
            return p;
        }

      
        Usuario usuario = usuarioRepo.findAll().stream()
                .filter(u -> u.getIdUsuario().equals(userId))
                .findFirst()
                .orElse(null);

        if (usuario == null || usuario.getPersonal() == null) {
            return null;
        }

        
        Integer idPersonal = usuario.getPersonal().getIdPersonal();

      
        Personal miPersonal = personalRepo.findAll().stream()
                .filter(per -> per.getIdPersonal().equals(idPersonal))
                .findFirst()
                .orElse(null);

       
        if (miPersonal != null && miPersonal.getIdPersonal().equals(id)) {
            return miPersonal;
        }

        
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Solo puedes ver tus propios datos");
    }



    @PostMapping
    public Personal crear(@RequestBody Personal p,
                          @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para crear personal");
        }

        if (p.getCargo() != null && p.getCargo().getIdCargo() != null) {
            Cargo cargoObj = cargoRepo.findById(p.getCargo().getIdCargo()).orElse(null);
            p.setCargo(cargoObj);
        }

        return personalRepo.save(p);
    }

    @PutMapping("/{id}")
    public Personal actualizar(@PathVariable Integer id, @RequestBody Personal nuevo,
                               @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);
        Integer userId = jwtService.extractId(token);

        return personalRepo.findById(id).map(p -> {

            if (!cargo.equals("Administrador de Sistemas") && !userId.equals(id)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo puedes actualizar tus propios datos");
            }

            p.setNombre(nuevo.getNombre());
            p.setApellPaterno(nuevo.getApellPaterno());
            p.setApellMaterno(nuevo.getApellMaterno());
            p.setEmail(nuevo.getEmail());
            p.setNroDocumento(nuevo.getNroDocumento());
            p.setFechaIngreso(nuevo.getFechaIngreso());
            p.setFechaNacimiento(nuevo.getFechaNacimiento());
            p.setIdTipoDocumento(nuevo.getIdTipoDocumento());

            if (cargo.equals("Administrador de Sistemas") && nuevo.getCargo() != null) {
                Cargo cargoObj = cargoRepo.findById(nuevo.getCargo().getIdCargo()).orElse(null);
                p.setCargo(cargoObj);
            }

            return personalRepo.save(p);

        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id,
                         @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String cargo = jwtService.extractCargo(token);

        if (!cargo.equals("Administrador de Sistemas")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para eliminar personal");
        }

        personalRepo.deleteById(id);
    }
}
