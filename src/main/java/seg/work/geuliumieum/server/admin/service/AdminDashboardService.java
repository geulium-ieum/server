package seg.work.geuliumieum.server.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.admin.dto.response.ActiveItemResponse;
import seg.work.geuliumieum.server.admin.dto.response.AdminUserListItemResponse;
import seg.work.geuliumieum.server.admin.dto.response.SystemStatsResponse;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.GuestbookEntryRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.OfferingRepository;
import seg.work.geuliumieum.server.common.repository.TributeRepository;
import seg.work.geuliumieum.server.common.repository.UserRepository;
import seg.work.geuliumieum.server.config.security.UserRole;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final MemorialRepository memorialRepository;
    private final TributeRepository tributeRepository;
    private final OfferingRepository offeringRepository;
    private final GuestbookEntryRepository guestbookEntryRepository;

    private void ensureAdmin(UserInfo user) {
        if (user == null || user.getRole() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        if (!(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    public SystemStatsResponse stats(UserInfo admin) {
        ensureAdmin(admin);
        long users = userRepository.count();
        long memorials = memorialRepository.count();
        long tributes = tributeRepository.count();
        long offerings = offeringRepository.count();
        long guestbooks = guestbookEntryRepository.count();
        return SystemStatsResponse.builder()
            .users(users)
            .memorials(memorials)
            .tributes(tributes)
            .offerings(offerings)
            .guestbooks(guestbooks)
            .build();
    }

    public Slice<AdminUserListItemResponse> recentUsers(UserInfo admin, @ParameterObject Pageable pageable) {
        ensureAdmin(admin);
        return userRepository.findAllByOrderByCreatedAtDesc(pageable).map(AdminUserListItemResponse::from);
    }

    public List<ActiveItemResponse> activeMemorials(UserInfo admin, int days) {
        ensureAdmin(admin);
        LocalDateTime since = LocalDateTime.now().minusDays(days <= 0 ? 30 : days);
        return tributeRepository.countByMemorialSince(since).stream()
            .map(r -> ActiveItemResponse.builder().id(r.getId()).count(r.getCount()).build())
            .collect(Collectors.toList());
    }

    public List<ActiveItemResponse> activeUsers(UserInfo admin, int days) {
        ensureAdmin(admin);
        LocalDateTime since = LocalDateTime.now().minusDays(days <= 0 ? 30 : days);
        return tributeRepository.countByUserSince(since).stream()
            .map(r -> ActiveItemResponse.builder().id(r.getId()).count(r.getCount()).build())
            .collect(Collectors.toList());
    }
}
