package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.service.JourneeTravailResponse;
import bzh.stack.apiavtrans.dto.service.ServiceDTO;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceMapper;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.service.TempsTravailCalculator.Bilan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static bzh.stack.apiavtrans.service.TempsTravailCalculatorTest.pause;
import static bzh.stack.apiavtrans.service.TempsTravailCalculatorTest.travail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TempsTravailServiceTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final ZonedDateTime MAINTENANT = ZonedDateTime.of(2026, 10, 13, 3, 0, 0, 0, PARIS);

    @Mock
    private ServiceRepository serviceRepository;

    private TempsTravailService tempsTravailService;
    private User chauffeur;
    private Service serviceDeNuit;
    private Service pauseApresMinuit;

    @BeforeEach
    void setUp() {
        TempsTravailCalculator calculator = new TempsTravailCalculator();
        tempsTravailService = new TempsTravailService(serviceRepository, calculator, new ServiceMapper(calculator));

        chauffeur = new User();
        chauffeur.setUuid(UUID.randomUUID());

        serviceDeNuit = travail(ZonedDateTime.of(2026, 10, 12, 21, 0, 0, 0, PARIS), null);
        serviceDeNuit.setUser(chauffeur);
        pauseApresMinuit = pause(serviceDeNuit, ZonedDateTime.of(2026, 10, 13, 1, 0, 0, 0, PARIS),
                ZonedDateTime.of(2026, 10, 13, 2, 0, 0, 0, PARIS));
        pauseApresMinuit.setUser(chauffeur);
    }

    @Test
    void should_not_return_negative_hours_today_when_break_after_midnight_belongs_to_yesterday_service() {
        when(serviceRepository.findServicesTravail(eq(chauffeur), any(), any())).thenReturn(List.of(serviceDeNuit));
        when(serviceRepository.findPausesDe(List.of(serviceDeNuit))).thenReturn(List.of(pauseApresMinuit));

        Map<PeriodeHeures, PeriodeHeures.Fenetre> fenetres = new EnumMap<>(PeriodeHeures.class);
        fenetres.put(PeriodeHeures.DAY, PeriodeHeures.DAY.fenetre(MAINTENANT));
        fenetres.put(PeriodeHeures.WEEK, PeriodeHeures.WEEK.fenetre(MAINTENANT));
        fenetres.put(PeriodeHeures.LASTMONTH, PeriodeHeures.LASTMONTH.fenetre(MAINTENANT));
        Map<PeriodeHeures, Bilan> bilans = tempsTravailService.bilans(chauffeur, fenetres, MAINTENANT);

        // Le 13/10 : aucun service débuté ce jour-là (plus de -1 h)
        assertThat(bilans.get(PeriodeHeures.DAY).secondes()).isZero();
        assertThat(bilans.get(PeriodeHeures.DAY).enCours()).isFalse();
        // Semaine : 21 h → 3 h moins la pause = 5 h, compteur en cours
        assertThat(bilans.get(PeriodeHeures.WEEK).secondes()).isEqualTo(5 * 3600);
        assertThat(bilans.get(PeriodeHeures.WEEK).enCours()).isTrue();
        assertThat(bilans.get(PeriodeHeures.LASTMONTH)).isEqualTo(Bilan.VIDE);
        // Une seule requête pour toutes les périodes
        verify(serviceRepository, times(1)).findServicesTravail(eq(chauffeur), any(), any());
    }

    @Test
    void should_count_whole_night_service_on_start_day_when_asking_that_day() {
        when(serviceRepository.findServicesTravail(eq(chauffeur), any(), any())).thenReturn(List.of(serviceDeNuit));
        when(serviceRepository.findPausesDe(List.of(serviceDeNuit))).thenReturn(List.of(pauseApresMinuit));

        Bilan bilan = tempsTravailService.bilan(chauffeur,
                PeriodeHeures.DAY.fenetre(MAINTENANT, 2026, 10, null, 12), MAINTENANT);

        assertThat(bilan.secondes()).isEqualTo(5 * 3600);
        assertThat(bilan.enCours()).isTrue();
        assertThat(TempsTravailService.versHeures(bilan).getHeures()).isEqualTo(5.0);
    }

    // ── Journée de travail (salarié de nuit après minuit) ──

    @Test
    void should_show_yesterday_night_service_when_it_ended_after_midnight() {
        // Pointe à 21 h le 12/10, pause de 2 h à 3 h, dépointe à 6 h le 13/10 ; il est 6 h 30
        ZonedDateTime maintenant = ZonedDateTime.of(2026, 10, 13, 6, 30, 0, 0, PARIS);
        Service nuit = travail(ZonedDateTime.of(2026, 10, 12, 21, 0, 0, 0, PARIS),
                ZonedDateTime.of(2026, 10, 13, 6, 0, 0, 0, PARIS));
        Service pause = pause(nuit, ZonedDateTime.of(2026, 10, 13, 2, 0, 0, 0, PARIS),
                ZonedDateTime.of(2026, 10, 13, 3, 0, 0, 0, PARIS));
        PeriodeHeures.Fenetre aujourdhui = PeriodeHeures.DAY.fenetre(maintenant);
        when(serviceRepository.findServicesTravailChevauchant(eq(chauffeur), eq(aujourdhui.debut()),
                eq(aujourdhui.fin()), any())).thenReturn(List.of(nuit));
        when(serviceRepository.findServicesTravail(eq(chauffeur), any(), any())).thenReturn(List.of(nuit));
        when(serviceRepository.findPausesDe(List.of(nuit))).thenReturn(List.of(pause));

        LocalDate journee = tempsTravailService.journeeEnCours(chauffeur, maintenant);
        JourneeTravailResponse response = tempsTravailService.journee(chauffeur, journee, maintenant);

        assertThat(journee).isEqualTo(LocalDate.of(2026, 10, 12));
        assertThat(response.getDate()).isEqualTo(LocalDate.of(2026, 10, 12));
        assertThat(response.getAujourdhui()).isFalse();
        assertThat(response.getServices()).hasSize(1);
        assertThat(response.getServices().get(0).getPauses()).hasSize(1);
        assertThat(response.getHeures().getSecondes()).isEqualTo(8 * 3600);
        assertThat(response.getHeures().getEnCours()).isFalse();
        verify(serviceRepository).findServicesTravail(chauffeur,
                ZonedDateTime.of(2026, 10, 12, 0, 0, 0, 0, PARIS), ZonedDateTime.of(2026, 10, 13, 0, 0, 0, 0, PARIS));
    }

    @Test
    void should_show_yesterday_night_service_while_it_is_still_running() {
        when(serviceRepository.findServicesTravailChevauchant(eq(chauffeur), any(), any(), any()))
                .thenReturn(List.of(serviceDeNuit));

        assertThat(tempsTravailService.journeeEnCours(chauffeur, MAINTENANT)).isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void should_return_today_as_empty_workday_when_no_service_touches_today() {
        when(serviceRepository.findServicesTravailChevauchant(eq(chauffeur), any(), any(), any())).thenReturn(List.of());
        when(serviceRepository.findServicesTravail(eq(chauffeur), any(), any())).thenReturn(List.of());

        LocalDate journee = tempsTravailService.journeeEnCours(chauffeur, MAINTENANT);
        JourneeTravailResponse response = tempsTravailService.journee(chauffeur, journee, MAINTENANT);

        assertThat(journee).isEqualTo(LocalDate.of(2026, 10, 13));
        assertThat(response.getAujourdhui()).isTrue();
        assertThat(response.getServices()).isEmpty();
        assertThat(response.getHeures().getSecondes()).isZero();
        verify(serviceRepository, never()).findPausesDe(any());
    }

    @Test
    void should_return_work_service_with_its_breaks_when_user_is_on_break() {
        Service pauseEnCours = pause(serviceDeNuit, ZonedDateTime.of(2026, 10, 13, 2, 30, 0, 0, PARIS), null);
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(chauffeur))
                .thenReturn(Optional.of(serviceDeNuit));
        when(serviceRepository.findByServiceParentOrderByDebutAsc(serviceDeNuit))
                .thenReturn(List.of(pauseApresMinuit, pauseEnCours));

        ServiceDTO actif = tempsTravailService.etatActuel(chauffeur, MAINTENANT);

        assertThat(actif.getUuid()).isEqualTo(serviceDeNuit.getUuid());
        assertThat(actif.getIsBreak()).isFalse();
        assertThat(actif.getEnPause()).isTrue();
        assertThat(actif.getEnCours()).isTrue();
        assertThat(actif.getDuree()).isEqualTo(6 * 3600);
        assertThat(actif.getDureePauses()).isEqualTo(3600 + 30 * 60);
        assertThat(actif.getDureeTravail()).isEqualTo(4 * 3600 + 30 * 60);
        assertThat(actif.getCalculeLe()).isEqualTo(MAINTENANT);
        assertThat(actif.getPauses()).extracting(ServiceDTO::getServiceParentUuid)
                .containsOnly(serviceDeNuit.getUuid());
        assertThat(TempsTravailService.statut(actif)).isEqualTo(TempsTravailService.STATUT_EN_PAUSE);
    }

    @Test
    void should_return_absent_status_when_no_service_is_running() {
        when(serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(chauffeur))
                .thenReturn(Optional.empty());

        assertThat(tempsTravailService.etatActuel(chauffeur, MAINTENANT)).isNull();
        assertThat(TempsTravailService.statut(null)).isEqualTo(TempsTravailService.STATUT_ABSENT);
    }

    @Test
    void should_not_query_breaks_when_there_is_no_work_service() {
        assertThat(tempsTravailService.pausesParService(List.of())).isEmpty();
        assertThat(tempsTravailService.pausesParService(List.of(pauseApresMinuit))).isEmpty();
        verify(serviceRepository, never()).findPausesDe(any());
    }

    @Test
    void should_embed_breaks_in_work_services_and_keep_parent_on_breaks_when_mapping_a_list() {
        when(serviceRepository.findPausesDe(List.of(serviceDeNuit))).thenReturn(List.of(pauseApresMinuit));

        List<ServiceDTO> dtos = tempsTravailService.versDTOs(List.of(serviceDeNuit, pauseApresMinuit), MAINTENANT);

        assertThat(dtos.get(0).getPauses()).hasSize(1);
        assertThat(dtos.get(0).getDureeTravail()).isEqualTo(5 * 3600);
        assertThat(dtos.get(1).getPauses()).isNull();
        assertThat(dtos.get(1).getServiceParentUuid()).isEqualTo(serviceDeNuit.getUuid());
        assertThat(dtos.get(1).getDuree()).isEqualTo(3600);
        assertThat(dtos.get(1).getEnCours()).isFalse();
    }
}
