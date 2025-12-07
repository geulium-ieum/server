package seg.work.geuliumieum.server.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.Memorial;

public interface MemorialRepository extends BaseRepository<Memorial> {

    Slice<Memorial> findAllBy(Pageable pageable);

    Slice<Memorial> findByDeceasedNameContainingIgnoreCase(String deceasedName, Pageable pageable);
}