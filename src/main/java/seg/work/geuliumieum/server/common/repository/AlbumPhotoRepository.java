package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.AlbumPhoto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AlbumPhotoRepository extends BaseRepository<AlbumPhoto> {

    Slice<AlbumPhoto> findByAlbumId(Long albumId, Pageable pageable);
}