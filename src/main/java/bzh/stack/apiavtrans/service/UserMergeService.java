package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.common.UserMergeItemDTO;
import bzh.stack.apiavtrans.dto.common.UserMergePreviewDTO;
import bzh.stack.apiavtrans.entity.Role;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.UserMergeRepository;
import bzh.stack.apiavtrans.repository.UserMergeRepository.Donnee;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Fusion de deux profils d'un même salarié (doublon de compte) : pointages, absences, acomptes, véhicules…
 * du profil supprimé passent au profil conservé, dont les informations (e-mail, mot de passe, rôle, contrat,
 * coordonnées) restent inchangées ; le profil supprimé disparaît.
 */
@Service
@RequiredArgsConstructor
public class UserMergeService {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final UserMergeRepository userMergeRepository;
    private final UserMapper userMapper;

    /** Ce que la fusion va transférer, ce qu'il faudra vérifier après, et ce qui l'empêche. */
    @Transactional(readOnly = true)
    public UserMergePreviewDTO previsualiser(UUID conserveUuid, UUID supprimeUuid, User demandeur) {
        User conserve = charger(conserveUuid, "conservé");
        User supprime = charger(supprimeUuid, "à supprimer");
        verifierDistincts(conserve, supprime);

        List<UserMergeItemDTO> donnees = new ArrayList<>();
        for (Donnee donnee : Donnee.values()) {
            long nombre = userMergeRepository.compter(donnee, supprime);
            if (nombre > 0) {
                donnees.add(new UserMergeItemDTO(donnee.name(), donnee.getLibelle(), nombre));
            }
        }

        List<String> blocages = blocages(conserve, supprime, demandeur);
        return new UserMergePreviewDTO(
                userMapper.toDTO(conserve),
                userMapper.toDTO(supprime),
                donnees,
                avertissements(conserve, supprime),
                blocages,
                blocages.isEmpty());
    }

    /**
     * Transfère toutes les données du profil supprimé au profil conservé puis supprime le profil.
     * Refusée tant qu'un blocage de l'aperçu subsiste.
     *
     * @return le profil conservé, rechargé
     */
    @Transactional
    public User fusionner(UUID conserveUuid, UUID supprimeUuid, User demandeur) {
        User conserve = charger(conserveUuid, "conservé");
        User supprime = charger(supprimeUuid, "à supprimer");
        verifierDistincts(conserve, supprime);

        List<String> blocages = blocages(conserve, supprime, demandeur);
        if (!blocages.isEmpty()) {
            throw new RuntimeException(String.join(" ", blocages));
        }

        userMergeRepository.fusionner(supprime, conserve);

        return userRepository.findById(conserveUuid)
                .orElseThrow(() -> new RuntimeException("Profil conservé introuvable après la fusion"));
    }

    private User charger(UUID uuid, String role) {
        if (uuid == null) {
            throw new RuntimeException("Le profil " + role + " est obligatoire");
        }
        return userRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Profil " + role + " introuvable : " + uuid));
    }

    private void verifierDistincts(User conserve, User supprime) {
        if (conserve.getUuid().equals(supprime.getUuid())) {
            throw new RuntimeException("Choisissez deux profils différents");
        }
    }

    private List<String> blocages(User conserve, User supprime, User demandeur) {
        List<String> blocages = new ArrayList<>();
        if (demandeur != null && supprime.getUuid().equals(demandeur.getUuid())) {
            blocages.add("Vous ne pouvez pas supprimer votre propre compte : connectez-vous avec un autre administrateur "
                    + "ou conservez ce profil.");
        }
        if (aUnServiceEnCours(conserve) && aUnServiceEnCours(supprime)) {
            blocages.add("Les deux profils ont un pointage en cours : terminez celui de " + nomDistinct(supprime, conserve)
                    + " avant la fusion.");
        }
        return blocages;
    }

    private List<String> avertissements(User conserve, User supprime) {
        List<String> avertissements = new ArrayList<>();
        String nomConserve = nomDistinct(conserve, supprime);
        String nomSupprime = nomDistinct(supprime, conserve);

        long services = userMergeRepository.compterServicesChevauchants(supprime, conserve, ZonedDateTime.now(PARIS));
        if (services > 0) {
            avertissements.add(nombre(services, "pointage", "pointages") + " de " + nomSupprime
                    + (services > 1 ? " chevauchent" : " chevauche") + " ceux de " + nomConserve
                    + " : ces heures seront comptées deux fois, à corriger après la fusion.");
        }

        long absences = userMergeRepository.compterAbsencesChevauchantes(supprime, conserve);
        if (absences > 0) {
            avertissements.add(nombre(absences, "absence", "absences") + " de " + nomSupprime
                    + (absences > 1 ? " chevauchent" : " chevauche") + " celles de " + nomConserve
                    + " : à vérifier après la fusion.");
        }

        long couchettes = userMergeRepository.compterCouchettesEnDoublon(supprime, conserve);
        if (couchettes > 0) {
            avertissements.add(nombre(couchettes, "couchette", "couchettes") + " de " + nomSupprime
                    + (couchettes > 1 ? " tombent" : " tombe") + " un jour où " + nomConserve
                    + " en a déjà une : " + (couchettes > 1 ? "elles seront supprimées." : "elle sera supprimée."));
        }

        String roleSupprime = nomRole(supprime.getRole());
        String roleConserve = nomRole(conserve.getRole());
        if (!Objects.equals(roleSupprime, roleConserve)) {
            avertissements.add(nomSupprime + " a le rôle « " + libelleRole(roleSupprime) + " » : "
                    + nomConserve + " garde le sien (« " + libelleRole(roleConserve) + " »).");
        }

        if (aUnServiceEnCours(supprime)) {
            avertissements.add("Le pointage en cours de " + nomSupprime + " continuera sur le profil de "
                    + nomConserve + ".");
        }

        return avertissements;
    }

    private boolean aUnServiceEnCours(User user) {
        return serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(user).isPresent();
    }

    private static String nom(User user) {
        String nom = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim();
        return nom.isEmpty() ? user.getEmail() : nom;
    }

    /** Nom du salarié, suivi de son e-mail quand l'autre profil porte le même nom (cas habituel d'un doublon). */
    private static String nomDistinct(User user, User autre) {
        String nom = nom(user);
        return nom.equalsIgnoreCase(nom(autre)) && !nom.equals(user.getEmail()) ? nom + " (" + user.getEmail() + ")" : nom;
    }

    private static String nomRole(Role role) {
        return role != null ? role.getNom() : null;
    }

    private static String libelleRole(String nomRole) {
        return nomRole != null ? nomRole : "aucun";
    }

    private static String nombre(long nombre, String singulier, String pluriel) {
        return nombre + " " + (nombre > 1 ? pluriel : singulier);
    }
}
