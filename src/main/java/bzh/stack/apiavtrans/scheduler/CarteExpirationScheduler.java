package bzh.stack.apiavtrans.scheduler;

import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.entity.Carte;
import bzh.stack.apiavtrans.repository.CarteRepository;
import bzh.stack.apiavtrans.repository.NotificationRepository;
import bzh.stack.apiavtrans.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarteExpirationScheduler {

    private final CarteRepository carteRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkCarteExpirations() {
        LocalDate today = LocalDate.now();
        LocalDate inOneMonth = today.plusMonths(1);

        List<Carte> expiringCartes = carteRepository.findByDateExpirationBetween(today, inOneMonth);

        for (Carte carte : expiringCartes) {
            String refId = carte.getUuid().toString();

            if (notificationRepository.existsByRefTypeAndRefId("carte_expiration", refId)) {
                continue;
            }

            String userName = carte.getUser() != null
                    ? carte.getUser().getFirstName() + " " + carte.getUser().getLastName()
                    : "Non assignee";

            NotificationCreateRequest request = new NotificationCreateRequest();
            request.setTitle("Carte " + carte.getNom() + " expire bientot");
            request.setDescription("La carte \"" + carte.getNom() + "\" (" + carte.getTypeCarte().getNom()
                    + ") attribuee a " + userName + " expire le " + carte.getDateExpiration() + ".");
            request.setRefType("carte_expiration");
            request.setRefId(refId);

            notificationService.sendNotificationToRole("Administrateur", request);
            log.info("Notification envoyee pour expiration carte: {} (expire le {})", carte.getNom(), carte.getDateExpiration());
        }
    }
}
