package seg.work.geuliumieum.server.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seg.work.geuliumieum.server.common.entity.Album;

public interface AlbumRepository extends BaseRepository<Album> {

    Slice<Album> findByMemorialId(Long memorialId, Pageable pageable);

    long countByMemorialId(Long memorialId);

    @Query("select a.id from Album a where a.memorialId = :memorialId")
    java.util.List<Long> findIdsByMemorialId(@Param("memorialId") Long memorialId);
}