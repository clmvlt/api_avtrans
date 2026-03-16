package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.common.*;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.UserMapper;
import bzh.stack.apiavtrans.service.AuthService;
import bzh.stack.apiavtrans.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;
    private final UserMapper userMapper;

    @RequireRole("Utilisateur")
    @Operation(summary = "[UTILISATEUR] Get own profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            User user = (User) request.getAttribute("user");
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @RequireRole("Utilisateur")
    @Operation(summary = "[UTILISATEUR] Update own profile")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid profile data")
    @PutMapping
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @RequestBody UpdateProfileRequest updateRequest) {
        try {
            User user = (User) request.getAttribute("user");

            User updatedUser = profileService.updateProfile(
                    user.getUuid(),
                    updateRequest.getFirstName(),
                    updateRequest.getLastName(),
                    updateRequest.getEmail(),
                    updateRequest.getPicture(),
                    updateRequest.getAddress(),
                    updateRequest.getDriverLicenseNumber()
            );

            UserDTO userDTO = userMapper.toDTO(updatedUser);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @RequireRole("Utilisateur")
    @Operation(summary = "[UTILISATEUR] Change own password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully",
            content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class)))
    @ApiResponse(responseCode = "400", description = "Current password incorrect or invalid new password")
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            HttpServletRequest request,
            @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            User user = (User) request.getAttribute("user");

            authService.changePassword(
                    user.getUuid(),
                    changePasswordRequest.getCurrentPassword(),
                    changePasswordRequest.getNewPassword()
            );

            return ResponseEntity.ok(new SuccessMessageResponse(true, "Mot de passe modifié avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
