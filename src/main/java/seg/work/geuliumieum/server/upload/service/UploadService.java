package seg.work.geuliumieum.server.upload.service;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final S3Client s3;
    private final MemorialRepository memorialRepository;
    private final AlbumRepository albumRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final String TMP_PREFIX = "tmp/";

    public UploadResponse uploadProfilePhoto(UserInfo userInfo, MultipartFile file) {
        ensureAuth(userInfo);
        validateImage(file);
        String key = buildKey(TMP_PREFIX + "profile/" + userInfo.getId(), originalExt(file));
        putToS3(key, file);
        return new UploadResponse(key, publicUrl(key), file.getSize(), contentType(file));
    }

    public UploadResponse uploadMemorialPhoto(UserInfo userInfo, Long memorialId, MultipartFile file) {
        ensureAuth(userInfo);
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        validateImage(file);
        String key = buildKey(TMP_PREFIX + "memorial/" + memorialId, originalExt(file));
        putToS3(key, file);
        return new UploadResponse(key, publicUrl(key), file.getSize(), contentType(file));
    }

    public UploadResponse uploadAlbumPhoto(UserInfo userInfo, Long albumId, MultipartFile file) {
        ensureAuth(userInfo);
        albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        validateImage(file);
        String key = buildKey(TMP_PREFIX + "album/" + albumId, originalExt(file));
        putToS3(key, file);
        return new UploadResponse(key, publicUrl(key), file.getSize(), contentType(file));
    }

    /**
     * 임시 경로(tmp/...)에 저장된 파일을 실제 경로로 이동시키고 최종 URL을 반환합니다.
     */
    public String confirmFile(String fileId) {
        if (fileId == null || !fileId.startsWith(TMP_PREFIX)) {
            return fileId;
        }

        String newKey = fileId.substring(TMP_PREFIX.length());

        try {
            // S3 Copy
            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(fileId)
                .destinationBucket(bucket)
                .destinationKey(newKey)
                .build();
            s3.copyObject(copyReq);

            // 원본(tmp) 삭제
            DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileId)
                .build();
            s3.deleteObject(delReq);

            return newKey;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    public void delete(UserInfo userInfo, String fileId) {
        ensureAuth(userInfo);
        if (fileId == null || fileId.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        // 권한 체크: 본인 폴더 내의 파일인지 확인 (profile/{userId}/... , memorial/{memorialId}/... 등)
        // 여기서는 간단히 profile 이미지에 대해서만 소유권을 체크하는 예시를 보여줍니다.
        // 실제로는 DB에 파일 정보를 기록하고 소유자를 확인하는 것이 가장 정확합니다.
        if (fileId.startsWith("profile/") && !fileId.startsWith("profile/" + userInfo.getId() + "/")) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileId)
                .build();
            s3.deleteObject(req);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.FILE_DELETE_ERROR);
        }
    }

    private void ensureAuth(UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ApiException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String ct = contentType(file);
        if (ct == null || !ct.toLowerCase().startsWith("image/")) {
            throw new ApiException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String contentType(MultipartFile file) {
        return file.getContentType() != null ? file.getContentType() : "application/octet-stream";
    }

    private String originalExt(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) {
            return "";
        }
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
        try (InputStream is = file.getInputStream()) {
            PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType(file))
                .build();
            s3.putObject(req, RequestBody.fromInputStream(is, file.getSize()));
        } catch (Exception e) {
            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    private String publicUrl(String key) {
        // 기본 퍼블릭 URL 형식 (버킷이 퍼블릭 또는 CloudFront로 매핑된 경우). 필요 시 커스터마이즈.
        String encKey = URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20");
        return endpoint + encKey;
    }
}
