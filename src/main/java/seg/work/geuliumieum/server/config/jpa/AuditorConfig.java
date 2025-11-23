package seg.work.geuliumieum.server.config.jpa;

import java.util.Optional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import seg.work.geuliumieum.server.config.security.CustomUserDetails;

@Configuration
public class AuditorConfig implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return Optional.ofNullable(cud.getId());
        }

        return Optional.empty();
    }
}
