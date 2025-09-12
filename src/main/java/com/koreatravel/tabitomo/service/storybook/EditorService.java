package com.koreatravel.tabitomo.service.storybook;

import com.koreatravel.tabitomo.domain.dto.storybook.SaveRequestDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.trip.TempsaveDTO;
import com.koreatravel.tabitomo.domain.entity.storybook.MediaEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.MediaEntity.MediaStatus;
import com.koreatravel.tabitomo.domain.entity.storybook.MediaEntity.MediaType;
import com.koreatravel.tabitomo.domain.entity.storybook.LikeEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.TempsaveEntity;
import com.koreatravel.tabitomo.repository.storybook.LikeRepository;
import com.koreatravel.tabitomo.repository.storybook.MediaRepository;
import com.koreatravel.tabitomo.repository.storybook.StorybookRepository;
import com.koreatravel.tabitomo.repository.storybook.TempsaveRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

@Service
@Slf4j
public class EditorService implements StorybookService {



    @Autowired
    private StorybookRepository storybookRepository;
    @Autowired
    private TempsaveRepository tempsaveRepository;
    @Autowired
    private MediaRepository mediaRepository;
    
    @Autowired
    private LikeRepository likeRepository;
    
    // MemberRepository is not used as we're now using email directly


    /**
     * 글 가져오기 서비스
     * @param booknum 해당 글 booknum
     * @return 해당 글 내용
     */
    /**
     * 스토리북 조회
     */
    public StorybookDTO getstory(Integer booknum) {
        StorybookEntity entity = storybookRepository.findById(booknum)
                .orElseThrow(() -> new EntityNotFoundException(booknum + " : 해당번호 없음"));

        // 글 + 미디어 DTO로 변환
        StorybookDTO dto = StorybookDTO.builder()
                .booknum(entity.getBookNum())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .content(entity.getContent())
                .createDate(entity.getCreatedAt())
                .likes(entity.getLikes())
                .build();

        return dto;
    }


    @Transactional
    public Integer tempsave(SaveRequestDTO saveRequestDTO) {
        TempsaveEntity entity;

        if (saveRequestDTO.getTempId() != null) {
            // 기존 임시저장 글 수정
            entity = tempsaveRepository.findById(saveRequestDTO.getTempId())
                    .orElseThrow(() -> new RuntimeException("게시물 없음"));
            entity.setTitle(saveRequestDTO.getTitle());
            entity.setSubtitle(saveRequestDTO.getSubtitle());
            entity.setContent(saveRequestDTO.getContent());

            // temp 미디어 조회
            List<MediaEntity> mediaList = mediaRepository.findByBookNumAndStatus(saveRequestDTO.getTempId(), MediaStatus.TEMP);

            // 서버 파일 삭제
            for (MediaEntity media : mediaList) {
                // mediaUrl: /uploadedImages/파일명
                String fileName = Paths.get(media.getMediaUrl()).getFileName().toString();
                String filePath = "C:/workspace1/editorTest/uploadedImages/" + fileName;

                File file = new File(filePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        System.out.println("파일 삭제 실패: " + filePath);
                    }
                }
            }

            // 미디어 데이터 삭제
            mediaRepository.deleteByBookNumAndStatus(saveRequestDTO.getTempId(), MediaEntity.MediaStatus.TEMP);
        } else {
            // 새 임시저장
            entity = TempsaveEntity.builder()
                    .title(saveRequestDTO.getTitle())
                    .subtitle(saveRequestDTO.getSubtitle())
                    .content(saveRequestDTO.getContent())
                    .build();
        }
        tempsaveRepository.save(entity);

