package com.smiback.smiback.Controllers;

import com.smiback.smiback.Entities.Categorie;
import com.smiback.smiback.Entities.Role;
import com.smiback.smiback.Entities.Utilisateur;
import com.smiback.smiback.Services.CategorieService;
import com.smiback.smiback.Services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    private final CategorieService categorieService;
    private final UtilisateurService utilisateurService;

    @Autowired
    public CategorieController(CategorieService categorieService, UtilisateurService utilisateurService) {
        this.categorieService = categorieService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<Categorie>> getAllCategories() {
        List<Categorie> categories = categorieService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategorieById(@PathVariable Long id) {
        Optional<Categorie> categorie = categorieService.getCategorieById(id);
        return categorie.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCategorie(
            @Valid @RequestBody Categorie categorie,
            @RequestHeader("X-User-Id") Long idConnecte) {

        // Vérifier si l'utilisateur a les permissions
        Optional<Utilisateur> utilisateur = utilisateurService.getOneUtilisateur(idConnecte);
        if (utilisateur.isEmpty() || (utilisateur.get().getRole() != Role.SUPER_ADMIN &&
                utilisateur.get().getRole() != Role.GERANT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'avez pas les permissions nécessaires");
        }

        // Vérifier si une catégorie avec le même nom existe déjà
        if (categorieService.existsByNom(categorie.getNom())) {
            return ResponseEntity.badRequest().body("Une catégorie avec ce nom existe déjà");
        }

        Categorie savedCategorie = categorieService.saveCategorie(categorie);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategorie);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategorie(
            @PathVariable Long id,
            @Valid @RequestBody Categorie categorieDetails,
            @RequestHeader("X-User-Id") Long idConnecte) {

        // Vérifier si l'utilisateur a les permissions
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(idConnecte, idConnecte);
        if (utilisateur.isEmpty() || (utilisateur.get().getRole() != Role.SUPER_ADMIN &&
                utilisateur.get().getRole() != Role.GERANT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'avez pas les permissions nécessaires");
        }

        // Vérifier si le nouveau nom est déjà utilisé par une autre catégorie
        Optional<Categorie> existingCategorieWithName = categorieService.getCategorieByNom(categorieDetails.getNom());
        if (existingCategorieWithName.isPresent() && !existingCategorieWithName.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body("Une autre catégorie avec ce nom existe déjà");
        }

        Optional<Categorie> updatedCategorie = categorieService.updateCategorie(id, categorieDetails);
        return updatedCategorie.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategorie(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long idConnecte) {

        // Vérifier si l'utilisateur existe
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(idConnecte, idConnecte);
        if (utilisateur.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        boolean deleted = categorieService.deleteCategorie(id, utilisateur.get());
        if (deleted) {
            return ResponseEntity.ok().body(Map.of("message", "Catégorie supprimée avec succès"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Impossible de supprimer la catégorie. Vérifiez vos permissions ou l'existence de la catégorie.");
        }
    }

    @GetMapping("/check-nom")
    public ResponseEntity<Boolean> checkNomExists(@RequestParam String nom) {
        boolean exists = categorieService.existsByNom(nom);
        return ResponseEntity.ok(exists);
    }
}