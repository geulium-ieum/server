package seg.work.geuliumieum.server.memorial.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.entity.MemorialMember;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialMemberRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.memorial.dto.request.MemberAddRequest;
import seg.work.geuliumieum.server.memorial.dto.request.MemberRoleUpdateRequest;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialMemberResponse;

@Service
@RequiredArgsConstructor
public class MemorialMemberService {

    private final MemorialRepository memorialRepository;
    private final MemorialMemberRepository memorialMemberRepository;

    public Slice<MemorialMemberResponse> list(Long memorialId, @ParameterObject Pageable pageable, UserInfo user) {
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        // 목록 조회/관리 권한: 소유자만
        if (user == null || user.getId() == null || !user.getId().equals(memorial.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        return memorialMemberRepository.findByMemorialId(memorialId, pageable).map(MemorialMemberResponse::from);
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void add(Long memorialId, UserInfo user, MemberAddRequest request) {
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!user.getId().equals(memorial.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        // 중복 방지
        boolean exists = memorialMemberRepository.existsByMemorialIdAndUserId(memorialId, request.getUserId());
        if (exists) {
            // 중복 멤버 추가 요청
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }
        MemorialMember memorialMember = new MemorialMember();
        memorialMember.setMemorialId(memorialId);
        memorialMember.setUserId(request.getUserId());
        memorialMember.setRelationship(request.getRelationship());
        memorialMember.setRole(request.getRole());
        memorialMemberRepository.save(memorialMember);
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void changeRole(Long memorialId, Long targetUserId, UserInfo user, MemberRoleUpdateRequest request) {
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!user.getId().equals(memorial.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        MemorialMember memorialMember = memorialMemberRepository.findByMemorialIdAndUserId(memorialId, targetUserId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        memorialMember.setRole(request.getRole());
        memorialMemberRepository.save(memorialMember);
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void remove(Long memorialId, Long targetUserId, UserInfo user) {
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!user.getId().equals(memorial.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        memorialMemberRepository.deleteByMemorialIdAndUserId(memorialId, targetUserId);
    }
}
