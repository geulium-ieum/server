package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.Offering;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfferingRepository extends BaseRepository<Offering> {

    long countByUserId(Long userId);

    Slice<Offering> findByMemorialId(Long memorialId, Pageable pageable);

    Slice<Offering> findByUserId(Long userId, Pageable pageable);

    interface OfferingTypeCount {
        String getType();
        long getCount();
    }

    @Query("select o.offeringType as type, count(o) as count from Offering o where o.memorialId = :memorialId group by o.offeringType")
    List<OfferingTypeCount> countByMemorialGroupByType(@Param("memorialId") Long memorialId);
}