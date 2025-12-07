package seg.work.geuliumieum.server.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.AlbumPhoto;

public interface AlbumPhotoRepository extends BaseRepository<AlbumPhoto> {

    Slice<AlbumPhoto> findByAlbumId(Long albumId, Pageable pageable);

    long countByAlbumIdIn(java.util.List<Long> albumIds);
}