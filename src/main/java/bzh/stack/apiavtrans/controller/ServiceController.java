package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.service.*;
import bzh.stack.apiavtrans.dto.common.*;
import bzh.stack.apiavtrans.dto.common.PagedResponse;
import bzh.stack.apiavtrans.entity.Service;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.ServiceMapper;
import bzh.stack.apiavtrans.repository.UserRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Services", description = "Employee services and breaks management")
public class ServiceController {

    private final ServiceService serviceService;
    private final ServiceMapper serviceMapper;
    private final UserRepository userRepository;

    @Operation(
            summary = "[UTILISATEUR] Start a service",
            description = "Starts a new service with GPS coordinates. If userUuid is provided, starts service for that user (admin only)."
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

            Service service = serviceService.startService(
                    targetUserEmail,
                    request.getLatitude(),
                    request.getLongitude(),
                    isAdminAction
            );

            ServiceDTO serviceDTO = serviceMapper.toDTO(service);
            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] End a service",
            description = "Ends the active service with GPS coordinates. If userUuid is provided, ends service for that user (admin only)."
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

            Service service = serviceService.endService(
                    targetUserEmail,
                    request.getLatitude(),
                    request.getLongitude()
            );

            ServiceDTO serviceDTO = serviceMapper.toDTO(service);
            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Start a break",
            description = "Starts a break during active service with GPS coordinates. If userUuid is provided, starts break for that user (admin only)."
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

            Service service = serviceService.startBreak(
                    targetUserEmail,
                    request.getLatitude(),
                    request.getLongitude()
            );

