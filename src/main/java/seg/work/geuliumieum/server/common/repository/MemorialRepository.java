package seg.work.geuliumieum.server.common.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.memorial.constant.STATUS;

public interface MemorialRepository extends BaseRepository<Memorial> {

    Slice<Memorial> findAllBy(Pageable pageable);

    Slice<Memorial> findByCreatedBy(Long createdBy, Pageable pageable);

    Slice<Memorial> findByStatus(STATUS status, Pageable pageable);

    List<Memorial> findAllByIdIn(Collection<Long> ids);
}