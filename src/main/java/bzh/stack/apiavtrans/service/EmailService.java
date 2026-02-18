package bzh.stack.apiavtrans.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Envoyer un email simple (texte brut)
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        String normalizedTo = to.toLowerCase();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@avtrans-concept.com");
        message.setTo(normalizedTo);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Envoyer un email HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        String normalizedTo = to.toLowerCase();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@avtrans-concept.com");
        helper.setTo(normalizedTo);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Envoyer un email de vérification
     */
    public void sendVerificationEmail(String to, String token) {
        String subject = "Vérification de votre adresse email - AVTRANS";
        String verificationLink = baseUrl + "/verify?token=" + token;
        int currentYear = Year.now().getValue();

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Vérification de votre adresse email - AVTRANS</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f9fafb; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 40px 20px;">
                            <table role="presentation" style="max-width: 600px; margin: 0 auto; border-collapse: collapse;">
                                <!-- Header -->
                                <tr>
                                    <td style="background-color: #581c87; padding: 24px; text-align: center; border-radius: 8px 8px 0 0;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 22px; font-weight: 700; letter-spacing: 0.5px;">AVTRANS</h1>
                                    </td>
                                </tr>
                                <!-- Content -->
                                <tr>
                                    <td style="background-color: #ffffff; padding: 32px; border-left: 1px solid #e5e7eb; border-right: 1px solid #e5e7eb;">
                                        <h2 style="margin: 0 0 16px 0; color: #111827; font-size: 20px; font-weight: 600;">Bienvenue !</h2>
                                        <p style="margin: 0 0 16px 0; color: #4b5563; font-size: 16px; line-height: 1.5;">
                                            Merci de vous être inscrit sur AVTRANS. Pour finaliser votre inscription, veuillez vérifier votre adresse email en cliquant sur le bouton ci-dessous :
                                        </p>
                                        <!-- Button -->
                                        <table role="presentation" style="margin: 24px 0;">
                                            <tr>
                                                <td>
                                                    <a href="%s" style="display: inline-block; background-color: #581c87; color: #ffffff; font-size: 16px; font-weight: 600; padding: 12px 24px; border-radius: 8px; text-decoration: none;">
                                                        Vérifier mon email
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        <!-- Info Box -->
                                        <div style="background-color: #f9fafb; border-left: 4px solid #581c87; padding: 16px; border-radius: 4px; margin: 16px 0;">
                                            <p style="margin: 0; color: #4b5563; font-size: 14px; line-height: 1.5;">
                                                Si le bouton ne fonctionne pas, copiez et collez ce lien dans votre navigateur :
                                            </p>
                                            <p style="margin: 8px 0 0 0;">
                                                <a href="%s" style="color: #581c87; font-size: 13px; word-break: break-all; text-decoration: underline;">%s</a>
                                            </p>
                                        </div>
                                        <p style="margin: 16px 0 0 0; color: #6b7280; font-size: 14px;">
                                            Vous n'avez pas créé de compte ? Ignorez cet email.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #ffffff; padding: 24px 32px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 8px 8px;">
                                        <p style="margin: 0 0 8px 0; color: #6b7280; font-size: 12px; text-align: center;">
                                            © %d AVTRANS CONCEPT. Tous droits réservés.
                                        </p>
                                        <p style="margin: 0; color: #9ca3af; font-size: 12px; text-align: center;">
                                            Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, verificationLink, verificationLink, verificationLink, currentYear);

        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            throw new RuntimeException("Échec de l'envoi de l'email de vérification", e);
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Réinitialisation de votre mot de passe - AVTRANS";
        String resetLink = baseUrl + "/password-reset?token=" + token;
        int currentYear = Year.now().getValue();

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Réinitialisation de votre mot de passe - AVTRANS</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f9fafb; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 40px 20px;">
                            <table role="presentation" style="max-width: 600px; margin: 0 auto; border-collapse: collapse;">
                                <!-- Header -->
                                <tr>
                                    <td style="background-color: #581c87; padding: 24px; text-align: center; border-radius: 8px 8px 0 0;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 22px; font-weight: 700; letter-spacing: 0.5px;">AVTRANS</h1>
                                    </td>
                                </tr>
                                <!-- Content -->
                                <tr>
                                    <td style="background-color: #ffffff; padding: 32px; border-left: 1px solid #e5e7eb; border-right: 1px solid #e5e7eb;">
                                        <h2 style="margin: 0 0 16px 0; color: #111827; font-size: 20px; font-weight: 600;">Réinitialisation de mot de passe</h2>
                                        <p style="margin: 0 0 16px 0; color: #4b5563; font-size: 16px; line-height: 1.5;">
                                            Vous avez demandé à réinitialiser votre mot de passe. Pour continuer, veuillez cliquer sur le bouton ci-dessous :
                                        </p>
                                        <!-- Button -->
                                        <table role="presentation" style="margin: 24px 0;">
                                            <tr>
                                                <td>
                                                    <a href="%s" style="display: inline-block; background-color: #581c87; color: #ffffff; font-size: 16px; font-weight: 600; padding: 12px 24px; border-radius: 8px; text-decoration: none;">
                                                        Réinitialiser mon mot de passe
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        <!-- Warning Box -->
                                        <div style="background-color: rgba(217, 119, 6, 0.1); border-left: 4px solid #d97706; padding: 16px; border-radius: 4px; margin: 16px 0;">
                                            <p style="margin: 0; color: #d97706; font-weight: 500; font-size: 14px;">
                                                <strong>Important :</strong> Ce lien expirera dans <strong>1 heure</strong>.
                                            </p>
                                        </div>
                                        <!-- Info Box -->
                                        <div style="background-color: #f9fafb; border-left: 4px solid #581c87; padding: 16px; border-radius: 4px; margin: 16px 0;">
                                            <p style="margin: 0; color: #4b5563; font-size: 14px; line-height: 1.5;">
                                                Si le bouton ne fonctionne pas, copiez et collez ce lien dans votre navigateur :
                                            </p>
                                            <p style="margin: 8px 0 0 0;">
                                                <a href="%s" style="color: #581c87; font-size: 13px; word-break: break-all; text-decoration: underline;">%s</a>
                                            </p>
                                        </div>
                                        <p style="margin: 16px 0 0 0; color: #6b7280; font-size: 14px;">
                                            Vous n'avez pas demandé cette réinitialisation ? Ignorez cet email en toute sécurité.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #ffffff; padding: 24px 32px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 8px 8px;">
                                        <p style="margin: 0 0 8px 0; color: #6b7280; font-size: 12px; text-align: center;">
                                            © %d AVTRANS CONCEPT. Tous droits réservés.
                                        </p>
                                        <p style="margin: 0; color: #9ca3af; font-size: 12px; text-align: center;">
                                            Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, resetLink, resetLink, resetLink, currentYear);

        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            throw new RuntimeException("Échec de l'envoi de l'email de réinitialisation", e);
        }
    }

    public void sendNotificationEmail(String to, String title, String description) {
        String subject = title + " - AVTRANS";
        int currentYear = Year.now().getValue();

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - AVTRANS</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f9fafb; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
                <table role="presentation" style="width: 100%%%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 40px 20px;">
                            <table role="presentation" style="max-width: 600px; margin: 0 auto; border-collapse: collapse;">
                                <!-- Header -->
                                <tr>
                                    <td style="background-color: #581c87; padding: 24px; text-align: center; border-radius: 8px 8px 0 0;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 22px; font-weight: 700; letter-spacing: 0.5px;">AVTRANS</h1>
                                    </td>
                                </tr>
                                <!-- Content -->
                                <tr>
                                    <td style="background-color: #ffffff; padding: 32px; border-left: 1px solid #e5e7eb; border-right: 1px solid #e5e7eb;">
                                        <h2 style="margin: 0 0 16px 0; color: #111827; font-size: 20px; font-weight: 600;">%s</h2>
                                        <p style="margin: 0 0 16px 0; color: #4b5563; font-size: 16px; line-height: 1.5;">
                                            %s
                                        </p>
                                        <!-- Button -->
                                        <table role="presentation" style="margin: 24px 0;">
                                            <tr>
                                                <td>
                                                    <a href="%s" style="display: inline-block; background-color: #581c87; color: #ffffff; font-size: 16px; font-weight: 600; padding: 12px 24px; border-radius: 8px; text-decoration: none;">
                                                        Voir sur AVTRANS
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #ffffff; padding: 24px 32px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 8px 8px;">
                                        <p style="margin: 0 0 8px 0; color: #6b7280; font-size: 12px; text-align: center;">
                                            © %d AVTRANS CONCEPT. Tous droits réservés.
                                        </p>
                                        <p style="margin: 0; color: #9ca3af; font-size: 12px; text-align: center;">
                                            Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, title, title, description, baseUrl, currentYear);

        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            throw new RuntimeException("Échec de l'envoi de l'email de notification", e);
        }
    }
}
