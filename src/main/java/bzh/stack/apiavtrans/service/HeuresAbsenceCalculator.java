package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.Absence.AbsencePeriod;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.AbsenceType.ModeDecompte;
import bzh.stack.apiavtrans.entity.User;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Heures créditées par les absences et les jours fériés chômés (calcul pur, sans accès base).
 *
 * <p>Règles (droit du travail et convention des transports routiers) :
 * <ul>
 *   <li>base : heures mensuelles du contrat ramenées à la semaine (× 12 / 52) ; sans contrat, 0 h ;</li>
 *   <li>valeur d'un jour = heures hebdomadaires / jours par semaine du mode de décompte
 *       (6 en jours ouvrables, 5 en jours ouvrés, 7 en jours calendaires) : une semaine complète
 *       vaut toujours les heures hebdomadaires du contrat ;</li>
 *   <li>jours ouvrables : dimanches et fériés exclus ; une absence en journée complète qui finit un
 *       vendredi décompte aussi le samedi suivant (veille de la reprise), sauf s'il est férié ;</li>
 *   <li>jours ouvrés : samedis, dimanches et fériés exclus ; jours calendaires : aucun jour exclu ;</li>
 *   <li>demi-journée : 0,5 jour par jour décompté ;</li>
 *   <li>jour férié chômé du lundi au samedi (non pointé, hors absence sans solde) : crédite
 *       heures hebdomadaires / 6.</li>
 * </ul>
 */
@Component
public class HeuresAbsenceCalculator {

    /** Nombre moyen de semaines par mois (52 / 12). */
    public static final double SEMAINES_PAR_MOIS = 52.0 / 12.0;

    /** Jours par semaine pour la valeur d'un jour férié chômé (jours ouvrables). */
    private static final int JOURS_OUVRABLES_PAR_SEMAINE = 6;

    /** Jours par semaine d'un temps plein du lundi au vendredi (prévision de fin de mois). */
    private static final int JOURS_OUVRES_PAR_SEMAINE = 5;

    private final JoursFeriesService joursFeriesService;

    public HeuresAbsenceCalculator(JoursFeriesService joursFeriesService) {
        this.joursFeriesService = joursFeriesService;
    }

    /** Raison pour laquelle un jour n'est pas décompté, ou samedi ajouté (null = jour décompté). */
    public enum MotifJour {
        DIMANCHE,
        SAMEDI,
        FERIE,
        SAMEDI_REPRISE
    }

    /**
     * Un jour du décompte.
     *
     * @param fraction 0 (non décompté), 0,5 (demi-journée) ou 1
     * @param heures   heures calculées pour ce jour (non arrondies)
     * @param motif    raison de l'exclusion, SAMEDI_REPRISE pour le samedi ajouté, null sinon
     * @param ferie    nom du jour férié, ou null
     */
    public record JourDecompte(LocalDate date, double fraction, double heures, MotifJour motif, String ferie) {
    }

    /** Résultat du décompte d'une absence. */
    public record AbsenceDecompte(
            ModeDecompte mode,
            boolean compteHeures,
            Double heureContratMensuel,
            Double heuresHebdo,
            double heuresParJour,
            double joursDecomptes,
            double heuresCalculees,
            Double heuresForcees,
            List<JourDecompte> jours
    ) {
        public boolean contratRenseigne() {
            return heuresHebdo != null;
        }

        /** Heures retenues : valeur forcée par un administrateur, sinon valeur calculée. */
        public double heures() {
            return heuresForcees != null ? heuresForcees : heuresCalculees;
        }

        /**
         * Heures retenues réparties par date (dates décomptées uniquement). Une valeur forcée est
         * répartie au prorata des jours décomptés, ou portée sur le premier jour si aucun jour
         * n'est décompté.
         */
        public Map<LocalDate, Double> heuresParDate() {
            Map<LocalDate, Double> result = new LinkedHashMap<>();
            if (heuresForcees == null) {
                for (JourDecompte jour : jours) {
                    if (jour.fraction() > 0) {
                        result.put(jour.date(), jour.heures());
                    }
                }
                return result;
            }
            if (joursDecomptes <= 0) {
                if (!jours.isEmpty()) {
                    result.put(jours.get(0).date(), heuresForcees);
                }
                return result;
            }
            for (JourDecompte jour : jours) {
                if (jour.fraction() > 0) {
                    result.put(jour.date(), heuresForcees * jour.fraction() / joursDecomptes);
                }
            }
            return result;
        }
    }

    /** Heures créditées pour un jour : absences et jour férié chômé. */
    public record CreditJour(double heuresAbsences, double heuresFerie) {
        public double total() {
            return heuresAbsences + heuresFerie;
        }
    }

    /**
     * Heures créditées sur une période. Les totaux sont arrondis à 2 décimales ; {@code total} est
     * l'arrondi de la somme exacte (comme la somme de la colonne de l'export Excel).
     */
    public record CreditsPeriode(
            Map<LocalDate, CreditJour> jours,
            double heuresAbsences,
            double heuresFeries,
            int joursFeries,
            double total
    ) {
    }

