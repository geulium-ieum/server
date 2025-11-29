package seg.work.geuliumieum.server.config.web.resolver;

import java.util.Optional;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.config.security.jwt.JwtTokenProvider;

@Component
public record CurrentUserArgumentResolver(JwtTokenProvider jwtTokenProvider) implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UserInfo.class.isAssignableFrom(parameter.getParameterType())
            || (Optional.class.isAssignableFrom(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {

        UserInfo info = buildFromSecurityContext();

        boolean isOptional = Optional.class.isAssignableFrom(parameter.getParameterType());
        if (isOptional) {
            return Optional.ofNullable(info);
        }
        return info;
    }

    @Nullable
    private UserInfo buildFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserInfo userInfo) {
                return userInfo;
            }
        }
        return null;
    }
}
