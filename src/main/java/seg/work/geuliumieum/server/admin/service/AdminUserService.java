package seg.work.geuliumieum.server.admin.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.admin.dto.request.RoleUpdateRequest;
import seg.work.geuliumieum.server.admin.dto.response.AdminUserDetailResponse;
import seg.work.geuliumieum.server.admin.dto.response.AdminUserListItemResponse;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.User;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.GuestbookEntryRepository;
import seg.work.geuliumieum.server.common.repository.OfferingRepository;
import seg.work.geuliumieum.server.common.repository.TributeRepository;
import seg.work.geuliumieum.server.common.repository.UserRepository;
import seg.work.geuliumieum.server.config.security.UserRole;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final TributeRepository tributeRepository;
    private final OfferingRepository offeringRepository;
    private final GuestbookEntryRepository guestbookEntryRepository;

    private void ensureAdmin(UserInfo user) {
        if (user == null || user.getRole() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        if (!(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    private void ensureSuperAdmin(UserInfo user) {
        if (user == null || user.getRole() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        if (user.getRole() != UserRole.SUPER_ADMIN) throw new ApiException(ErrorCode.FORBIDDEN);
    }

    public Slice<AdminUserListItemResponse> list(UserInfo admin, @ParameterObject Pageable pageable) {
        ensureAdmin(admin);
        return userRepository.findAll(pageable).map(AdminUserListItemResponse::from);
    }

    public AdminUserDetailResponse get(UserInfo admin, Long userId) {
        ensureAdmin(admin);
        User u = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        long tribute = tributeRepository.countByUserId(userId);
        long offering = offeringRepository.countByUserId(userId);
        long guestbook = guestbookEntryRepository.countByUserId(userId);
        return AdminUserDetailResponse.from(u, tribute, offering, guestbook);
    }

    @Transactional
    public void changeRole(UserInfo superAdmin, Long userId, RoleUpdateRequest request) {
        ensureSuperAdmin(superAdmin);
        User u = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        u.setRole(request.getRole());
        userRepository.save(u);
    }

    @Transactional
    public void activate(UserInfo admin, Long userId) {
        ensureAdmin(admin);
        User u = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        u.setIsActive(true);
        userRepository.save(u);
    }

    @Transactional
    public void deactivate(UserInfo admin, Long userId) {
        ensureAdmin(admin);
        User u = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        u.setIsActive(false);
        userRepository.save(u);
    }

    @Transactional
    public void delete(UserInfo admin, Long userId) {
        ensureAdmin(admin);
        User u = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(u);
    }
}
