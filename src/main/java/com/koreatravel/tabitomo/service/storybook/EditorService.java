package com.koreatravel.tabitomo.service.storybook;


import com.koreatravel.tabitomo.domain.dto.storybook.SaveRequestDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;

import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.domain.dto.trip.TripRegion;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.*;
import com.koreatravel.tabitomo.domain.entity.tag.StoryTagEntity;
import com.koreatravel.tabitomo.domain.entity.tag.TagMasterEntity;
import com.koreatravel.tabitomo.domain.entity.trip.Place;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.storybook.LikedbookRepository;
import com.koreatravel.tabitomo.repository.storybook.MediaRepository;
import com.koreatravel.tabitomo.repository.storybook.StorybookRepository;
import com.koreatravel.tabitomo.repository.storybook.TempsaveRepository;
import com.koreatravel.tabitomo.repository.tag.StorytagRepository;
import com.koreatravel.tabitomo.repository.tag.TagMasterRepository;

import com.koreatravel.tabitomo.repository.trip.TripRepository;
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
import java.lang.reflect.Member;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Autowired
    private TripRepository tripRepository;



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
                .memberId(entity.getMember().getId())
                .nickname(entity.getMember().getNickname())
                .createDate(entity.getCreateDate())
                .likes(entity.getLikes())
                .tags(tagNames)
                .build();

        return dto;
    }


    @Transactional
    public Integer tempsave(SaveRequestDTO saveRequestDTO, String userEmail) {
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

            // 기존 임시저장 글의 미디어 삭제 (temp)
            mediaRepository.deleteByNumAndStatus(saveRequestDTO.getTempId(), "temp");
        }
        else {

            MemberEntity member = memberRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("회원 없음"));

            // 새 임시저장
            entity = TempsaveEntity.builder()
                    .title(saveRequestDTO.getTitle())
                    .subtitle(saveRequestDTO.getSubtitle())
                    .content(saveRequestDTO.getContent())
                    .member(member)
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
    public List<StorybookListDTO> getMyStorybookList(UUID memberId) {
        return storybookRepository.StorybookList(memberId);
    }

    /**
     * 스토리북 삭제
     * @param booknum 삭제하고자 하는 스토리북 번호
     */
    @Transactional
    public void delete(Integer booknum) {

        // 미디어 조회
        List<MediaEntity> mediaList = mediaRepository.findByNumAndStatus(booknum, "upload");

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
        return TempsaveDTO.builder()
                .tempId(entity.getTempId())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .content(entity.getContent())
                .tags(entity.getTags())
                .memberId(entity.getMember().getId())
                .nickname(entity.getMember().getNickname())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .build();
    }

    /**
     * temp -> storybook
     * @param saveRequestDTO 저장하는 내용
     * @return
     */
    @Transactional
    public Integer saveTempAsPost(SaveRequestDTO saveRequestDTO, String userEmail) {
        // 기존 temp 글 삭제
        if (saveRequestDTO.getTempId() != null) {

            // 미디어 조회
            List<MediaEntity> mediaList = mediaRepository.findByNumAndStatus(saveRequestDTO.getTempId(), "temp");


            mediaRepository.deleteByNumAndStatus(saveRequestDTO.getTempId(),"temp");
            tempsaveRepository.deleteById(saveRequestDTO.getTempId());
        }
        MemberEntity member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        // 스토리북 새 글로 저장
        StorybookEntity entity = StorybookEntity.builder()
                .title(saveRequestDTO.getTitle())
                .subtitle(saveRequestDTO.getSubtitle())
                .content(saveRequestDTO.getContent())
                .member(member)
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

            boolean exists = storytagRepository.existsByStorybookAndTag(entity, tagentity);
            if (!exists) {
                StoryTagEntity storytag = new StoryTagEntity();
                storytag.setStorybook(entity);
                storytag.setTag(tagentity);
                storytagRepository.save(storytag);
            }

        }

        return entity.getBooknum();
    }

    /**
     * 스토리북 저장
     * @param saveRequestDTO
     * @return
     */
    @Transactional
    public Integer savePost(SaveRequestDTO saveRequestDTO, String userEmail) {
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


            // 기존 미디어 삭제 (upload)
            mediaRepository.deleteByNumAndStatus(saveRequestDTO.getBooknum(), "upload");

        } else {
            MemberEntity member = memberRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("회원 없음"));

            // 새 글 저장
            entity = StorybookEntity.builder()
                    .title(saveRequestDTO.getTitle())
                    .subtitle(saveRequestDTO.getSubtitle())
                    .content(saveRequestDTO.getContent())
                    .member(member)
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