    /**
     * Prévision de fin de mois.
     *
     * @param joursOuvresRestants jours ouvrés (lun.-ven., hors fériés) d'aujourd'hui inclus à la fin du
     *                            mois, absences approuvées déduites (demi-journée = 0,5)
     * @param heuresParJour       heures d'un jour ouvré selon le contrat (heures hebdomadaires / 5),
     *                            null sans contrat
     * @param heuresRestantes     heures encore attendues d'ici la fin du mois (aujourd'hui : heures déjà
     *                            pointées déduites), null sans contrat
     */
    public record PrevisionMois(double joursOuvresRestants, Double heuresParJour, Double heuresRestantes) {
    }

    // ── Règles des types d'absence ──

    /** Mode de décompte d'un type (jours ouvrables par défaut, et pour un type personnalisé). */
    public static ModeDecompte modeDe(AbsenceType type) {
        return type == null || type.getModeDecompte() == null
                ? ModeDecompte.JOURS_OUVRABLES
                : type.getModeDecompte();
    }

    /** Le type crédite-t-il des heures (oui par défaut, et pour un type personnalisé) ? */
    public static boolean compteHeuresDe(AbsenceType type) {
        return type == null || type.getCompteHeures() == null || type.getCompteHeures();
    }

    /** Heures hebdomadaires d'un contrat mensuel, ou null si le contrat n'est pas renseigné. */
    public static Double heuresHebdo(Double heureContratMensuel) {
        if (heureContratMensuel == null || heureContratMensuel <= 0) {
            return null;
        }
        return heureContratMensuel / SEMAINES_PAR_MOIS;
    }

    // ── Décompte d'une absence ──

    /** Décompte d'une absence enregistrée (type, contrat actuel de l'employé, heures forcées). */
    public AbsenceDecompte decompter(Absence absence) {
        AbsenceType type = absence.getAbsenceType();
        Double contrat = absence.getUser() != null ? absence.getUser().getHeureContrat() : null;
        return decompter(absence.getStartDate(), absence.getEndDate(), absence.getPeriod(),
                modeDe(type), compteHeuresDe(type), contrat, absence.getHeuresForcees());
    }

    /**
     * Décompte d'une plage de dates (bornes incluses).
     *
     * @param heureContratMensuel heures mensuelles du contrat (null ou ≤ 0 : 0 h)
     * @param heuresForcees       valeur fixée à la main, ou null
     */
    public AbsenceDecompte decompter(LocalDate start, LocalDate end, AbsencePeriod period,
                                     ModeDecompte mode, boolean compteHeures,
                                     Double heureContratMensuel, Double heuresForcees) {
        ModeDecompte effectiveMode = mode != null ? mode : ModeDecompte.JOURS_OUVRABLES;
        double fractionJour = period == null || period == AbsencePeriod.FULL_DAY ? 1.0 : 0.5;
        Double hebdo = heuresHebdo(heureContratMensuel);
        double heuresParJour = hebdo != null && compteHeures
                ? hebdo / effectiveMode.getJoursParSemaine()
                : 0.0;

        List<JourDecompte> jours = new ArrayList<>();
        double joursDecomptes = 0;

        if (start != null && end != null && !start.isAfter(end)) {
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                String ferie = joursFeriesService.nom(date);
                MotifJour motif = motifExclusion(date, ferie, effectiveMode);
                double fraction = motif == null ? fractionJour : 0.0;
                joursDecomptes += fraction;
                jours.add(new JourDecompte(date, fraction, fraction * heuresParJour, motif, ferie));
            }

            // Jours ouvrables : le samedi qui précède la reprise du lundi est décompté
            if (effectiveMode == ModeDecompte.JOURS_OUVRABLES
                    && fractionJour == 1.0
                    && end.getDayOfWeek() == DayOfWeek.FRIDAY) {
                LocalDate samedi = end.plusDays(1);
                String ferie = joursFeriesService.nom(samedi);
                if (ferie == null) {
                    joursDecomptes += 1.0;
                    jours.add(new JourDecompte(samedi, 1.0, heuresParJour, MotifJour.SAMEDI_REPRISE, null));
                } else {
                    jours.add(new JourDecompte(samedi, 0.0, 0.0, MotifJour.FERIE, ferie));
                }
            }
        }

