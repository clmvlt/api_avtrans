package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.auth.GoogleProfileDTO;
import bzh.stack.apiavtrans.entity.User;

/**
 * Résultat interne d'une tentative de connexion Google.
 * Soit l'utilisateur est authentifié (compte existant et actif),
 * soit aucun compte n'existe et il faut proposer la création (profil Google fourni).
 */
public record GoogleLoginResult(boolean authenticated, User user, GoogleProfileDTO profile) {

    public static GoogleLoginResult authenticated(User user) {
        return new GoogleLoginResult(true, user, null);
    }

    public static GoogleLoginResult needsRegistration(GoogleProfileDTO profile) {
        return new GoogleLoginResult(false, null, profile);
    }
}
