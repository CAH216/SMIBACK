package com.smiback.smiback.Services;

import com.smiback.smiback.Entities.Categorie;
import com.smiback.smiback.Entities.Produit;
import com.smiback.smiback.Entities.Role;
import com.smiback.smiback.Entities.StatutProduit;
import com.smiback.smiback.Entities.Utilisateur;
import com.smiback.smiback.Repositories.CategorieRepository;
import com.smiback.smiback.Repositories.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    @Autowired
    public ProduitService(ProduitRepository produitRepository, CategorieRepository categorieRepository) {
        this.produitRepository = produitRepository;
        this.categorieRepository = categorieRepository;
    }

    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    public Optional<Produit> getProduitById(Long id) {
        return produitRepository.findById(id);
    }

    public List<Produit> getProduitsByCategorie(Long categorieId) {
        return produitRepository.findByCategorieId(categorieId);
    }

    public List<Produit> getProduitsByStatut(StatutProduit statut) {
        return produitRepository.findByStatut(statut);
    }

    public List<Produit> searchProduits(String nom) {
        return produitRepository.findByNomContainingIgnoreCase(nom);
    }

    public Produit createProduit(String nom, String description, BigDecimal prix,
                                 Long categorieId, MultipartFile imageFile) throws IOException {

        Optional<Categorie> categorieOpt = categorieRepository.findById(categorieId);

        if (categorieOpt.isEmpty()) {
            throw new IllegalArgumentException("Catégorie non trouvée avec l'ID: " + categorieId);
        }

        Produit produit = new Produit();
        produit.setNom(nom);
        produit.setDescription(description);
        produit.setPrix(prix);
        produit.setCategorie(categorieOpt.get());
        produit.setStatut(StatutProduit.EN_ATTENTE);

        // Traitement de l'image
        if (imageFile != null && !imageFile.isEmpty()) {
            produit.setImage(imageFile.getBytes());
        }

        return produitRepository.save(produit);
    }

    public Optional<Produit> updateProduit(Long id, String nom, String description,
                                           BigDecimal prix, Long categorieId,
                                           MultipartFile imageFile) throws IOException {

        return produitRepository.findById(id).map(produit -> {
            produit.setNom(nom);
            produit.setDescription(description);
            produit.setPrix(prix);

            if (categorieId != null) {
                categorieRepository.findById(categorieId).ifPresent(produit::setCategorie);
            }

            // Ne mettre à jour l'image que si un nouveau fichier est fourni
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    produit.setImage(imageFile.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException("Erreur lors du traitement de l'image", e);
                }
            }

            return produitRepository.save(produit);
        });
    }

    public Optional<Produit> updateProduitStatut(Long id, StatutProduit statut, Utilisateur utilisateur) {
        // Vérifier si l'utilisateur a les droits pour changer le statut
        if (utilisateur.getRole() != Role.SUPER_ADMIN && utilisateur.getRole() != Role.GERANT) {
            return Optional.empty();
        }

        return produitRepository.findById(id).map(produit -> {
            produit.setStatut(statut);
            return produitRepository.save(produit);
        });
    }

    public boolean deleteProduit(Long id, Utilisateur utilisateur) {
        // Vérifier si l'utilisateur a les droits pour supprimer
        if (utilisateur.getRole() != Role.SUPER_ADMIN && utilisateur.getRole() != Role.GERANT) {
            return false;
        }

        if (produitRepository.existsById(id)) {
            produitRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsByNom(String nom) {
        return produitRepository.existsByNom(nom);
    }
}