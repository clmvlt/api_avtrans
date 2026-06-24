package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.dto.auth.*;
import bzh.stack.apiavtrans.dto.common.*;
import bzh.stack.apiavtrans.entity.PasswordResetToken;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.service.AuthService;
import bzh.stack.apiavtrans.service.GoogleLoginResult;
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
        summary = "Authenticate via Google",
        description = "Tente de connecter l'utilisateur à partir d'un ID token Google (Sign in with Google). " +
                "Retourne status=AUTHENTICATED (avec 'user' et son token) si le compte existe et est actif, " +
                "ou status=NEEDS_REGISTRATION (avec 'googleProfile') si aucun compte n'existe — le frontend " +
                "doit alors afficher une page de création pré-remplie et appeler POST /auth/google/register."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "AUTHENTICATED (connecté) ou NEEDS_REGISTRATION (création nécessaire)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GoogleAuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Authentication failed (token invalide, email non vérifié, compte non activé, etc.)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/google")
    public ResponseEntity<?> google(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Google ID token",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GoogleAuthRequest.class)
                    )
            )
            @RequestBody GoogleAuthRequest request) {
        try {
            GoogleLoginResult result = authService.googleLogin(request.getIdToken());

            if (result.authenticated()) {
                AuthUserDTO userDTO = userMapper.toAuthDTO(result.user());
                return ResponseEntity.ok(new GoogleAuthResponse(
                        true, "AUTHENTICATED", "Connexion réussie", userDTO, null));
            }

            return ResponseEntity.ok(new GoogleAuthResponse(
                    true, "NEEDS_REGISTRATION",
                    "Aucun compte n'existe pour cet email. Veuillez compléter votre inscription.",
                    null, result.profile()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(
        summary = "Register via Google",
        description = "Crée effectivement un compte via Google après confirmation de l'utilisateur sur la page " +
                "d'inscription pré-remplie. Le compte est créé avec email vérifié mais inactif : il doit être " +
                "activé par un administrateur avant la première connexion (status=PENDING_ACTIVATION)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Compte créé, en attente d'activation (status=PENDING_ACTIVATION)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GoogleAuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Création impossible (token invalide, email non vérifié, compte déjà existant, etc.)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/google/register")
    public ResponseEntity<?> googleRegister(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Google ID token + prénom/nom confirmés",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GoogleRegisterRequest.class)
                    )
            )
            @RequestBody GoogleRegisterRequest request) {
        try {
            authService.googleRegister(request.getIdToken(), request.getFirstName(), request.getLastName());

            return ResponseEntity.ok(new GoogleAuthResponse(
                    true, "PENDING_ACTIVATION",
                    "Votre compte a été créé via Google. Il doit être activé par un administrateur avant la première connexion.",
                    null, null));
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