            ServiceDTO serviceDTO = serviceMapper.toDTO(service);
            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] End a break",
            description = "Ends the current break and resumes service with GPS coordinates. If userUuid is provided, ends break for that user (admin only)."
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

            Service service = serviceService.endBreak(
                    targetUserEmail,
                    request.getLatitude(),
                    request.getLongitude()
            );

            ServiceDTO serviceDTO = serviceMapper.toDTO(service);
            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get active service",
            description = "Returns the currently active service for the authenticated user, or null if no service is active."
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
            Service service = serviceService.getActiveService(user.getEmail());

            ServiceDTO serviceDTO = service != null ? serviceMapper.toDTO(service) : null;
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

            Page<Service> servicePage = serviceService.getUserServicesHistory(
                    user.getEmail(), page, size, request.getIsBreak(),
                    startDate, endDate, sortBy, sortDirection
            );

            List<ServiceDTO> serviceDTOs = servicePage.getContent().stream()
                    .map(serviceMapper::toDTO)
                    .collect(Collectors.toList());

            PagedResponse<ServiceDTO> response = new PagedResponse<>(
                    true,
                    serviceDTOs,
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
            description = "Retrieves all services for the current day (from 00:00 to 23:59) for the authenticated user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Daily services retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceDTO.class)))
    )
    @RequireRole("Utilisateur")
    @GetMapping("/user/daily")
    public ResponseEntity<?> getDailyServices(HttpServletRequest httpRequest) {
        try {
            User user = (User) httpRequest.getAttribute("user");
            List<Service> services = serviceService.getDailyServices(user.getEmail());
            List<ServiceDTO> serviceDTOs = services.stream()
                    .map(serviceMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(serviceDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get monthly services",
            description = "Retrieves all services for a given month. If year and month are not provided, uses current month."
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
            List<Service> services = serviceService.getServicesForMonth(user.getEmail(), year, month);
            List<ServiceDTO> serviceDTOs = services.stream()
                    .map(serviceMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(serviceDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[UTILISATEUR] Get worked hours",
            description = "Calculates worked hours (service duration - break duration). If period is not provided, returns all periods (year, month, week, day, lastMonth)."
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

            WorkedHoursDto response = new WorkedHoursDto();

            if (period != null && !period.isEmpty()) {
                double hours = serviceService.calculateWorkedHours(user.getEmail(), period, year, month, week, day);
                switch (period) {
                    case "year" -> response.setYear(hours);
                    case "month" -> response.setMonth(hours);
                    case "week" -> response.setWeek(hours);
                    case "day" -> response.setDay(hours);
                    case "lastmonth" -> response.setLastMonth(hours);
                }
            } else {
                response.setYear(serviceService.calculateWorkedHours(user.getEmail(), "year", year, null, null, null));
                response.setMonth(serviceService.calculateWorkedHours(user.getEmail(), "month", year, month, null, null));
                response.setWeek(serviceService.calculateWorkedHours(user.getEmail(), "week", year, null, week, null));
                response.setDay(serviceService.calculateWorkedHours(user.getEmail(), "day", year, month, null, day));
                response.setLastMonth(serviceService.calculateWorkedHours(user.getEmail(), "lastmonth", null, null, null, null));
            }

            return ResponseEntity.ok(response);
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

            Page<Service> servicePage = serviceService.getUserServicesHistoryByUuid(
                    java.util.UUID.fromString(uuid), page, size, request.getIsBreak(),
                    startDate, endDate, sortBy, sortDirection
            );

            List<ServiceDTO> serviceDTOs = servicePage.getContent().stream()
                    .map(serviceMapper::toDTO)
                    .collect(Collectors.toList());

            PagedResponse<ServiceDTO> response = new PagedResponse<>(
                    true,
                    serviceDTOs,
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
            description = "Creates a service for a specific user."
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
            @RequestBody ServiceCreateRequest request) {
        try {
            Service service = serviceService.createServiceForUser(
                    request.getUserUuid(),
                    request.getDebut(),
                    request.getFin(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getLatitudeEnd(),
                    request.getLongitudeEnd(),
                    request.getIsBreak()
            );

            ServiceDTO serviceDTO = serviceMapper.toDTO(service);
            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Update service",
            description = "Updates an existing service."
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
            @RequestBody ServiceUpdateRequest request) {
        try {
            Service service = serviceService.updateService(
                    java.util.UUID.fromString(uuid),
                    request.getDebut(),
                    request.getFin(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getLatitudeEnd(),
                    request.getLongitudeEnd(),
                    request.getIsBreak()
            );

            ServiceDTO serviceDTO = serviceMapper.toDTO(service);
            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Delete service",
            description = "Permanently deletes a service."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Service deleted successfully"
    )
    @RequireRole("Administrateur")
    @DeleteMapping("/admin/{uuid}")
    public ResponseEntity<?> deleteService(
            @Parameter(description = "Service UUID to delete") @PathVariable String uuid) {
        try {
            serviceService.deleteService(java.util.UUID.fromString(uuid));
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get user worked hours",
            description = "Calculates worked hours for a specific user. If period is not provided, returns all periods (year, month, week, day, lastMonth)."
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
            WorkedHoursDto response = new WorkedHoursDto();

            if (period != null && !period.isEmpty()) {
                double hours = serviceService.calculateWorkedHoursByUuid(java.util.UUID.fromString(uuid), period, year, month, week, day);
                switch (period) {
                    case "year" -> response.setYear(hours);
                    case "month" -> response.setMonth(hours);
                    case "week" -> response.setWeek(hours);
                    case "day" -> response.setDay(hours);
                    case "lastmonth" -> response.setLastMonth(hours);
                }
            } else {
                response.setYear(serviceService.calculateWorkedHoursByUuid(java.util.UUID.fromString(uuid), "year", year, null, null, null));
                response.setMonth(serviceService.calculateWorkedHoursByUuid(java.util.UUID.fromString(uuid), "month", year, month, null, null));
                response.setWeek(serviceService.calculateWorkedHoursByUuid(java.util.UUID.fromString(uuid), "week", year, null, week, null));
                response.setDay(serviceService.calculateWorkedHoursByUuid(java.util.UUID.fromString(uuid), "day", year, month, null, day));
                response.setLastMonth(serviceService.calculateWorkedHoursByUuid(java.util.UUID.fromString(uuid), "lastmonth", null, null, null, null));
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
            summary = "[ADMINISTRATEUR] Get user active service",
            description = "Returns the currently active service for a specific user, or null if no service is active."
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
            Service service = serviceService.getActiveServiceByUuid(java.util.UUID.fromString(uuid));
            ServiceDTO serviceDTO = service != null ? serviceMapper.toDTO(service) : null;
            return ResponseEntity.ok(new ActiveServiceResponse(true, serviceDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
