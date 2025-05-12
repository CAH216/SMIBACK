package com.smiback.smiback.Repositories;

import com.smiback.smiback.Entities.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    boolean existsByNom(String nom);
    Optional<Categorie> findByNom(String nom);

}
