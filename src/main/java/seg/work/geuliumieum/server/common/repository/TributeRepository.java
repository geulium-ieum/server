package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.Tribute;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TributeRepository extends BaseRepository<Tribute> {

    long countByUserId(Long userId);

    org.springframework.data.domain.Slice<Tribute> findByMemorialId(Long memorialId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Slice<Tribute> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    interface ActivityCount {
        Long getId();
        long getCount();
    }

    @Query("select t.memorialId as id, count(t) as count from Tribute t where t.createdAt >= :since group by t.memorialId order by count(t) desc")
    List<ActivityCount> countByMemorialSince(@Param("since") LocalDateTime since);

    @Query("select t.userId as id, count(t) as count from Tribute t where t.createdAt >= :since group by t.userId order by count(t) desc")
    List<ActivityCount> countByUserSince(@Param("since") LocalDateTime since);
}