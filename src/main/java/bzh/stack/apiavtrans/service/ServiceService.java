package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.service.JourneeTravailResponse;
import bzh.stack.apiavtrans.dto.service.ServiceDTO;
import bzh.stack.apiavtrans.dto.service.WorkedHoursDto;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceMapper;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import bzh.stack.apiavtrans.service.PeriodeHeures.Fenetre;
import bzh.stack.apiavtrans.service.TempsTravailCalculator.Bilan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Pointage : un service de travail par salarié en cours au plus, et ses pauses rattachées
 * (au plus une pause en cours, dans un service en cours).
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private static final DateTimeFormatter FORMAT_DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceModificationService serviceModificationService;
    private final TempsTravailService tempsTravailService;
    private final ServiceMapper serviceMapper;

    // ── Pointage du salarié ──

    @Transactional
    public ServiceDTO startService(String userEmail, Double latitude, Double longitude, Boolean isAdmin) {
        User user = userByEmail(userEmail);

        if (tempsTravailService.serviceActif(user).isPresent()) {
            throw new RuntimeException("User already has an active service");
        }

        ZonedDateTime maintenant = maintenant();
        Service service = new Service();
        service.setDebut(maintenant);
        service.setUser(user);
        service.setLatitude(latitude);
        service.setLongitude(longitude);
        service.setIsBreak(false);
        service.setIsAdmin(isAdmin != null ? isAdmin : false);

        return tempsTravailService.etat(serviceRepository.save(service), maintenant);
    }

    @Transactional
    public ServiceDTO endService(String userEmail, Double latitude, Double longitude) {
        User user = userByEmail(userEmail);
        Service service = tempsTravailService.serviceActif(user)
                .orElseThrow(() -> new RuntimeException("No active service found for user"));

        if (serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(service).isPresent()) {
            throw new RuntimeException("Cannot end service: user is on break. End break first.");
        }

        ZonedDateTime maintenant = maintenant();
        cloturer(service, maintenant, latitude, longitude);

        return tempsTravailService.etat(serviceRepository.save(service), maintenant);
    }

    @Transactional
    public ServiceDTO startBreak(String userEmail, Double latitude, Double longitude) {
        User user = userByEmail(userEmail);
        Service service = tempsTravailService.serviceActif(user)
                .orElseThrow(() -> new RuntimeException("Cannot start break: No active service found or already on break"));

        if (serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(service).isPresent()) {
            throw new RuntimeException("Cannot start break: No active service found or already on break");
        }

        ZonedDateTime maintenant = maintenant();
        Service pause = new Service();
        pause.setDebut(maintenant);
        pause.setUser(user);
        pause.setLatitude(latitude);
        pause.setLongitude(longitude);
        pause.setIsBreak(true);
        pause.setIsAdmin(false);
        pause.setServiceParent(service);
        serviceRepository.save(pause);

        return tempsTravailService.etat(service, maintenant);
    }

    @Transactional
    public ServiceDTO endBreak(String userEmail, Double latitude, Double longitude) {
        User user = userByEmail(userEmail);
        Service service = tempsTravailService.serviceActif(user)
                .orElseThrow(() -> new RuntimeException("No active service found"));
        Service pause = serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(service)
                .orElseThrow(() -> new RuntimeException("Active service is not a break"));

        ZonedDateTime maintenant = maintenant();
        cloturer(pause, maintenant, latitude, longitude);
        serviceRepository.save(pause);

        return tempsTravailService.etat(service, maintenant);
    }

    // ── Lecture ──

    @Transactional(readOnly = true)
    public ServiceDTO getActiveService(String userEmail) {
        return tempsTravailService.etatActuel(userByEmail(userEmail), maintenant());
    }

    @Transactional(readOnly = true)
    public ServiceDTO getActiveServiceByUuid(UUID userUuid) {
        return tempsTravailService.etatActuel(userByUuid(userUuid), maintenant());
    }

    /**
     * Journée de travail en cours avec ses services et leurs pauses : celle d'aujourd'hui, ou celle de la veille
     * pour un service de nuit en cours ou terminé après minuit (tant qu'aucun service n'a repris aujourd'hui).
     */
    @Transactional(readOnly = true)
    public JourneeTravailResponse getDailyServices(String userEmail) {
        User user = userByEmail(userEmail);
        ZonedDateTime maintenant = maintenant();
        return tempsTravailService.journee(user, tempsTravailService.journeeEnCours(user, maintenant), maintenant);
    }

    /** Services de travail débutés dans le mois, avec leurs pauses. */
    @Transactional(readOnly = true)
    public List<ServiceDTO> getServicesForMonth(String userEmail, Integer year, Integer month) {
        ZonedDateTime maintenant = maintenant();
        Fenetre mois = PeriodeHeures.MONTH.fenetre(maintenant, year, month, null, null);
        return servicesDeLaFenetre(userByEmail(userEmail), mois, maintenant);
    }

    @Transactional(readOnly = true)
    public WorkedHoursDto getWorkedHours(String userEmail, String period, Integer year, Integer month, Integer week, Integer day) {
        return heuresTravaillees(userByEmail(userEmail), period, year, month, week, day);
    }

    @Transactional(readOnly = true)
    public WorkedHoursDto getWorkedHoursByUuid(UUID userUuid, String period, Integer year, Integer month, Integer week, Integer day) {
        return heuresTravaillees(userByUuid(userUuid), period, year, month, week, day);
    }

    @Transactional(readOnly = true)
    public Page<ServiceDTO> getUserServicesHistory(String userEmail, int page, int size,
                                                   Boolean isBreak, ZonedDateTime startDate, ZonedDateTime endDate,
                                                   String sortBy, String sortDirection) {
        return historique(userByEmail(userEmail), page, size, isBreak, startDate, endDate, sortBy, sortDirection);
    }

    @Transactional(readOnly = true)
    public Page<ServiceDTO> getUserServicesHistoryByUuid(UUID userUuid, int page, int size,
                                                         Boolean isBreak, ZonedDateTime startDate, ZonedDateTime endDate,
                                                         String sortBy, String sortDirection) {
        ZonedDateTime maintenant = maintenant();
        ZonedDateTime effectiveStartDate = startDate != null ? startDate : maintenant.minusDays(30);
        ZonedDateTime effectiveEndDate = endDate != null ? endDate : maintenant.plusYears(10);
        return historique(userByUuid(userUuid), page, size, isBreak, effectiveStartDate, effectiveEndDate, sortBy, sortDirection);
    }

    // ── Administration ──

    @Transactional
    public ServiceDTO createServiceForUser(UUID userUuid, ZonedDateTime debut, ZonedDateTime fin,
                                           Double latitude, Double longitude,
                                           Double latitudeEnd, Double longitudeEnd,
                                           Boolean isBreak, UUID serviceParentUuid, User admin) {
        User user = userByUuid(userUuid);
        ZonedDateTime maintenant = maintenant();

        Service service = new Service();
        service.setDebut(debut != null ? debut : maintenant);
        service.setFin(fin);
        service.setUser(user);
        service.setLatitude(latitude);
        service.setLongitude(longitude);
        service.setLatitudeEnd(latitudeEnd);
        service.setLongitudeEnd(longitudeEnd);
        service.setIsBreak(isBreak != null ? isBreak : false);
        service.setIsAdmin(true);

        verifierBornes(service);
        if (service.getIsBreak()) {
            service.setServiceParent(resoudreServiceParent(service, serviceParentUuid));
            verifierPause(service);
        } else if (service.getFin() == null) {
            verifierAucunAutreServiceEnCours(service);
        }
        service.setDuree(dureeStockee(service));

        Service saved = serviceRepository.save(service);
        serviceModificationService.logCreation(saved, admin);
        return versDTO(saved, maintenant);
    }

    @Transactional
    public ServiceDTO updateService(UUID serviceUuid, ZonedDateTime debut, ZonedDateTime fin,
                                    Double latitude, Double longitude,
                                    Double latitudeEnd, Double longitudeEnd,
                                    Boolean isBreak, UUID serviceParentUuid, User admin) {
        Service service = serviceRepository.findById(serviceUuid)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        ZonedDateTime oldDebut = service.getDebut();
        ZonedDateTime oldFin = service.getFin();
        Boolean oldIsBreak = service.getIsBreak();
        boolean devientPause = Boolean.TRUE.equals(isBreak) && !Boolean.TRUE.equals(oldIsBreak);

        if (devientPause && !serviceRepository.findByServiceParentOrderByDebutAsc(service).isEmpty()) {
            throw new RuntimeException("Ce service contient des pauses : supprimez-les avant de le transformer en pause");
        }

        if (debut != null) {
            service.setDebut(debut);
        }
        service.setFin(fin);
        service.setLatitude(latitude);
        service.setLongitude(longitude);
        service.setLatitudeEnd(latitudeEnd);
        service.setLongitudeEnd(longitudeEnd);
        if (isBreak != null) {
            service.setIsBreak(isBreak);
        }

        verifierBornes(service);
        if (service.getIsBreak()) {
            Service parentActuel = devientPause ? null : service.getServiceParent();
            // Re-rattachement si demandé, ou si la pause ne commence plus pendant son service
            if (serviceParentUuid != null || parentActuel == null || !contient(parentActuel, service.getDebut())) {
                service.setServiceParent(resoudreServiceParent(service, serviceParentUuid));
            }
            verifierPause(service);
        } else {
            service.setServiceParent(null);
            if (service.getFin() == null) {
                verifierAucunAutreServiceEnCours(service);
            } else if (serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(service).isPresent()) {
                throw new RuntimeException("Ce service a une pause en cours : terminez la pause avant de clôturer le service");
            }
        }
        service.setDuree(dureeStockee(service));

        boolean changed = !isSameSecond(oldDebut, service.getDebut())
                || !isSameSecond(oldFin, service.getFin())
                || !Objects.equals(oldIsBreak, service.getIsBreak());
        if (changed) {
            service.setModifiedAt(maintenant());
            service.setModifiedBy(admin);
        }

        Service saved = serviceRepository.save(service);
        if (changed) {
            serviceModificationService.logUpdate(saved, oldDebut, oldFin, oldIsBreak, admin);
        }
        return versDTO(saved, maintenant());
    }

    /** Supprime un pointage ; un service de travail est supprimé avec ses pauses (chacune tracée). */
    @Transactional
    public void deleteService(UUID serviceUuid, User admin) {
        Service service = serviceRepository.findById(serviceUuid)
                .orElseThrow(() -> new RuntimeException("Service not found with uuid: " + serviceUuid));

        if (!Boolean.TRUE.equals(service.getIsBreak())) {
            for (Service pause : serviceRepository.findByServiceParentOrderByDebutAsc(service)) {
                serviceModificationService.logDeletion(pause, admin);
                serviceRepository.delete(pause);
            }
        }
        serviceModificationService.logDeletion(service, admin);
        serviceRepository.delete(service);
    }

    // ── Interne ──

    private WorkedHoursDto heuresTravaillees(User user, String period, Integer year, Integer month, Integer week, Integer day) {
        ZonedDateTime maintenant = maintenant();
        Map<PeriodeHeures, Fenetre> fenetres = new EnumMap<>(PeriodeHeures.class);
        if (period != null && !period.isBlank()) {
            PeriodeHeures periode = PeriodeHeures.parse(period);
            fenetres.put(periode, periode == PeriodeHeures.DAY
                    ? fenetreJour(user, maintenant, year, month, day)
                    : periode.fenetre(maintenant, year, month, week, day));
        } else {
            fenetres.put(PeriodeHeures.YEAR, PeriodeHeures.YEAR.fenetre(maintenant, year, null, null, null));
            fenetres.put(PeriodeHeures.MONTH, PeriodeHeures.MONTH.fenetre(maintenant, year, month, null, null));
            fenetres.put(PeriodeHeures.WEEK, PeriodeHeures.WEEK.fenetre(maintenant, year, null, week, null));
            fenetres.put(PeriodeHeures.DAY, fenetreJour(user, maintenant, year, month, day));
            fenetres.put(PeriodeHeures.LASTMONTH, PeriodeHeures.LASTMONTH.fenetre(maintenant));
        }

        Map<PeriodeHeures, Bilan> bilans = tempsTravailService.bilans(user, fenetres, maintenant);
        WorkedHoursDto dto = new WorkedHoursDto();
        dto.setCalculeLe(maintenant);
        if (fenetres.containsKey(PeriodeHeures.DAY)) {
            dto.setJour(fenetres.get(PeriodeHeures.DAY).debut().toLocalDate());
        }
        bilans.forEach((periode, bilan) -> {
            var heures = TempsTravailService.versHeures(bilan);
            switch (periode) {
                case YEAR -> dto.setYear(heures);
                case MONTH -> dto.setMonth(heures);
                case WEEK -> dto.setWeek(heures);
                case DAY -> dto.setDay(heures);
                case LASTMONTH -> dto.setLastMonth(heures);
            }
        });
        return dto;
    }

    /** Jour demandé ; sans paramètre, la journée de travail en cours (celle de la veille pour un service de nuit). */
    private Fenetre fenetreJour(User user, ZonedDateTime maintenant, Integer year, Integer month, Integer day) {
        if (year == null && month == null && day == null) {
            LocalDate journee = tempsTravailService.journeeEnCours(user, maintenant);
            return Fenetre.jours(journee, journee);
        }
        return PeriodeHeures.DAY.fenetre(maintenant, year, month, null, day);
    }

    private List<ServiceDTO> servicesDeLaFenetre(User user, Fenetre fenetre, ZonedDateTime maintenant) {
        return tempsTravailService.versDTOs(tempsTravailService.servicesTravail(user, fenetre), maintenant);
    }

    private Page<ServiceDTO> historique(User user, int page, int size, Boolean isBreak,
                                        ZonedDateTime startDate, ZonedDateTime endDate,
                                        String sortBy, String sortDirection) {
        String sortField = sortBy != null ? sortBy : "debut";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<Service> lignes = serviceRepository.findByUserWithFilters(user, isBreak, startDate, endDate, pageable);
        List<ServiceDTO> dtos = tempsTravailService.versDTOs(lignes.getContent(), maintenant());
        return new PageImpl<>(dtos, lignes.getPageable(), lignes.getTotalElements());
    }

    private ServiceDTO versDTO(Service ligne, ZonedDateTime maintenant) {
        return Boolean.TRUE.equals(ligne.getIsBreak())
                ? serviceMapper.toDTO(ligne, null, maintenant)
                : tempsTravailService.etat(ligne, maintenant);
    }

    private void cloturer(Service ligne, ZonedDateTime fin, Double latitude, Double longitude) {
        ligne.setFin(fin);
        ligne.setDuree(dureeStockee(ligne));
        if (latitude != null) {
            ligne.setLatitudeEnd(latitude);
        }
        if (longitude != null) {
            ligne.setLongitudeEnd(longitude);
        }
    }

    /** Durée brute stockée en base : null tant que la ligne est en cours. */
    private static Long dureeStockee(Service ligne) {
        if (ligne.getDebut() == null || ligne.getFin() == null) {
            return null;
        }
        return Duration.between(ligne.getDebut(), ligne.getFin()).getSeconds();
    }

    private static void verifierBornes(Service ligne) {
        if (ligne.getFin() != null && ligne.getFin().isBefore(ligne.getDebut())) {
            throw new RuntimeException("La fin doit être postérieure au début");
        }
    }

    /**
     * Service de travail d'une pause : celui demandé (même salarié, la pause commence pendant le service),
     * sinon le service du salarié en cours au début de la pause.
     */
    private Service resoudreServiceParent(Service pause, UUID serviceParentUuid) {
        if (serviceParentUuid != null) {
            Service parent = serviceRepository.findById(serviceParentUuid)
                    .orElseThrow(() -> new RuntimeException("Service parent introuvable : " + serviceParentUuid));
            if (Boolean.TRUE.equals(parent.getIsBreak())) {
                throw new RuntimeException("Une pause ne peut être rattachée qu'à un service de travail");
            }
            if (!parent.getUser().getUuid().equals(pause.getUser().getUuid())) {
                throw new RuntimeException("Le service parent appartient à un autre utilisateur");
            }
            if (!contient(parent, pause.getDebut())) {
                throw new RuntimeException("La pause doit commencer pendant son service ("
                        + formater(parent.getDebut()) + " → "
                        + (parent.getFin() != null ? formater(parent.getFin()) : "en cours") + ")");
            }
            return parent;
        }
        return serviceRepository.findServicesTravailContenant(pause.getUser(), pause.getDebut()).stream()
                .filter(s -> !s.getUuid().equals(pause.getUuid()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun service de travail ne couvre le début de la pause ("
                        + formater(pause.getDebut()) + ") : créez d'abord le service ou précisez serviceParentUuid"));
    }

    /** Une pause sans fin appartient à un service en cours, qui n'a pas d'autre pause en cours. */
    private void verifierPause(Service pause) {
        if (pause.getFin() != null) {
            return;
        }
        Service parent = pause.getServiceParent();
        if (parent.getFin() != null) {
            throw new RuntimeException("Une pause sans fin ne peut appartenir qu'à un service en cours");
        }
        serviceRepository.findFirstByServiceParentAndFinIsNullOrderByDebutDesc(parent)
                .filter(autre -> !autre.getUuid().equals(pause.getUuid()))
                .ifPresent(autre -> {
                    throw new RuntimeException("Ce service a déjà une pause en cours");
                });
    }

    private void verifierAucunAutreServiceEnCours(Service service) {
        tempsTravailService.serviceActif(service.getUser())
                .filter(autre -> !autre.getUuid().equals(service.getUuid()))
                .ifPresent(autre -> {
                    throw new RuntimeException("L'utilisateur a déjà un service en cours depuis le " + formater(autre.getDebut()));
                });
    }

    private static boolean contient(Service travail, ZonedDateTime instant) {
        return instant != null
                && !instant.isBefore(travail.getDebut())
                && (travail.getFin() == null || !instant.isAfter(travail.getFin()));
    }

    // Comparaison à la seconde : le front renvoie les dates avec une précision moindre que la base (microsecondes)
    private boolean isSameSecond(ZonedDateTime a, ZonedDateTime b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.toInstant().truncatedTo(ChronoUnit.SECONDS).equals(b.toInstant().truncatedTo(ChronoUnit.SECONDS));
    }

    private static String formater(ZonedDateTime instant) {
        return instant.withZoneSameInstant(PeriodeHeures.PARIS).format(FORMAT_DATE_HEURE);
    }

    private static ZonedDateTime maintenant() {
        return ZonedDateTime.now(PeriodeHeures.PARIS);
    }

    private User userByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
    }

    private User userByUuid(UUID userUuid) {
        return userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));
    }
}
