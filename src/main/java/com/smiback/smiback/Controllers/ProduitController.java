package com.smiback.smiback.Controllers;

import com.smiback.smiback.DTOs.ProduitRequest;
import com.smiback.smiback.Entities.Produit;
import com.smiback.smiback.Entities.StatutProduit;
import com.smiback.smiback.Entities.Utilisateur;
import com.smiback.smiback.Services.ProduitService;
import com.smiback.smiback.Services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService produitService;
    private final UtilisateurService utilisateurService;

    @Autowired
    public ProduitController(ProduitService produitService, UtilisateurService utilisateurService) {
        this.produitService = produitService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<Produit>> getAllProduits() {
        List<Produit> produits = produitService.getAllProduits();
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduitById(@PathVariable Long id) {
        Optional<Produit> produit = produitService.getProduitById(id);
        return produit.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProduitImage(@PathVariable Long id) {
        Optional<Produit> produitOpt = produitService.getProduitById(id);

        if (produitOpt.isPresent() && produitOpt.get().getImage() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Ou déterminer dynamiquement selon le type d'image

            return new ResponseEntity<>(produitOpt.get().getImage(), headers, HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<List<Produit>> getProduitsByCategorie(@PathVariable Long categorieId) {
        List<Produit> produits = produitService.getProduitsByCategorie(categorieId);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Produit>> getProduitsByStatut(@PathVariable StatutProduit statut) {
        List<Produit> produits = produitService.getProduitsByStatut(statut);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Produit>> searchProduits(@RequestParam String nom) {
        List<Produit> produits = produitService.searchProduits(nom);
        return ResponseEntity.ok(produits);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduit(
            @RequestParam("nom") String nom,
            @RequestParam("description") String description,
            @RequestParam("prix") String prix,
            @RequestParam("categorieId") Long categorieId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader("X-User-Id") Long idConnecte) {

        // Vérifier si l'utilisateur existe
        Optional<Utilisateur> utilisateurOpt = utilisateurService.getOneUtilisateur(idConnecte);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        // Validation du nom
        if (produitService.existsByNom(nom)) {
            return ResponseEntity.badRequest().body("Un produit avec ce nom existe déjà");
        }

        try {
            Produit savedProduit = produitService.createProduit(
                    nom,
                    description,
                    new java.math.BigDecimal(prix),
                    categorieId,
                    imageFile
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduit);
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduit(
            @PathVariable Long id,
            @RequestParam("nom") String nom,
            @RequestParam("description") String description,
            @RequestParam("prix") String prix,
            @RequestParam("categorieId") Long categorieId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader("X-User-Id") Long idConnecte) {

        // Vérifier si l'utilisateur existe
        Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurById(idConnecte, idConnecte);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        try {
            Optional<Produit> updatedProduit = produitService.updateProduit(
                    id,
                    nom,
                    description,
                    new java.math.BigDecimal(prix),
                    categorieId,
                    imageFile
            );

            return updatedProduit.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Produit> updateProduitStatut(
            @PathVariable Long id,
            @RequestParam StatutProduit statut,
            @RequestHeader("X-User-Id") Long idConnecte) {

        // Vérifier si l'utilisateur existe
        Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurById(idConnecte, idConnecte);
        if (utilisateurOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé");
        }

        Optional<Produit> updatedProduit = produitService.updateProduitStatut(id, statut, utilisateurOpt.get());

        if (updatedProduit.isPresent()) {
            return ResponseEntity.ok(updatedProduit.get());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Impossible de mettre à jour le statut. Vérifiez vos permissions ou l'existence du produit.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduit(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long idConnecte) {

        // Vérifier si l'utilisateur existe
        Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurById(idConnecte, idConnecte);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        boolean deleted = produitService.deleteProduit(id, utilisateurOpt.get());

        if (deleted) {
            return ResponseEntity.ok().body(Map.of("message", "Produit supprimé avec succès"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Impossible de supprimer le produit. Vérifiez vos permissions ou l'existence du produit.");
        }
    }

    @GetMapping("/check-nom")
    public ResponseEntity<Boolean> checkNomExists(@RequestParam String nom) {
        boolean exists = produitService.existsByNom(nom);
        return ResponseEntity.ok(exists);
    }
}