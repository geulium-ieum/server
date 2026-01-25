package seg.work.geuliumieum.server.memorial.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.FamilyGroupMemorial;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.FamilyGroupMemberRepository;
import seg.work.geuliumieum.server.common.repository.FamilyGroupMemorialRepository;
import seg.work.geuliumieum.server.common.repository.MemorialMemberRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;
import seg.work.geuliumieum.server.memorial.dto.response.AccessResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemorialAccessService {

    private final MemorialRepository memorialRepository;
    private final MemorialMemberRepository memorialMemberRepository;
    private final FamilyGroupMemberRepository familyGroupMemberRepository;
    private final FamilyGroupMemorialRepository familyGroupMemorialRepository;

    @Cacheable(cacheNames = "memorial:access", key = "#id + ':' + (#userInfo != null ? #userInfo.id : 'guest')")
    public AccessResponse getAccess(UserInfo userInfo, Long id) {
        Memorial memorial = memorialRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));

        // 1. 전체 공개
        if (memorial.getVisibility() == VISIBILITY.PUBLIC) {
            return AccessResponse.builder().allowed(true).reason("PUBLIC").build();
        }

        // 2. 비공개 또는 가족 공개인데 로그인하지 않은 경우
        if (userInfo == null || userInfo.getId() == null) {
            return AccessResponse.builder().allowed(false).reason("LOGIN_REQUIRED").build();
        }

        // 3. 관리자 권한 (전역 관리자)
        if (userInfo.getRole() == UserRole.ADMIN || userInfo.getRole() == UserRole.SUPER_ADMIN) {
            return AccessResponse.builder().allowed(true).reason("ADMIN").build();
        }

        // 4. 소유자 (생성자)
        if (Objects.equals(memorial.getCreatedBy(), userInfo.getId())) {
            return AccessResponse.builder().allowed(true).reason("OWNER").build();
        }

        // 5. 추모관 직접 멤버십 확인
        boolean isMemorialMember = memorialMemberRepository.existsByMemorialIdAndUserId(id, userInfo.getId());
        if (isMemorialMember) {
            return AccessResponse.builder().allowed(true).reason("MEMORIAL_MEMBER").build();
        }

        // 6. 가족 공개일 경우 가족 그룹 멤버십 확인
        if (memorial.getVisibility() == VISIBILITY.FAMILY_ONLY) {
            // 이 추모관이 연결된 모든 가족 그룹 ID 조회
            List<Long> groupIds = familyGroupMemorialRepository.findAllByMemorialId(id)
                .stream()
                .map(FamilyGroupMemorial::getGroupId)
                .toList();

            if (!groupIds.isEmpty()) {
                // 사용자가 해당 그룹들 중 하나라도 멤버인지 확인
                boolean isFamilyMember = familyGroupMemberRepository.existsByUserIdAndGroupIdIn(userInfo.getId(), groupIds);
                if (isFamilyMember) {
                    return AccessResponse.builder().allowed(true).reason("FAMILY_MEMBER").build();
                }
            }
        }

        return AccessResponse.builder().allowed(false).reason("NO_PERMISSION").build();
    }
}
