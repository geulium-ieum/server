package seg.work.geuliumieum.server.album.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.AlbumPhotoRepository;
import seg.work.geuliumieum.server.common.repository.AlbumRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.memorial.service.MemorialService;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final MemorialRepository memorialRepository;
    private final MemorialService memorialService;

    @Cacheable(cacheNames = "album:list", key = "#memorialId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Slice<AlbumResponse> listByMemorial(Long memorialId, @ParameterObject Pageable pageable, UserInfo userInfo) {
        memorialService.checkAccess(userInfo, memorialId);
        return albumRepository.findByMemorialId(memorialId, pageable).map(AlbumResponse::from);
    }

    @Cacheable(cacheNames = "album:detail", key = "#albumId")
    public AlbumResponse getAlbum(Long albumId, UserInfo userInfo) {
        Album album = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        memorialService.checkAccess(userInfo, album.getMemorialId());
        return AlbumResponse.from(album);
    }

    @Transactional
    @CacheEvict(cacheNames = "album:list", key = "#memorialId")
    public AlbumResponse createAlbum(Long memorialId, UserInfo userInfo, AlbumCreateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Album album = new Album();
        album.setMemorialId(memorialId);
        album.setTitle(request.getTitle());
        album.setDescription(request.getDescription());
        album.setCreatedBy(userInfo.getId());
        albumRepository.save(album);
        return AlbumResponse.from(album);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "album:detail", key = "#albumId"),
        @CacheEvict(cacheNames = "album:list", allEntries = true)
    })
    public void updateAlbum(Long albumId, UserInfo userInfo, AlbumUpdateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Album album = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(album.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getTitle() != null) {
            album.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            album.setDescription(request.getDescription());
        }
        albumRepository.save(album);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "album:detail", key = "#albumId"),
        @CacheEvict(cacheNames = "album:list", allEntries = true)
    })
    public void deleteAlbum(Long albumId, UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Album album = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(album.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        albumRepository.delete(album);
    }

    @Cacheable(cacheNames = "album:photo:list", key = "#albumId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Slice<PhotoResponse> listPhotos(Long albumId, @ParameterObject Pageable pageable, UserInfo userInfo) {
        Album album = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        memorialService.checkAccess(userInfo, album.getMemorialId());
        return albumPhotoRepository.findByAlbumId(albumId, pageable).map(PhotoResponse::from);
    }

    @Transactional
    @CacheEvict(cacheNames = "album:photo:list", key = "#albumId")
    public PhotoResponse createPhoto(Long albumId, UserInfo userInfo, PhotoCreateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Album album = albumRepository.findById(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 작성 권한: 앨범 소유자만으로 제한
        if (!userInfo.getId().equals(album.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        AlbumPhoto albumPhoto = new AlbumPhoto();
        albumPhoto.setAlbumId(albumId);
        albumPhoto.setPhotoUrl(request.getPhotoUrl());
        albumPhoto.setCaption(request.getCaption());
        albumPhoto.setUploadedBy(userInfo.getId());
        albumPhotoRepository.save(albumPhoto);
        return PhotoResponse.from(albumPhoto);
    }

    @Transactional
    @CacheEvict(cacheNames = "album:photo:list", allEntries = true)
    public void updatePhoto(Long photoId, UserInfo userInfo, PhotoUpdateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        AlbumPhoto albumPhoto = albumPhotoRepository.findById(photoId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        Album album = albumRepository.findById(albumPhoto.getAlbumId()).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 수정 권한: 업로더 또는 앨범 소유자
        if (!userInfo.getId().equals(albumPhoto.getUploadedBy()) && !userInfo.getId().equals(album.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getCaption() != null) {
            albumPhoto.setCaption(request.getCaption());
        }
        albumPhotoRepository.save(albumPhoto);
    }

    @Transactional
    @CacheEvict(cacheNames = "album:photo:list", allEntries = true)
    public void deletePhoto(Long photoId, UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        AlbumPhoto albumPhoto = albumPhotoRepository.findById(photoId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        Album album = albumRepository.findById(albumPhoto.getAlbumId()).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(albumPhoto.getUploadedBy()) && !userInfo.getId().equals(album.getCreatedBy())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        albumPhotoRepository.delete(albumPhoto);
    }
}
