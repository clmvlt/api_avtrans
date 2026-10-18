package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.service.PeriodeHeures.Fenetre;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodeHeuresTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final ZonedDateTime MAINTENANT = ZonedDateTime.of(2026, 10, 13, 3, 0, 0, 0, PARIS);

    @Test
    void should_cover_calendar_day_when_period_is_day() {
        Fenetre fenetre = PeriodeHeures.DAY.fenetre(MAINTENANT);

        assertThat(fenetre.debut()).isEqualTo(ZonedDateTime.of(2026, 10, 13, 0, 0, 0, 0, PARIS));
        assertThat(fenetre.fin()).isEqualTo(ZonedDateTime.of(2026, 10, 14, 0, 0, 0, 0, PARIS));
        assertThat(fenetre.contient(ZonedDateTime.of(2026, 10, 12, 21, 0, 0, 0, PARIS))).isFalse();
        assertThat(fenetre.contient(fenetre.fin())).isFalse();
        assertThat(fenetre.contient(fenetre.debut())).isTrue();
    }

    @Test
    void should_use_requested_day_when_parameters_are_given() {
        Fenetre fenetre = PeriodeHeures.DAY.fenetre(MAINTENANT, 2026, 10, null, 12);

        assertThat(fenetre.debut().toLocalDate()).isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void should_start_on_monday_when_period_is_week() {
        assertThat(PeriodeHeures.WEEK.fenetre(MAINTENANT).debut().toLocalDate()).isEqualTo(LocalDate.of(2026, 10, 12));
        // Semaine ISO 1 de 2027 : du lundi 4 janvier
        Fenetre semaine1 = PeriodeHeures.WEEK.fenetre(MAINTENANT, 2027, null, 1, null);
        assertThat(semaine1.debut().toLocalDate()).isEqualTo(LocalDate.of(2027, 1, 4));
        assertThat(semaine1.fin().toLocalDate()).isEqualTo(LocalDate.of(2027, 1, 11));
    }

    @Test
    void should_return_previous_year_december_when_last_month_is_asked_in_january() {
        Fenetre fenetre = PeriodeHeures.LASTMONTH.fenetre(ZonedDateTime.of(2027, 1, 5, 10, 0, 0, 0, PARIS));

        assertThat(fenetre.debut().toLocalDate()).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(fenetre.fin().toLocalDate()).isEqualTo(LocalDate.of(2027, 1, 1));
    }

    @Test
    void should_parse_period_case_insensitively_and_reject_unknown_ones() {
        assertThat(PeriodeHeures.parse("lastmonth")).isEqualTo(PeriodeHeures.LASTMONTH);
        assertThat(PeriodeHeures.parse("Day")).isEqualTo(PeriodeHeures.DAY);
        assertThatThrownBy(() -> PeriodeHeures.parse("decade")).hasMessageContaining("Invalid period");
        assertThatThrownBy(() -> PeriodeHeures.parse(null)).hasMessageContaining("Invalid period");
    }
}
