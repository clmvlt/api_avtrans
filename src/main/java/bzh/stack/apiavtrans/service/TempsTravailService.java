package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.service.HeuresPeriodeDTO;
import bzh.stack.apiavtrans.dto.service.JourneeTravailResponse;
import bzh.stack.apiavtrans.dto.service.ServiceDTO;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceMapper;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.service.PeriodeHeures.Fenetre;
import bzh.stack.apiavtrans.service.TempsTravailCalculator.Bilan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Lecture du temps de travail : services de travail d'une période avec leurs pauses, bilans d'heures
 * et DTO de pointage. Point d'entrée unique de toutes les routes qui affichent des heures.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class TempsTravailService {

    public static final String STATUT_PRESENT = "PRESENT";
    public static final String STATUT_EN_PAUSE = "ON_BREAK";
    public static final String STATUT_ABSENT = "ABSENT";

    private final ServiceRepository serviceRepository;
    private final TempsTravailCalculator calculator;
    private final ServiceMapper serviceMapper;

    /** Services de travail débutés dans la fenêtre, par début croissant. */
    @Transactional(readOnly = true)
    public List<Service> servicesTravail(User user, Fenetre fenetre) {
        return serviceRepository.findServicesTravail(user, fenetre.debut(), fenetre.fin());
    }

    /** Pauses de chaque service de travail (une seule requête), indexées par UUID du service. */
    @Transactional(readOnly = true)
    public Map<UUID, List<Service>> pausesParService(Collection<Service> travaux) {
        List<Service> services = travaux.stream().filter(s -> !Boolean.TRUE.equals(s.getIsBreak())).toList();
        if (services.isEmpty()) {
            return Map.of();
        }
        Map<UUID, List<Service>> pauses = new HashMap<>();
        for (Service pause : serviceRepository.findPausesDe(services)) {
            pauses.computeIfAbsent(pause.getServiceParent().getUuid(), k -> new ArrayList<>()).add(pause);
        }
        return pauses;
    }

    @Transactional(readOnly = true)
    public Optional<Service> serviceActif(User user) {
        return serviceRepository.findFirstByUserAndIsBreakFalseAndFinIsNullOrderByDebutDesc(user);
    }

    /** Service de travail avec ses pauses et ses durées à {@code maintenant}. */
    @Transactional(readOnly = true)
    public ServiceDTO etat(Service travail, ZonedDateTime maintenant) {
        return serviceMapper.toDTO(travail, serviceRepository.findByServiceParentOrderByDebutAsc(travail), maintenant);
    }

    /** Service de travail en cours du salarié (pauses comprises), ou null. */
    @Transactional(readOnly = true)
    public ServiceDTO etatActuel(User user, ZonedDateTime maintenant) {
        return serviceActif(user).map(travail -> etat(travail, maintenant)).orElse(null);
    }

    /**
     * Journée de travail en cours : jour de début du dernier service qui touche aujourd'hui (débuté aujourd'hui,
     * en cours, ou terminé aujourd'hui après minuit), sinon aujourd'hui. Un salarié de nuit qui a pointé
     * de 21 h à 6 h retrouve ainsi sa journée de la veille jusqu'à son prochain service.
     */
    @Transactional(readOnly = true)
    public LocalDate journeeEnCours(User user, ZonedDateTime maintenant) {
        Fenetre aujourdhui = PeriodeHeures.DAY.fenetre(maintenant);
        return serviceRepository.findServicesTravailChevauchant(user, aujourdhui.debut(), aujourdhui.fin(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(service -> service.getDebut().withZoneSameInstant(PeriodeHeures.PARIS).toLocalDate())
                .orElse(aujourdhui.debut().toLocalDate());
    }

    /** Services de travail rattachés au jour (avec leurs pauses) et temps net de la journée, en une seule lecture. */
    @Transactional(readOnly = true)
    public JourneeTravailResponse journee(User user, LocalDate jour, ZonedDateTime maintenant) {
        List<Service> travaux = servicesTravail(user, Fenetre.jours(jour, jour));
        Map<UUID, List<Service>> pauses = pausesParService(travaux);
        List<ServiceDTO> services = travaux.stream()
                .map(travail -> serviceMapper.toDTO(travail, pauses.getOrDefault(travail.getUuid(), List.of()), maintenant))
                .toList();
        boolean aujourdhui = jour.equals(maintenant.withZoneSameInstant(PeriodeHeures.PARIS).toLocalDate());
        return new JourneeTravailResponse(true, jour, aujourdhui,
                versHeures(calculator.bilan(travaux, pauses, maintenant)), services, maintenant);
    }

    /** Statut de présence déduit du service en cours. */
    public static String statut(ServiceDTO serviceActif) {
        if (serviceActif == null) {
            return STATUT_ABSENT;
        }
        return Boolean.TRUE.equals(serviceActif.getEnPause()) ? STATUT_EN_PAUSE : STATUT_PRESENT;
    }

    /** DTO d'une liste de pointages : les services de travail embarquent leurs pauses. */
    @Transactional(readOnly = true)
    public List<ServiceDTO> versDTOs(List<Service> lignes, ZonedDateTime maintenant) {
        Map<UUID, List<Service>> pauses = pausesParService(lignes);
        return lignes.stream()
                .map(ligne -> serviceMapper.toDTO(ligne, pauses.getOrDefault(ligne.getUuid(), List.of()), maintenant))
                .toList();
    }

    @Transactional(readOnly = true)
    public Bilan bilan(User user, Fenetre fenetre, ZonedDateTime maintenant) {
        List<Service> travaux = servicesTravail(user, fenetre);
        return calculator.bilan(travaux, pausesParService(travaux), maintenant);
    }

    /** Bilans de plusieurs périodes en chargeant une seule fois les services de la plage qui les couvre. */
    @Transactional(readOnly = true)
    public Map<PeriodeHeures, Bilan> bilans(User user, Map<PeriodeHeures, Fenetre> fenetres, ZonedDateTime maintenant) {
        Map<PeriodeHeures, Bilan> bilans = new EnumMap<>(PeriodeHeures.class);
        if (fenetres.isEmpty()) {
            return bilans;
        }
        ZonedDateTime debut = fenetres.values().stream().map(Fenetre::debut).min(ZonedDateTime::compareTo).orElseThrow();
        ZonedDateTime fin = fenetres.values().stream().map(Fenetre::fin).max(ZonedDateTime::compareTo).orElseThrow();
        List<Service> travaux = servicesTravail(user, new Fenetre(debut, fin));
        Map<UUID, List<Service>> pauses = pausesParService(travaux);

        fenetres.forEach((periode, fenetre) -> {
            List<Service> travauxPeriode = travaux.stream().filter(s -> fenetre.contient(s.getDebut())).toList();
            bilans.put(periode, calculator.bilan(travauxPeriode, pauses, maintenant));
        });
        return bilans;
    }

    public static HeuresPeriodeDTO versHeures(Bilan bilan) {
        return new HeuresPeriodeDTO(bilan.secondes(), bilan.heures(), bilan.enCours());
    }
}
