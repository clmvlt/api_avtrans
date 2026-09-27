package bzh.stack.apiavtrans.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jours fériés légaux en France métropolitaine (11 jours, hors Alsace-Moselle).
 * Calcul pur, mis en cache par année.
 */
@Service
public class JoursFeriesService {

    private final Map<Integer, Map<LocalDate, String>> cacheParAnnee = new ConcurrentHashMap<>();

    /** Jours fériés de l'année, date → nom. */
    public Map<LocalDate, String> annee(int year) {
        return cacheParAnnee.computeIfAbsent(year, JoursFeriesService::calculerAnnee);
    }

    /** Jours fériés compris entre deux dates incluses, triés par date. */
    public Map<LocalDate, String> entre(LocalDate from, LocalDate to) {
        Map<LocalDate, String> result = new TreeMap<>();
        for (int year = from.getYear(); year <= to.getYear(); year++) {
            annee(year).forEach((date, nom) -> {
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    result.put(date, nom);
                }
            });
        }
        return result;
    }

    public boolean estFerie(LocalDate date) {
        return annee(date.getYear()).containsKey(date);
    }

    /** Nom du jour férié, ou null si la date n'est pas fériée. */
    public String nom(LocalDate date) {
        return annee(date.getYear()).get(date);
    }

    private static Map<LocalDate, String> calculerAnnee(int year) {
        Map<LocalDate, String> holidays = new HashMap<>();
        holidays.put(LocalDate.of(year, 1, 1), "Jour de l'An");
        holidays.put(LocalDate.of(year, 5, 1), "Fête du Travail");
        holidays.put(LocalDate.of(year, 5, 8), "Victoire 1945");
        holidays.put(LocalDate.of(year, 7, 14), "Fête nationale");
        holidays.put(LocalDate.of(year, 8, 15), "Assomption");
        holidays.put(LocalDate.of(year, 11, 1), "Toussaint");
        holidays.put(LocalDate.of(year, 11, 11), "Armistice 1918");
        holidays.put(LocalDate.of(year, 12, 25), "Noël");

        LocalDate easter = computeEaster(year);
        holidays.put(easter.plusDays(1), "Lundi de Pâques");
        holidays.put(easter.plusDays(39), "Ascension");
        holidays.put(easter.plusDays(50), "Lundi de Pentecôte");
        return Collections.unmodifiableMap(holidays);
    }

    // Algorithme de Meeus/Jones/Butcher (Pâques grégorien)
    static LocalDate computeEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
