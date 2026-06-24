package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Absence;
import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.Signature;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.AbsenceRepository;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.SignatureRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ExportService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final SignatureRepository signatureRepository;
    private final AbsenceRepository absenceRepository;
    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");

    private static final String[] COLUMN_HEADERS = {
            "Date", "Jour", "Début journée", "Début pause", "Fin pause",
            "Fin journée", "Heures travaillées", "Informations complémentaires", "Autres pauses", "Absence"
    };

    private static final String[] FR_DAYS_SHORT = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

    @Transactional(readOnly = true)
    public byte[] exportWorkedHoursToExcel(List<UUID> userUuids, LocalDate startDate, LocalDate endDate) throws IOException {
        List<User> users = userRepository.findAllById(userUuids);

        if (users.isEmpty()) {
            throw new RuntimeException("Aucun utilisateur trouvé");
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        // Cache de styles partagé sur tout le classeur (couleurs de type d'absence, conflits, wrap)
        Map<String, CellStyle> styleCache = new HashMap<>();

        for (User user : users) {
            createUserSheet(workbook, user, startDate, endDate, styleCache);
        }

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        workbook.setForceFormulaRecalculation(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Construit un nom de fichier lisible : Heures_2026-06_Dupont_Jean.xlsx pour un mois plein
     * et un seul salarié, sinon une variante avec la plage de dates / le nombre de salariés.
     */
    @Transactional(readOnly = true)
    public String buildExportFileName(List<UUID> userUuids, LocalDate startDate, LocalDate endDate) {
        String period;
        YearMonth ymStart = YearMonth.from(startDate);
        if (ymStart.equals(YearMonth.from(endDate))
                && startDate.equals(ymStart.atDay(1))
                && endDate.equals(ymStart.atEndOfMonth())) {
            period = ymStart.toString(); // yyyy-MM
        } else {
            period = startDate + "_au_" + endDate;
        }

        String who;
        if (userUuids.size() == 1) {
            who = userRepository.findById(userUuids.get(0))
                    .map(u -> sanitizeFileNamePart(u.getLastName() + "_" + u.getFirstName()))
                    .orElse("salarie");
        } else {
            who = userUuids.size() + "_salaries";
        }

        return "Heures_" + period + "_" + who + ".xlsx";
    }

    private void createUserSheet(Workbook workbook, User user, LocalDate startDate, LocalDate endDate,
                                 Map<String, CellStyle> styleCache) {
        String sheetName = sanitizeSheetName(user.getFirstName() + "_" + user.getLastName());
        Sheet sheet = workbook.createSheet(sheetName);

        // ── Styles ──
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle columnHeaderStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle timeStyle = createTimeStyle(workbook);
        CellStyle greyedStyle = createGreyedStyle(workbook);
        CellStyle excelTimeStyle = createExcelTimeStyle(workbook);
        CellStyle excelGreyedTimeStyle = createExcelGreyedTimeStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle greyedNumberStyle = createGreyedNumberStyle(workbook);
        CellStyle weekendDataStyle = createWeekendDataStyle(workbook);
        CellStyle weekendExcelTimeStyle = createWeekendExcelTimeStyle(workbook);
        CellStyle weekendNumberStyle = createWeekendNumberStyle(workbook);
        CellStyle holidayDataStyle = createHolidayDataStyle(workbook);
        CellStyle holidayExcelTimeStyle = createHolidayExcelTimeStyle(workbook);
        CellStyle holidayNumberStyle = createHolidayNumberStyle(workbook);
        CellStyle monthHeaderStyle = createMonthHeaderStyle(workbook);
        CellStyle signatureBarStyle = createSignatureBarStyle(workbook);
        CellStyle signatureLabelStyle = createSignatureLabelStyle(workbook);
        CellStyle totalLabelStyle = createTotalLabelStyle(workbook);
        CellStyle totalValueStyle = createTotalValueStyle(workbook);
        CellStyle weekLabelStyle = createWeekSubtotalLabelStyle(workbook);
        CellStyle weekValueStyle = createWeekSubtotalValueStyle(workbook);

        int rowNum = 0;
        DateTimeFormatter periodFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // ── Titre + sous-titre ──
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(user.getFirstName() + " " + user.getLastName());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Période du " + startDate.format(periodFmt) + " au " + endDate.format(periodFmt));
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));

        // ── Traitement des services ──
        ZonedDateTime startDateTime = startDate.atStartOfDay(PARIS_ZONE);
        ZonedDateTime endDateTime = endDate.plusDays(1).atStartOfDay(PARIS_ZONE);

        List<bzh.stack.apiavtrans.entity.Service> services = serviceRepository
                .findByUserAndDebutBetween(user, startDateTime, endDateTime);

        Map<LocalDate, DayData> dayDataMap = new HashMap<>();
        List<bzh.stack.apiavtrans.entity.Service> workServices = new ArrayList<>();

        for (bzh.stack.apiavtrans.entity.Service service : services) {
            if (!service.getIsBreak()) {
                workServices.add(service);
            }
        }

        for (bzh.stack.apiavtrans.entity.Service service : services) {
            LocalDate serviceStartDate = service.getDebut().withZoneSameInstant(PARIS_ZONE).toLocalDate();
            LocalDate serviceEndDate = service.getFin() != null
                ? service.getFin().withZoneSameInstant(PARIS_ZONE).toLocalDate()
                : serviceStartDate;

            if (service.getIsBreak()) {
                LocalDate breakDayToUse = findDayForBreak(service, workServices);
                DayData dayData = dayDataMap.computeIfAbsent(breakDayToUse, k -> new DayData());
                if (service.getDebut() != null && service.getFin() != null) {
                    dayData.breaks.add(new BreakPeriod(service.getDebut(), service.getFin()));
                }
            } else {
                DayData startDayData = dayDataMap.computeIfAbsent(serviceStartDate, k -> new DayData());
                if (startDayData.workStart == null || service.getDebut().isBefore(startDayData.workStart)) {
                    startDayData.workStart = service.getDebut();
                }

                if (service.getFin() != null) {
                    if (serviceStartDate.equals(serviceEndDate)) {
                        if (startDayData.workEnd == null || service.getFin().isAfter(startDayData.workEnd)) {
                            startDayData.workEnd = service.getFin();
                        }
                    } else {
                        long hoursBetween = ChronoUnit.HOURS.between(service.getDebut(), service.getFin());
                        boolean isNightShift = hoursBetween <= 12 && serviceEndDate.equals(serviceStartDate.plusDays(1));

                        if (isNightShift) {
                            if (startDayData.workEnd == null || service.getFin().isAfter(startDayData.workEnd)) {
                                startDayData.workEnd = service.getFin();
                            }
                            startDayData.isNightShift = true;
                            startDayData.nightShiftEndDate = serviceEndDate;
                        } else {
                            startDayData.isMultiDayStart = true;

                            DayData endDayData = dayDataMap.computeIfAbsent(serviceEndDate, k -> new DayData());
                            if (endDayData.workEnd == null || service.getFin().isAfter(endDayData.workEnd)) {
                                endDayData.workEnd = service.getFin();
                            }
                            endDayData.isMultiDayEnd = true;

                            LocalDate betweenDate = serviceStartDate.plusDays(1);
                            while (betweenDate.isBefore(serviceEndDate)) {
                                DayData betweenDayData = dayDataMap.computeIfAbsent(betweenDate, k -> new DayData());
                                betweenDayData.isMultiDayService = true;
                                betweenDate = betweenDate.plusDays(1);
                            }
                        }
                    }
                } else {
                    startDayData.hasIncomplete = true;
                }
            }

            if (service.getFin() == null) {
                DayData dayData = dayDataMap.computeIfAbsent(serviceStartDate, k -> new DayData());
                dayData.hasIncomplete = true;
            }
        }

        // ── Absences approuvées sur la période (une seule requête, indexées par jour) ──
        Map<LocalDate, List<Absence>> absenceByDay = new HashMap<>();
        List<Absence> approvedAbsences = absenceRepository
                .findApprovedByUserAndDateRange(user, startDate, endDate);
        for (Absence absence : approvedAbsences) {
            LocalDate from = absence.getStartDate().isBefore(startDate) ? startDate : absence.getStartDate();
            LocalDate to = absence.getEndDate().isAfter(endDate) ? endDate : absence.getEndDate();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                absenceByDay.computeIfAbsent(d, k -> new ArrayList<>()).add(absence);
            }
        }

        // ── Jours fériés français sur la plage d'années couverte ──
        Map<LocalDate, String> holidays = new HashMap<>();
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            addFrenchHolidays(holidays, year);
        }

        // ── Rendu Excel ──
        LocalDate currentDate = startDate;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        YearMonth currentMonth = YearMonth.from(startDate);

        // Premier mois : en-tête + colonnes
        rowNum = writeSectionHeader(sheet, rowNum, "Heures — ", currentMonth, monthHeaderStyle);
        int firstColumnHeaderRow = rowNum;
        rowNum = writeColumnHeaders(sheet, rowNum, columnHeaderStyle);

        // Suivi des sous-totaux hebdomadaires (lignes Excel des cellules de total semaine du mois courant)
        List<Integer> monthWeekSubtotalRows = new ArrayList<>();
        int weekStartExcelRow = -1;
        int weekEndExcelRow = -1;
        LocalDate weekStartDate = null;
        LocalDate weekEndDate = null;
        LocalDate currentWeekMonday = null;

        while (!currentDate.isAfter(endDate)) {
            YearMonth dayMonth = YearMonth.from(currentDate);

            if (!dayMonth.equals(currentMonth)) {
                // Clôture de la semaine ouverte puis du mois précédent : total + signature
                if (weekStartExcelRow != -1) {
                    int subRow = rowNum + 1;
                    rowNum = writeWeekSubtotalRow(sheet, rowNum, weekStartExcelRow, weekEndExcelRow,
                            weekStartDate, weekEndDate, weekLabelStyle, weekValueStyle);
                    monthWeekSubtotalRows.add(subRow);
                    weekStartExcelRow = -1;
                }
                rowNum = writeMonthTotalRow(sheet, rowNum, monthWeekSubtotalRows, totalLabelStyle, totalValueStyle);
                rowNum = writeSignatureSection(sheet, workbook, user, currentMonth, rowNum,
                        signatureBarStyle, signatureLabelStyle, dataStyle);
                currentMonth = dayMonth;
                monthWeekSubtotalRows.clear();
                currentWeekMonday = null;
                // Nouveau mois
                rowNum = writeSectionHeader(sheet, rowNum, "Heures — ", currentMonth, monthHeaderStyle);
                rowNum = writeColumnHeaders(sheet, rowNum, columnHeaderStyle);
            }

            // Changement de semaine ISO (lundi) à l'intérieur du mois → flush sous-total
            LocalDate weekMonday = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);
            if (currentWeekMonday != null && !weekMonday.equals(currentWeekMonday) && weekStartExcelRow != -1) {
                int subRow = rowNum + 1;
                rowNum = writeWeekSubtotalRow(sheet, rowNum, weekStartExcelRow, weekEndExcelRow,
                        weekStartDate, weekEndDate, weekLabelStyle, weekValueStyle);
                monthWeekSubtotalRows.add(subRow);
                weekStartExcelRow = -1;
            }
            currentWeekMonday = weekMonday;

            boolean isWeekend = currentDate.getDayOfWeek() == DayOfWeek.SATURDAY
                             || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isHoliday = holidays.containsKey(currentDate);
            String holidayName = holidays.get(currentDate);

            Row row = sheet.createRow(rowNum++);
            int dayExcelRow = rowNum;
            if (weekStartExcelRow == -1) {
                weekStartExcelRow = dayExcelRow;
                weekStartDate = currentDate;
            }
            weekEndExcelRow = dayExcelRow;
            weekEndDate = currentDate;

            DayData dayData = dayDataMap.get(currentDate);
            List<Absence> dayAbsences = absenceByDay.get(currentDate);
            boolean conflict = dayAbsences != null && !dayAbsences.isEmpty() && dayData != null;
            String typeHex = singleAbsenceColor(dayAbsences);
            String absenceText = buildAbsenceText(dayAbsences);
            if (conflict && !absenceText.isEmpty()) {
                absenceText = "⚠ Conflit pointage\n" + absenceText;
            }

            // Priorité de coloration de ligne : férié > week-end > normal
            CellStyle currentDataStyle = isHoliday ? holidayDataStyle : (isWeekend ? weekendDataStyle : dataStyle);
            CellStyle currentExcelTime = isHoliday ? holidayExcelTimeStyle : (isWeekend ? weekendExcelTimeStyle : excelTimeStyle);
            CellStyle currentNumber = isHoliday ? holidayNumberStyle : (isWeekend ? weekendNumberStyle : numberStyle);
            String baseKey = isHoliday ? "hol" : (isWeekend ? "we" : "nrm");

            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(currentDate.format(dateFormatter));
            dateCell.setCellStyle(currentDataStyle);

            Cell dayCell = row.createCell(1);
            dayCell.setCellValue(FR_DAYS_SHORT[currentDate.getDayOfWeek().getValue() - 1]);
            dayCell.setCellStyle(currentDataStyle);

            if (dayData == null) {
                for (int i = 2; i < 9; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue("");
                    cell.setCellStyle(currentDataStyle);
                }
                if (isHoliday) {
                    row.getCell(7).setCellValue("Férié : " + holidayName);
                }
                writeAbsenceCell(workbook, styleCache, row, absenceText, conflict, typeHex,
                        currentDataStyle, baseKey);
            } else if (dayData.isMultiDayService) {
                for (int i = 2; i <= 8; i++) {
                    Cell cell = row.createCell(i);
                    if (i == 7) {
                        cell.setCellValue("Service en cours sur plusieurs jours");
                    } else {
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(greyedStyle);
                }
                writeAbsenceCell(workbook, styleCache, row, absenceText, conflict, typeHex,
                        greyedStyle, "grey");
            } else {
                boolean isMultiDay = dayData.isMultiDayStart || dayData.isMultiDayEnd;
                CellStyle cellStyleToUse = isMultiDay ? greyedStyle : (isHoliday ? holidayDataStyle : (isWeekend ? weekendDataStyle : timeStyle));
                CellStyle dataCellStyleToUse = isMultiDay ? greyedStyle : currentDataStyle;
                CellStyle excelTimeStyleToUse = isMultiDay ? excelGreyedTimeStyle : currentExcelTime;
                CellStyle numberStyleToUse = isMultiDay ? greyedNumberStyle : currentNumber;

                Cell startCell = row.createCell(2);
                if (dayData.workStart != null) {
                    startCell.setCellValue(java.util.Date.from(dayData.workStart.toInstant()));
                    startCell.setCellStyle(excelTimeStyleToUse);
                } else {
                    startCell.setCellValue("");
                    startCell.setCellStyle(cellStyleToUse);
                }

                Cell breakStartCell = row.createCell(3);
                Cell breakEndCell = row.createCell(4);

                BreakPeriod longestBreak = null;
                if (!dayData.breaks.isEmpty()) {
                    longestBreak = dayData.breaks.get(0);
                    long longestDuration = ChronoUnit.MINUTES.between(longestBreak.start, longestBreak.end);

                    for (BreakPeriod breakPeriod : dayData.breaks) {
                        long duration = ChronoUnit.MINUTES.between(breakPeriod.start, breakPeriod.end);
                        if (duration > longestDuration) {
                            longestBreak = breakPeriod;
                            longestDuration = duration;
                        }
                    }

                    breakStartCell.setCellValue(java.util.Date.from(longestBreak.start.toInstant()));
                    breakStartCell.setCellStyle(excelTimeStyleToUse);
                    breakEndCell.setCellValue(java.util.Date.from(longestBreak.end.toInstant()));
                    breakEndCell.setCellStyle(excelTimeStyleToUse);
                } else {
                    breakStartCell.setCellValue("");
                    breakStartCell.setCellStyle(cellStyleToUse);
                    breakEndCell.setCellValue("");
                    breakEndCell.setCellStyle(cellStyleToUse);
                }

                Cell endCell = row.createCell(5);
                if (dayData.workEnd != null) {
                    endCell.setCellValue(java.util.Date.from(dayData.workEnd.toInstant()));
                    endCell.setCellStyle(excelTimeStyleToUse);
                } else {
                    endCell.setCellValue("");
                    endCell.setCellStyle(cellStyleToUse);
                }

                Cell hoursCell = row.createCell(6);
                if (dayData.workStart != null && dayData.workEnd != null) {
                    String formula = buildHoursFormula(dayExcelRow, dayData, longestBreak);
                    hoursCell.setCellFormula(formula);
                }
                hoursCell.setCellStyle(numberStyleToUse);

                Cell infoCell = row.createCell(7);
                String infoText = "";
                if (dayData.isMultiDayStart || dayData.isMultiDayEnd) {
                    infoText = "Service s'étend sur plusieurs jours";
                } else if (dayData.isNightShift && dayData.nightShiftEndDate != null) {
                    infoText = "Fin de service le " + dayData.nightShiftEndDate.format(dateFormatter);
                } else if (dayData.hasIncomplete) {
                    infoText = "Pointage présent mais incomplet.";
                }
                if (isHoliday) {
                    infoText = (infoText.isEmpty() ? "" : infoText + " — ") + "Férié : " + holidayName;
                }
                infoCell.setCellValue(infoText);
                infoCell.setCellStyle(dataCellStyleToUse);

                Cell otherBreaksCell = row.createCell(8);
                if (dayData.breaks.size() > 1 && longestBreak != null) {
                    StringBuilder otherBreaks = new StringBuilder();
                    boolean first = true;
                    for (BreakPeriod breakPeriod : dayData.breaks) {
                        if (breakPeriod != longestBreak) {
                            if (!first) {
                                otherBreaks.append("\n");
                            }
                            otherBreaks.append(breakPeriod.start.withZoneSameInstant(PARIS_ZONE).format(timeFormatter));
                            otherBreaks.append(" - ");
                            otherBreaks.append(breakPeriod.end.withZoneSameInstant(PARIS_ZONE).format(timeFormatter));
                            first = false;
                        }
                    }

                    CellStyle wrapTextStyle = workbook.createCellStyle();
                    wrapTextStyle.cloneStyleFrom(dataCellStyleToUse);
                    wrapTextStyle.setWrapText(true);

                    otherBreaksCell.setCellValue(otherBreaks.toString());
                    otherBreaksCell.setCellStyle(wrapTextStyle);
                } else {
                    otherBreaksCell.setCellValue("");
                    otherBreaksCell.setCellStyle(dataCellStyleToUse);
                }

                String workedBaseKey = isMultiDay ? "grey" : baseKey;
                writeAbsenceCell(workbook, styleCache, row, absenceText, conflict, typeHex,
                        dataCellStyleToUse, workedBaseKey);
            }

            currentDate = currentDate.plusDays(1);
        }

        // Clôture du dernier mois : semaine restante + total + signature
        if (weekStartExcelRow != -1) {
            int subRow = rowNum + 1;
            rowNum = writeWeekSubtotalRow(sheet, rowNum, weekStartExcelRow, weekEndExcelRow,
                    weekStartDate, weekEndDate, weekLabelStyle, weekValueStyle);
            monthWeekSubtotalRows.add(subRow);
        }
        rowNum = writeMonthTotalRow(sheet, rowNum, monthWeekSubtotalRows, totalLabelStyle, totalValueStyle);
        rowNum = writeSignatureSection(sheet, workbook, user, currentMonth, rowNum,
                signatureBarStyle, signatureLabelStyle, dataStyle);

        // Légende
        writeLegend(sheet, workbook, styleCache, rowNum,
                weekendDataStyle, holidayDataStyle, greyedStyle);

        // Auto-size
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
        }

        // Volets figés : garder les 2 premières colonnes (Date, Jour) et la 1re ligne d'en-têtes visibles
        sheet.createFreezePane(2, firstColumnHeaderRow + 1);
    }

    // ── Helpers de rendu ──

    private int writeColumnHeaders(Sheet sheet, int rowNum, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(COLUMN_HEADERS[i]);
            cell.setCellStyle(style);
        }
        return rowNum + 1;
    }

    private int writeSectionHeader(Sheet sheet, int rowNum, String prefix, YearMonth month, CellStyle style) {
        String monthName = month.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String capitalizedMonth = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        String text = prefix + capitalizedMonth + " " + month.getYear();

        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        for (int i = 1; i < 10; i++) {
            row.createCell(i).setCellStyle(style);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 9));
        return rowNum + 1;
    }

    private int writeWeekSubtotalRow(Sheet sheet, int rowNum, int weekStartExcelRow, int weekEndExcelRow,
                                     LocalDate weekStart, LocalDate weekEnd,
                                     CellStyle labelStyle, CellStyle valueStyle) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM");
        Row row = sheet.createRow(rowNum);

        Cell label = row.createCell(0);
        label.setCellValue("Total semaine (" + weekStart.format(f) + " – " + weekEnd.format(f) + ")");
        label.setCellStyle(labelStyle);
        for (int i = 1; i <= 5; i++) {
            row.createCell(i).setCellStyle(labelStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5));

        Cell value = row.createCell(6);
        value.setCellFormula("SUM(G" + weekStartExcelRow + ":G" + weekEndExcelRow + ")");
        value.setCellStyle(valueStyle);

        for (int i = 7; i <= 9; i++) {
            row.createCell(i).setCellStyle(labelStyle);
        }

        return rowNum + 1;
    }

    private int writeMonthTotalRow(Sheet sheet, int rowNum, List<Integer> weekSubtotalRows,
                                   CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);

        Cell label = row.createCell(0);
        label.setCellValue("TOTAL DU MOIS");
        label.setCellStyle(labelStyle);
        for (int i = 1; i <= 5; i++) {
            row.createCell(i).setCellStyle(labelStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5));

        Cell value = row.createCell(6);
        if (!weekSubtotalRows.isEmpty()) {
            StringBuilder sb = new StringBuilder("SUM(");
            for (int i = 0; i < weekSubtotalRows.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("G").append(weekSubtotalRows.get(i));
            }
            sb.append(")");
            value.setCellFormula(sb.toString());
        }
        value.setCellStyle(valueStyle);

        for (int i = 7; i <= 9; i++) {
            row.createCell(i).setCellStyle(labelStyle);
        }

        return rowNum + 1;
    }

    private int writeSignatureSection(Sheet sheet, Workbook workbook, User user, YearMonth month,
                                       int rowNum, CellStyle barStyle,
                                       CellStyle labelStyle, CellStyle dataStyle) {
        YearMonth signatureMonth = month.plusMonths(1);
        ZonedDateTime sigStart = signatureMonth.atDay(1).atStartOfDay(PARIS_ZONE);
        ZonedDateTime sigEnd = signatureMonth.atEndOfMonth().atTime(23, 59, 59).atZone(PARIS_ZONE);

        List<Signature> signatures = signatureRepository.findByUserAndDateBetweenOrderByDateDesc(
                user, sigStart, sigEnd);

        rowNum = writeSectionHeader(sheet, rowNum, "Signature — ", month, barStyle);

        if (!signatures.isEmpty()) {
            Signature sig = signatures.get(0);
            DateTimeFormatter sigFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
            String signedDate = sig.getDate().withZoneSameInstant(PARIS_ZONE).format(sigFmt);
            String signedBy = user.getFirstName() + " " + user.getLastName();

            Row infoRow = sheet.createRow(rowNum);

            Cell signerLabel = infoRow.createCell(0);
            signerLabel.setCellValue("Signé par :");
            signerLabel.setCellStyle(labelStyle);

            Cell signerValue = infoRow.createCell(1);
            signerValue.setCellValue(signedBy);
            signerValue.setCellStyle(dataStyle);

            Cell dateLabel = infoRow.createCell(2);
            dateLabel.setCellValue("Le :");
            dateLabel.setCellStyle(labelStyle);

            Cell dateValue = infoRow.createCell(3);
            dateValue.setCellValue(signedDate);
            dateValue.setCellStyle(dataStyle);

            Cell hoursLabel = infoRow.createCell(4);
            hoursLabel.setCellValue("Heures :");
            hoursLabel.setCellStyle(labelStyle);

            Cell hoursValue = infoRow.createCell(5);
            hoursValue.setCellValue(sig.getHeuresSignees() + " h");
            hoursValue.setCellStyle(dataStyle);

            Cell statusLabel = infoRow.createCell(6);
            statusLabel.setCellValue("Statut :");
            statusLabel.setCellStyle(labelStyle);

            Cell statusValue = infoRow.createCell(7);
            statusValue.setCellValue("Signé");
            statusValue.setCellStyle(dataStyle);

            infoRow.createCell(8).setCellStyle(dataStyle);
            infoRow.createCell(9).setCellStyle(dataStyle);

            rowNum++;

            // Image collée à droite (colonnes H-J)
            try {
                String base64 = sig.getSignatureBase64();
                int imageType = Workbook.PICTURE_TYPE_PNG;

                if (base64.startsWith("data:")) {
                    String prefix = base64.substring(0, base64.indexOf(","));
                    if (prefix.contains("jpeg") || prefix.contains("jpg")) {
                        imageType = Workbook.PICTURE_TYPE_JPEG;
                    }
                    base64 = base64.substring(base64.indexOf(",") + 1);
                }

                byte[] imageBytes = Base64.getDecoder().decode(base64);
                int pictureIdx = workbook.addPicture(imageBytes, imageType);

                Drawing<?> drawing = sheet.createDrawingPatriarch();
                CreationHelper helper = workbook.getCreationHelper();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(7);
                anchor.setRow1(rowNum);
                anchor.setCol2(10);
                anchor.setRow2(rowNum + 3);
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                drawing.createPicture(anchor, pictureIdx);

                for (int i = 0; i < 3; i++) {
                    sheet.createRow(rowNum + i);
                }
                rowNum += 3;
            } catch (Exception e) {
                Row errorRow = sheet.createRow(rowNum++);
                Cell errorCell = errorRow.createCell(0);
                errorCell.setCellValue("(Image de signature non disponible)");
                errorCell.setCellStyle(dataStyle);
            }
        } else {
            Row notSignedRow = sheet.createRow(rowNum);
            Cell notSignedCell = notSignedRow.createCell(0);
            notSignedCell.setCellValue("Non signé pour ce mois");
            notSignedCell.setCellStyle(dataStyle);
            for (int i = 1; i < 10; i++) {
                notSignedRow.createCell(i).setCellStyle(dataStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 9));
            rowNum++;
        }

        return rowNum;
    }

    private int writeLegend(Sheet sheet, Workbook workbook, Map<String, CellStyle> styleCache, int rowNum,
                            CellStyle weekendStyle, CellStyle holidayStyle, CellStyle greyedStyle) {
        rowNum++; // ligne d'espacement

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 11);
        titleStyle.setFont(titleFont);

        Row titleRow = sheet.createRow(rowNum);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Légende");
        titleCell.setCellStyle(titleStyle);
        rowNum++;

        rowNum = legendItem(sheet, rowNum, weekendStyle, "Week-end");
        rowNum = legendItem(sheet, rowNum, holidayStyle, "Jour férié");
        rowNum = legendItem(sheet, rowNum, greyedStyle,
                "Service sur plusieurs jours (heures non comptées ce jour-là)");
        CellStyle conflictSample = buildAbsenceStyle(workbook, styleCache, null, false, true);
        rowNum = legendItem(sheet, rowNum, conflictSample,
                "Conflit : pointage et absence le même jour (à vérifier)");
        rowNum = legendText(sheet, rowNum,
                "« Pointage présent mais incomplet » : service sans heure de fin");
        rowNum = legendText(sheet, rowNum,
                "Colonne « Absence » : la cellule reprend la couleur du type d'absence");
        rowNum = legendText(sheet, rowNum,
                "Total semaine : base de calcul des heures supplémentaires (> 35 h)");
        rowNum = legendText(sheet, rowNum, "Jours : Lun, Mar, Mer, Jeu, Ven, Sam, Dim");

        return rowNum;
    }

    private int legendItem(Sheet sheet, int rowNum, CellStyle sample, String description) {
        Row row = sheet.createRow(rowNum);
        Cell swatch = row.createCell(0);
        swatch.setCellStyle(sample);
        Cell desc = row.createCell(1);
        desc.setCellValue(description);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 7));
        return rowNum + 1;
    }

    private int legendText(Sheet sheet, int rowNum, String description) {
        Row row = sheet.createRow(rowNum);
        Cell desc = row.createCell(1);
        desc.setCellValue(description);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 7));
        return rowNum + 1;
    }

    // ── Formules ──

    private String buildHoursFormula(int excelRow, DayData dayData, BreakPeriod longestBreak) {
        String formula = "ROUND(((F" + excelRow + "-C" + excelRow + ")";

        if (longestBreak != null) {
            formula += "-(E" + excelRow + "-D" + excelRow + ")";
        }

        if (dayData.breaks.size() > 1) {
            long otherBreaksMinutes = 0;

            for (BreakPeriod breakPeriod : dayData.breaks) {
                if (breakPeriod != longestBreak) {
                    long duration = ChronoUnit.MINUTES.between(breakPeriod.start, breakPeriod.end);
                    otherBreaksMinutes += duration;
                }
            }

            if (otherBreaksMinutes > 0) {
                double otherBreaksDays = otherBreaksMinutes / 1440.0;
                formula += "-" + String.format(Locale.US, "%.10f", otherBreaksDays);
            }
        }

        formula += ")*24,2)";
        return formula;
    }

    // ── Styles ──

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTimeStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createGreyedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createExcelTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("HH:mm:ss"));
        return style;
    }

    private CellStyle createExcelGreyedTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("HH:mm:ss"));
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        return style;
    }

    private CellStyle createGreyedNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createWeekendDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createWeekendExcelTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("HH:mm:ss"));
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createWeekendNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    // Jours fériés : teinte ambre (#FFE699)
    private CellStyle createHolidayDataStyle(Workbook workbook) {
        XSSFCellStyle style = baseBorderedCenter(workbook);
        applyHolidayFill(style);
        return style;
    }

    private CellStyle createHolidayExcelTimeStyle(Workbook workbook) {
        XSSFCellStyle style = baseBorderedCenter(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("HH:mm:ss"));
        applyHolidayFill(style);
        return style;
    }

    private CellStyle createHolidayNumberStyle(Workbook workbook) {
        XSSFCellStyle style = baseBorderedCenter(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        applyHolidayFill(style);
        return style;
    }

    private void applyHolidayFill(XSSFCellStyle style) {
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xE6, (byte) 0x99}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private XSSFCellStyle baseBorderedCenter(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createMonthHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createSignatureBarStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createSignatureLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTotalLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTotalValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        return style;
    }

    // Sous-total hebdomadaire : gris clair (#F2F2F2), plus discret que le total du mois
    private CellStyle createWeekSubtotalLabelStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setItalic(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0xF2, (byte) 0xF2, (byte) 0xF2}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createWeekSubtotalValueStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setItalic(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0xF2, (byte) 0xF2, (byte) 0xF2}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    // ── Absences (cellule colorée par type / conflit) ──

    private void writeAbsenceCell(Workbook workbook, Map<String, CellStyle> styleCache, Row row, String text,
                                  boolean conflict, String typeHex, CellStyle baseStyle, String baseKey) {
        Cell cell = row.createCell(9);
        cell.setCellValue(text);
        boolean wrap = text.contains("\n");

        CellStyle style;
        if (conflict || typeHex != null) {
            style = buildAbsenceStyle(workbook, styleCache, typeHex, wrap, conflict);
        } else if (wrap) {
            style = styleCache.computeIfAbsent("WRAP|" + baseKey, k -> {
                CellStyle s = workbook.createCellStyle();
                s.cloneStyleFrom(baseStyle);
                s.setWrapText(true);
                return s;
            });
        } else {
            style = baseStyle;
        }
        cell.setCellStyle(style);
    }

    private CellStyle buildAbsenceStyle(Workbook workbook, Map<String, CellStyle> styleCache,
                                        String typeHex, boolean wrap, boolean conflict) {
        byte[] rgb = conflict
                ? new byte[]{(byte) 0xF4, (byte) 0xCC, (byte) 0xCC} // rouge clair d'alerte
                : hexToRgb(typeHex);
        String key = "ABS|" + conflict + "|" + (typeHex == null ? "" : typeHex.toUpperCase()) + "|" + wrap;
        return styleCache.computeIfAbsent(key, k -> {
            XSSFCellStyle style = baseBorderedCenter(workbook);
            if (wrap) {
                style.setWrapText(true);
            }
            if (rgb != null) {
                style.setFillForegroundColor(new XSSFColor(rgb, null));
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                double luminance = (0.299 * (rgb[0] & 0xFF) + 0.587 * (rgb[1] & 0xFF) + 0.114 * (rgb[2] & 0xFF)) / 255.0;
                Font font = workbook.createFont();
                font.setColor(luminance < 0.5 ? IndexedColors.WHITE.getIndex() : IndexedColors.BLACK.getIndex());
                style.setFont(font);
            }
            return style;
        });
    }

    private String singleAbsenceColor(List<Absence> absences) {
        if (absences == null || absences.size() != 1) {
            return null;
        }
        AbsenceType type = absences.get(0).getAbsenceType();
        if (type != null && type.getColor() != null && type.getColor().matches("#?[0-9a-fA-F]{6}")) {
            return type.getColor();
        }
        return null;
    }

    private byte[] hexToRgb(String hex) {
        if (hex == null || !hex.matches("#?[0-9a-fA-F]{6}")) {
            return null;
        }
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        return new byte[]{
                (byte) Integer.parseInt(h.substring(0, 2), 16),
                (byte) Integer.parseInt(h.substring(2, 4), 16),
                (byte) Integer.parseInt(h.substring(4, 6), 16)
        };
    }

    private String buildAbsenceText(List<Absence> absences) {
        if (absences == null || absences.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Absence absence : absences) {
            if (!first) {
                sb.append("\n");
            }
            String type;
            if (absence.getAbsenceType() != null) {
                type = absence.getAbsenceType().getName();
            } else if (absence.getCustomType() != null && !absence.getCustomType().isBlank()) {
                type = absence.getCustomType();
            } else {
                type = "Absence";
            }
            sb.append(type).append(" — ").append(periodLabel(absence.getPeriod()));
            first = false;
        }
        return sb.toString();
    }

    private String periodLabel(Absence.AbsencePeriod period) {
        if (period == null) {
            return "Journée complète";
        }
        switch (period) {
            case MORNING:
                return "Demi-journée (matin)";
            case AFTERNOON:
                return "Demi-journée (après-midi)";
            case FULL_DAY:
            default:
                return "Journée complète";
        }
    }

    // ── Jours fériés français ──

    private void addFrenchHolidays(Map<LocalDate, String> holidays, int year) {
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
    }

    // Algorithme de Meeus/Jones/Butcher (Pâques grégorien)
    private LocalDate computeEaster(int year) {
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

    // ── Utilitaires ──

    private String sanitizeSheetName(String name) {
        String sanitized = name.replaceAll("[\\\\/:*?\\[\\]]+", "_");
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        return sanitized;
    }

    private String sanitizeFileNamePart(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String cleaned = normalized.replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return cleaned.isEmpty() ? "salarie" : cleaned;
    }

    private LocalDate findDayForBreak(bzh.stack.apiavtrans.entity.Service breakService,
                                      List<bzh.stack.apiavtrans.entity.Service> workServices) {
        ZonedDateTime breakStart = breakService.getDebut();
        LocalDate breakStartDate = breakStart.withZoneSameInstant(PARIS_ZONE).toLocalDate();

        for (bzh.stack.apiavtrans.entity.Service workService : workServices) {
            if (workService.getFin() != null) {
                if (!breakStart.isBefore(workService.getDebut()) && !breakStart.isAfter(workService.getFin())) {
                    return workService.getDebut().withZoneSameInstant(PARIS_ZONE).toLocalDate();
                }
            }
        }

        return breakStartDate;
    }

    // ── Classes internes ──

    private static class DayData {
        ZonedDateTime workStart;
        ZonedDateTime workEnd;
        List<BreakPeriod> breaks = new ArrayList<>();
        boolean hasIncomplete = false;
        boolean isMultiDayService = false;
        boolean isMultiDayStart = false;
        boolean isMultiDayEnd = false;
        boolean isNightShift = false;
        LocalDate nightShiftEndDate = null;
    }

    private static class BreakPeriod {
        ZonedDateTime start;
        ZonedDateTime end;

        BreakPeriod(ZonedDateTime start, ZonedDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
