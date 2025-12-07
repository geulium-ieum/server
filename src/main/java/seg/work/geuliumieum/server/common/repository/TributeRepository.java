package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.Tribute;

public interface TributeRepository extends BaseRepository<Tribute> {

    long countByUserId(Long userId);

    org.springframework.data.domain.Slice<Tribute> findByMemorialId(Long memorialId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Slice<Tribute> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);
}