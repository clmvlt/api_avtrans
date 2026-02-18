package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.export.ExportRequest;
import bzh.stack.apiavtrans.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Work hours export operations")
public class ExportController {

    private final ExportService exportService;

    @RequireRole("Administrateur")
    @Operation(summary = "[ADMINISTRATEUR] Export worked hours to Excel")
    @ApiResponse(
            responseCode = "200",
            description = "Excel file generated successfully",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    )
    @PostMapping("/hours")
    public ResponseEntity<?> exportWorkedHours(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Export parameters: user UUIDs, start date, and end date",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExportRequest.class))
            )
            @RequestBody ExportRequest request,
            HttpServletRequest httpRequest) {
        try {
            if (request.getUserUuids() == null || request.getUserUuids().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse(false, "User list cannot be empty"));
            }

            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse(false, "Start date and end date are required"));
            }

            if (request.getStartDate().isAfter(request.getEndDate())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse(false, "Start date must be before end date"));
            }

            byte[] excelFile = exportService.exportWorkedHoursToExcel(
                    request.getUserUuids(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            String filename = String.format("Heures_Travail_%s_%s.xlsx",
                    request.getStartDate(),
                    request.getEndDate());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(excelFile);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse(false, "Error generating Excel file: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
