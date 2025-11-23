package seg.work.geuliumieum.server.config.jpa;

import java.util.Optional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.config.security.CustomUserDetails;

@Configuration
public class AuditorConfig implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        } else {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserInfo userInfo) {
                return Optional.ofNullable(userInfo.getId());
            } else if (principal instanceof CustomUserDetails cud) {
                return Optional.ofNullable(cud.getId());
            }

            return Optional.empty();
        }
    }
}
