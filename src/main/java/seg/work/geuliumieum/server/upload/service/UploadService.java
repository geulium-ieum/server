package seg.work.geuliumieum.server.upload.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.AlbumRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.upload.dto.UploadResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final S3Client s3;
    private final MemorialRepository memorialRepository;
    private final AlbumRepository albumRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region}")
    private String region;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024; // 10MB

    public UploadResponse uploadProfilePhoto(UserInfo user, MultipartFile file) {
        ensureAuth(user);
        validateImage(file);
        String key = buildKey("profile/" + user.getId(), originalExt(file));
        putToS3(key, file);
        return new UploadResponse(key, publicUrl(key), file.getSize(), contentType(file));
    }

    public UploadResponse uploadMemorialPhoto(UserInfo user, Long memorialId, MultipartFile file) {
        ensureAuth(user);
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        validateImage(file);
        String key = buildKey("memorial/" + memorialId, originalExt(file));
        putToS3(key, file);
        return new UploadResponse(key, publicUrl(key), file.getSize(), contentType(file));
    }

    public UploadResponse uploadAlbumPhoto(UserInfo user, Long albumId, MultipartFile file) {
        ensureAuth(user);
        albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        validateImage(file);
        String key = buildKey("album/" + albumId, originalExt(file));
        putToS3(key, file);
        return new UploadResponse(key, publicUrl(key), file.getSize(), contentType(file));
    }

    public void delete(UserInfo user, String fileId) {
        ensureAuth(user);
        if (fileId == null || fileId.isBlank()) throw new ApiException(ErrorCode.BAD_REQUEST);
        DeleteObjectRequest req = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(fileId)
            .build();
        s3.deleteObject(req);
    }

    private void ensureAuth(UserInfo user) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException(ErrorCode.BAD_REQUEST);
        if (file.getSize() > MAX_IMAGE_SIZE) throw new ApiException(ErrorCode.BAD_REQUEST);
        String ct = contentType(file);
        if (ct == null || !ct.toLowerCase().startsWith("image/")) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }
    }

    private String contentType(MultipartFile file) {
        return file.getContentType() != null ? file.getContentType() : "application/octet-stream";
    }

    private String originalExt(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return idx > -1 ? name.substring(idx) : "";
    }

    private String buildKey(String baseFolder, String ext) {
        String date = LocalDate.now().toString();
        String name = UUID.randomUUID().toString().replace("-", "");
        String safeExt = ext == null ? "" : ext;
        return baseFolder + "/" + date + "/" + name + safeExt;
    }

    private void putToS3(String key, MultipartFile file) {
        try {
            PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType(file))
                .build();
            s3.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String publicUrl(String key) {
        // 기본 퍼블릭 URL 형식 (버킷이 퍼블릭 또는 CloudFront로 매핑된 경우). 필요 시 커스터마이즈.
        String encKey = URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20");
        return endpoint + encKey;
    }
}
