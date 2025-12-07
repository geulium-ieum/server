package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.Album;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AlbumRepository extends BaseRepository<Album> {

    Slice<Album> findByMemorialId(Long memorialId, Pageable pageable);
}