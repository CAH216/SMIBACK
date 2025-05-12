package com.smiback.smiback.Services;

import com.smiback.smiback.Entities.*;
import com.smiback.smiback.Repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Connexion utilisateur
    public Optional<Utilisateur> connecterUtilisateur(String email, String mot_de_passe) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findByEmail(email);
        if (optionalUtilisateur.isPresent()) {
            Utilisateur utilisateur = optionalUtilisateur.get();
            // Vérifie le mot de passe
            if (passwordEncoder.matches(mot_de_passe, utilisateur.getMot_de_passe())) {
                utilisateur.setEstConnecte(true);
                utilisateurRepository.save(utilisateur); // mettre à jour son statut
                return Optional.of(utilisateur);
            }
        }
        return Optional.empty();
    }

    public Utilisateur inscrireUtilisateur(String nom, String prenom, String email, String mot_de_passe, Role role) {
        // Encoder le mot de passe avant de l'enregistrer
        String encodedPassword = passwordEncoder.encode(mot_de_passe);

        Utilisateur utilisateur;

        if (Role.SUPER_ADMIN == role) {
            utilisateur = new SuperAdmin(nom, prenom, email, encodedPassword);
        } else if (Role.GERANT == role) {
            utilisateur = new Gerant(nom, prenom, email, encodedPassword);
        } else {
            utilisateur = new Employe(nom, prenom, email, encodedPassword);
        }

        return utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> getAllUtilisateurs(long idConnecte) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findById(idConnecte);
        if (optionalUtilisateur.isPresent()) {
            Utilisateur utilisateur = optionalUtilisateur.get();
            if (utilisateur.getRole() == Role.SUPER_ADMIN || utilisateur.getRole() == Role.GERANT) {
                return utilisateurRepository.findAll();
            }
        }
        return null;
    }

    public Optional<Utilisateur> getUtilisateurById(long idConnecte, Long id) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findById(idConnecte);
        if (optionalUtilisateur.isPresent()) {
            Utilisateur utilisateur = optionalUtilisateur.get();
            if (utilisateur.getRole() == Role.SUPER_ADMIN || utilisateur.getRole() == Role.GERANT) {
                return utilisateurRepository.findById(id);
            }
        }
        return Optional.empty();
    }

    public Optional<Utilisateur> getOneUtilisateur(Long id) {
        return utilisateurRepository.findById(id);
    }

    public Optional<Utilisateur> updateUtilisateur(Utilisateur utilisateur) {
            return Optional.of(utilisateurRepository.save(utilisateur));
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password); // Par exemple avec BCryptPasswordEncoder
    }
    public void deleteUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }

    public boolean emailExiste(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}