package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi d'emails
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Envoie un email de bienvenue
     */
    public void sendWelcomeEmail(String toEmail, String userName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Bienvenue sur SGII-Ville");
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Bienvenue sur la plateforme SGII-Ville (Système de Gestion Intelligente des Interventions).\n\n" +
            "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter et accéder à nos services.\n\n" +
            "Cordialement,\n" +
            "L'équipe SGII-Ville",
            userName
        ));

        mailSender.send(message);
    }

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        String resetLink = baseUrl + "/api/auth/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe - SGII-Ville");
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
            "Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :\n" +
            "%s\n\n" +
            "Ce lien est valide pendant 1 heure.\n\n" +
            "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
            "Cordialement,\n" +
            "L'équipe SGII-Ville",
            userName,
            resetLink
        ));

        mailSender.send(message);
    }

    /**
     * Envoie une notification pour un changement d'état de demande
     */
    public void sendDemandeStatusChangeEmail(String toEmail, String userName, int demandeId, String newStatus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mise à jour de votre demande #" + demandeId);
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Le statut de votre demande #%d a été mis à jour.\n\n" +
            "Nouveau statut : %s\n\n" +
            "Vous pouvez consulter les détails de votre demande en vous connectant à la plateforme.\n\n" +
            "Cordialement,\n" +
            "L'équipe SGII-Ville",
            userName,
            demandeId,
            newStatus
        ));

        mailSender.send(message);
    }

    /**
     * Envoie une notification au chef de service pour une nouvelle demande
     */
    public void sendNewDemandeNotificationToChef(String toEmail, String chefName, int demandeId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Nouvelle demande citoyenne #" + demandeId);
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Une nouvelle demande citoyenne (#%d) a été soumise et nécessite votre attention.\n\n" +
            "Connectez-vous à la plateforme pour consulter les détails et prendre les mesures nécessaires.\n\n" +
            "Cordialement,\n" +
            "SGII-Ville",
            chefName,
            demandeId
        ));

        mailSender.send(message);
    }

    /**
     * Envoie une notification d'affectation d'intervention à un technicien
     */
    public void sendInterventionAssignmentEmail(String toEmail, String technicianName, int interventionId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Nouvelle intervention assignée #" + interventionId);
        message.setText(String.format(
            "Bonjour %s,\n\n" +
            "Une nouvelle intervention (#%d) vous a été assignée.\n\n" +
            "Connectez-vous à la plateforme pour consulter les détails et planifier votre travail.\n\n" +
            "Cordialement,\n" +
            "SGII-Ville",
            technicianName,
            interventionId
        ));

        mailSender.send(message);
    }

    /**
     * Envoie un email générique
     */
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