        return entity.getTempId();
    }

    /**
     * 임시저장 글의 미디어테이블에 저장
     * @param tempId 임시저장 글 번호
     * @param content 임시저장 내용
     */
    public void tempsavemedia(Integer tempId, String content) {

        Document doc = Jsoup.parse(content);
        Elements images = doc.select("img");
        Elements videos = doc.select("div[data-oembed-url]");

        // image 저장
        for (Element img : images) {
            String src = img.attr("src");

            MediaEntity media = MediaEntity.builder()
                    .displayOrder(tempId)
                    .status(MediaStatus.TEMP)
                    .mediaUrl(src)
                    .mediaType(MediaType.IMAGE)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            mediaRepository.save(media);
        }

        // youtube 링크 저장
        for (Element video : videos) {
            String videoUrl = video.attr("data-oembed-url");

            MediaEntity media = MediaEntity.builder()
                    .displayOrder(tempId)
                    .status(MediaStatus.TEMP)
                    .mediaUrl(videoUrl)
                    .mediaType(MediaType.VIDEO)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            mediaRepository.save(media);
        }
    }

    /**
     * 스토리북 리스트 가져오기
     * @return 쿼리에 해당하는 리스트 가져옴
     */
    public List<StorybookListDTO> getStorybookList() {
        return storybookRepository.StorybookList();
    }

    /**
     * 스토리북 삭제
     * @param booknum 삭제하고자 하는 스토리북 번호
     */
    @Transactional
    public void delete(Integer booknum) {

        // 미디어 조회
        List<MediaEntity> mediaList = mediaRepository.findByBookNumAndStatus(booknum, MediaStatus.UPLOAD);

        // 서버 파일 삭제
        for (MediaEntity media : mediaList) {
            // mediaUrl: /uploadedImages/파일명
            String fileName = Paths.get(media.getMediaUrl()).getFileName().toString();
            String filePath = "C:/workspace1/editorTest/uploadedImages/" + fileName;

            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.out.println("파일 삭제 실패: " + filePath);
                }
            }
        }

        // 미디어 데이터 삭제
        mediaRepository.deleteByBookNumAndStatus(booknum, MediaStatus.UPLOAD);

        // 스토리북 삭제
        StorybookEntity entity = storybookRepository.findById(booknum).orElse(null);
        if (entity != null) {
            storybookRepository.delete(entity);
        }

    }

    public TempsaveDTO gettemp(Integer tempId) {
        TempsaveEntity entity = tempsaveRepository.findById(tempId)
                .orElseThrow(() -> new EntityNotFoundException(tempId + " : 해당번호 없음"));


        // 글 + 미디어 DTO로 변환
        TempsaveDTO dto = TempsaveDTO.builder()
                .tempId(entity.getTempId())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .content(entity.getContent())
                .build();

        return dto;
    }

    /**
     * temp -> storybook
     * @param saveRequestDTO 저장하는 내용
     * @return
     */
    @Transactional
    public Integer saveTempAsPost(SaveRequestDTO saveRequestDTO) {
        // 기존 temp 글 삭제
        if (saveRequestDTO.getTempId() != null) {

            // 미디어 조회
            List<MediaEntity> mediaList = mediaRepository.findByBookNumAndStatus(saveRequestDTO.getTempId(), MediaEntity.MediaStatus.TEMP);

            // 서버 파일 삭제
            for (MediaEntity media : mediaList) {
                // mediaUrl: /uploadedImages/파일명
                String fileName = Paths.get(media.getMediaUrl()).getFileName().toString();
                String filePath = "C:/workspace1/editorTest/uploadedImages/" + fileName;

                File file = new File(filePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        System.out.println("파일 삭제 실패: " + filePath);
                    }
                }
            }

            mediaRepository.deleteByBookNumAndStatus(saveRequestDTO.getTempId(), MediaEntity.MediaStatus.TEMP);
            tempsaveRepository.deleteById(saveRequestDTO.getTempId());
        }

        // 스토리북 새 글로 저장
        StorybookEntity entity = StorybookEntity.builder()
                .title(saveRequestDTO.getTitle())
                .subtitle(saveRequestDTO.getSubtitle())
                .content(saveRequestDTO.getContent())
                .build();

        storybookRepository.save(entity);
        return entity.getBookNum();
    }

    /**
     * 스토리북 저장
     * @param saveRequestDTO
     * @return
     */
    @Transactional
    public Integer savePost(SaveRequestDTO saveRequestDTO) {
        StorybookEntity entity;

        if (saveRequestDTO.getBooknum() != null) {
            // 기존 글 수정 후 저장
            entity = storybookRepository.findById(saveRequestDTO.getBooknum())
                    .orElseThrow(() -> new RuntimeException("게시물 없음"));
            entity.setTitle(saveRequestDTO.getTitle());
            entity.setSubtitle(saveRequestDTO.getSubtitle());
            entity.setContent(saveRequestDTO.getContent());

            // 미디어 조회
            List<MediaEntity> mediaList = mediaRepository.findByBookNumAndStatus(saveRequestDTO.getBooknum(), MediaStatus.UPLOAD);

            // 서버 파일 삭제
            for (MediaEntity media : mediaList) {
                // mediaUrl: /uploadedImages/파일명
                String fileName = Paths.get(media.getMediaUrl()).getFileName().toString();
                String filePath = "C:/workspace1/editorTest/uploadedImages/" + fileName;

                File file = new File(filePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        System.out.println("파일 삭제 실패: " + filePath);
                    }
                }
            }

            // 기존 미디어 삭제 (upload)
            mediaRepository.deleteByBookNumAndStatus(saveRequestDTO.getBooknum(), MediaStatus.UPLOAD);
        } else {
            // 새 글 저장
            entity = StorybookEntity.builder()
                    .title(saveRequestDTO.getTitle())
                    .subtitle(saveRequestDTO.getSubtitle())
                    .content(saveRequestDTO.getContent())
                    .build();
        }

        storybookRepository.save(entity);
        return entity.getBookNum();
    }

    /**
     * 미디어 저장
     * @param booknum 게시글 번호
     * @param content 미디어 포함 컨텐츠 내용
     */
    @Transactional
    public void saveMedia(Integer booknum, String content) {
        Document doc = Jsoup.parse(content);
        Elements images = doc.select("img");
        Elements videos = doc.select("div[data-oembed-url]");

        // image 저장
        for (Element img : images) {
            MediaEntity media = MediaEntity.builder()
                    .bookNum(booknum)
                    .displayOrder(booknum)
                    .status(MediaStatus.UPLOAD)
                    .mediaUrl(img.attr("src"))
                    .mediaType(MediaType.IMAGE)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            mediaRepository.save(media);
        }

        // video 저장
        for (Element video : videos) {
            MediaEntity media = MediaEntity.builder()
                    .bookNum(booknum)
                    .displayOrder(booknum)
                    .status(MediaStatus.UPLOAD)
                    .mediaUrl(video.attr("data-oembed-url"))
                    .mediaType(MediaType.VIDEO)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            mediaRepository.save(media);
        }
    }

    /**
     * 임시 저장된 미디어 삭제
     * @param tempId 삭제할 임시 저장 ID
     */
    @Transactional
    public void tempdel(Integer tempId) {
        // 미디어 조회
        List<MediaEntity> mediaList = mediaRepository.findByBookNumAndStatus(tempId, MediaStatus.TEMP);

        // 서버 파일 삭제
        for (MediaEntity media : mediaList) {
            // mediaUrl: /uploadedImages/파일명
            String fileName = Paths.get(media.getMediaUrl()).getFileName().toString();
            String filePath = "C:/workspace1/editorTest/uploadedImages/" + fileName;

            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.out.println("파일 삭제 실패: " + filePath);
                }
            }
        }
        // 미디어 데이터 삭제
        mediaRepository.deleteByBookNumAndStatus(tempId, MediaStatus.TEMP);
    }
    
    /**
     * 스토리북에 좋아요 추가
     */
    @Transactional
    public int addLike(Integer booknum) {
        // 현재 인증된 사용자 이메일 가져오기
        String email = getCurrentUserEmail();
        if (email == null || "anonymousUser".equals(email)) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        
        // 이미 좋아요를 눌렀는지 확인
        if (likeRepository.existsByBooknumAndEmail(booknum, email)) {
            throw new IllegalStateException("이미 좋아요를 누르셨습니다.");
        }
        
        // 좋아요 추가
        LikeEntity like = LikeEntity.builder()
                .booknum(booknum)
                .email(email)
                .build();
        
        likeRepository.save(like);
        
        // 좋아요 수 업데이트
        StorybookEntity storybook = storybookRepository.findById(booknum)
                .orElseThrow(() -> new EntityNotFoundException("스토리북을 찾을 수 없습니다."));
        storybook.setLikes(storybook.getLikes() + 1);
        storybookRepository.save(storybook);
        
        return storybook.getLikes();
    }
    
    /**
     * 스토리북 좋아요 취소
     */
    @Transactional
    public int removeLike(Integer booknum) {
        // 현재 인증된 사용자 이메일 가져오기
        String email = getCurrentUserEmail();
        if (email == null || "anonymousUser".equals(email)) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        
        // 좋아요 삭제
        likeRepository.deleteByBooknumAndEmail(booknum, email);
        
        // 좋아요 수 업데이트
        StorybookEntity storybook = storybookRepository.findById(booknum)
                .orElseThrow(() -> new EntityNotFoundException("스토리북을 찾을 수 없습니다."));
        int newLikeCount = Math.max(0, storybook.getLikes() - 1);
        storybook.setLikes(newLikeCount);
        storybookRepository.save(storybook);
        
        return newLikeCount;
    }
    
    /**
     * 현재 사용자가 특정 스토리북에 좋아요를 눌렀는지 확인
     */
    public boolean isLikedByCurrentUser(Integer booknum) {
        // 현재 인증된 사용자 이메일 가져오기
        String email = getCurrentUserEmail();
        if (email == null || "anonymousUser".equals(email)) {
            return false;
        }
        
        try {
            return likeRepository.existsByBooknumAndEmail(booknum, email);
        } catch (Exception e) {
            log.error("Error checking like status", e);
            return false;
        }
    }
    
    /**
     * 현재 인증된 사용자의 이메일 가져오기
     */
    private String getCurrentUserEmail() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }
}

