package seg.work.geuliumieum.server.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.AlbumPhotoRepository;
import seg.work.geuliumieum.server.common.repository.AlbumRepository;
import seg.work.geuliumieum.server.common.repository.GuestbookEntryRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.OfferingRepository;
import seg.work.geuliumieum.server.common.repository.TributeRepository;
import seg.work.geuliumieum.server.common.repository.UserRepository;
import seg.work.geuliumieum.server.stats.dto.MemorialStatsResponse;
import seg.work.geuliumieum.server.stats.dto.StatsOverviewResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final MemorialRepository memorialRepository;
    private final TributeRepository tributeRepository;
    private final OfferingRepository offeringRepository;
    private final GuestbookEntryRepository guestbookEntryRepository;
    private final AlbumRepository albumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;

    public StatsOverviewResponse overview() {
        return StatsOverviewResponse.builder()
            .users(userRepository.count())
            .memorials(memorialRepository.count())
            .tributes(tributeRepository.count())
            .offerings(offeringRepository.count())
            .guestbooks(guestbookEntryRepository.count())
            .build();
    }

    public MemorialStatsResponse memorialStats(Long memorialId) {
        // 존재 확인 (없으면 404)
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));

        long tribute = tributeRepository.countByMemorialId(memorialId);
        long offering = offeringRepository.countByMemorialId(memorialId);
        long guestbook = guestbookEntryRepository.countByMemorialId(memorialId);
        long album = albumRepository.countByMemorialId(memorialId);
        List<Long> albumIds = albumRepository.findIdsByMemorialId(memorialId);
        long photo = albumIds == null || albumIds.isEmpty() ? 0 : albumPhotoRepository.countByAlbumIdIn(albumIds);
        return MemorialStatsResponse.builder()
            .tributeCount(tribute)
            .offeringCount(offering)
            .guestbookCount(guestbook)
            .albumCount(album)
            .photoCount(photo)
            .build();
    }
}
