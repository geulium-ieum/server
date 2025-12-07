package seg.work.geuliumieum.server.offering.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.entity.Offering;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.OfferingRepository;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;
import seg.work.geuliumieum.server.offering.dto.request.OfferingRequest;
import seg.work.geuliumieum.server.offering.dto.response.OfferingResponse;
import seg.work.geuliumieum.server.offering.dto.response.OfferingStatsResponse;

@Service
@RequiredArgsConstructor
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final MemorialRepository memorialRepository;

    public Slice<OfferingResponse> listByMemorial(Long memorialId, @ParameterObject Pageable pageable, UserInfo user) {
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (memorial.getVisibility() != VISIBILITY.PUBLIC && (user == null || user.getId() == null)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return offeringRepository.findByMemorialId(memorialId, pageable).map(OfferingResponse::from);
    }

    @Transactional
    public OfferingResponse create(Long memorialId, UserInfo user, OfferingRequest request) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Offering o = new Offering();
        o.setMemorialId(memorialId);
        o.setUserId(user.getId());
        o.setOfferingType(request.getOfferingType());
        o.setMessage(request.getMessage());
        offeringRepository.save(o);
        return OfferingResponse.from(o);
    }

    public OfferingStatsResponse statsByMemorial(Long memorialId) {
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        List<OfferingRepository.OfferingTypeCount> rows = offeringRepository.countByMemorialGroupByType(memorialId);
        long total = 0;
        long flower = 0;
        long incense = 0;
        long candle = 0;
        for (var r : rows) {
            long c = r.getCount();
            total += c;
            String type = r.getType();
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