/**
 * 임시저장 글 삭제
 * @param tempId 삭제할 임시저장 글 ID
 */
@Transactional
public void tempdel(Integer tempId) {
    // 미디어 조회
    List<MediaEntity> mediaList = mediaRepository.findByNumAndStatus(tempId, "temp");

        // 미디어 데이터 삭제
        mediaRepository.deleteByNum(tempId);

        // 임시저장 글 삭제
        TempsaveEntity entity = tempsaveRepository.findById(tempId).orElse(null);
        if (entity != null) {
            tempsaveRepository.delete(entity);
        }
    }

    /**
     * 임시저장 리스트 가져오기
     * @return 임시저장된 글 목록
     */
    public List<TempsaveDTO> getTempsaveList(String email) {
        return tempsaveRepository.findByMember_Email(email).stream()
                .map(entity -> TempsaveDTO.builder()
                        .tempId(entity.getTempId())
                        .title(entity.getTitle())
                        .subtitle(entity.getSubtitle())
                        .content(entity.getContent())
                        .tags(entity.getTags())
                        .memberId(entity.getMember().getId())
                        .nickname(entity.getMember().getNickname())
                        .createDate(entity.getCreateDate())
                        .updateDate(entity.getUpdateDate())
                        .build())
                .collect(Collectors.toList());
    }
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
        System.out.println("서비스");
        StorybookEntity bookentity = storybookRepository.findById(booknum)
                .orElseThrow(() -> new RuntimeException("해당 booknum 존재하지 않음"));
        MemberEntity memberentity = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 memberId 존재하지 않음"));

        System.out.println("bookentity: " + bookentity);
        System.out.println("memberentity: " + memberentity);
        return likedbookRepository.existsByStorybookAndMember(bookentity, memberentity);
    }

    /**
     * 스토리 리스트 페이지 (랜덤)
     * @return
     */
    public List<StorybookListDTO> getStorybookList(UUID loginUserId, String sort) {
        return storybookRepository.findListRandom(loginUserId);
    }

    /**
     * hot or new를 눌렀을 때 리스트 불러오기 (페이징)
     * @param sort random, new, hot 종류
     * @param page
     * @param size
     * @return
     */
    public Page<StorybookListDTO> gethotORnewList(UUID loginUserId, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return storybookRepository.hotORnewPage(loginUserId,sort,pageable);
    }

    // 태그의 화이트리스트 배열 가져오기
    public List<String> gettaglist() {
        return tagMasterRepository.findRandomTagNames();
    }

    /**
     * 스토리북 키워드 검색 결과
     * @param keyword 검색 키워드
     * @param page
     * @param size
     * @return
     */
    public Page<StorybookListDTO> getSearchList(UUID loginUserId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return storybookRepository.findbykeyword(loginUserId, keyword, pageable);
    }

    public List<StorybookListDTO> getmainStory() {
        return storybookRepository.findmainStory();
    }

    @Transactional
    public List<TripRegion> findTripsByEmail(String email) {
        List<Trip> tripsList = tripRepository.findByMemberEmail(email);
        List<TripRegion> result = new ArrayList<>();

        for(Trip trip : tripsList) {
            String region = null;
            if (trip.getAccommodation() != null) {
                region = trip.getAccommodation().getRegion();
            }

            result.add(new TripRegion(trip.getId(), trip.getTitle(), region));
        }
        return result;

    }

    @Transactional
    public String getRecentPlan(String email) {
        // 로그인 유저 찾기
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        // 최근 Trip 가져오기, accommodation도 fetch
        Trip recentTrip = tripRepository.findTopByMemberWithAccommodation(member)
                .orElse(null);

        if (recentTrip == null || recentTrip.getAccommodation() == null) {
            return null; // 최근 Trip 없거나 숙소 없음
        }

        // 숙소의 place에서 region 가져오기
        Place accommodation = recentTrip.getAccommodation();
        String region = accommodation.getRegion();
        System.out.println("지역"+ region);
        return region;
    }
}

