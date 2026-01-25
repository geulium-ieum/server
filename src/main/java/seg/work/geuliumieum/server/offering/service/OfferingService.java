package seg.work.geuliumieum.server.offering.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Offering;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.OfferingRepository;
import seg.work.geuliumieum.server.common.repository.OfferingRepository.OfferingTypeCount;
import seg.work.geuliumieum.server.memorial.service.MemorialService;
import seg.work.geuliumieum.server.offering.dto.request.OfferingRequest;
import seg.work.geuliumieum.server.offering.dto.response.OfferingResponse;
import seg.work.geuliumieum.server.offering.dto.response.OfferingStatsResponse;

@Service
@RequiredArgsConstructor
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final MemorialRepository memorialRepository;
    private final MemorialService memorialService;

    public Slice<OfferingResponse> listByMemorial(UserInfo userInfo, Long memorialId, @ParameterObject Pageable pageable) {
        memorialService.checkAccess(userInfo, memorialId);
        return offeringRepository.findByMemorialId(memorialId, pageable).map(OfferingResponse::from);
    }

    @Transactional
    @CacheEvict(cacheNames = "offering:stats", key = "#memorialId")
    public OfferingResponse create(UserInfo userInfo, Long memorialId, OfferingRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Offering offering = new Offering();
        offering.setMemorialId(memorialId);
        offering.setUserId(userInfo.getId());
        offering.setOfferingType(request.getOfferingType());
        offering.setMessage(request.getMessage());
        offeringRepository.save(offering);
        return OfferingResponse.from(offering);
    }

    @Cacheable(cacheNames = "offering:stats", key = "#memorialId")
    public OfferingStatsResponse statsByMemorial(Long memorialId) {
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        List<OfferingRepository.OfferingTypeCount> rows = offeringRepository.countByMemorialGroupByType(memorialId);
        long total = 0;
        long flower = 0;
        long incense = 0;
        long candle = 0;
        for (OfferingTypeCount offeringTypeCount : rows) {
            long c = offeringTypeCount.getCount();
            total += c;
            String type = offeringTypeCount.getType();
            if ("FLOWER".equalsIgnoreCase(type)) {
                flower += c;
            } else if ("INCENSE".equalsIgnoreCase(type)) {
                incense += c;
            } else if ("CANDLE".equalsIgnoreCase(type)) {
                candle += c;
            }
        }
        return OfferingStatsResponse.builder()
            .total(total)
            .flower(flower)
            .incense(incense)
            .candle(candle)
            .build();
    }

    public Slice<OfferingResponse> listByUser(Long userId, @ParameterObject Pageable pageable) {
        return offeringRepository.findByUserId(userId, pageable).map(OfferingResponse::from);
    }
}
