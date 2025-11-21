package tn.SGII_Ville.dto;

import tn.SGII_Ville.model.enums.RoleType;

public class LoginResponse {
    
    private String token;
    private String type = "Bearer";
    private int userId;
    private String nom;
    private String email;
    private RoleType role;

    // Constructeurs
    public LoginResponse() {}

    public LoginResponse(String token, int userId, String nom, String email, RoleType role) {
        this.token = token;
        this.userId = userId;
        this.nom = nom;
        this.email = email;
        this.role = role;
    }

    // Getters et Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "type='" + type + '\'' +
                ", userId=" + userId +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
