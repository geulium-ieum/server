package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import seg.work.geuliumieum.server.config.security.UserRole;

@Getter
@Setter
@Entity
@Table(name = "users")
@Comment("사용자 정보 테이블")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -8738931564074914885L;

    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "pwd", nullable = false)
    private String pwd;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "role", length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "profile_photo_url", length = Integer.MAX_VALUE)
    private String profilePhotoUrl;

    @Column(name = "provider", length = 20)
    private String provider;

    @Column(name = "provider_id", length = 50)
    private String providerId;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("false")
    @Column(name = "marketing_agreed")
    @Comment("마케팅 정보 수신 동의 여부")
    private Boolean marketingAgreed = false;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}