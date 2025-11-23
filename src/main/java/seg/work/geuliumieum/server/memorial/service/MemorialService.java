package seg.work.geuliumieum.server.memorial.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.memorial.dto.request.RegisterRequest;

@Service
@RequiredArgsConstructor
public class MemorialService {

    private final MemorialRepository memorialRepository;

    public void createMemorial(UserInfo userInfo, RegisterRequest request) {
        Memorial memorial = request.toEntity();
        memorialRepository.save(memorial);
    }
}
