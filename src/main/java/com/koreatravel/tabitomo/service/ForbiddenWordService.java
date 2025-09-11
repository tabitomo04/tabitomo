package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.repository.ForbiddenWordRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ForbiddenWordService {

    private final ForbiddenWordRepository forbiddenWordRepository;
    private Set<String> forbiddenWords;

    public ForbiddenWordService(ForbiddenWordRepository forbiddenWordRepository) {
        this.forbiddenWordRepository = forbiddenWordRepository;
        // 애플리케이션 시작 시 금지어 목록을 미리 로드
        this.forbiddenWords = forbiddenWordRepository.findAll().stream()
                .map(forbiddenWord -> forbiddenWord.getWord().toLowerCase())
                .collect(Collectors.toSet());
    }

    public boolean containsForbiddenWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String lowerCaseText = text.toLowerCase();
        // 금지어 목록을 순회하며 입력 텍스트에 포함되어 있는지 확인
        return forbiddenWords.stream()
                .anyMatch(lowerCaseText::contains);
    }
}
