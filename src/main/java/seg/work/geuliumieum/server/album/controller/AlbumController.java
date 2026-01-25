package seg.work.geuliumieum.server.album.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.album.dto.request.AlbumCreateRequest;
import seg.work.geuliumieum.server.album.dto.request.AlbumUpdateRequest;
import seg.work.geuliumieum.server.album.dto.request.PhotoCreateRequest;
import seg.work.geuliumieum.server.album.dto.request.PhotoUpdateRequest;
import seg.work.geuliumieum.server.album.dto.response.AlbumResponse;
import seg.work.geuliumieum.server.album.dto.response.PhotoResponse;
import seg.work.geuliumieum.server.album.service.AlbumService;
import seg.work.geuliumieum.server.common.dto.UserInfo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/album")
@Tag(name = "Album", description = "앨범/사진 API")
public class AlbumController {

    private final AlbumService albumService;

    // 5.1 앨범 관리
    @Operation(summary = "앨범 목록", description = "추모관별 앨범 목록을 조회합니다.")
    @GetMapping("/memorial/{id}/list")
    public ResponseEntity<Slice<AlbumResponse>> listAlbums(UserInfo userInfo, @PathVariable("id") Long memorialId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(albumService.listByMemorial(memorialId, pageable, userInfo));
    }

    @Operation(summary = "앨범 상세 조회", description = "앨범 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbum(UserInfo userInfo, @PathVariable("id") Long albumId) {
        return ResponseEntity.ok(albumService.getAlbum(albumId, userInfo));
    }

    @Operation(summary = "앨범 생성", description = "해당 추모관에 앨범을 생성합니다.")
    @PostMapping("/memorial/{id}")
    public ResponseEntity<AlbumResponse> createAlbum(UserInfo userInfo, @PathVariable("id") Long memorialId, @Valid @RequestBody AlbumCreateRequest request) {
        return ResponseEntity.ok(albumService.createAlbum(memorialId, userInfo, request));
    }

    @Operation(summary = "앨범 수정", description = "앨범을 수정합니다(소유자만 가능).")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAlbum(UserInfo userInfo, @PathVariable("id") Long albumId, @Valid @RequestBody AlbumUpdateRequest request) {
        albumService.updateAlbum(albumId, userInfo, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "앨범 삭제", description = "앨범을 삭제합니다(소유자만 가능).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(UserInfo userInfo, @PathVariable("id") Long albumId) {
        albumService.deleteAlbum(albumId, userInfo);
        return ResponseEntity.ok().build();
    }

    // 5.2 앨범 사진 관리
    @Operation(summary = "앨범 사진 목록", description = "앨범에 포함된 사진 목록을 조회합니다.")
    @GetMapping("/{id}/photo/list")
    public ResponseEntity<Slice<PhotoResponse>> listPhotos(UserInfo userInfo, @PathVariable("id") Long albumId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(albumService.listPhotos(albumId, pageable, userInfo));
    }

    @Operation(summary = "사진 업로드", description = "앨범에 사진을 업로드(또는 URL 등록)합니다.")
    @PostMapping("/{id}/photo")
    public ResponseEntity<PhotoResponse> addPhoto(UserInfo userInfo, @PathVariable("id") Long albumId, @Valid @RequestBody PhotoCreateRequest request) {
        return ResponseEntity.ok(albumService.createPhoto(albumId, userInfo, request));
    }

    @Operation(summary = "사진 캡션 수정", description = "사진의 캡션을 수정합니다(업로더 또는 앨범 소유자).")
    @PutMapping("/photo/{id}")
    public ResponseEntity<Void> updatePhoto(UserInfo userInfo, @PathVariable("id") Long photoId, @Valid @RequestBody PhotoUpdateRequest request) {
        albumService.updatePhoto(photoId, userInfo, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사진 삭제", description = "사진을 삭제합니다(업로더 또는 앨범 소유자).")
    @DeleteMapping("/photo/{id}")
    public ResponseEntity<Void> deletePhoto(UserInfo userInfo, @PathVariable("id") Long photoId) {
        albumService.deletePhoto(photoId, userInfo);
        return ResponseEntity.ok().build();
    }
}
