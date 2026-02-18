package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.dto.auth.*;
import bzh.stack.apiavtrans.dto.common.*;
import bzh.stack.apiavtrans.entity.PasswordResetToken;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.service.AuthService;
import bzh.stack.apiavtrans.service.EmailService;
import bzh.stack.apiavtrans.service.EmailVerificationService;
import bzh.stack.apiavtrans.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management API")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final PasswordResetService passwordResetService;

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and sends a verification email. Account remains inactive until email is verified."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Registration failed (email already exists, invalid data, etc.)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration information",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class)
                    )
            )
            @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
            );

            return ResponseEntity.ok(new RegisterResponse(
                true,
                "Utilisateur enregistré avec succès. Veuillez vérifier votre email pour activer votre compte.",
                user.getUuid()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
        summary = "Authenticate user",
        description = "Authenticates user with email and password. Account must be active to log in."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Authentication failed (invalid credentials, inactive account, etc.)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            )
            @RequestBody LoginRequest request) {
        try {
            User user = authService.login(request.getEmail(), request.getPassword());
            AuthUserDTO userDTO = userMapper.toAuthDTO(user);

            return ResponseEntity.ok(new LoginResponse(true, "Connexion réussie", userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
        summary = "Get current user",
        description = "Returns authenticated user information via Bearer token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid token",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @Parameter(description = "Bearer token", required = true)
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            User user = authService.getUserByToken(token);
            AuthUserDTO userDTO = userMapper.toAuthDTO(user);

            return ResponseEntity.ok(new LoginResponse(true, "Utilisateur trouvé", userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
        summary = "Verify email address",
        description = "Verifies user email address using token received via email. Marks email as verified."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EmailVerificationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired token",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/verify")
    public ResponseEntity<?> verify(
        @Parameter(description = "Verification token received via email", required = true)
        @RequestParam String token
    ) {
        try {
            User user = emailVerificationService.validateVerification(token);
            return ResponseEntity.ok(new EmailVerificationResponse(true, "Email vérifié avec succès", user.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
        summary = "Get user status",
        description = "Returns user email verification and activation status. No authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User status retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserStatusResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getUserStatus(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        return authService.getUserById(userId)
                .map(user -> ResponseEntity.ok(new UserStatusResponse(user.getIsMailVerified(), user.getIsActive())))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Request password reset",
        description = "Sends email with password reset link valid for 1 hour."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset email sent successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SuccessMessageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request failed (user not found, etc.)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User email address",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PasswordResetRequestDTO.class)
                    )
            )
            @RequestBody PasswordResetRequestDTO request) {
        try {
            PasswordResetToken token = passwordResetService.createResetToken(request.getEmail());
            emailService.sendPasswordResetEmail(request.getEmail(), token.getToken());

            return ResponseEntity.ok(new SuccessMessageResponse(true, "Email de réinitialisation envoyé"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
        summary = "Confirm password reset",
        description = "Resets password using token received via email."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SuccessMessageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid, expired, or already used token",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reset token and new password",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PasswordResetConfirmDTO.class)
                    )
            )
            @RequestBody PasswordResetConfirmDTO request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Mot de passe réinitialisé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
