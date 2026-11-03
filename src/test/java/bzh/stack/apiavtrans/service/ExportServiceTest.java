package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceMapper;
import bzh.stack.apiavtrans.repository.AbsenceRepository;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.SignatureRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SignatureRepository signatureRepository;
    @Mock
    private AbsenceRepository absenceRepository;

    private ExportService exportService;
    private User employe;

    @BeforeEach
    void setUp() {
        JoursFeriesService joursFeriesService = new JoursFeriesService();
        TempsTravailCalculator calculator = new TempsTravailCalculator();
        TempsTravailService tempsTravailService = new TempsTravailService(serviceRepository, calculator,
                new ServiceMapper(calculator));
        exportService = new ExportService(tempsTravailService, calculator, userRepository, signatureRepository,
                absenceRepository, joursFeriesService, new HeuresAbsenceCalculator(joursFeriesService));

        employe = new User();
        employe.setUuid(UUID.randomUUID());
        employe.setFirstName("Jean");
        employe.setLastName("Dupont");
        employe.setHeureContrat(151.67);
    }

    @Test
    void should_write_credited_hours_column_and_totals_when_user_has_approved_leave() throws Exception {
        // Congés lun. 9 → ven. 13/11/2026 (férié le 11) : 5 jours + férié = 35 h créditées
        AbsenceType conges = new AbsenceType();
        conges.setName("Congés payés");
        Absence absence = new Absence();
        absence.setUser(employe);
        absence.setAbsenceType(conges);
        absence.setStartDate(LocalDate.of(2026, 11, 9));
        absence.setEndDate(LocalDate.of(2026, 11, 13));
        absence.setStatus(Absence.AbsenceStatus.APPROVED);

        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);
        when(userRepository.findAllById(List.of(employe.getUuid()))).thenReturn(List.of(employe));
        when(serviceRepository.findServicesTravail(eq(employe), any(), any())).thenReturn(List.of());
        when(absenceRepository.findApprovedByUserAndDateRange(employe, start.minusDays(1), end))
                .thenReturn(List.of(absence));
        when(signatureRepository.findByUserAndDateBetweenOrderByDateDesc(eq(employe), any(), any()))
                .thenReturn(List.of());

        byte[] bytes = exportService.exportWorkedHoursToExcel(List.of(employe.getUuid()), start, end);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(findRow(sheet, "Date").getCell(10).getStringCellValue()).isEqualTo("Heures créditées");
            assertThat(findRow(sheet, "11/11/2026").getCell(10).getNumericCellValue()).isCloseTo(5.83, within(0.01));
            assertThat(findRow(sheet, "14/11/2026").getCell(10).getNumericCellValue()).isCloseTo(5.83, within(0.01));
            assertThat(findRow(sheet, "TOTAL DU MOIS").getCell(10).getNumericCellValue()).isCloseTo(35.0, within(0.005));
            assertThat(findRow(sheet, "TOTAL DU MOIS (travail + absences + fériés)").getCell(6).getNumericCellValue())
                    .isCloseTo(35.0, within(0.005));
        }
    }

    @Test
    void should_count_night_service_and_its_break_after_midnight_on_start_day() throws Exception {
        // Service du 12/10 21 h au 13/10 5 h, pause le 13/10 de 1 h à 2 h → 7 h le 12/10, rien le 13/10
        Service nuit = travail(at(12, 21, 0), at(13, 5, 0));
        Service pause = pauseDe(nuit, at(13, 1, 0), at(13, 2, 0));

        Sheet sheet = exporter(List.of(nuit), List.of(pause));

        Row jour12 = findRow(sheet, "12/10/2026");
        assertThat(jour12.getCell(6).getNumericCellValue()).isCloseTo(7.0, within(0.001));
        assertThat(jour12.getCell(7).getStringCellValue()).contains("Fin de service le 13/10/2026");
        assertThat(findRow(sheet, "13/10/2026").getCell(6).getCellType()).isNotEqualTo(CellType.FORMULA);
        assertThat(findRow(sheet, "TOTAL DU MOIS").getCell(6).getNumericCellValue()).isCloseTo(7.0, within(0.001));
    }

    @Test
    void should_not_count_gap_between_two_services_of_the_same_day() throws Exception {
        Service matin = travail(at(12, 8, 0), at(12, 12, 0));
        Service apresMidi = travail(at(12, 14, 0), at(12, 18, 0));
        Service pause = pauseDe(apresMidi, at(12, 16, 0), at(12, 16, 15));

        Sheet sheet = exporter(List.of(matin, apresMidi), List.of(pause));

        Row jour12 = findRow(sheet, "12/10/2026");
        assertThat(jour12.getCell(6).getNumericCellValue()).isCloseTo(7.75, within(0.001));
        assertThat(jour12.getCell(7).getStringCellValue()).contains("2 services");
    }

    @Test
    void should_count_and_flag_service_longer_than_twelve_hours() throws Exception {
        Service long30h = travail(at(12, 8, 0), at(13, 14, 0));

        Sheet sheet = exporter(List.of(long30h), List.of());

        Row jour12 = findRow(sheet, "12/10/2026");
        assertThat(jour12.getCell(6).getNumericCellValue()).isCloseTo(30.0, within(0.001));
        assertThat(jour12.getCell(7).getStringCellValue()).contains("plus de 12 h");
    }

    @Test
    void should_count_real_duration_when_night_service_spans_dst_change() throws Exception {
        // Nuit du 24 au 25/10/2026 (on recule d'une heure) : 22 h → 6 h = 9 h réelles
        Service nuit = travail(at(24, 22, 0), at(25, 6, 0));

        Sheet sheet = exporter(List.of(nuit), List.of());

        assertThat(findRow(sheet, "24/10/2026").getCell(6).getNumericCellValue()).isCloseTo(9.0, within(0.001));
    }

    @Test
    void should_grey_out_days_fully_covered_by_a_multi_day_service() throws Exception {
        Service troisJours = travail(at(12, 8, 0), at(14, 10, 0));

        Sheet sheet = exporter(List.of(troisJours), List.of());

        assertThat(findRow(sheet, "13/10/2026").getCell(7).getStringCellValue()).isEqualTo("Suite du service du 12/10/2026");
        assertThat(findRow(sheet, "14/10/2026").getCell(7).getStringCellValue()).isEmpty();
    }

    @Test
    void should_round_totals_on_exact_sum_when_daily_hours_have_more_than_two_decimals() throws Exception {
        // 22 jours ouvrés de 7 h 07 (7,1167 h) : l'API donne 156,57 h, pas 22 × 7,12 = 156,64 h
        List<Service> travaux = new ArrayList<>();
        for (int jour = 1; jour <= 31; jour++) {
            if (LocalDate.of(2026, 10, jour).getDayOfWeek().getValue() <= 5) {
                travaux.add(travail(at(jour, 8, 0), at(jour, 15, 7)));
            }
        }

        Sheet sheet = exporter(travaux, List.of());

        assertThat(findRow(sheet, "05/10/2026").getCell(6).getNumericCellValue()).isCloseTo(7.1167, within(0.0001));
        assertThat(findRow(sheet, "Total semaine (05/10 – 11/10)").getCell(6).getNumericCellValue())
                .isEqualTo(35.58);
        assertThat(findRow(sheet, "TOTAL DU MOIS").getCell(6).getNumericCellValue()).isEqualTo(156.57);
    }

    @Test
    void should_create_one_sheet_per_user_when_two_users_share_the_same_name() throws Exception {
        User homonyme = new User();
        homonyme.setUuid(UUID.randomUUID());
        homonyme.setFirstName("Jean");
        homonyme.setLastName("Dupont");
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        List<UUID> uuids = List.of(employe.getUuid(), homonyme.getUuid());
        when(userRepository.findAllById(uuids)).thenReturn(List.of(employe, homonyme));
        when(serviceRepository.findServicesTravail(any(), any(), any())).thenReturn(List.of());
        when(absenceRepository.findApprovedByUserAndDateRange(any(), any(), any())).thenReturn(List.of());
        when(signatureRepository.findByUserAndDateBetweenOrderByDateDesc(any(), any(), any())).thenReturn(List.of());

        byte[] bytes = exportService.exportWorkedHoursToExcel(uuids, start, end);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            assertThat(workbook.getSheetName(0)).isEqualTo("Jean_Dupont");
            assertThat(workbook.getSheetName(1)).isEqualTo("Jean_Dupont (2)");
        }
    }

    @Test
    void should_not_flag_conflict_when_user_works_the_other_half_of_a_half_day_absence() throws Exception {
        Service apresMidi = travail(at(12, 13, 0), at(12, 17, 0));
        Service jourEntier = travail(at(13, 8, 0), at(13, 12, 0));

        Sheet sheet = exporter(List.of(apresMidi, jourEntier), List.of(),
                List.of(absence(12, Absence.AbsencePeriod.MORNING), absence(13, Absence.AbsencePeriod.FULL_DAY)));

        assertThat(findRow(sheet, "12/10/2026").getCell(9).getStringCellValue())
                .isEqualTo("Congés payés — Demi-journée (matin)");
        assertThat(findRow(sheet, "13/10/2026").getCell(9).getStringCellValue()).startsWith("⚠ Conflit pointage");
    }

    private Sheet exporter(List<Service> travaux, List<Service> pauses) throws Exception {
        return exporter(travaux, pauses, List.of());
    }

    private Sheet exporter(List<Service> travaux, List<Service> pauses, List<Absence> absences) throws Exception {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        when(userRepository.findAllById(List.of(employe.getUuid()))).thenReturn(List.of(employe));
        when(serviceRepository.findServicesTravail(eq(employe), any(), any())).thenReturn(travaux);
        if (!travaux.isEmpty()) {
            when(serviceRepository.findPausesDe(any())).thenReturn(pauses);
        }
        when(absenceRepository.findApprovedByUserAndDateRange(employe, start.minusDays(1), end)).thenReturn(absences);
        when(signatureRepository.findByUserAndDateBetweenOrderByDateDesc(eq(employe), any(), any()))
                .thenReturn(List.of());

        byte[] bytes = exportService.exportWorkedHoursToExcel(List.of(employe.getUuid()), start, end);
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        return workbook.getSheetAt(0);
    }

    private Service travail(ZonedDateTime debut, ZonedDateTime fin) {
        Service service = new Service();
        service.setUuid(UUID.randomUUID());
        service.setUser(employe);
        service.setDebut(debut);
        service.setFin(fin);
        service.setIsBreak(false);
        return service;
    }

    private Absence absence(int jourOctobre, Absence.AbsencePeriod periode) {
        AbsenceType conges = new AbsenceType();
        conges.setName("Congés payés");
        Absence absence = new Absence();
        absence.setUser(employe);
        absence.setAbsenceType(conges);
        absence.setStartDate(LocalDate.of(2026, 10, jourOctobre));
        absence.setEndDate(LocalDate.of(2026, 10, jourOctobre));
        absence.setPeriod(periode);
        absence.setStatus(Absence.AbsenceStatus.APPROVED);
        return absence;
    }

    private Service pauseDe(Service parent, ZonedDateTime debut, ZonedDateTime fin) {
        Service pause = travail(debut, fin);
        pause.setIsBreak(true);
        pause.setServiceParent(parent);
        return pause;
    }

    private static ZonedDateTime at(int jourOctobre, int heure, int minute) {
        return ZonedDateTime.of(2026, 10, jourOctobre, heure, minute, 0, 0, ZoneId.of("Europe/Paris"));
    }

    private static Row findRow(Sheet sheet, String firstCell) {
        for (Row row : sheet) {
            Cell cell = row.getCell(0);
            if (cell != null && cell.getCellType() == CellType.STRING && cell.getStringCellValue().equals(firstCell)) {
                return row;
            }
        }
        throw new AssertionError("Ligne introuvable : " + firstCell);
    }
}
