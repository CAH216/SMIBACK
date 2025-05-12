package com.smiback.smiback.Repositories;

import com.smiback.smiback.Entities.Produit;
import com.smiback.smiback.Entities.StatutProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByCategorieId(Long categorieId);
    List<Produit> findByStatut(StatutProduit statut);
    boolean existsByNom(String nom);
    List<Produit> findByNomContainingIgnoreCase(String nom);
}