package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Service;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Calcul du temps de travail net, sans accès à la base.
 * <ul>
 *   <li>Temps net d'un service = durée du service − durée de ses pauses.</li>
 *   <li>Les pauses sont ramenées dans les bornes de leur service et leurs chevauchements fusionnés
 *       (une pause saisie hors du service ou en double n'est jamais déduite deux fois).</li>
 *   <li>Un élément sans fin court jusqu'à {@code maintenant}.</li>
 *   <li>Un service compte en entier sur son jour de début, pauses comprises, même s'il finit le lendemain.</li>
 * </ul>
 */
@Component
public class TempsTravailCalculator {

    /** Bilan d'un ensemble de services : temps net, compteur en cours, temps net par jour de début. */
    public record Bilan(long secondes, boolean enCours, Map<LocalDate, Long> secondesParJour) {
        public static final Bilan VIDE = new Bilan(0, false, Map.of());

        public double heures() {
            return TempsTravailCalculator.heures(secondes);
        }
    }

    /** Durée brute (fin ou maintenant − début) en secondes, jamais négative. */
    public long dureeBrute(Service ligne, ZonedDateTime maintenant) {
        if (ligne == null || ligne.getDebut() == null) {
            return 0;
        }
        Instant fin = finEffective(ligne, maintenant);
        return Math.max(0, Duration.between(ligne.getDebut().toInstant(), fin).getSeconds());
    }

    /** Durée des pauses comprises dans le service, chevauchements fusionnés. */
    public long dureePauses(Service travail, List<Service> pauses, ZonedDateTime maintenant) {
        if (travail == null || travail.getDebut() == null || pauses == null || pauses.isEmpty()) {
            return 0;
        }
        Instant debutService = travail.getDebut().toInstant();
        Instant finService = finEffective(travail, maintenant);

        List<Instant[]> intervalles = new ArrayList<>();
        for (Service pause : pauses) {
            if (pause.getDebut() == null) {
                continue;
            }
            Instant debut = max(pause.getDebut().toInstant(), debutService);
            Instant fin = min(finEffective(pause, maintenant), finService);
            if (fin.isAfter(debut)) {
                intervalles.add(new Instant[]{debut, fin});
            }
        }
        intervalles.sort(Comparator.comparing(i -> i[0]));

        long total = 0;
        Instant debutCourant = null;
        Instant finCourante = null;
        for (Instant[] intervalle : intervalles) {
            if (finCourante == null || intervalle[0].isAfter(finCourante)) {
                if (finCourante != null) {
                    total += Duration.between(debutCourant, finCourante).getSeconds();
                }
                debutCourant = intervalle[0];
                finCourante = intervalle[1];
            } else if (intervalle[1].isAfter(finCourante)) {
                finCourante = intervalle[1];
            }
        }
        if (finCourante != null) {
            total += Duration.between(debutCourant, finCourante).getSeconds();
        }
        return total;
    }

    /** Temps de travail net du service (durée − pauses), jamais négatif. */
    public long dureeTravail(Service travail, List<Service> pauses, ZonedDateTime maintenant) {
        return Math.max(0, dureeBrute(travail, maintenant) - dureePauses(travail, pauses, maintenant));
    }

    /** Service en cours avec une pause en cours. */
    public boolean enPause(Service travail, List<Service> pauses) {
        if (travail == null || travail.getFin() != null || pauses == null) {
            return false;
        }
        return pauses.stream().anyMatch(p -> p.getFin() == null);
    }

    /** Service en cours hors pause : son temps de travail augmente d'une seconde par seconde. */
    public boolean travailEnCours(Service travail, List<Service> pauses) {
        return travail != null && travail.getFin() == null && !enPause(travail, pauses);
    }

    /** Bilan de services de travail, pauses indexées par UUID du service. */
    public Bilan bilan(List<Service> travaux, Map<UUID, List<Service>> pausesParService, ZonedDateTime maintenant) {
        if (travaux == null || travaux.isEmpty()) {
            return Bilan.VIDE;
        }
        long total = 0;
        boolean enCours = false;
        Map<LocalDate, Long> parJour = new TreeMap<>();
        for (Service travail : travaux) {
            List<Service> pauses = pausesParService.getOrDefault(travail.getUuid(), Collections.emptyList());
            long secondes = dureeTravail(travail, pauses, maintenant);
            total += secondes;
            enCours |= travailEnCours(travail, pauses);
            if (dureeBrute(travail, maintenant) > 0) {
                LocalDate jour = travail.getDebut().withZoneSameInstant(PeriodeHeures.PARIS).toLocalDate();
                parJour.merge(jour, secondes, Long::sum);
            }
        }
        return new Bilan(total, enCours, parJour);
    }

    /** Secondes → heures décimales arrondies au centième. */
    public static double heures(long secondes) {
        return BigDecimal.valueOf(secondes / 3600.0).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static Instant finEffective(Service ligne, ZonedDateTime maintenant) {
        return ligne.getFin() != null ? ligne.getFin().toInstant() : maintenant.toInstant();
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }
}
