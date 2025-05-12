package com.smiback.smiback.Entities;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Employe")
public class Employe extends Utilisateur{

    public Employe (String nom, String prenom, String email, String encodedPassword) {
        super(nom,prenom,email,encodedPassword,Role.EMPLOYE);
    }

    public Employe() {
    }
}
