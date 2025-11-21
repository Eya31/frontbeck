package tn.SGII_Ville.entities;


import java.time.LocalDateTime;

public class Notification {
    private int idNotification;
    private String message;
    private LocalDateTime createdAt;
    private int userId;
    private boolean readable;

    // Constructeurs
    public Notification() {}

    public Notification(int idNotification, String message, LocalDateTime createdAt, 
                       int userId, boolean readable) {
        this.idNotification = idNotification;
        this.message = message;
        this.createdAt = createdAt;
        this.userId = userId;
        this.readable = readable;
    }

    // Getters et Setters
    public int getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(int idNotification) {
        this.idNotification = idNotification;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "idNotification=" + idNotification +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", userId=" + userId +
                ", readable=" + readable +
                '}';
    }
}
