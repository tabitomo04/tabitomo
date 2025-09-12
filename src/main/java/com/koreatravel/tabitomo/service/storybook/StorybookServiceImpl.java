package com.koreatravel.tabitomo.service.storybook;

import com.koreatravel.tabitomo.domain.dto.storybook.*;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.TempsaveEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.storybook.StorybookRepository;
import com.koreatravel.tabitomo.repository.storybook.TempsaveRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorybookServiceImpl implements StorybookService {

    private final StorybookRepository storybookRepository;
    private final TempsaveRepository tempsaveRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public StorybookDTO getstory(Integer booknum) {
        StorybookEntity storybook = storybookRepository.findById(booknum)
                .orElseThrow(() -> new EntityNotFoundException("Storybook not found with id: " + booknum));
        
        return StorybookDTO.builder()
                .booknum(storybook.getBookNum())
                .title(storybook.getTitle())
                .subtitle(storybook.getSubtitle())
                .content(storybook.getContent())
                .createDate(storybook.getCreateDate())
                .updateDate(storybook.getUpdateDate())
                .likes(storybook.getLikes())
                .build();
    }

    @Override
    @Transactional
    public Integer tempsave(SaveRequestDTO saveRequestDTO) {
        MemberEntity member = memberRepository.findByEmail(saveRequestDTO.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        TempsaveEntity tempsave = TempsaveEntity.builder()
                .title(saveRequestDTO.getTitle())
                .subtitle(saveRequestDTO.getSubtitle())
                .content(saveRequestDTO.getContent())
                .member(member)
                .saveTime(LocalDateTime.now())
                .status("TEMP")
                .build();

        if (saveRequestDTO.getBooknum() != null) {
            StorybookEntity storybook = storybookRepository.findById(saveRequestDTO.getBooknum())
                    .orElseThrow(() -> new EntityNotFoundException("Storybook not found"));
            tempsave.setStorybook(storybook);
        }

        return tempsaveRepository.save(tempsave).getTempId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorybookListDTO> getStorybookList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<StorybookListDTO> page = storybookRepository.findStorybookList(pageable);
        return page.getContent();
    }

    @Override
    @Transactional
    public void delete(Integer booknum) {
        StorybookEntity storybook = storybookRepository.findById(booknum)
                .orElseThrow(() -> new EntityNotFoundException("Storybook not found"));
        storybookRepository.delete(storybook);
    }

    @Override
    @Transactional(readOnly = true)
    public Object gettemp(Integer tempId) {
        return tempsaveRepository.findById(tempId)
                .map(temp -> TempsaveDTO.builder()
                        .tempId(temp.getTempId())
                        .title(temp.getTitle())
                        .subtitle(temp.getSubtitle())
                        .content(temp.getContent())
                        .saveTime(temp.getSaveTime())
                        .booknum(temp.getStorybook() != null ? temp.getStorybook().getBookNum() : null)
                        .status(temp.getStatus())
                        .build())
                .orElseThrow(() -> new EntityNotFoundException("Temporary save not found"));
    }

    @Override
    @Transactional
    public Integer saveTempAsPost(SaveRequestDTO saveRequestDTO) {
        MemberEntity member = memberRepository.findByEmail(saveRequestDTO.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        // Create a new storybook from temp save
        StorybookEntity storybook = StorybookEntity.builder()
                .title(saveRequestDTO.getTitle())
                .subtitle(saveRequestDTO.getSubtitle())
                .content(saveRequestDTO.getContent())
                .member(member)
                .likes(0)
                .build();

        // Extract thumbnail from content
        String thumbnail = extractThumbnail(saveRequestDTO.getContent());
        storybook.setThumbnail(thumbnail);

        return storybookRepository.save(storybook).getBookNum();
    }

    @Override
    @Transactional
    public Integer savePost(SaveRequestDTO saveRequestDTO) {
        MemberEntity member = memberRepository.findByEmail(saveRequestDTO.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (saveRequestDTO.getBooknum() != null) {
            // Update existing storybook
            StorybookEntity storybook = storybookRepository.findById(saveRequestDTO.getBooknum())
                    .orElseThrow(() -> new EntityNotFoundException("Storybook not found"));
            
            storybook.updateTitle(saveRequestDTO.getTitle());
            storybook.setSubtitle(saveRequestDTO.getSubtitle());
            storybook.setContent(saveRequestDTO.getContent());
            
            // Update thumbnail if content has changed
            String thumbnail = extractThumbnail(saveRequestDTO.getContent());
            storybook.setThumbnail(thumbnail);
            
            // Save the updated storybook and return its ID
            return storybookRepository.save(storybook).getBookNum();
        } else {
            // Create new storybook
            StorybookEntity storybook = StorybookEntity.builder()
                    .title(saveRequestDTO.getTitle())
                    .subtitle(saveRequestDTO.getSubtitle())
                    .content(saveRequestDTO.getContent())
                    .member(member)
                    .likes(0)
                    .build();
            
            // Extract thumbnail from content
            String thumbnail = extractThumbnail(saveRequestDTO.getContent());
            storybook.setThumbnail(thumbnail);
            
            return storybookRepository.save(storybook).getBookNum();
        }
    }

    @Override
    @Transactional
    public void tempdel(Integer tempId) {
        TempsaveEntity tempsave = tempsaveRepository.findById(tempId)
                .orElseThrow(() -> new EntityNotFoundException("Temporary save not found"));
        tempsaveRepository.delete(tempsave);
    }

    private String extractThumbnail(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        try {
            Document doc = Jsoup.parse(content);
            Elements imgElements = doc.select("img[src]");
            
            if (!imgElements.isEmpty()) {
                return imgElements.first().attr("src");
            }
        } catch (Exception e) {
            log.error("Error extracting thumbnail from content", e);
        }
        
        return null;
    }
}
