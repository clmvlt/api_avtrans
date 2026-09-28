package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.Absence.AbsencePeriod;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.AbsenceType.ModeDecompte;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator.AbsenceDecompte;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator.CreditsPeriode;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator.JourDecompte;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator.MotifJour;
import bzh.stack.apiavtrans.service.HeuresAbsenceCalculator.PrevisionMois;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HeuresAbsenceCalculatorTest {

    private static final double CONTRAT_35H = 151.67;
    private static final double CONTRAT_39H = 169.0;

    private final HeuresAbsenceCalculator calculator = new HeuresAbsenceCalculator(new JoursFeriesService());

    // ── Jours ouvrables (règle des congés payés) ──

    @Test
    void should_count_six_days_and_weekly_hours_when_leave_is_monday_to_friday() {
        AbsenceDecompte decompte = ouvrables(date(2026, 10, 5), date(2026, 10, 9), CONTRAT_35H);

        assertThat(decompte.joursDecomptes()).isEqualTo(6.0);
        assertThat(decompte.heuresCalculees()).isEqualTo(35.0);
        JourDecompte samedi = decompte.jours().get(decompte.jours().size() - 1);
        assertThat(samedi.date()).isEqualTo(date(2026, 10, 10));
        assertThat(samedi.motif()).isEqualTo(MotifJour.SAMEDI_REPRISE);
        assertThat(samedi.fraction()).isEqualTo(1.0);
    }

    @Test
    void should_count_six_days_when_leave_is_monday_to_saturday() {
        AbsenceDecompte decompte = ouvrables(date(2026, 10, 5), date(2026, 10, 10), CONTRAT_35H);

        assertThat(decompte.joursDecomptes()).isEqualTo(6.0);
        assertThat(decompte.heuresCalculees()).isEqualTo(35.0);
        assertThat(decompte.jours()).extracting(JourDecompte::motif).doesNotContain(MotifJour.SAMEDI_REPRISE);
    }

    @Test
    void should_exclude_sunday_when_leave_is_monday_to_sunday() {
        AbsenceDecompte decompte = ouvrables(date(2026, 10, 5), date(2026, 10, 11), CONTRAT_35H);

        assertThat(decompte.joursDecomptes()).isEqualTo(6.0);
        assertThat(decompte.heuresCalculees()).isEqualTo(35.0);
        assertThat(decompte.jours().get(6).motif()).isEqualTo(MotifJour.DIMANCHE);
        assertThat(decompte.jours().get(6).fraction()).isZero();
    }

    @Test
    void should_exclude_holiday_when_leave_week_contains_one() {
        // 11/11/2026 est un mercredi
        AbsenceDecompte decompte = ouvrables(date(2026, 11, 9), date(2026, 11, 13), CONTRAT_35H);

        assertThat(decompte.joursDecomptes()).isEqualTo(5.0);
        assertThat(decompte.heuresCalculees()).isEqualTo(29.17);
        JourDecompte mercredi = decompte.jours().get(2);
        assertThat(mercredi.motif()).isEqualTo(MotifJour.FERIE);
        assertThat(mercredi.ferie()).isEqualTo("Armistice 1918");
    }

    @Test
    void should_not_add_saturday_when_it_is_a_holiday() {
        // 15/08/2026 est un samedi
        AbsenceDecompte decompte = ouvrables(date(2026, 8, 10), date(2026, 8, 14), CONTRAT_35H);

        assertThat(decompte.joursDecomptes()).isEqualTo(5.0);
        JourDecompte samedi = decompte.jours().get(decompte.jours().size() - 1);
        assertThat(samedi.date()).isEqualTo(date(2026, 8, 15));
        assertThat(samedi.motif()).isEqualTo(MotifJour.FERIE);
        assertThat(samedi.fraction()).isZero();
    }

    @Test
    void should_value_a_day_at_six_and_a_half_hours_when_contract_is_169h() {
        AbsenceDecompte decompte = ouvrables(date(2026, 10, 5), date(2026, 10, 9), CONTRAT_39H);

        assertThat(decompte.heuresParJour()).isCloseTo(6.5, within(1e-9));
        assertThat(decompte.heuresCalculees()).isEqualTo(39.0);
    }

    // ── Autres modes ──

    @Test
    void should_exclude_weekend_when_mode_is_jours_ouvres() {
        AbsenceDecompte decompte = calculator.decompter(date(2026, 10, 5), date(2026, 10, 11), AbsencePeriod.FULL_DAY,
                ModeDecompte.JOURS_OUVRES, true, CONTRAT_35H, null);

        assertThat(decompte.joursDecomptes()).isEqualTo(5.0);
        assertThat(decompte.heuresCalculees()).isEqualTo(35.0);
        assertThat(decompte.jours().get(5).motif()).isEqualTo(MotifJour.SAMEDI);
        assertThat(decompte.jours()).hasSize(7);
    }

    @Test
    void should_count_every_day_when_mode_is_jours_calendaires() {
        AbsenceDecompte decompte = calculator.decompter(date(2026, 11, 9), date(2026, 11, 15), AbsencePeriod.FULL_DAY,
                ModeDecompte.JOURS_CALENDAIRES, true, CONTRAT_35H, null);

        assertThat(decompte.joursDecomptes()).isEqualTo(7.0);
        assertThat(decompte.heuresCalculees()).isEqualTo(35.0);
        assertThat(decompte.jours().get(2).ferie()).isEqualTo("Armistice 1918");
        assertThat(decompte.jours().get(2).motif()).isNull();
    }

    // ── Demi-journées ──

    @Test
    void should_count_half_day_when_period_is_morning() {
        AbsenceDecompte decompte = calculator.decompter(date(2026, 10, 5), date(2026, 10, 5), AbsencePeriod.MORNING,
                ModeDecompte.JOURS_OUVRABLES, true, CONTRAT_35H, null);

        assertThat(decompte.joursDecomptes()).isEqualTo(0.5);
        assertThat(decompte.heuresCalculees()).isEqualTo(2.92);
    }

    @Test
    void should_not_add_saturday_when_half_day_on_friday() {
        AbsenceDecompte decompte = calculator.decompter(date(2026, 10, 9), date(2026, 10, 9), AbsencePeriod.AFTERNOON,
                ModeDecompte.JOURS_OUVRABLES, true, CONTRAT_35H, null);

        assertThat(decompte.joursDecomptes()).isEqualTo(0.5);
        assertThat(decompte.jours()).hasSize(1);
    }

    // ── Contrat, type et heures forcées ──

    @Test
    void should_count_days_but_no_hours_when_contract_is_missing() {
        AbsenceDecompte decompte = ouvrables(date(2026, 10, 5), date(2026, 10, 9), null);

        assertThat(decompte.joursDecomptes()).isEqualTo(6.0);
        assertThat(decompte.heuresCalculees()).isZero();
        assertThat(decompte.contratRenseigne()).isFalse();
    }

    @Test
    void should_count_no_hours_when_type_does_not_credit_hours() {
        AbsenceDecompte decompte = calculator.decompter(date(2026, 10, 5), date(2026, 10, 9), AbsencePeriod.FULL_DAY,
                ModeDecompte.JOURS_OUVRABLES, false, CONTRAT_35H, null);

        assertThat(decompte.joursDecomptes()).isEqualTo(6.0);
        assertThat(decompte.heuresCalculees()).isZero();
        assertThat(decompte.contratRenseigne()).isTrue();
    }

    @Test
    void should_return_empty_count_when_start_is_after_end() {
        AbsenceDecompte decompte = ouvrables(date(2026, 10, 9), date(2026, 10, 5), CONTRAT_35H);

        assertThat(decompte.jours()).isEmpty();
        assertThat(decompte.joursDecomptes()).isZero();
    }

    @Test
    void should_use_default_rules_when_absence_has_no_type() {
        Absence absence = absence(user(CONTRAT_35H), null, date(2026, 10, 5), date(2026, 10, 9));

        AbsenceDecompte decompte = calculator.decompter(absence);

        assertThat(decompte.mode()).isEqualTo(ModeDecompte.JOURS_OUVRABLES);
        assertThat(decompte.compteHeures()).isTrue();
        assertThat(decompte.heures()).isEqualTo(35.0);
    }

    @Test
    void should_keep_forced_hours_and_spread_them_when_absence_spans_two_months() {
        // Mer. 30/09 → ven. 02/10/2026 + samedi 03/10 : 4 jours, 20 h forcées → 5 h par jour
        Absence absence = absence(user(CONTRAT_35H), type(ModeDecompte.JOURS_OUVRABLES, true),
                date(2026, 9, 30), date(2026, 10, 2));
        absence.setHeuresForcees(20.0);

        AbsenceDecompte decompte = calculator.decompter(absence);
        CreditsPeriode octobre = calculator.crediterPeriode(absence.getUser(), List.of(absence),
                date(2026, 10, 1), date(2026, 10, 31), Set.of());

        assertThat(decompte.heures()).isEqualTo(20.0);
        assertThat(decompte.heuresCalculees()).isEqualTo(23.33);
        assertThat(decompte.heuresParDate()).containsEntry(date(2026, 9, 30), 5.0);
        assertThat(octobre.heuresAbsences()).isEqualTo(15.0);
    }

    // ── Crédit sur une période ──

    @Test
    void should_credit_saturday_after_friday_end_and_saturday_holiday_when_next_month() {
        // Absence lun. 27/07 → ven. 31/07/2026 : le samedi 01/08 compte en août ; 15/08 (samedi) férié chômé
        User user = user(CONTRAT_35H);
        Absence absence = absence(user, type(ModeDecompte.JOURS_OUVRABLES, true), date(2026, 7, 27), date(2026, 7, 31));

        CreditsPeriode aout = calculator.crediterPeriode(user, List.of(absence),
                date(2026, 8, 1), date(2026, 8, 31), Set.of());

        assertThat(aout.heuresAbsences()).isEqualTo(5.83);
        assertThat(aout.heuresFeries()).isEqualTo(5.83);
        assertThat(aout.joursFeries()).isEqualTo(1);
        assertThat(aout.total()).isEqualTo(11.67);
        assertThat(aout.jours()).containsKeys(date(2026, 8, 1), date(2026, 8, 15));
    }

    @Test
    void should_not_credit_holiday_when_it_was_worked_or_on_sunday() {
        // Novembre 2026 : 1er (dimanche) et 11 (mercredi, pointé)
        CreditsPeriode novembre = calculator.crediterPeriode(user(CONTRAT_35H), List.of(),
                date(2026, 11, 1), date(2026, 11, 30), Set.of(date(2026, 11, 11)));

        assertThat(novembre.joursFeries()).isZero();
        assertThat(novembre.heuresFeries()).isZero();
    }

    @Test
    void should_credit_holiday_during_paid_leave_but_not_during_unpaid_leave() {
        User user = user(CONTRAT_35H);
        Absence conges = absence(user, type(ModeDecompte.JOURS_OUVRABLES, true), date(2026, 11, 9), date(2026, 11, 13));
        Absence sansSolde = absence(user, type(ModeDecompte.JOURS_OUVRABLES, false), date(2026, 11, 9), date(2026, 11, 13));

        CreditsPeriode avecConges = calculator.crediterPeriode(user, List.of(conges),
                date(2026, 11, 1), date(2026, 11, 30), Set.of());
        CreditsPeriode avecSansSolde = calculator.crediterPeriode(user, List.of(sansSolde),
                date(2026, 11, 1), date(2026, 11, 30), Set.of());

        // Congés : 5 jours (29,17 h) + férié du 11 (5,83 h) = une semaine de contrat
        assertThat(avecConges.heuresAbsences()).isEqualTo(29.17);
        assertThat(avecConges.heuresFeries()).isEqualTo(5.83);
        assertThat(avecConges.total()).isEqualTo(35.0);
        assertThat(avecSansSolde.heuresAbsences()).isZero();
        assertThat(avecSansSolde.heuresFeries()).isZero();
        assertThat(avecSansSolde.joursFeries()).isZero();
    }

    @Test
    void should_count_holiday_without_hours_when_contract_is_missing() {
        CreditsPeriode novembre = calculator.crediterPeriode(user(null), List.of(),
                date(2026, 11, 1), date(2026, 11, 30), Set.of());

        assertThat(novembre.joursFeries()).isEqualTo(1);
        assertThat(novembre.heuresFeries()).isZero();
        assertThat(novembre.jours()).isEmpty();
    }

    // ── Prévision de fin de mois ──

    @Test
    void should_expect_seven_hours_per_remaining_weekday_when_contract_is_35h() {
        // Lundi 28 septembre 2026 : restent le 28, le 29 et le 30
        PrevisionMois prevision = calculator.prevoirFinDeMois(CONTRAT_35H, List.of(),
                date(2026, 9, 1), date(2026, 9, 30), date(2026, 9, 28), 0);

        assertThat(prevision.joursOuvresRestants()).isEqualTo(3.0);
        assertThat(prevision.heuresParJour()).isEqualTo(7.0);
        assertThat(prevision.heuresRestantes()).isEqualTo(21.0);
    }

    @Test
    void should_deduct_hours_already_worked_today_when_forecasting() {
        PrevisionMois partiel = calculator.prevoirFinDeMois(CONTRAT_35H, List.of(),
                date(2026, 9, 1), date(2026, 9, 30), date(2026, 9, 28), 3);
        PrevisionMois depasse = calculator.prevoirFinDeMois(CONTRAT_35H, List.of(),
                date(2026, 9, 1), date(2026, 9, 30), date(2026, 9, 28), 9);

        assertThat(partiel.joursOuvresRestants()).isEqualTo(3.0);
        assertThat(partiel.heuresRestantes()).isEqualTo(18.0);
        // Journée déjà dépassée : rien n'est retiré aux jours suivants
        assertThat(depasse.heuresRestantes()).isEqualTo(14.0);
    }

    @Test
    void should_skip_holidays_and_approved_absences_when_forecasting() {
        // Lundi 9 novembre 2026 : 16 jours lun.-ven. jusqu'au 30, moins le 11 (férié),
        // moins la semaine du 16 au 20 (congés), moins une demi-journée le 23
        User user = user(CONTRAT_35H);
        Absence conges = absence(user, type(ModeDecompte.JOURS_OUVRABLES, true), date(2026, 11, 16), date(2026, 11, 20));
        Absence demiJournee = absence(user, type(ModeDecompte.JOURS_OUVRABLES, true), date(2026, 11, 23), date(2026, 11, 23));
        demiJournee.setPeriod(AbsencePeriod.MORNING);

        PrevisionMois prevision = calculator.prevoirFinDeMois(CONTRAT_35H, List.of(conges, demiJournee),
                date(2026, 11, 1), date(2026, 11, 30), date(2026, 11, 9), 0);

        assertThat(prevision.joursOuvresRestants()).isEqualTo(9.5);
        assertThat(prevision.heuresRestantes()).isEqualTo(66.5);
    }

    @Test
    void should_skip_days_of_unpaid_leave_when_forecasting() {
        User user = user(CONTRAT_35H);
        Absence sansSolde = absence(user, type(ModeDecompte.JOURS_OUVRABLES, false), date(2026, 9, 29), date(2026, 9, 30));

        PrevisionMois prevision = calculator.prevoirFinDeMois(CONTRAT_35H, List.of(sansSolde),
                date(2026, 9, 1), date(2026, 9, 30), date(2026, 9, 28), 0);

        assertThat(prevision.joursOuvresRestants()).isEqualTo(1.0);
        assertThat(prevision.heuresRestantes()).isEqualTo(7.0);
    }

    @Test
    void should_have_nothing_left_when_month_is_past() {
        PrevisionMois prevision = calculator.prevoirFinDeMois(CONTRAT_35H, List.of(),
                date(2026, 8, 1), date(2026, 8, 31), date(2026, 9, 28), 5);

        assertThat(prevision.joursOuvresRestants()).isZero();
        assertThat(prevision.heuresRestantes()).isZero();
    }

    @Test
    void should_count_whole_month_when_month_is_future() {
        // Décembre 2026 : 23 jours lun.-ven., moins Noël (vendredi 25)
        PrevisionMois prevision = calculator.prevoirFinDeMois(CONTRAT_39H, List.of(),
                date(2026, 12, 1), date(2026, 12, 31), date(2026, 9, 28), 0);

        assertThat(prevision.joursOuvresRestants()).isEqualTo(22.0);
        assertThat(prevision.heuresParJour()).isEqualTo(7.8);
        assertThat(prevision.heuresRestantes()).isEqualTo(171.6);
    }

    @Test
    void should_count_days_without_hours_when_contract_is_missing() {
        PrevisionMois prevision = calculator.prevoirFinDeMois(null, List.of(),
                date(2026, 9, 1), date(2026, 9, 30), date(2026, 9, 28), 0);

        assertThat(prevision.joursOuvresRestants()).isEqualTo(3.0);
        assertThat(prevision.heuresParJour()).isNull();
        assertThat(prevision.heuresRestantes()).isNull();
    }

    // ── Helpers ──

    private AbsenceDecompte ouvrables(LocalDate start, LocalDate end, Double contrat) {
        return calculator.decompter(start, end, AbsencePeriod.FULL_DAY, ModeDecompte.JOURS_OUVRABLES, true, contrat, null);
    }

    private static LocalDate date(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    private static User user(Double heureContrat) {
        User user = new User();
        user.setHeureContrat(heureContrat);
        return user;
    }

    private static AbsenceType type(ModeDecompte mode, boolean compteHeures) {
        AbsenceType type = new AbsenceType();
        type.setName("Type");
        type.setModeDecompte(mode);
        type.setCompteHeures(compteHeures);
        return type;
    }

    private static Absence absence(User user, AbsenceType type, LocalDate start, LocalDate end) {
        Absence absence = new Absence();
        absence.setUser(user);
        absence.setAbsenceType(type);
        absence.setStartDate(start);
        absence.setEndDate(end);
        absence.setPeriod(AbsencePeriod.FULL_DAY);
        absence.setStatus(Absence.AbsenceStatus.APPROVED);
        return absence;
    }
}
