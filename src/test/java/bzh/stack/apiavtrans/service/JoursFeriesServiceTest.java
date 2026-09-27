package bzh.stack.apiavtrans.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JoursFeriesServiceTest {

    private final JoursFeriesService joursFeriesService = new JoursFeriesService();

    @Test
    void should_compute_easter_when_known_years() {
        assertThat(JoursFeriesService.computeEaster(2024)).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(JoursFeriesService.computeEaster(2025)).isEqualTo(LocalDate.of(2025, 4, 20));
        assertThat(JoursFeriesService.computeEaster(2026)).isEqualTo(LocalDate.of(2026, 4, 5));
    }

    @Test
    void should_return_eleven_holidays_with_moving_ones_when_year_2026() {
        Map<LocalDate, String> holidays = joursFeriesService.annee(2026);

        assertThat(holidays).hasSize(11);
        assertThat(holidays)
                .containsEntry(LocalDate.of(2026, 4, 6), "Lundi de Pâques")
                .containsEntry(LocalDate.of(2026, 5, 14), "Ascension")
                .containsEntry(LocalDate.of(2026, 5, 25), "Lundi de Pentecôte")
                .containsEntry(LocalDate.of(2026, 8, 15), "Assomption")
                .containsEntry(LocalDate.of(2026, 11, 11), "Armistice 1918");
    }

    @Test
    void should_return_sorted_holidays_between_dates_when_range_spans_years() {
        Map<LocalDate, String> holidays = joursFeriesService.entre(LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 1));

        assertThat(holidays.keySet()).containsExactly(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 11),
                LocalDate.of(2026, 12, 25),
                LocalDate.of(2027, 1, 1));
    }

    @Test
    void should_return_empty_when_no_holiday_in_range() {
        assertThat(joursFeriesService.entre(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31))).isEmpty();
    }

    @Test
    void should_tell_holiday_name_when_date_is_holiday() {
        assertThat(joursFeriesService.estFerie(LocalDate.of(2026, 12, 25))).isTrue();
        assertThat(joursFeriesService.nom(LocalDate.of(2026, 12, 25))).isEqualTo("Noël");
        assertThat(joursFeriesService.estFerie(LocalDate.of(2026, 12, 24))).isFalse();
        assertThat(joursFeriesService.nom(LocalDate.of(2026, 12, 24))).isNull();
    }
}
