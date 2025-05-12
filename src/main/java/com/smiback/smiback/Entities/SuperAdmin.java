package com.smiback.smiback.Entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SUPERADMIN")
public class SuperAdmin extends Utilisateur{
    public SuperAdmin(String nom, String prenom, String email, String encodedPassword) {
        super(nom,prenom,email,encodedPassword,Role.SUPER_ADMIN);
    }

    public SuperAdmin() {

    }
}