        return new AbsenceDecompte(
                effectiveMode,
                compteHeures,
                heureContratMensuel,
                hebdo,
                heuresParJour,
                joursDecomptes,
                round2(joursDecomptes * heuresParJour),
                heuresForcees,
                Collections.unmodifiableList(jours)
        );
    }

    private static MotifJour motifExclusion(LocalDate date, String ferie, ModeDecompte mode) {
        if (mode == ModeDecompte.JOURS_CALENDAIRES) {
            return null;
        }
        if (ferie != null) {
            return MotifJour.FERIE;
        }
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SUNDAY) {
            return MotifJour.DIMANCHE;
        }
        if (day == DayOfWeek.SATURDAY && mode == ModeDecompte.JOURS_OUVRES) {
            return MotifJour.SAMEDI;
        }
        return null;
    }

    // ── Crédit d'heures sur une période ──

    /**
     * Heures créditées jour par jour sur une période (bornes incluses) : absences approuvées et
     * jours fériés chômés.
     *
     * @param absencesApprouvees absences approuvées de l'employé chevauchant la période ; inclure
     *                           celles qui finissent la veille de {@code from} (samedi de reprise)
     * @param joursTravailles    jours pointés : un férié travaillé n'est pas crédité
     */
    public CreditsPeriode crediterPeriode(User user, List<Absence> absencesApprouvees,
                                          LocalDate from, LocalDate to,
                                          Set<LocalDate> joursTravailles) {
        Map<LocalDate, double[]> parJour = new TreeMap<>();
        Set<LocalDate> joursSansCredit = new HashSet<>();
        double totalAbsences = 0;

        for (Absence absence : absencesApprouvees) {
            AbsenceDecompte decompte = decompter(absence);
            for (Map.Entry<LocalDate, Double> entry : decompte.heuresParDate().entrySet()) {
                LocalDate date = entry.getKey();
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    parJour.computeIfAbsent(date, d -> new double[2])[0] += entry.getValue();
                    totalAbsences += entry.getValue();
                }
            }
            if (!decompte.compteHeures()) {
                for (LocalDate d = absence.getStartDate(); !d.isAfter(absence.getEndDate()); d = d.plusDays(1)) {
                    joursSansCredit.add(d);
                }
            }
        }

        Double hebdo = heuresHebdo(user != null ? user.getHeureContrat() : null);
        double valeurFerie = hebdo != null ? hebdo / JOURS_OUVRABLES_PAR_SEMAINE : 0.0;
        double totalFeries = 0;
        int joursFeries = 0;

        for (LocalDate date : joursFeriesService.entre(from, to).keySet()) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY
                    || (joursTravailles != null && joursTravailles.contains(date))
                    || joursSansCredit.contains(date)) {
                continue;
            }
            joursFeries++;
            if (valeurFerie > 0) {
                parJour.computeIfAbsent(date, d -> new double[2])[1] += valeurFerie;
                totalFeries += valeurFerie;
            }
        }

        Map<LocalDate, CreditJour> jours = new TreeMap<>();
        parJour.forEach((date, values) -> jours.put(date, new CreditJour(values[0], values[1])));

        return new CreditsPeriode(
                Collections.unmodifiableMap(jours),
                round2(totalAbsences),
                round2(totalFeries),
                joursFeries,
                round2(totalAbsences + totalFeries)
        );
    }

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // ── Prévision de fin de mois ──

    /**
     * Jours ouvrés restants et heures encore attendues jusqu'à la fin du mois, au rythme du contrat
     * (heures hebdomadaires / 5 par jour ouvré). Les fériés et les absences approuvées sont déjà
     * crédités sur le mois entier : ces jours ne sont pas comptés une seconde fois. Un mois passé
     * n'a plus de jour restant ; un mois futur est compté en entier.
     *
     * @param absencesApprouvees absences approuvées de l'employé chevauchant le mois
     * @param aujourdhui         date du jour (Europe/Paris)
     * @param heuresAujourdhui   heures déjà pointées aujourd'hui, pauses déduites
     */
    public PrevisionMois prevoirFinDeMois(Double heureContratMensuel, List<Absence> absencesApprouvees,
                                          LocalDate firstDay, LocalDate lastDay,
                                          LocalDate aujourdhui, double heuresAujourdhui) {
        Double hebdo = heuresHebdo(heureContratMensuel);
        Double heuresParJour = hebdo != null ? hebdo / JOURS_OUVRES_PAR_SEMAINE : null;
        LocalDate debut = aujourdhui.isAfter(firstDay) ? aujourdhui : firstDay;

        double jours = 0;
        double heures = 0;
        for (LocalDate date = debut; !date.isAfter(lastDay); date = date.plusDays(1)) {
            double fraction = fractionATravailler(date, absencesApprouvees);
            if (fraction <= 0) {
                continue;
            }
            jours += fraction;
            if (heuresParJour != null) {
                double attendu = heuresParJour * fraction;
                if (date.equals(aujourdhui)) {
                    attendu = Math.max(0, attendu - heuresAujourdhui);
                }
                heures += attendu;
            }
        }

        return new PrevisionMois(
                jours,
                heuresParJour != null ? round2(heuresParJour) : null,
                heuresParJour != null ? round2(heures) : null
        );
    }

    /**
     * Part d'une journée encore à travailler : 0 le week-end, un jour férié ou pendant une absence
     * en journée complète, 0,5 pendant une demi-journée d'absence, 1 sinon.
     */
    private double fractionATravailler(LocalDate date, List<Absence> absences) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || joursFeriesService.estFerie(date)) {
            return 0;
        }
        double fraction = 1;
        for (Absence absence : absences) {
            if (absence.getStartDate() == null || absence.getEndDate() == null
                    || date.isBefore(absence.getStartDate()) || date.isAfter(absence.getEndDate())) {
                continue;
            }
            if (absence.getPeriod() == null || absence.getPeriod() == AbsencePeriod.FULL_DAY) {
                return 0;
            }
            fraction = 0.5;
        }
        return fraction;
    }
}
