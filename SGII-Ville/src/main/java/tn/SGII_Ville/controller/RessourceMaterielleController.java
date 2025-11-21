package tn.SGII_Ville.controller;

import tn.SGII_Ville.entities.RessourceMaterielle;
import tn.SGII_Ville.service.RessourceMaterielleService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ressources")
@CrossOrigin(origins = "http://localhost:4200")
public class RessourceMaterielleController {

    private final RessourceMaterielleService service;

    public RessourceMaterielleController(RessourceMaterielleService service) {
        this.service = service;
    }

    @GetMapping
    public List<RessourceMaterielle> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public RessourceMaterielle getOne(@PathVariable int id) { return service.getById(id); }

    @PostMapping
    public RessourceMaterielle create(@RequestBody RessourceMaterielle r) { return service.create(r); }

    @PutMapping("/{id}")
    public RessourceMaterielle update(@PathVariable int id, @RequestBody RessourceMaterielle r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) { service.delete(id); }
}
