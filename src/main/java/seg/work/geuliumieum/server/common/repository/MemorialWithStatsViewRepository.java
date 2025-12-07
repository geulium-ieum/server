package seg.work.geuliumieum.server.common.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import seg.work.geuliumieum.server.common.entity.MemorialWithStatsView;

public interface MemorialWithStatsViewRepository extends JpaRepository<MemorialWithStatsView, Long> {

    Optional<MemorialWithStatsView> findByMemorialId(Long memorialId);
}
