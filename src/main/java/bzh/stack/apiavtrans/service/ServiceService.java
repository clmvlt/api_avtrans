package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    private long calculateServiceDuration(Service service, ZonedDateTime calculationTime) {
        if (service.getDuree() != null) {
            return service.getDuree();
        } else if (service.getFin() == null && service.getDebut() != null) {
            return Duration.between(service.getDebut(), calculationTime).getSeconds();
        }
        return 0;
    }

    @Transactional
    public Service startService(String userEmail, Double latitude, Double longitude, Boolean isAdmin) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        if (serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user).isPresent()) {
            throw new RuntimeException("User already has an active service");
        }

        Service service = new Service();
        service.setDebut(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        service.setUser(user);
        service.setLatitude(latitude);
        service.setLongitude(longitude);
        service.setIsBreak(false);
        service.setIsAdmin(isAdmin != null ? isAdmin : false);

        return serviceRepository.save(service);
    }

    @Transactional
    public Service endService(String userEmail, Double latitude, Double longitude) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Service service = serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user)
                .orElseThrow(() -> new RuntimeException("No active service found for user"));

        if (service.getIsBreak()) {
            throw new RuntimeException("Cannot end service: user is on break. End break first.");
        }

        ZonedDateTime fin = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        service.setFin(fin);

        long durationInSeconds = Duration.between(service.getDebut(), fin).getSeconds();
        service.setDuree(durationInSeconds);

        if (latitude != null) {
            service.setLatitudeEnd(latitude);
        }
        if (longitude != null) {
            service.setLongitudeEnd(longitude);
        }

        return serviceRepository.save(service);
    }

    @Transactional
    public Service startBreak(String userEmail, Double latitude, Double longitude) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Service activeService = serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user).orElse(null);
        if (activeService == null || activeService.getIsBreak()) {
            throw new RuntimeException("Cannot start break: No active service found or already on break");
        }

        Service breakService = new Service();
        breakService.setDebut(ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        breakService.setUser(user);
        breakService.setLatitude(latitude);
        breakService.setLongitude(longitude);
        breakService.setIsBreak(true);
        breakService.setIsAdmin(false);

        return serviceRepository.save(breakService);
    }

    @Transactional
    public Service endBreak(String userEmail, Double latitude, Double longitude) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Service breakService = serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user)
                .orElseThrow(() -> new RuntimeException("No active service found"));

        if (!breakService.getIsBreak()) {
            throw new RuntimeException("Active service is not a break");
        }

        ZonedDateTime fin = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        breakService.setFin(fin);

        long durationInSeconds = Duration.between(breakService.getDebut(), fin).getSeconds();
        breakService.setDuree(durationInSeconds);

        if (latitude != null) {
            breakService.setLatitudeEnd(latitude);
        }
        if (longitude != null) {
            breakService.setLongitudeEnd(longitude);
        }

        return serviceRepository.save(breakService);
    }

    public List<Service> getUserServices(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        return serviceRepository.findByUser(user);
    }

    public List<Service> getUserServicesByUuid(UUID userUuid) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        return serviceRepository.findByUser(user);
    }

    public Service getActiveService(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        return serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user).orElse(null);
    }

    public Service getActiveServiceByUuid(UUID userUuid) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        return serviceRepository.findFirstByUserAndFinIsNullOrderByDebutDesc(user).orElse(null);
    }

    public List<Service> getServicesForMonth(String userEmail, Integer year, Integer month) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        ZonedDateTime startOfMonth = ZonedDateTime.of(targetYear, targetMonth, 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
        ZonedDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        return serviceRepository.findByUserAndDebutBetween(user, startOfMonth, endOfMonth);
    }

    public double calculateWorkedHours(String userEmail, String period, Integer year, Integer month, Integer week, Integer day) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        ZonedDateTime start;
        ZonedDateTime end = now;

        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetWeek = week != null ? week : now.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        int targetDay = day != null ? day : now.getDayOfMonth();

        switch (period.toLowerCase()) {
            case "day":
                start = ZonedDateTime.of(targetYear, targetMonth, targetDay, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                end = start.plusDays(1).minusNanos(1);
                break;
            case "week":
                start = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                        .with(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear(), targetWeek)
                        .with(java.time.temporal.WeekFields.ISO.dayOfWeek(), 1)
                        .withYear(targetYear)
                        .toLocalDate().atStartOfDay(ZoneId.of("Europe/Paris"));
                end = start.plusWeeks(1).minusNanos(1);
                break;
            case "month":
                start = ZonedDateTime.of(targetYear, targetMonth, 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                end = start.plusMonths(1).minusNanos(1);
                break;
            case "year":
                start = ZonedDateTime.of(targetYear, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                end = start.plusYears(1).minusNanos(1);
                break;
            case "lastmonth":
                ZonedDateTime firstDayOfCurrentMonth = ZonedDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                start = firstDayOfCurrentMonth.minusMonths(1);
                end = firstDayOfCurrentMonth.minusNanos(1);
                break;
            default:
                throw new RuntimeException("Invalid period. Use: day, week, month, year, or lastmonth");
        }

        List<Service> services = serviceRepository.findByUserAndDebutBetween(user, start, end);

        long totalSeconds = 0;
        ZonedDateTime calculationTime = ZonedDateTime.now(ZoneId.of("Europe/Paris"));

        for (Service service : services) {
            long duration = calculateServiceDuration(service, calculationTime);
            if (duration > 0) {
                if (service.getIsBreak()) {
                    totalSeconds -= duration;
                } else {
                    totalSeconds += duration;
                }
            }
        }

        double hours = totalSeconds / 3600.0;
        return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double calculateWorkedHoursByUuid(UUID userUuid, String period, Integer year, Integer month, Integer week, Integer day) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        ZonedDateTime start;
        ZonedDateTime end = now;

        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetWeek = week != null ? week : now.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        int targetDay = day != null ? day : now.getDayOfMonth();

        switch (period.toLowerCase()) {
            case "day":
                start = ZonedDateTime.of(targetYear, targetMonth, targetDay, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                end = start.plusDays(1).minusNanos(1);
                break;
            case "week":
                start = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                        .with(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear(), targetWeek)
                        .with(java.time.temporal.WeekFields.ISO.dayOfWeek(), 1)
                        .withYear(targetYear)
                        .toLocalDate().atStartOfDay(ZoneId.of("Europe/Paris"));
                end = start.plusWeeks(1).minusNanos(1);
                break;
            case "month":
                start = ZonedDateTime.of(targetYear, targetMonth, 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                end = start.plusMonths(1).minusNanos(1);
                break;
            case "year":
                start = ZonedDateTime.of(targetYear, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                end = start.plusYears(1).minusNanos(1);
                break;
            case "lastmonth":
                ZonedDateTime firstDayOfCurrentMonth = ZonedDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
                start = firstDayOfCurrentMonth.minusMonths(1);
                end = firstDayOfCurrentMonth.minusNanos(1);
                break;
            default:
                throw new RuntimeException("Invalid period. Use: day, week, month, year, or lastmonth");
        }

        List<Service> services = serviceRepository.findByUserAndDebutBetween(user, start, end);

        long totalSeconds = 0;
        ZonedDateTime calculationTime = ZonedDateTime.now(ZoneId.of("Europe/Paris"));

        for (Service service : services) {
            long duration = calculateServiceDuration(service, calculationTime);
            if (duration > 0) {
                if (service.getIsBreak()) {
                    totalSeconds -= duration;
                } else {
                    totalSeconds += duration;
                }
            }
        }

        double hours = totalSeconds / 3600.0;
        return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public Page<Service> getUserServicesPaginated(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "debut"));
        return serviceRepository.findByUser(user, pageable);
    }

    public Page<Service> getUserServicesHistory(String userEmail, int page, int size,
                                                 Boolean isBreak, ZonedDateTime startDate, ZonedDateTime endDate,
                                                 String sortBy, String sortDirection) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        String sortField = sortBy != null ? sortBy : "debut";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return serviceRepository.findByUserWithFilters(user, isBreak, startDate, endDate, pageable);
    }

    @Transactional
    public Service createServiceForUser(UUID userUuid, ZonedDateTime debut, ZonedDateTime fin,
                                       Double latitude, Double longitude,
                                       Double latitudeEnd, Double longitudeEnd,
                                       Boolean isBreak) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        Service service = new Service();
        service.setDebut(debut != null ? debut : ZonedDateTime.now(ZoneId.of("Europe/Paris")));
        service.setUser(user);
        service.setLatitude(latitude);
        service.setLongitude(longitude);
        service.setLatitudeEnd(latitudeEnd);
        service.setLongitudeEnd(longitudeEnd);
        service.setIsBreak(isBreak != null ? isBreak : false);
        service.setIsAdmin(true);

        if (fin != null) {
            service.setFin(fin);
            long durationInSeconds = Duration.between(service.getDebut(), fin).getSeconds();
            service.setDuree(durationInSeconds);
        }

        return serviceRepository.save(service);
    }

    @Transactional
    public Service updateService(UUID serviceUuid, ZonedDateTime debut, ZonedDateTime fin,
                                Double latitude, Double longitude,
                                Double latitudeEnd, Double longitudeEnd,
                                Boolean isBreak) {
        Service service = serviceRepository.findById(serviceUuid)
                .orElseThrow(() -> new RuntimeException("Service not found"));

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

        if (service.getDebut() != null && service.getFin() != null) {
            long durationInSeconds = Duration.between(service.getDebut(), service.getFin()).getSeconds();
            service.setDuree(durationInSeconds);
        } else if (service.getFin() == null) {
            service.setDuree(null);
        }

        return serviceRepository.save(service);
    }

    @Transactional
    public void deleteService(UUID serviceUuid) {
        if (!serviceRepository.existsById(serviceUuid)) {
            throw new RuntimeException("Service not found with uuid: " + serviceUuid);
        }
        serviceRepository.deleteById(serviceUuid);
    }

    public Page<Service> getUserServicesHistoryByUuid(UUID userUuid, int page, int size,
                                                       Boolean isBreak, ZonedDateTime startDate, ZonedDateTime endDate,
                                                       String sortBy, String sortDirection) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));

        ZonedDateTime effectiveStartDate = startDate;
        ZonedDateTime effectiveEndDate = endDate;

        if (effectiveStartDate == null) {
            effectiveStartDate = ZonedDateTime.now(ZoneId.of("Europe/Paris")).minusDays(30);
        }
        if (effectiveEndDate == null) {
            effectiveEndDate = ZonedDateTime.now(ZoneId.of("Europe/Paris")).plusYears(10);
        }

        String sortField = sortBy != null ? sortBy : "debut";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return serviceRepository.findByUserWithFilters(user, isBreak, effectiveStartDate, effectiveEndDate, pageable);
    }

    public List<Service> getDailyServices(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        ZonedDateTime startOfDay = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toLocalDate()
                .atStartOfDay(ZoneId.of("Europe/Paris"));
        ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return serviceRepository.findByUserAndDebutBetween(user, startOfDay, endOfDay);
    }
}