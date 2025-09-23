package com.koreatravel.tabitomo.service.chat;

import com.koreatravel.tabitomo.repository.chat.ForbiddenWordRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ForbiddenWordService {

    private final ForbiddenWordRepository forbiddenWordRepository;
    private final Set<String> forbiddenWords;

    public ForbiddenWordService(ForbiddenWordRepository forbiddenWordRepository) {
        this.forbiddenWordRepository = forbiddenWordRepository;
        // 애플리케이션 시작 시 금지어 목록을 미리 로드
        this.forbiddenWords = forbiddenWordRepository.findAll().stream()
                .map(forbiddenWord -> forbiddenWord.getWord().toLowerCase())
                .collect(Collectors.toSet());
    }

    /**
     * 입력된 텍스트에 금지어가 포함되어 있는지 확인합니다.
     *
     * @param text 확인할 텍스트
     * @return 금지어가 포함되어 있으면 true, 아니면 false
     */
    public boolean containsForbiddenWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String lowerCaseText = text.toLowerCase();
        // 금지어 목록을 순회하며 입력 텍스트에 포함되어 있는지 확인
        return forbiddenWords.stream()
                .anyMatch(lowerCaseText::contains);
    }

    /**
     * (디버깅용) 현재 서비스에 로드된 금지어 목록을 반환합니다.
     *
     * @return 금지어 목록 Set
     */
    public Set<String> getForbiddenWordsForDebugging() {
        return this.forbiddenWords;
    }
}
