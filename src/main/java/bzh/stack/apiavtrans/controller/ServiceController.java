package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.service.*;
import bzh.stack.apiavtrans.dto.common.*;
import bzh.stack.apiavtrans.dto.common.PagedResponse;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.UserRepository;
import bzh.stack.apiavtrans.service.ServiceModificationService;
import bzh.stack.apiavtrans.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Services", description = "Employee services and breaks management")
public class ServiceController {

    private final ServiceService serviceService;
    private final UserRepository userRepository;
    private final ServiceModificationService serviceModificationService;

    @Operation(
            summary = "[UTILISATEUR] Start a service",
            description = "Starts a new service with GPS coordinates. If userUuid is provided, starts service for that user (admin only). " +
                    "Returns the work service (pauses, dureeTravail, enCours, calculeLe)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service started successfully",
            content = @Content(schema = @Schema(implementation = ServiceDTO.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/start")
    public ResponseEntity<?> startService(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "GPS coordinates and optional user UUID (admin only)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ServiceStartRequest.class))
            )
            @RequestBody ServiceStartRequest request,
            HttpServletRequest httpRequest) {
        try {
            User authenticatedUser = (User) httpRequest.getAttribute("user");
            String targetUserEmail;
            boolean isAdminAction = false;

            if (request.getUserUuid() != null) {
                if (!"Administrateur".equals(authenticatedUser.getRole().getNom())) {
                    return ResponseEntity.status(403).body(new ErrorResponse(false, "Seuls les administrateurs peuvent démarrer un service pour un autre utilisateur"));
                }
                User targetUser = userRepository.findById(request.getUserUuid())
                        .orElseThrow(() -> new RuntimeException("User not found with uuid: " + request.getUserUuid()));
                targetUserEmail = targetUser.getEmail();
                isAdminAction = true;
            } else {
                targetUserEmail = authenticatedUser.getEmail();
            }

            ServiceDTO serviceDTO = serviceService.startService(
                    targetUserEmail,
                    request.getLatitude(),
                    request.getLongitude(),
                    isAdminAction
            );

            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] End a service",
            description = "Ends the active service with GPS coordinates. If userUuid is provided, ends service for that user (admin only). " +
                    "Refused while a break is in progress. Returns the closed work service with its pauses."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service ended successfully",
            content = @Content(schema = @Schema(implementation = ServiceDTO.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/end")
    public ResponseEntity<?> endService(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "GPS coordinates and optional user UUID (admin only)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ServiceEndRequest.class))
            )
            @RequestBody ServiceEndRequest request,
            HttpServletRequest httpRequest) {
        try {
            User authenticatedUser = (User) httpRequest.getAttribute("user");
            String targetUserEmail;

            if (request.getUserUuid() != null) {
                if (!"Administrateur".equals(authenticatedUser.getRole().getNom())) {
                    return ResponseEntity.status(403).body(new ErrorResponse(false, "Seuls les administrateurs peuvent terminer un service pour un autre utilisateur"));
                }
                User targetUser = userRepository.findById(request.getUserUuid())
                        .orElseThrow(() -> new RuntimeException("User not found with uuid: " + request.getUserUuid()));
                targetUserEmail = targetUser.getEmail();
            } else {
                targetUserEmail = authenticatedUser.getEmail();
            }

            ServiceDTO serviceDTO = serviceService.endService(
                    targetUserEmail,
                    request.getLatitude(),
                    request.getLongitude()
            );

            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Start a break",
            description = "Starts a break attached to the active service, with GPS coordinates. If userUuid is provided, starts break for that user (admin only). " +
                    "Returns the work service (enPause=true, the running break is in pauses)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Break started successfully",
            content = @Content(schema = @Schema(implementation = ServiceDTO.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/break/start")
    public ResponseEntity<?> startBreak(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "GPS coordinates and optional user UUID (admin only)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BreakStartRequest.class))
            )
            @RequestBody BreakStartRequest request,
            HttpServletRequest httpRequest) {
        try {
            User authenticatedUser = (User) httpRequest.getAttribute("user");
            String targetUserEmail;

            if (request.getUserUuid() != null) {
                if (!"Administrateur".equals(authenticatedUser.getRole().getNom())) {
                    return ResponseEntity.status(403).body(new ErrorResponse(false, "Seuls les administrateurs peuvent démarrer une pause pour un autre utilisateur"));
                }
                User targetUser = userRepository.findById(request.getUserUuid())
                        .orElseThrow(() -> new RuntimeException("User not found with uuid: " + request.getUserUuid()));
                targetUserEmail = targetUser.getEmail();
            } else {
                targetUserEmail = authenticatedUser.getEmail();
            }

            ServiceDTO serviceDTO = serviceService.startBreak(
                    targetUserEmail,
                    request.getLatitude(),
                    request.getLongitude()
            );

            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] End a break",
            description = "Ends the current break and resumes service with GPS coordinates. If userUuid is provided, ends break for that user (admin only). " +
                    "Returns the work service with its pauses."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Break ended successfully",
            content = @Content(schema = @Schema(implementation = ServiceDTO.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/break/end")
    public ResponseEntity<?> endBreak(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "GPS coordinates and optional user UUID (admin only)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BreakEndRequest.class))
            )
            @RequestBody BreakEndRequest request,
            HttpServletRequest httpRequest) {
        try {
            User authenticatedUser = (User) httpRequest.getAttribute("user");
            String targetUserEmail;

            if (request.getUserUuid() != null) {
                if (!"Administrateur".equals(authenticatedUser.getRole().getNom())) {
                    return ResponseEntity.status(403).body(new ErrorResponse(false, "Seuls les administrateurs peuvent terminer une pause pour un autre utilisateur"));
                }
                User targetUser = userRepository.findById(request.getUserUuid())
                        .orElseThrow(() -> new RuntimeException("User not found with uuid: " + request.getUserUuid()));
                targetUserEmail = targetUser.getEmail();
            } else {
                targetUserEmail = authenticatedUser.getEmail();
            }

            ServiceDTO serviceDTO = serviceService.endBreak(
                    targetUserEmail,
                    request.getLatitude(),
                    request.getLongitude()
            );

            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get active service",
            description = "Returns the running work service of the authenticated user (never a break: a running break is in pauses and enPause=true), " +
                    "or null. Live display: add the time elapsed since the response to dureeTravail when enCours && !enPause."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Active service retrieved successfully",
            content = @Content(schema = @Schema(implementation = ActiveServiceResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/active")
    public ResponseEntity<?> getActiveService(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            ServiceDTO serviceDTO = serviceService.getActiveService(user.getEmail());
            return ResponseEntity.ok(new ActiveServiceResponse(true, serviceDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get services history",
            description = "Retrieves paginated services history for the user with optional filters."
    )
    @ApiResponse(
            responseCode = "200",
            description = "History retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
    )
    @RequireRole("Utilisateur")
    @PostMapping("/history")
    public ResponseEntity<?> getServicesHistory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Filters and pagination for history",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ServiceHistoryRequest.class))
            )
            @RequestBody ServiceHistoryRequest request,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");

            int page = request.getPage() != null ? request.getPage() : 0;
            int size = request.getSize() != null ? request.getSize() : 10;
            String sortBy = request.getSortBy() != null ? request.getSortBy() : "debut";
            String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

            java.time.ZonedDateTime startDate = request.getStartDate() != null
                    ? request.getStartDate().atStartOfDay(java.time.ZoneId.of("Europe/Paris"))
                    : null;
            java.time.ZonedDateTime endDate = request.getEndDate() != null
                    ? request.getEndDate().atStartOfDay(java.time.ZoneId.of("Europe/Paris"))
                    : null;

            Page<ServiceDTO> servicePage = serviceService.getUserServicesHistory(
                    user.getEmail(), page, size, request.getIsBreak(),
                    startDate, endDate, sortBy, sortDirection
            );

            PagedResponse<ServiceDTO> response = new PagedResponse<>(
                    true,
                    servicePage.getContent(),
                    servicePage.getNumber(),
                    servicePage.getSize(),
                    servicePage.getTotalElements(),
                    servicePage.getTotalPages(),
                    servicePage.isFirst(),
                    servicePage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get daily services",
            description = "Current workday of the authenticated user: work services attached to one day (their start day, Europe/Paris), " +
                    "each with its pauses, plus the net hours of that day. It is today, or yesterday for a night service still running " +
                    "or ended after midnight, as long as no service started today (aujourdhui=false, date=yesterday)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Workday retrieved successfully",
            content = @Content(schema = @Schema(implementation = JourneeTravailResponse.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/user/daily")
    public ResponseEntity<?> getDailyServices(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            return ResponseEntity.ok(serviceService.getDailyServices(user.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get monthly services",
            description = "Work services started in a given month, each with its pauses. If year and month are not provided, uses current month."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Monthly services retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceDTO.class)))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/month")
    public ResponseEntity<?> getServicesForMonth(
            @Parameter(description = "Year (e.g., 2025). Defaults to current year") @RequestParam(required = false) Integer year,
            @Parameter(description = "Month (1-12). Defaults to current month") @RequestParam(required = false) Integer month,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            List<ServiceDTO> serviceDTOs = serviceService.getServicesForMonth(user.getEmail(), year, month);

            return ResponseEntity.ok(serviceDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get worked hours",
            description = "Net worked hours (service duration - its pauses). A service counts entirely, pauses included, on the period of its start day. " +
                    "Each period gives {secondes, heures, enCours} at calculeLe. If period is not provided, returns all periods (year, month, week, day, lastMonth)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Worked hours calculated successfully",
            content = @Content(schema = @Schema(implementation = WorkedHoursDto.class))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/hours")
    public ResponseEntity<?> getWorkedHours(
            @Parameter(description = "Period: 'day', 'week', 'month', 'year', or 'lastmonth'. If not provided, returns all periods")
            @RequestParam(required = false) String period,
            @Parameter(description = "Year number (e.g., 2025). Defaults to current year")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Month number (1-12). Defaults to current month")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Week number (1-53). Defaults to current week")
            @RequestParam(required = false) Integer week,
            @Parameter(description = "Day number (1-31). Defaults to current day")
            @RequestParam(required = false) Integer day,
            HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");

            return ResponseEntity.ok(serviceService.getWorkedHours(user.getEmail(), period, year, month, week, day));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get user services",
            description = "Retrieves services for a specific user with optional filters. Defaults to last 30 days and all future services."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Services retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
    )
    @RequireRole("Administrateur")
    @PostMapping("/admin/user/{uuid}")
    public ResponseEntity<?> getServicesForUser(
            @Parameter(description = "User UUID") @PathVariable String uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Filters and pagination (optional)",
                    required = false,
                    content = @Content(schema = @Schema(implementation = AdminServiceSearchRequest.class))
            )
            @RequestBody(required = false) AdminServiceSearchRequest request) {
        try {
            if (request == null) {
                request = new AdminServiceSearchRequest();
            }

            int page = request.getPage() != null ? request.getPage() : 0;
            int size = request.getSize() != null ? request.getSize() : 20;
            String sortBy = request.getSortBy() != null ? request.getSortBy() : "debut";
            String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

            java.time.ZonedDateTime startDate = request.getStartDate() != null
                    ? request.getStartDate().atZone(java.time.ZoneId.of("Europe/Paris"))
                    : null;
            java.time.ZonedDateTime endDate = request.getEndDate() != null
                    ? request.getEndDate().atZone(java.time.ZoneId.of("Europe/Paris"))
                    : null;

            Page<ServiceDTO> servicePage = serviceService.getUserServicesHistoryByUuid(
                    java.util.UUID.fromString(uuid), page, size, request.getIsBreak(),
                    startDate, endDate, sortBy, sortDirection
            );

            PagedResponse<ServiceDTO> response = new PagedResponse<>(
                    true,
                    servicePage.getContent(),
                    servicePage.getNumber(),
                    servicePage.getSize(),
                    servicePage.getTotalElements(),
                    servicePage.getTotalPages(),
                    servicePage.isFirst(),
                    servicePage.isLast()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Create service for user",
            description = "Creates a service or a break for a specific user. A break is attached to serviceParentUuid, or else to the user's " +
                    "work service running at the break start (400 if none). The action is recorded in the modifications log."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service created successfully",
            content = @Content(schema = @Schema(implementation = ServiceDTO.class))
    )
    @RequireRole("Administrateur")
    @PostMapping("/admin/create")
    public ResponseEntity<?> createServiceForUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Service data to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ServiceCreateRequest.class))
            )
            @RequestBody ServiceCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User admin = (User) httpRequest.getAttribute("user");
            ServiceDTO serviceDTO = serviceService.createServiceForUser(
                    request.getUserUuid(),
                    request.getDebut(),
                    request.getFin(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getLatitudeEnd(),
                    request.getLongitudeEnd(),
                    request.getIsBreak(),
                    request.getServiceParentUuid(),
                    admin
            );

            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Update service",
            description = "Updates an existing service. If debut, fin or isBreak change, the service is flagged as modified " +
                    "(modifiedAt, modifiedByUuid, modifiedByName), the old and new values are recorded in the modifications log " +
                    "and other admins are notified. A break that no longer starts within its service is re-attached to the service " +
                    "running at its start (or to serviceParentUuid)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service updated successfully",
            content = @Content(schema = @Schema(implementation = ServiceDTO.class))
    )
    @RequireRole("Administrateur")
    @PutMapping("/admin/{uuid}")
    public ResponseEntity<?> updateService(
            @Parameter(description = "Service UUID to update") @PathVariable String uuid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Service data to update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ServiceUpdateRequest.class))
            )
            @RequestBody ServiceUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            User admin = (User) httpRequest.getAttribute("user");
            ServiceDTO serviceDTO = serviceService.updateService(
                    java.util.UUID.fromString(uuid),
                    request.getDebut(),
                    request.getFin(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getLatitudeEnd(),
                    request.getLongitudeEnd(),
                    request.getIsBreak(),
                    request.getServiceParentUuid(),
                    admin
            );

            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Delete service",
            description = "Permanently deletes a service, together with its pauses for a work service. The deleted values are kept in the modifications log."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Service deleted successfully"
    )
    @RequireRole("Administrateur")
    @DeleteMapping("/admin/{uuid}")
    public ResponseEntity<?> deleteService(
            @Parameter(description = "Service UUID to delete") @PathVariable String uuid,
            HttpServletRequest httpRequest) {
        try {
            User admin = (User) httpRequest.getAttribute("user");
            serviceService.deleteService(java.util.UUID.fromString(uuid), admin);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get user worked hours",
            description = "Net worked hours for a specific user, same rules as /services/hours. If period is not provided, returns all periods (year, month, week, day, lastMonth)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Worked hours calculated successfully",
            content = @Content(schema = @Schema(implementation = WorkedHoursDto.class))
    )
    @RequireRole("Administrateur")
    @GetMapping("/admin/hours/{uuid}")
    public ResponseEntity<?> getWorkedHoursForUser(
            @Parameter(description = "User UUID") @PathVariable String uuid,
            @Parameter(description = "Period: 'day', 'week', 'month', 'year', or 'lastmonth'. If not provided, returns all periods")
            @RequestParam(required = false) String period,
            @Parameter(description = "Year number (e.g., 2025). Defaults to current year")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Month number (1-12). Defaults to current month")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Week number (1-53). Defaults to current week")
            @RequestParam(required = false) Integer week,
            @Parameter(description = "Day number (1-31). Defaults to current day")
            @RequestParam(required = false) Integer day) {
        try {
            return ResponseEntity.ok(serviceService.getWorkedHoursByUuid(
                    java.util.UUID.fromString(uuid), period, year, month, week, day));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get user active service",
            description = "Returns the running work service of a specific user (with its pauses, enPause, dureeTravail), or null."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Active service retrieved successfully",
            content = @Content(schema = @Schema(implementation = ActiveServiceResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "User not found")
    @RequireRole("Administrateur")
    @GetMapping("/admin/{uuid}/active")
    public ResponseEntity<?> getActiveServiceForUser(
            @Parameter(description = "User UUID") @PathVariable String uuid) {
        try {
            ServiceDTO serviceDTO = serviceService.getActiveServiceByUuid(java.util.UUID.fromString(uuid));
            return ResponseEntity.ok(new ActiveServiceResponse(true, serviceDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get service modifications history",
            description = "Returns every admin action (CREATE, UPDATE, DELETE) recorded on a service, most recent first. " +
                    "Works even if the service has been deleted."
    )
    @ApiResponse(
            responseCode = "200",
            description = "History retrieved successfully",
            content = @Content(schema = @Schema(implementation = ServiceModificationListResponse.class))
    )
    @RequireRole("Administrateur")
    @GetMapping("/admin/{uuid}/modifications")
    public ResponseEntity<?> getServiceModifications(
            @Parameter(description = "Service UUID") @PathVariable String uuid) {
        try {
            return ResponseEntity.ok(serviceModificationService.getModificationsForService(java.util.UUID.fromString(uuid)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Search services modifications log",
            description = "Paginated log of admin actions on services, most recent first. " +
                    "Optional filters: userUuid (employee), modifiedByUuid (admin), action (CREATE, UPDATE, DELETE), startDate/endDate (date of the action)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Log retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
    )
    @RequireRole("Administrateur")
    @PostMapping("/admin/modifications")
    public ResponseEntity<?> searchServiceModifications(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Filters and pagination (optional)",
                    required = false,
                    content = @Content(schema = @Schema(implementation = ServiceModificationSearchRequest.class))
            )
            @RequestBody(required = false) ServiceModificationSearchRequest request) {
        try {
            return ResponseEntity.ok(serviceModificationService.searchModifications(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
