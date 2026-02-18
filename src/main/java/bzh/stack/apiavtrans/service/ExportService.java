package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ExportService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");

    public byte[] exportWorkedHoursToExcel(List<UUID> userUuids, LocalDate startDate, LocalDate endDate) throws IOException {
        List<User> users = userRepository.findAllById(userUuids);

        if (users.isEmpty()) {
            throw new RuntimeException("Aucun utilisateur trouvé");
        }

        XSSFWorkbook workbook = new XSSFWorkbook();

        for (User user : users) {
            createUserSheet(workbook, user, startDate, endDate);
        }

        // Évaluer toutes les formules pour qu'elles s'affichent immédiatement
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

        // Forcer le recalcul des formules à l'ouverture
        workbook.setForceFormulaRecalculation(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private void createUserSheet(Workbook workbook, User user, LocalDate startDate, LocalDate endDate) {
        String sheetName = sanitizeSheetName(user.getFirstName() + "_" + user.getLastName());
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle timeStyle = createTimeStyle(workbook);
        CellStyle greyedStyle = createGreyedStyle(workbook);
        CellStyle excelTimeStyle = createExcelTimeStyle(workbook);
        CellStyle excelGreyedTimeStyle = createExcelGreyedTimeStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle greyedNumberStyle = createGreyedNumberStyle(workbook);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Heures de " + user.getFirstName() + " " + user.getLastName());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Date", "Début journée", "Début pause", "Fin pause", "Fin journée", "Heures travaillées", "Informations complémentaires", "Autres pauses"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

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

        LocalDate currentDate = startDate;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        while (!currentDate.isAfter(endDate)) {
            Row row = sheet.createRow(rowNum++);
            DayData dayData = dayDataMap.get(currentDate);

            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(currentDate.format(dateFormatter));
            dateCell.setCellStyle(dataStyle);

            if (dayData == null) {
                for (int i = 1; i < 8; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue("");
                    cell.setCellStyle(dataStyle);
                }
            } else if (dayData.isMultiDayService) {
                for (int i = 1; i <= 7; i++) {
                    Cell cell = row.createCell(i);
                    if (i == 6) {
                        cell.setCellValue("Service en cours sur plusieurs jours");
                    } else {
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(greyedStyle);
                }
            } else {
                CellStyle cellStyleToUse = (dayData.isMultiDayStart || dayData.isMultiDayEnd) ? greyedStyle : timeStyle;
                CellStyle dataCellStyleToUse = (dayData.isMultiDayStart || dayData.isMultiDayEnd) ? greyedStyle : dataStyle;
                CellStyle excelTimeStyleToUse = (dayData.isMultiDayStart || dayData.isMultiDayEnd) ? excelGreyedTimeStyle : excelTimeStyle;
                CellStyle numberStyleToUse = (dayData.isMultiDayStart || dayData.isMultiDayEnd) ? greyedNumberStyle : numberStyle;

                Cell startCell = row.createCell(1);
                if (dayData.workStart != null) {
                    startCell.setCellValue(java.util.Date.from(dayData.workStart.toInstant()));
                    startCell.setCellStyle(excelTimeStyleToUse);
                } else {
                    startCell.setCellValue("");
                    startCell.setCellStyle(cellStyleToUse);
                }

                Cell breakStartCell = row.createCell(2);
                Cell breakEndCell = row.createCell(3);

                BreakPeriod longestBreak = null;
                if (!dayData.breaks.isEmpty()) {
                    // Trouver la pause la plus longue
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

                Cell endCell = row.createCell(4);
                if (dayData.workEnd != null) {
                    endCell.setCellValue(java.util.Date.from(dayData.workEnd.toInstant()));
                    endCell.setCellStyle(excelTimeStyleToUse);
                } else {
                    endCell.setCellValue("");
                    endCell.setCellStyle(cellStyleToUse);
                }

                Cell hoursCell = row.createCell(5);
                if (dayData.workStart != null && dayData.workEnd != null) {
                    // Formule Excel: ROUND((Fin - Début - (Fin pause - Début pause) - autres pauses) * 24, 2)
                    String formula = buildHoursFormula(rowNum, dayData, longestBreak);
                    hoursCell.setCellFormula(formula);
                }
                hoursCell.setCellStyle(numberStyleToUse);

                Cell infoCell = row.createCell(6);
                if (dayData.isMultiDayStart || dayData.isMultiDayEnd) {
                    infoCell.setCellValue("Service s'étend sur plusieurs jours");
                } else if (dayData.isNightShift && dayData.nightShiftEndDate != null) {
                    String endDateStr = dayData.nightShiftEndDate.format(dateFormatter);
                    infoCell.setCellValue("Fin de service le " + endDateStr);
                } else if (dayData.hasIncomplete) {
                    infoCell.setCellValue("Pointage présent mais incomplet.");
                }
                infoCell.setCellStyle(dataCellStyleToUse);

                Cell otherBreaksCell = row.createCell(7);
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
            }

            currentDate = currentDate.plusDays(1);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
        }
    }

    private String buildHoursFormula(int excelRow, DayData dayData, BreakPeriod longestBreak) {
        // Colonnes: B=Début, C=Début pause, D=Fin pause, E=Fin
        String formula = "ROUND(((E" + excelRow + "-B" + excelRow + ")";

        // Soustraire la pause principale si elle existe
        if (longestBreak != null) {
            formula += "-(D" + excelRow + "-C" + excelRow + ")";
        }

        // Soustraire les autres pauses (calculées en minutes puis converties en jours)
        if (dayData.breaks.size() > 1) {
            long otherBreaksMinutes = 0;

            for (BreakPeriod breakPeriod : dayData.breaks) {
                if (breakPeriod != longestBreak) {
                    long duration = ChronoUnit.MINUTES.between(breakPeriod.start, breakPeriod.end);
                    otherBreaksMinutes += duration;
                }
            }

            if (otherBreaksMinutes > 0) {
                // Convertir les minutes en fraction de jour (minutes / (24*60))
                double otherBreaksDays = otherBreaksMinutes / 1440.0;
                formula += "-" + String.format(Locale.US, "%.10f", otherBreaksDays);
            }
        }

        formula += ")*24,2)";
        return formula;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
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
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
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

    private String sanitizeSheetName(String name) {
        String sanitized = name.replaceAll("[\\\\/:*?\\[\\]]+", "_");
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        return sanitized;
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
