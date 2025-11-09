package seg.work.geuliumieum.server.common.repository;

import java.util.Optional;
import seg.work.geuliumieum.server.common.entity.User;

public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNameAndPhone(String name, String phone);
}