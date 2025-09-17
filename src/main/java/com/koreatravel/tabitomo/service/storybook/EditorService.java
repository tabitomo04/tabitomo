package com.koreatravel.tabitomo.service.storybook;


import com.koreatravel.tabitomo.domain.dto.storybook.SaveRequestDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;

import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.*;
import com.koreatravel.tabitomo.domain.entity.tag.StoryTagEntity;
import com.koreatravel.tabitomo.domain.entity.tag.TagMasterEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.storybook.LikedbookRepository;
import com.koreatravel.tabitomo.repository.storybook.MediaRepository;
import com.koreatravel.tabitomo.repository.storybook.StorybookRepository;
import com.koreatravel.tabitomo.repository.storybook.TempsaveRepository;
import com.koreatravel.tabitomo.repository.tag.StorytagRepository;
import com.koreatravel.tabitomo.repository.tag.TagMasterRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EditorService {



    @Autowired
    private StorybookRepository storybookRepository;
    @Autowired
    private TempsaveRepository tempsaveRepository;
    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private LikedbookRepository likedbookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TagMasterRepository tagMasterRepository;
    @Autowired
    private StorytagRepository storytagRepository;



    /**
     * 글 가져오기 서비스
     * @param booknum 해당 글 booknum
     * @return 해당 글 내용
     */
    public StorybookDTO getstory(Integer booknum) {
        StorybookEntity entity = storybookRepository.findById(booknum)
                .orElseThrow(() -> new EntityNotFoundException(booknum + " : 해당번호 없음"));

        // 태그마스터에서 태그이름 가져오기
        List<String> tagNames = new ArrayList<>();

        for (StoryTagEntity storytag : entity.getTags()){
            TagMasterEntity tagId = storytag.getTag(); //storytag에서 tagmaster의 태그 찾음
            String tagName = tagId.getTagName(); // tagmaster에서 tagID로 tagName찾음
            tagNames.add(tagName);
        }


        // 글 + 미디어 DTO로 변환
        StorybookDTO dto = StorybookDTO.builder()
                .booknum(entity.getBooknum())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .content(entity.getContent())
                .createDate(entity.getCreateDate())
                .likes(entity.getLikes())
                .tags(tagNames)
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
            entity.setTags(String.join(",", saveRequestDTO.getTemptags()));
            entity.setContent(saveRequestDTO.getContent());

            // temp 미디어 조회
            List<MediaEntity> mediaList = mediaRepository.findByNumAndStatus(saveRequestDTO.getTempId(), "temp");

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

            // 기존 임시저장 글의 미디어 삭제 (temp)
            mediaRepository.deleteByNumAndStatus(saveRequestDTO.getTempId(), "temp");
        }
        else {
            // 새 임시저장
            entity = TempsaveEntity.builder()
                    .title(saveRequestDTO.getTitle())
                    .subtitle(saveRequestDTO.getSubtitle())
                    .content(saveRequestDTO.getContent())
                    .tags(String.join(",", saveRequestDTO.getTemptags()))
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
                    .num(tempId)
                    .status("temp")
                    .mediaUrl(src)
                    .mediaType("image")
                    .uploadTime(LocalDateTime.now())
                    .build();

            mediaRepository.save(media);
        }

        // youtube 링크 저장
        for (Element video : videos) {
            String videoUrl = video.attr("data-oembed-url");

            MediaEntity media = MediaEntity.builder()
                    .num(tempId)
                    .status("temp")
                    .mediaUrl(videoUrl)
                    .mediaType("video")
                    .uploadTime(LocalDateTime.now())
                    .build();

            mediaRepository.save(media);
        }

    }

    /**
     * 마이페이지의 스토리북 리스트 가져오기
     * @return 쿼리에 해당하는 리스트 가져옴
     */
    public List<StorybookListDTO> getMyStorybookList() {
        return storybookRepository.StorybookList(Sort.by(Sort.Direction.DESC, "createDate"));
    }

    /**
     * 스토리북 삭제
     * @param booknum 삭제하고자 하는 스토리북 번호
     */
    @Transactional
    public void delete(Integer booknum) {

        // 미디어 조회
        List<MediaEntity> mediaList = mediaRepository.findByNumAndStatus(booknum, "upload");

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
        mediaRepository.deleteByNum(booknum);

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
                .tags(entity.getTags())
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
            List<MediaEntity> mediaList = mediaRepository.findByNumAndStatus(saveRequestDTO.getTempId(), "temp");

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

            mediaRepository.deleteByNumAndStatus(saveRequestDTO.getTempId(),"temp");
            tempsaveRepository.deleteById(saveRequestDTO.getTempId());
        }

        // 스토리북 새 글로 저장
        StorybookEntity entity = StorybookEntity.builder()
                .title(saveRequestDTO.getTitle())
                .subtitle(saveRequestDTO.getSubtitle())
                .content(saveRequestDTO.getContent())
                .build();

        entity = storybookRepository.save(entity);
        // 해시태그 저장
        // 태그마스터에 없는 새로운 태그가 있을 시 저장
        for (String tagName : saveRequestDTO.getTags()) {
            TagMasterEntity tagentity = tagMasterRepository.findBytagName(tagName)
                    .orElseGet(() -> tagMasterRepository.save(
                            TagMasterEntity.builder()
                                    .tagName(tagName)
                                    .build()
                    ));
            StoryTagEntity storytag = new StoryTagEntity();
            storytag.setStorybook(entity);
            storytag.setTag(tagentity);
            storytagRepository.save(storytag);
        }

        return entity.getBooknum();
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

            // 기존 스토리태그 삭제
            storytagRepository.deleteByStorybook_Booknum(saveRequestDTO.getBooknum());

            // 기존 글 수정 후 저장
            entity = storybookRepository.findById(saveRequestDTO.getBooknum())
                    .orElseThrow(() -> new RuntimeException("게시물 없음"));
            entity.setTitle(saveRequestDTO.getTitle());
            entity.setSubtitle(saveRequestDTO.getSubtitle());
            entity.setContent(saveRequestDTO.getContent());

            // 해시태그 저장
            for (String tagName : saveRequestDTO.getTags()) {
                TagMasterEntity tagentity = tagMasterRepository.findBytagName(tagName)
                        .orElseGet(() -> tagMasterRepository.save(
                                TagMasterEntity.builder()
                                        .tagName(tagName)
                                        .build()
                        ));

                StoryTagEntity storytag = new StoryTagEntity();
                storytag.setStorybook(entity);
                storytag.setTag(tagentity);
                storytagRepository.save(storytag);
            }


            // 미디어 조회
            List<MediaEntity> mediaList = mediaRepository.findByNumAndStatus(saveRequestDTO.getBooknum(), "upload");

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
            mediaRepository.deleteByNumAndStatus(saveRequestDTO.getBooknum(), "upload");

        } else {
            // 새 글 저장
            entity = StorybookEntity.builder()
                    .title(saveRequestDTO.getTitle())
                    .subtitle(saveRequestDTO.getSubtitle())
                    .content(saveRequestDTO.getContent())
                    .build();
            entity = storybookRepository.save(entity);

            // 해시태그 저장
            for (String tagName : saveRequestDTO.getTags()) {
                TagMasterEntity tagentity = tagMasterRepository.findBytagName(tagName)
                        .orElseGet(() -> tagMasterRepository.save(
                                TagMasterEntity.builder()
                                        .tagName(tagName)
                                        .build()
                        ));

                StoryTagEntity storytag = new StoryTagEntity();
                storytag.setStorybook(entity);
                storytag.setTag(tagentity);
                storytagRepository.save(storytag);
            }
        }


        return entity.getBooknum();
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
                    .num(booknum)
                    .status("upload")
                    .mediaUrl(img.attr("src"))
                    .mediaType("image")
                    .uploadTime(LocalDateTime.now())
                    .build();
            mediaRepository.save(media);
        }

        // video 저장
        for (Element video : videos) {
            MediaEntity media = MediaEntity.builder()
                    .num(booknum)
                    .status("upload")
                    .mediaUrl(video.attr("data-oembed-url"))
                    .mediaType("video")
                    .uploadTime(LocalDateTime.now())
                    .build();
            mediaRepository.save(media);
        }
    }

    @Transactional
    public void tempdel(Integer tempId) {
        // 미디어 조회
        List<MediaEntity> mediaList = mediaRepository.findByNumAndStatus(tempId, "temp");

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
        mediaRepository.deleteByNum(tempId);

        // 스토리북 삭제
        TempsaveEntity entity = tempsaveRepository.findById(tempId).orElse(null);
        if (entity != null) {
            tempsaveRepository.delete(entity);
        }
    }

    /**
     * 임시저장 리스트 가져오기
     * @return
     */
    public List<TempsaveDTO> getTempsaveList() {
        List<TempsaveEntity> listEntity = tempsaveRepository.findAll();
        List<TempsaveDTO> listDTO = new ArrayList<>();

        for(TempsaveEntity entity : listEntity) {
            TempsaveDTO dto = TempsaveDTO.builder()
                    .tempId(entity.getTempId())
                    .title(entity.getTitle())
                    .subtitle(entity.getSubtitle())
                    .updateDate(entity.getUpdateDate())
                    .build();
            listDTO.add(dto);
        }
        return listDTO;
    }

    /**
     * 좋아요 토글
     * @param booknum 해당 글 번호
     * @param memberId 유저 memberId
     * @return
     */
    @Transactional
    public int likeBook(Integer booknum, UUID memberId) {
        StorybookEntity bookentity = storybookRepository.findById(booknum)
                .orElseThrow(() -> new RuntimeException("해당 booknum 존재하지 않음"));
        MemberEntity memberentity = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 memberId 존재하지 않음"));

        bookentity.setLikes(bookentity.getLikes() + 1);
        storybookRepository.save(bookentity);

        if (!likedbookRepository.existsByStorybookAndMember(bookentity, memberentity)) {
            LikedbookEntity liked = LikedbookEntity.builder()
                    .storybook(bookentity)
                    .member(memberentity)
                    .createDate(LocalDateTime.now())
                    .build();
            likedbookRepository.save(liked);
        }

        return bookentity.getLikes();
    }

    /**
     * 좋아요 취소
     * @param booknum 해당 글 번호
     * @param memberId 유저 memberId
     * @return
     */
    @Transactional
    public int unlikeBook(Integer booknum, UUID memberId) {
        StorybookEntity bookentity = storybookRepository.findById(booknum)
                .orElseThrow(() -> new RuntimeException("해당 booknum 존재하지 않음"));

        MemberEntity memberentity = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 memberId 존재하지 않음"));

        bookentity.setLikes(Math.max(bookentity.getLikes() - 1, 0));
        storybookRepository.save(bookentity);
        if (likedbookRepository.existsByStorybookAndMember(bookentity, memberentity)) {
            likedbookRepository.deleteByStorybookAndMember(bookentity, memberentity);
        }
        return bookentity.getLikes();
    }

    /**
     * 좋아요 유무 확인
     * @param booknum 해당 글번호
     * @param memberId 유저 memberId
     * @return
     */
    @Transactional
    public boolean isLiked(Integer booknum, UUID memberId) {
        StorybookEntity bookentity = storybookRepository.findById(booknum)
                .orElseThrow(() -> new RuntimeException("해당 booknum 존재하지 않음"));
        MemberEntity memberentity = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 memberId 존재하지 않음"));
        return likedbookRepository.existsByStorybookAndMember(bookentity, memberentity);
    }

    /**
     * 스토리 리스트 페이지 (랜덤)
     * @return
     */
    public List<StorybookListDTO> getStorybookList(String sort) {
        return storybookRepository.findListRandom();
    }

    /**
     * hot or new를 눌렀을 때 리스트 불러오기 (페이징)
     * @param sort random, new, hot 종류
     * @param page
     * @param size
     * @return
     */
    public Page<StorybookListDTO> gethotORnewList(String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return storybookRepository.hotORnewPage(sort,pageable);
    }

    public List<String> gettaglist() {
        return tagMasterRepository.findRandomTagNames();
    }

}

