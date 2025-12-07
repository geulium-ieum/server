package seg.work.geuliumieum.server.common.repository;

import java.util.Optional;
import seg.work.geuliumieum.server.common.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNameAndPhone(String name, String phone);

    Slice<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
}