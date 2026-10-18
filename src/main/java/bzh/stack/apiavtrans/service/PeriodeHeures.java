package bzh.stack.apiavtrans.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Périodes de calcul des heures travaillées. Une période est une fenêtre [debut, fin[ sur le
 * jour de début des services (heure de Paris) : un service compte en entier sur la période où il a commencé.
 */
public enum PeriodeHeures {
    DAY, WEEK, MONTH, YEAR, LASTMONTH;

    public static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    /** Fenêtre [debut, fin[ (fin exclue). */
    public record Fenetre(ZonedDateTime debut, ZonedDateTime fin) {
        public boolean contient(ZonedDateTime instant) {
            return instant != null && !instant.isBefore(debut) && instant.isBefore(fin);
        }

        public static Fenetre jours(LocalDate premierJour, LocalDate dernierJour) {
            return new Fenetre(premierJour.atStartOfDay(PARIS), dernierJour.plusDays(1).atStartOfDay(PARIS));
        }
    }

    public static PeriodeHeures parse(String period) {
        if (period == null) {
            throw new RuntimeException("Invalid period. Use: day, week, month, year, or lastmonth");
        }
        try {
            return valueOf(period.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid period. Use: day, week, month, year, or lastmonth");
        }
    }

    /** Fenêtre de la période en cours à {@code maintenant}. */
    public Fenetre fenetre(ZonedDateTime maintenant) {
        return fenetre(maintenant, null, null, null, null);
    }

    /**
     * Fenêtre de la période ciblée ; chaque paramètre absent prend la valeur courante.
     * Pour la semaine, {@code year} est l'année ISO de la semaine.
     */
    public Fenetre fenetre(ZonedDateTime maintenant, Integer year, Integer month, Integer week, Integer day) {
        LocalDate aujourdhui = maintenant.withZoneSameInstant(PARIS).toLocalDate();
        int annee = year != null ? year : aujourdhui.getYear();
        int mois = month != null ? month : aujourdhui.getMonthValue();

        return switch (this) {
            case DAY -> {
                LocalDate jour = LocalDate.of(annee, mois, day != null ? day : aujourdhui.getDayOfMonth());
                yield Fenetre.jours(jour, jour);
            }
            case WEEK -> {
                LocalDate lundi;
                if (year == null && week == null) {
                    lundi = aujourdhui.with(DayOfWeek.MONDAY);
                } else {
                    int anneeIso = year != null ? year : aujourdhui.get(WeekFields.ISO.weekBasedYear());
                    int semaine = week != null ? week : aujourdhui.get(WeekFields.ISO.weekOfWeekBasedYear());
                    lundi = LocalDate.of(anneeIso, 1, 4)
                            .with(WeekFields.ISO.weekOfWeekBasedYear(), semaine)
                            .with(DayOfWeek.MONDAY);
                }
                yield Fenetre.jours(lundi, lundi.plusDays(6));
            }
            case MONTH -> {
                LocalDate premier = LocalDate.of(annee, mois, 1);
                yield Fenetre.jours(premier, premier.plusMonths(1).minusDays(1));
            }
            case YEAR -> Fenetre.jours(LocalDate.of(annee, 1, 1), LocalDate.of(annee, 12, 31));
            case LASTMONTH -> {
                LocalDate premier = aujourdhui.withDayOfMonth(1).minusMonths(1);
                yield Fenetre.jours(premier, premier.plusMonths(1).minusDays(1));
            }
        };
    }
}
