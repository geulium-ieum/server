package seg.work.geuliumieum.server.common.repository;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import seg.work.geuliumieum.server.announcement.dto.response.AnnouncementResponse;
import seg.work.geuliumieum.server.common.entity.Announcement;

public interface AnnouncementRepository extends BaseRepository<Announcement> {

    @Query("""
      SELECT
          new seg.work.geuliumieum.server.announcement.dto.response.AnnouncementResponse(a, u.name)
      FROM Announcement a
      JOIN User u ON a.authorId = u.id
      WHERE a.isPublished = true
      ORDER BY a.isPinned DESC, a.createdAt DESC
    """)
    Slice<AnnouncementResponse> findByIsPublishedTrueOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);

    @Query("""
      SELECT
          new seg.work.geuliumieum.server.announcement.dto.response.AnnouncementResponse(a, u.name)
      FROM Announcement a
      JOIN User u ON a.authorId = u.id
      WHERE a.isPublished = true
        AND a.isPinned = true
      ORDER BY a.createdAt DESC
    """)
    Slice<AnnouncementResponse> findByIsPublishedTrueAndIsPinnedTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
          SELECT
              new seg.work.geuliumieum.server.announcement.dto.response.AnnouncementResponse(a, u.name)
          FROM Announcement a
          JOIN User u ON a.authorId = u.id
          WHERE a.id = :id
            AND a.isPublished = true
        """)
    Optional<AnnouncementResponse> findByIdAndIsPublishedTrue(Long id);
}