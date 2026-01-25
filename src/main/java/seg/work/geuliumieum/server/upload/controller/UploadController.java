package seg.work.geuliumieum.server.upload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.upload.dto.UploadResponse;
import seg.work.geuliumieum.server.upload.service.UploadService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
@Tag(name = "Upload", description = "파일 업로드 API (S3)")
public class UploadController {

    private final UploadService uploadService;

    @Operation(summary = "프로필 사진 업로드", description = "현재 사용자의 프로필 사진을 업로드합니다.")
    @PostMapping("/profile-photo")
    public ResponseEntity<UploadResponse> uploadProfile(UserInfo userInfo, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(uploadService.uploadProfilePhoto(userInfo, file));
    }

    @Operation(summary = "추모관 사진 업로드", description = "특정 추모관의 사진을 업로드합니다.")
    @PostMapping("/memorial-photo")
    public ResponseEntity<UploadResponse> uploadMemorial(UserInfo userInfo, @RequestParam("memorialId") Long memorialId, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(uploadService.uploadMemorialPhoto(userInfo, memorialId, file));
    }

    @Operation(summary = "앨범 사진 업로드", description = "특정 앨범에 사진을 업로드합니다.")
    @PostMapping("/album-photo")
    public ResponseEntity<UploadResponse> uploadAlbum(UserInfo userInfo, @RequestParam("albumId") Long albumId, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(uploadService.uploadAlbumPhoto(userInfo, albumId, file));
    }

    @Operation(summary = "파일 삭제", description = "업로드한 파일을 삭제합니다.")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(UserInfo userInfo, @PathVariable String fileId) {
        uploadService.delete(userInfo, fileId);
        return ResponseEntity.ok().build();
    }
}
