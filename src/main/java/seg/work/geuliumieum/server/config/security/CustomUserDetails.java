package seg.work.geuliumieum.server.config.security;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import seg.work.geuliumieum.server.common.entity.User;

@Getter
public class CustomUserDetails implements UserDetails {

    @Serial
    private static final long serialVersionUID = 3061709774601068428L;

    private final Long id;
    private final String email;
    private final String password;
    private final boolean active;

    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPwd();
        this.active = user.getIsActive() == null || user.getIsActive();
        UserRole role = user.getRole() == null ? UserRole.USER : user.getRole();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public String getUsername() {
        return email;
    }
}
