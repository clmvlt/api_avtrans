package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.absence.*;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.AbsenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/absences")
@RequiredArgsConstructor
@Tag(name = "Absences", description = "Absence request management")
public class AbsenceController {

        private final AbsenceService absenceService;

        @RequireRole("Utilisateur")
        @Operation(summary = "[UTILISATEUR] Create an absence request")
        @ApiResponse(responseCode = "200", description = "Absence request created successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceResponse.class)))
        @PostMapping
        public ResponseEntity<?> createAbsence(
                        @Valid @RequestBody AbsenceCreateRequest request,
                        HttpServletRequest httpRequest) {
                try {
                        User user = (User) httpRequest.getAttribute("user");
                        AbsenceResponse response = absenceService.createAbsence(user.getEmail(), request);

                        if (!response.isSuccess()) {
                                return ResponseEntity.badRequest()
                                                .body(new ErrorResponse(false, response.getMessage()));
                        }

                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Utilisateur")
        @Operation(summary = "[UTILISATEUR] Get my absence requests")
        @ApiResponse(responseCode = "200", description = "Absence list retrieved successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceListResponse.class)))
        @PostMapping("/my")
        public ResponseEntity<?> getMyAbsences(
                        @RequestBody(required = false) AbsenceSearchRequest request,
                        HttpServletRequest httpRequest) {
                try {
                        User user = (User) httpRequest.getAttribute("user");
                        if (request == null) {
                                request = new AbsenceSearchRequest();
                        }
                        AbsenceListResponse response = absenceService.getMyAbsences(user.getEmail(), request);
                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Utilisateur")
        @Operation(summary = "[UTILISATEUR] Cancel an absence request")
        @ApiResponse(responseCode = "200", description = "Absence request cancelled successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceResponse.class)))
        @ApiResponse(responseCode = "404", description = "Absence request not found")
        @DeleteMapping("/{uuid}")
        public ResponseEntity<?> cancelAbsence(
                        @Parameter(description = "Absence UUID") @PathVariable UUID uuid,
                        HttpServletRequest httpRequest) {
                try {
                        User user = (User) httpRequest.getAttribute("user");
                        AbsenceResponse response = absenceService.cancelAbsence(uuid, user.getEmail());

                        if (!response.isSuccess()) {
                                return ResponseEntity.badRequest()
                                                .body(new ErrorResponse(false, response.getMessage()));
                        }

                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Administrateur")
        @Operation(summary = "[ADMINISTRATEUR] Search and filter absence requests")
        @ApiResponse(responseCode = "200", description = "Absence list retrieved successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceListResponse.class)))
        @PostMapping("/admin/search")
        public ResponseEntity<?> searchAbsences(
                        @RequestBody(required = false) AbsenceSearchRequest request) {
                try {
                        if (request == null) {
                                request = new AbsenceSearchRequest();
                        }
                        AbsenceListResponse response = absenceService.getAllAbsences(request);
                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Administrateur")
        @Operation(summary = "[ADMINISTRATEUR] Delete an absence request")
        @ApiResponse(responseCode = "200", description = "Absence request deleted successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceResponse.class)))
        @ApiResponse(responseCode = "404", description = "Absence request not found")
        @DeleteMapping("/admin/{uuid}")
        public ResponseEntity<?> deleteAbsenceByAdmin(
                        @Parameter(description = "Absence UUID") @PathVariable UUID uuid) {
                try {
                        AbsenceResponse response = absenceService.deleteAbsenceByAdmin(uuid);
                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Administrateur")
        @Operation(
                summary = "[ADMINISTRATEUR] Update an absence request",
                description = "Permet de modifier une absence uniquement si elle n'est pas approuvée (PENDING ou REJECTED). Si l'absence était REJECTED, elle repasse en PENDING après modification."
        )
        @ApiResponse(responseCode = "200", description = "Absence request updated successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceResponse.class)))
        @ApiResponse(responseCode = "400", description = "Absence already approved or invalid data")
        @ApiResponse(responseCode = "404", description = "Absence request not found")
        @PutMapping("/admin/{uuid}")
        public ResponseEntity<?> updateAbsenceByAdmin(
                        @Parameter(description = "Absence UUID") @PathVariable UUID uuid,
                        @Valid @RequestBody AdminAbsenceUpdateRequest request) {
                try {
                        AbsenceResponse response = absenceService.updateAbsenceByAdmin(uuid, request);

                        if (!response.isSuccess()) {
                                return ResponseEntity.badRequest()
                                                .body(new ErrorResponse(false, response.getMessage()));
                        }

                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Administrateur")
        @Operation(summary = "[ADMINISTRATEUR] Approve or reject an absence request")
        @ApiResponse(responseCode = "200", description = "Absence request validated successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceResponse.class)))
        @ApiResponse(responseCode = "404", description = "Absence request not found")
        @PostMapping("/admin/{uuid}/validate")
        public ResponseEntity<?> validateAbsence(
                        @Parameter(description = "Absence UUID") @PathVariable UUID uuid,
                        @Valid @RequestBody AbsenceValidationRequest request,
                        HttpServletRequest httpRequest) {
                try {
                        User admin = (User) httpRequest.getAttribute("user");
                        AbsenceResponse response = absenceService.validateAbsence(uuid, admin.getEmail(), request);

                        if (!response.isSuccess()) {
                                return ResponseEntity.badRequest()
                                                .body(new ErrorResponse(false, response.getMessage()));
                        }

                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Administrateur")
        @Operation(summary = "[ADMINISTRATEUR] Get absence request by UUID")
        @ApiResponse(responseCode = "200", description = "Absence request retrieved successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceResponse.class)))
        @ApiResponse(responseCode = "404", description = "Absence request not found")
        @GetMapping("/admin/{uuid}")
        public ResponseEntity<?> getAbsenceById(
                        @Parameter(description = "Absence UUID") @PathVariable UUID uuid) {
                try {
                        AbsenceResponse response = absenceService.getAbsenceById(uuid);
                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Administrateur")
        @Operation(summary = "[ADMINISTRATEUR] Get absence planning schedule")
        @ApiResponse(responseCode = "200", description = "Planning schedule retrieved successfully",
                        content = @Content(schema = @Schema(implementation = PlanningResponse.class)))
        @GetMapping("/admin/planning")
        public ResponseEntity<?> getPlanning(
                        @Parameter(description = "Period type: 'week' or 'month'") @RequestParam(defaultValue = "month") String periodType,
                        @Parameter(description = "Year (e.g., 2025)") @RequestParam(required = false) Integer year,
                        @Parameter(description = "Month (1-12), used when periodType='month'") @RequestParam(required = false) Integer month,
                        @Parameter(description = "Week number (1-53), used when periodType='week'") @RequestParam(required = false) Integer week) {
                try {
                        PlanningResponse response = absenceService.getPlanning(periodType, year, month, week);
                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }

        @RequireRole("Administrateur")
        @Operation(summary = "[ADMINISTRATEUR] Create absence request for a user")
        @ApiResponse(responseCode = "200", description = "Absence request created successfully",
                        content = @Content(schema = @Schema(implementation = AbsenceResponse.class)))
        @PostMapping("/admin/create")
        public ResponseEntity<?> createAbsenceByAdmin(
                        @Valid @RequestBody AdminAbsenceCreateRequest request,
                        HttpServletRequest httpRequest) {
                try {
                        User admin = (User) httpRequest.getAttribute("user");
                        AbsenceResponse response = absenceService.createAbsenceByAdmin(request, admin.getEmail());

                        if (!response.isSuccess()) {
                                return ResponseEntity.badRequest()
                                                .body(new ErrorResponse(false, response.getMessage()));
                        }

                        return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
                }
        }
}
