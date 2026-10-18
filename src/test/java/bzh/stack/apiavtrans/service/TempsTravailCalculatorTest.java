package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.service.TempsTravailCalculator.Bilan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TempsTravailCalculatorTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final long HEURE = 3600;

    private final TempsTravailCalculator calculator = new TempsTravailCalculator();

    // ── Service de nuit (cas remonté : service le 12/10 à 21 h, pause le 13/10 de 1 h à 2 h) ──

    @Test
    void should_deduct_break_taken_after_midnight_when_night_service_is_running() {
        Service service = travail(at(12, 21, 0), null);
        Service pause = pause(service, at(13, 1, 0), at(13, 2, 0));
        ZonedDateTime maintenant = at(13, 3, 0);

        assertThat(calculator.dureeBrute(service, maintenant)).isEqualTo(6 * HEURE);
        assertThat(calculator.dureePauses(service, List.of(pause), maintenant)).isEqualTo(HEURE);
        assertThat(calculator.dureeTravail(service, List.of(pause), maintenant)).isEqualTo(5 * HEURE);
        assertThat(calculator.travailEnCours(service, List.of(pause))).isTrue();
        assertThat(calculator.enPause(service, List.of(pause))).isFalse();
    }

    @Test
    void should_count_night_service_on_its_start_day_only_when_building_bilan() {
        Service service = travail(at(12, 21, 0), null);
        Service pause = pause(service, at(13, 1, 0), at(13, 2, 0));

        Bilan bilan = calculator.bilan(List.of(service), Map.of(service.getUuid(), List.of(pause)), at(13, 3, 0));

        assertThat(bilan.secondes()).isEqualTo(5 * HEURE);
        assertThat(bilan.enCours()).isTrue();
        assertThat(bilan.secondesParJour()).containsOnlyKeys(LocalDate.of(2026, 10, 12));
    }

    @Test
    void should_freeze_worked_time_when_break_is_running() {
        Service service = travail(at(12, 21, 0), null);
        Service pause = pause(service, at(13, 1, 0), null);

        long a1h30 = calculator.dureeTravail(service, List.of(pause), at(13, 1, 30));
        long a1h45 = calculator.dureeTravail(service, List.of(pause), at(13, 1, 45));

        assertThat(a1h30).isEqualTo(4 * HEURE).isEqualTo(a1h45);
        assertThat(calculator.enPause(service, List.of(pause))).isTrue();
        assertThat(calculator.travailEnCours(service, List.of(pause))).isFalse();
    }

    @Test
    void should_compute_closed_night_service_net_duration() {
        Service service = travail(at(12, 21, 0), at(13, 5, 0));
        Service pause = pause(service, at(13, 1, 0), at(13, 2, 0));

        assertThat(calculator.dureeTravail(service, List.of(pause), at(17, 12, 0))).isEqualTo(7 * HEURE);
        assertThat(calculator.travailEnCours(service, List.of(pause))).isFalse();
    }

    // ── Pauses incohérentes ──

    @Test
    void should_not_deduct_overlapping_breaks_twice() {
        Service service = travail(at(12, 8, 0), at(12, 17, 0));
        List<Service> pauses = List.of(
                pause(service, at(12, 12, 0), at(12, 13, 0)),
                pause(service, at(12, 12, 30), at(12, 13, 30)),
                pause(service, at(12, 15, 0), at(12, 15, 15)));

        assertThat(calculator.dureePauses(service, pauses, at(12, 18, 0))).isEqualTo(90 * 60 + 15 * 60);
    }

    @Test
    void should_clip_break_to_service_bounds_when_break_overflows() {
        Service service = travail(at(12, 8, 0), at(12, 12, 0));
        Service pause = pause(service, at(12, 11, 30), at(12, 13, 0));

        assertThat(calculator.dureePauses(service, List.of(pause), at(12, 18, 0))).isEqualTo(30 * 60);
        assertThat(calculator.dureeTravail(service, List.of(pause), at(12, 18, 0))).isEqualTo(3 * HEURE + 30 * 60);
    }

    @Test
    void should_ignore_break_entirely_outside_service() {
        Service service = travail(at(12, 8, 0), at(12, 12, 0));
        Service pause = pause(service, at(12, 14, 0), at(12, 15, 0));

        assertThat(calculator.dureeTravail(service, List.of(pause), at(12, 18, 0))).isEqualTo(4 * HEURE);
    }

    // ── Cas limites ──

    @Test
    void should_count_real_elapsed_time_when_night_service_spans_dst_change() {
        // Nuit du 24 au 25/10/2026 : à 3 h on recule à 2 h, 22 h → 6 h fait 9 h réelles
        Service service = travail(ZonedDateTime.of(2026, 10, 24, 22, 0, 0, 0, PARIS),
                ZonedDateTime.of(2026, 10, 25, 6, 0, 0, 0, PARIS));

        assertThat(calculator.dureeTravail(service, List.of(), ZonedDateTime.of(2026, 10, 26, 0, 0, 0, 0, PARIS)))
                .isEqualTo(9 * HEURE);
    }

    @Test
    void should_return_zero_when_service_starts_in_the_future() {
        Service service = travail(at(14, 8, 0), null);

        assertThat(calculator.dureeTravail(service, List.of(), at(13, 8, 0))).isZero();
    }

    @Test
    void should_return_zero_when_service_or_breaks_are_missing() {
        assertThat(calculator.dureeBrute(null, at(13, 8, 0))).isZero();
        assertThat(calculator.dureePauses(travail(at(12, 8, 0), at(12, 9, 0)), null, at(13, 8, 0))).isZero();
        assertThat(calculator.bilan(List.of(), Map.of(), at(13, 8, 0))).isEqualTo(Bilan.VIDE);
        assertThat(calculator.bilan(null, Map.of(), at(13, 8, 0))).isEqualTo(Bilan.VIDE);
    }

    @Test
    void should_sum_services_of_several_days_and_round_hours() {
        Service lundi = travail(at(12, 8, 0), at(12, 12, 20));
        Service mardi = travail(at(13, 8, 0), at(13, 10, 0));

        Bilan bilan = calculator.bilan(List.of(lundi, mardi), Map.of(), at(17, 8, 0));

        assertThat(bilan.secondes()).isEqualTo(6 * HEURE + 20 * 60);
        assertThat(bilan.heures()).isEqualTo(6.33);
        assertThat(bilan.enCours()).isFalse();
        assertThat(bilan.secondesParJour()).containsEntry(LocalDate.of(2026, 10, 12), 4 * HEURE + 20 * 60)
                .containsEntry(LocalDate.of(2026, 10, 13), 2 * HEURE);
    }

    private static ZonedDateTime at(int jourOctobre, int heure, int minute) {
        return ZonedDateTime.of(2026, 10, jourOctobre, heure, minute, 0, 0, PARIS);
    }

    static Service travail(ZonedDateTime debut, ZonedDateTime fin) {
        Service service = new Service();
        service.setUuid(UUID.randomUUID());
        service.setDebut(debut);
        service.setFin(fin);
        service.setIsBreak(false);
        return service;
    }

    static Service pause(Service parent, ZonedDateTime debut, ZonedDateTime fin) {
        Service pause = travail(debut, fin);
        pause.setIsBreak(true);
        pause.setServiceParent(parent);
        return pause;
    }
}
