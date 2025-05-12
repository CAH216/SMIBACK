package com.smiback.smiback.Services;

import com.smiback.smiback.Entities.Categorie;
import com.smiback.smiback.Entities.Role;
import com.smiback.smiback.Entities.Utilisateur;
import com.smiback.smiback.Repositories.CategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategorieService {

    private final CategorieRepository categorieRepository;

    @Autowired
    public CategorieService(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    public List<Categorie> getAllCategories() {
        return categorieRepository.findAll();
    }

    public Optional<Categorie> getCategorieById(Long id) {
        return categorieRepository.findById(id);
    }

    public Categorie saveCategorie(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    public Optional<Categorie> updateCategorie(Long id, Categorie categorieDetails) {
        return categorieRepository.findById(id)
                .map(categorie -> {
                    categorie.setNom(categorieDetails.getNom());
                    // Ne pas modifier la liste des produits directement ici
                    // car cela pourrait entraîner des problèmes de persistance
                    return categorieRepository.save(categorie);
                });
    }

    public boolean deleteCategorie(Long id, Utilisateur utilisateur) {
        // Vérifier si l'utilisateur a les droits pour supprimer une catégorie
        if (utilisateur.getRole() == Role.SUPER_ADMIN || utilisateur.getRole() == Role.GERANT) {
            if (categorieRepository.existsById(id)) {
                categorieRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

    public boolean existsByNom(String nom) {
        return categorieRepository.existsByNom(nom);
    }

    // Méthode pour obtenir une catégorie par son nom
    public Optional<Categorie> getCategorieByNom(String nom) {
        return categorieRepository.findByNom(nom);
    }
}