package com.smiback.smiback.Controllers;

import com.smiback.smiback.Entities.Role;
import com.smiback.smiback.Entities.Utilisateur;
import com.smiback.smiback.Services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @Autowired
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> connecterUtilisateur(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String mot_de_passe = credentials.get("mot_de_passe");

        if (email == null || mot_de_passe == null) {
            return ResponseEntity.badRequest().body("Email et mot de passe requis");
        }

        Optional<Utilisateur> utilisateur = utilisateurService.connecterUtilisateur(email, mot_de_passe);
        if (utilisateur.isPresent()) {
            return ResponseEntity.ok(utilisateur.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> inscrireUtilisateur(@RequestBody Map<String, String> utilisateurData) {
        String nom = utilisateurData.get("nom");
        String prenom = utilisateurData.get("prenom");
        String email = utilisateurData.get("email");
        String mot_de_passe = utilisateurData.get("mot_de_passe");
        Role role;

        try {
            role = Role.valueOf(utilisateurData.get("role"));
        } catch (IllegalArgumentException | NullPointerException e) {
            // Par défaut, nouvel utilisateur avec rôle standard
            role = Role.EMPLOYE;
        }

        if (nom == null || prenom == null || email == null || mot_de_passe == null) {
            return ResponseEntity.badRequest().body("Tous les champs sont requis");
        }

        if (utilisateurService.emailExiste(email)) {
            return ResponseEntity.badRequest().body("Cet email est déjà utilisé");
        }

        Utilisateur utilisateur = utilisateurService.inscrireUtilisateur(nom, prenom, email, mot_de_passe, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateur);
    }

    @GetMapping
    public ResponseEntity<?> getAllUtilisateurs(@RequestHeader("X-User-Id") long idConnecte) {
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs(idConnecte);
        if (utilisateurs != null) {
            return ResponseEntity.ok(utilisateurs);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'avez pas les permissions nécessaires");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUtilisateurById(@RequestHeader("X-User-Id") long idConnecte, @PathVariable Long id) {
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(idConnecte, id);
        if (utilisateur.isPresent()) {
            return ResponseEntity.ok(utilisateur.get());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Utilisateur non trouvé ou permissions insuffisantes");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUtilisateur(
            @PathVariable Long id,
            @RequestBody Utilisateur utilisateur) {


        // Vérifier que l'utilisateur est le même que celui connecté ou un admin
        Optional<Utilisateur> connectedUser = utilisateurService.getOneUtilisateur(id);
        Optional<Utilisateur> utilisateurExistant = utilisateurService.getOneUtilisateur(utilisateur.getId());
        if (!connectedUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non autorisé");
        }

        boolean isAdminOrManager = connectedUser.get().getRole() == Role.SUPER_ADMIN ||
                connectedUser.get().getRole() == Role.GERANT;
        boolean isSelfUpdate = utilisateur.getId().equals(id);

        if (!isAdminOrManager && !isSelfUpdate) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous ne pouvez modifier que votre propre profil");
        }

        Utilisateur utilisateurToSave = utilisateurExistant.get();

        if (utilisateur.getNom() != null && utilisateur.getNom().isEmpty()){
            utilisateurToSave.setNom(utilisateur.getNom());
        }
        if (utilisateur.getPrenom() != null && !utilisateur.getPrenom().isEmpty()) {
            utilisateurToSave.setPrenom(utilisateur.getPrenom());
        }
        if (utilisateur.getEmail() != null && !utilisateur.getEmail().isEmpty()) {
            utilisateurToSave.setEmail(utilisateur.getEmail());
        }
        if (utilisateur.getStatut()!= null && !utilisateur.getStatut().isEmpty()) {
            utilisateurToSave.setStatut(utilisateur.getStatut());
        }
        if (utilisateur.getMot_de_passe() != null && !utilisateur.getMot_de_passe().isEmpty()) {
            // Hacher le mot de passe
            String hashedPassword = utilisateurService.hashPassword(utilisateur.getMot_de_passe());
            utilisateurToSave.setMot_de_passe(hashedPassword);
        }

        Optional<Utilisateur> updated = utilisateurService.updateUtilisateur(utilisateurToSave);
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUtilisateur(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") long idConnecte) {

        // Vérifier que l'utilisateur connecté a le droit de supprimer
        Optional<Utilisateur> connectedUser = utilisateurService.getUtilisateurById(idConnecte, idConnecte);
        if (connectedUser.isPresent() &&
                (connectedUser.get().getRole() == Role.SUPER_ADMIN ||
                        connectedUser.get().getRole() == Role.GERANT)) {

            utilisateurService.deleteUtilisateur(id);
            return ResponseEntity.ok().body("Utilisateur supprimé avec succès");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'avez pas les permissions nécessaires");
        }
    }

    @GetMapping("/email-existe")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = utilisateurService.emailExiste(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * Endpoint spécial pour la vérification d'un compte utilisateur
     */
    @PutMapping("/verify/{id}")
    public ResponseEntity<?> verifyUtilisateur(
            @PathVariable Long id,
            @RequestBody Map<String, Object> verificationData) {

        // Vérifier que les données nécessaires sont présentes
        if (!verificationData.containsKey("email")) {
            return ResponseEntity.badRequest().body("Email requis pour la vérification");
        }

        String email = (String) verificationData.get("email");

        // Récupérer l'utilisateur par ID
        Optional<Utilisateur> utilisateurOpt = utilisateurService.getOneUtilisateur(id);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Vérifier que l'email correspond (sécurité)
        if (!utilisateur.getEmail().equals(email)) {
            return ResponseEntity.badRequest().body("Informations de vérification incorrectes");
        }

        // Mettre à jour uniquement le statut et le flag de vérification
        utilisateur.setStatut("A"); // Actif
        utilisateur.setEstVerifie(true);

        // Sauvegarder les modifications
        Optional<Utilisateur> updated = utilisateurService.updateUtilisateur(utilisateur);
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour du statut");
        }
    }
}