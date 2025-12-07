package seg.work.geuliumieum.server.album.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.album.dto.request.AlbumCreateRequest;
import seg.work.geuliumieum.server.album.dto.request.AlbumUpdateRequest;
import seg.work.geuliumieum.server.album.dto.request.PhotoCreateRequest;
import seg.work.geuliumieum.server.album.dto.request.PhotoUpdateRequest;
import seg.work.geuliumieum.server.album.dto.response.AlbumResponse;
import seg.work.geuliumieum.server.album.dto.response.PhotoResponse;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Album;
import seg.work.geuliumieum.server.common.entity.AlbumPhoto;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.AlbumPhotoRepository;
import seg.work.geuliumieum.server.common.repository.AlbumRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final MemorialRepository memorialRepository;

    public Slice<AlbumResponse> listByMemorial(Long memorialId, @ParameterObject Pageable pageable, UserInfo user) {
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (memorial.getVisibility() != VISIBILITY.PUBLIC && (user == null || user.getId() == null)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return albumRepository.findByMemorialId(memorialId, pageable).map(AlbumResponse::from);
    }

    public AlbumResponse getAlbum(Long albumId, UserInfo user) {
        Album a = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        Memorial memorial = memorialRepository.findById(a.getMemorialId())
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (memorial.getVisibility() != VISIBILITY.PUBLIC && (user == null || user.getId() == null)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return AlbumResponse.from(a);
    }

    @Transactional
    public AlbumResponse createAlbum(Long memorialId, UserInfo user, AlbumCreateRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Album a = new Album();
        a.setMemorialId(memorialId);
        a.setTitle(request.getTitle());
        a.setDescription(request.getDescription());
        a.setCreatedBy(user.getId());
        albumRepository.save(a);
        return AlbumResponse.from(a);
    }

    @Transactional
    public void updateAlbum(Long albumId, UserInfo user, AlbumUpdateRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        Album a = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(a.getCreatedBy())) throw new ApiException(ErrorCode.FORBIDDEN);
        if (request.getTitle() != null) a.setTitle(request.getTitle());
        if (request.getDescription() != null) a.setDescription(request.getDescription());
        albumRepository.save(a);
    }

    @Transactional
    public void deleteAlbum(Long albumId, UserInfo user) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        Album a = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(a.getCreatedBy())) throw new ApiException(ErrorCode.FORBIDDEN);
        albumRepository.delete(a);
    }

    public Slice<PhotoResponse> listPhotos(Long albumId, @ParameterObject Pageable pageable, UserInfo user) {
        Album a = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        Memorial memorial = memorialRepository.findById(a.getMemorialId())
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (memorial.getVisibility() != VISIBILITY.PUBLIC && (user == null || user.getId() == null)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return albumPhotoRepository.findByAlbumId(albumId, pageable).map(PhotoResponse::from);
    }

    @Transactional
    public PhotoResponse createPhoto(Long albumId, UserInfo user, PhotoCreateRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        Album a = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 작성 권한: 앨범 소유자만으로 제한
        if (!user.getId().equals(a.getCreatedBy())) throw new ApiException(ErrorCode.FORBIDDEN);
        AlbumPhoto p = new AlbumPhoto();
        p.setAlbumId(albumId);
        p.setPhotoUrl(request.getPhotoUrl());
        p.setCaption(request.getCaption());
        p.setUploadedBy(user.getId());
        albumPhotoRepository.save(p);
        return PhotoResponse.from(p);
    }

    @Transactional
    public void updatePhoto(Long photoId, UserInfo user, PhotoUpdateRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        AlbumPhoto p = albumPhotoRepository.findById(photoId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        Album a = albumRepository.findById(p.getAlbumId()).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 수정 권한: 업로더 또는 앨범 소유자
        if (!user.getId().equals(p.getUploadedBy()) && !user.getId().equals(a.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getCaption() != null) p.setCaption(request.getCaption());
        albumPhotoRepository.save(p);
    }

    @Transactional
    public void deletePhoto(Long photoId, UserInfo user) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        AlbumPhoto p = albumPhotoRepository.findById(photoId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        Album a = albumRepository.findById(p.getAlbumId()).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(p.getUploadedBy()) && !user.getId().equals(a.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        albumPhotoRepository.delete(p);
    }
}
