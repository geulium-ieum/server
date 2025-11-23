package seg.work.geuliumieum.server.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.common.entity.User;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.UserRepository;
import seg.work.geuliumieum.server.user.dto.response.UserMeResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(cacheNames = "user:me", key = "#userId", unless = "#result == null")
    public UserMeResponse getCurrentUser(Long userId) {
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return UserMeResponse.from(user);
    }
}
