package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.User;
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
        exportService = new ExportService(serviceRepository, userRepository, signatureRepository, absenceRepository,
                joursFeriesService, new HeuresAbsenceCalculator(joursFeriesService));

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
        when(serviceRepository.findByUserAndDebutBetween(eq(employe), any(), any())).thenReturn(List.of());
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
