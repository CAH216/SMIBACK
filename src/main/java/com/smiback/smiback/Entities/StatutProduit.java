package com.smiback.smiback.Entities;

public enum StatutProduit {
    EN_ATTENTE,   // Produit nouvellement ajouté, en attente de validation
    VALIDE,       // Produit validé et disponible
    REJETE,       // Produit rejeté
    INACTIF       // Produit temporairement indisponible
}