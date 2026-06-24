package bzh.stack.apiavtrans.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Vérifie les ID tokens (JWT) émis par Google lors d'une connexion "Sign in with Google".
 * La signature, l'expiration, l'émetteur (issuer) et l'audience (client id) sont validés
 * localement à partir des certificats publics de Google (mis en cache automatiquement).
 */
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.oauth.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Vérifie le token et retourne son payload (informations sur l'utilisateur Google).
     *
     * @param idTokenString l'ID token transmis par le frontend
     * @return le payload validé du token
     * @throws RuntimeException si le token est invalide, expiré ou non destiné à cette application
     */
    public GoogleIdToken.Payload verify(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new RuntimeException("Token Google manquant");
        }
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Token Google invalide");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Erreur lors de la vérification du token Google", e);
        }
    }
}
