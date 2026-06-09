package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.entity.Signature;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.ServiceRepository;
import bzh.stack.apiavtrans.repository.SignatureRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");

    private static final String[] COLUMN_HEADERS = {
            "Date", "D\u00e9but journ\u00e9e", "D\u00e9but pause", "Fin pause",
            "Fin journ\u00e9e", "Heures travaill\u00e9es", "Informations compl\u00e9mentaires", "Autres pauses"
    };

    public byte[] exportWorkedHoursToExcel(List<UUID> userUuids, LocalDate startDate, LocalDate endDate) throws IOException {
        List<User> users = userRepository.findAllById(userUuids);

        if (users.isEmpty()) {
            throw new RuntimeException("Aucun utilisateur trouv\u00e9");
        }

        XSSFWorkbook workbook = new XSSFWorkbook();

        for (User user : users) {
            createUserSheet(workbook, user, startDate, endDate);
        }

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        workbook.setForceFormulaRecalculation(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private void createUserSheet(Workbook workbook, User user, LocalDate startDate, LocalDate endDate) {
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
        CellStyle monthHeaderStyle = createMonthHeaderStyle(workbook);
        CellStyle signatureBarStyle = createSignatureBarStyle(workbook);
        CellStyle signatureLabelStyle = createSignatureLabelStyle(workbook);
        CellStyle totalLabelStyle = createTotalLabelStyle(workbook);
        CellStyle totalValueStyle = createTotalValueStyle(workbook);

        int rowNum = 0;
        DateTimeFormatter periodFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // ── Titre + sous-titre ──
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(user.getFirstName() + " " + user.getLastName());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("P\u00e9riode du " + startDate.format(periodFmt) + " au " + endDate.format(periodFmt));
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

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

        // ── Rendu Excel ──
        LocalDate currentDate = startDate;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        YearMonth currentMonth = YearMonth.from(startDate);
        int monthStartExcelRow = -1;

        // Premier mois : en-t\u00eate + colonnes
        rowNum = writeSectionHeader(sheet, rowNum, "Heures \u2014 ", currentMonth, monthHeaderStyle);
        rowNum = writeColumnHeaders(sheet, rowNum, columnHeaderStyle);

        while (!currentDate.isAfter(endDate)) {
            YearMonth dayMonth = YearMonth.from(currentDate);

            if (!dayMonth.equals(currentMonth)) {
                // Cl\u00f4ture du mois pr\u00e9c\u00e9dent : total + signature
                rowNum = writeTotalRow(sheet, rowNum, monthStartExcelRow, totalLabelStyle, totalValueStyle);
                rowNum = writeSignatureSection(sheet, workbook, user, currentMonth, rowNum,
                        signatureBarStyle, signatureLabelStyle, dataStyle);
                currentMonth = dayMonth;
                monthStartExcelRow = -1;
                // Nouveau mois
                rowNum = writeSectionHeader(sheet, rowNum, "Heures \u2014 ", currentMonth, monthHeaderStyle);
                rowNum = writeColumnHeaders(sheet, rowNum, columnHeaderStyle);
            }

            boolean isWeekend = currentDate.getDayOfWeek() == DayOfWeek.SATURDAY
                             || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY;

            Row row = sheet.createRow(rowNum++);
            if (monthStartExcelRow == -1) {
                monthStartExcelRow = rowNum; // Excel row = rowNum apr\u00e8s post-incr\u00e9ment
            }

            DayData dayData = dayDataMap.get(currentDate);

            CellStyle currentDataStyle = isWeekend ? weekendDataStyle : dataStyle;
            CellStyle currentExcelTime = isWeekend ? weekendExcelTimeStyle : excelTimeStyle;
            CellStyle currentNumber = isWeekend ? weekendNumberStyle : numberStyle;

            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(currentDate.format(dateFormatter));
            dateCell.setCellStyle(currentDataStyle);

            if (dayData == null) {
                for (int i = 1; i < 8; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue("");
                    cell.setCellStyle(currentDataStyle);
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
                boolean isMultiDay = dayData.isMultiDayStart || dayData.isMultiDayEnd;
                CellStyle cellStyleToUse = isMultiDay ? greyedStyle : (isWeekend ? weekendDataStyle : timeStyle);
                CellStyle dataCellStyleToUse = isMultiDay ? greyedStyle : currentDataStyle;
                CellStyle excelTimeStyleToUse = isMultiDay ? excelGreyedTimeStyle : currentExcelTime;
                CellStyle numberStyleToUse = isMultiDay ? greyedNumberStyle : currentNumber;

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
                    String formula = buildHoursFormula(rowNum, dayData, longestBreak);
                    hoursCell.setCellFormula(formula);
                }
                hoursCell.setCellStyle(numberStyleToUse);

                Cell infoCell = row.createCell(6);
                if (dayData.isMultiDayStart || dayData.isMultiDayEnd) {
                    infoCell.setCellValue("Service s'\u00e9tend sur plusieurs jours");
                } else if (dayData.isNightShift && dayData.nightShiftEndDate != null) {
                    String endDateStr = dayData.nightShiftEndDate.format(dateFormatter);
                    infoCell.setCellValue("Fin de service le " + endDateStr);
                } else if (dayData.hasIncomplete) {
                    infoCell.setCellValue("Pointage pr\u00e9sent mais incomplet.");
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

        // Cl\u00f4ture du dernier mois
        rowNum = writeTotalRow(sheet, rowNum, monthStartExcelRow, totalLabelStyle, totalValueStyle);
        rowNum = writeSignatureSection(sheet, workbook, user, currentMonth, rowNum,
                signatureBarStyle, signatureLabelStyle, dataStyle);

        // Auto-size
        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
        }
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
        for (int i = 1; i < 8; i++) {
            row.createCell(i).setCellStyle(style);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 7));
        return rowNum + 1;
    }

    private int writeTotalRow(Sheet sheet, int rowNum, int monthStartExcelRow,
                              CellStyle labelStyle, CellStyle valueStyle) {
        int lastDataExcelRow = rowNum;
        Row row = sheet.createRow(rowNum);

        Cell label = row.createCell(0);
        label.setCellValue("Total");
        label.setCellStyle(labelStyle);
        for (int i = 1; i < 5; i++) {
            row.createCell(i).setCellStyle(labelStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 4));

        Cell value = row.createCell(5);
        if (monthStartExcelRow > 0) {
            value.setCellFormula("SUM(F" + monthStartExcelRow + ":F" + lastDataExcelRow + ")");
        }
        value.setCellStyle(valueStyle);

        for (int i = 6; i < 8; i++) {
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

        rowNum = writeSectionHeader(sheet, rowNum, "Signature \u2014 ", month, barStyle);

        if (!signatures.isEmpty()) {
            Signature sig = signatures.get(0);
            DateTimeFormatter sigFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy '\u00e0' HH:mm");
            String signedDate = sig.getDate().withZoneSameInstant(PARIS_ZONE).format(sigFmt);
            String signedBy = user.getFirstName() + " " + user.getLastName();

            Row infoRow = sheet.createRow(rowNum);

            Cell signerLabel = infoRow.createCell(0);
            signerLabel.setCellValue("Sign\u00e9 par :");
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
            statusValue.setCellValue("Sign\u00e9");
            statusValue.setCellStyle(dataStyle);

            rowNum++;

            // Image coll\u00e9e \u00e0 droite (colonnes F-H)
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
                anchor.setCol1(5);
                anchor.setRow1(rowNum);
                anchor.setCol2(8);
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
            notSignedCell.setCellValue("Non sign\u00e9 pour ce mois");
            notSignedCell.setCellStyle(dataStyle);
            for (int i = 1; i < 8; i++) {
                notSignedRow.createCell(i).setCellStyle(dataStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 7));
            rowNum++;
        }

        return rowNum;
    }

    // ── Formules ──

    private String buildHoursFormula(int excelRow, DayData dayData, BreakPeriod longestBreak) {
        String formula = "ROUND(((E" + excelRow + "-B" + excelRow + ")";

        if (longestBreak != null) {
            formula += "-(D" + excelRow + "-C" + excelRow + ")";
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

    // ── Utilitaires ──

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
