package seg.work.geuliumieum.server.announcement.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.announcement.dto.response.AnnouncementResponse;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.AnnouncementRepository;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public Slice<AnnouncementResponse> list(@ParameterObject Pageable pageable) {
        return announcementRepository.findByIsPublishedTrueOrderByIsPinnedDescCreatedAtDesc(pageable);
    }

    public Slice<AnnouncementResponse> pinned(@ParameterObject Pageable pageable) {
        return announcementRepository.findByIsPublishedTrueAndIsPinnedTrueOrderByCreatedAtDesc(pageable);
    }

//    @Cacheable(cacheNames = "announcement:detail", key = "#id")
    public AnnouncementResponse get(Long id) {
        return announcementRepository.findByIdAndIsPublishedTrue(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }
}
