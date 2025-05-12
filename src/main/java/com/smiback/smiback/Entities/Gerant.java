package com.smiback.smiback.Entities;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("GERANT")
public class Gerant extends Utilisateur{

   public Gerant (String nom, String prenom, String email, String encodedPassword) {
        super(nom,prenom,email,encodedPassword,Role.GERANT);
    }

    public Gerant() {
    }
}
