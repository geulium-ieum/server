package seg.work.geuliumieum.server.config.jpa;

import java.util.Optional;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.config.security.CustomUserDetails;

@Configuration
public class AuditorConfig implements AuditorAware<Long> {

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // 비인증 컨텍스트에서는 감사자 정보를 비워 둔다.
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserInfo userInfo) {
            return Optional.ofNullable(userInfo.getId());
        } else if (principal instanceof CustomUserDetails cud) {
            return Optional.ofNullable(cud.getId());
        }
        return Optional.empty();
    }
}
