package bzh.stack.apiavtrans.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 64)
    private String passwordHash;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "is_mail_verified", nullable = false)
    private Boolean isMailVerified = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "role_uuid", nullable = true)
    private Role role;

    @Column(name = "token", unique = true, length = 255)
    private String token;

    @Column(name = "picture_path", length = 500)
    private String picturePath;

    @Column(name = "is_couchette", nullable = false)
    private Boolean isCouchette = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "notif_pref_acompte")
    private NotificationPreference notifPrefAcompte = NotificationPreference.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "notif_pref_absence")
    private NotificationPreference notifPrefAbsence = NotificationPreference.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "notif_pref_user_created")
    private NotificationPreference notifPrefUserCreated = NotificationPreference.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "notif_pref_rapport_vehicule")
    private NotificationPreference notifPrefRapportVehicule = NotificationPreference.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "notif_pref_todo")
    private NotificationPreference notifPrefTodo = NotificationPreference.NONE;
}
