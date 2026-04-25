package seg.work.geuliumieum.server.common.repository;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.User;

public interface UserRepository extends BaseRepository<User> {

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByNameAndPhoneAndDeletedAtIsNull(String name, String phone);

    Slice<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Slice<User> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
}