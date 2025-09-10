package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.dto.ChatRequest;
import com.koreatravel.tabitomo.dto.ChatResponse;
import com.koreatravel.tabitomo.entity.ChatQA;
import com.koreatravel.tabitomo.entity.ForbiddenWord;
import com.koreatravel.tabitomo.entity.MainCategory;
import com.koreatravel.tabitomo.entity.SubCategory;
import com.koreatravel.tabitomo.repository.ChatQARepository;
import com.koreatravel.tabitomo.repository.ForbiddenWordRepository;
import com.koreatravel.tabitomo.repository.MainCategoryRepository;
import com.koreatravel.tabitomo.repository.SubCategoryRepository;
import com.koreatravel.tabitomo.service.AnswerService;
import com.koreatravel.tabitomo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") // 기본 경로를 /api로 변경
@RequiredArgsConstructor
public class ChatController {

    private final ChatQARepository chatQARepository;
    private final ForbiddenWordRepository forbiddenWordRepository;
    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final AnswerService answerService;
    private final ChatService chatService;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    /**
     * 모든 메인 카테고리 목록을 조회합니다.
     * @return 모든 MainCategory 리스트
     */
    @GetMapping("/main-categories")
    public List<MainCategory> getMainCategories() {
        return mainCategoryRepository.findAll();
    }

    /**
     * 특정 메인 카테고리에 속한 모든 서브 카테고리 목록을 조회합니다.
     * @param mainCategoryId 메인 카테고리 ID
     * @return 해당 메인 카테고리의 SubCategory 리스트
     */
    @GetMapping("/sub-categories/{mainCategoryId}")
    public ResponseEntity<List<SubCategory>> getSubCategoriesByMainCategory(@PathVariable Integer mainCategoryId) {
        try {
            List<SubCategory> subCategories = subCategoryRepository.findByMainCategoryId(mainCategoryId);
            return ResponseEntity.ok(subCategories);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 서브 카테고리에 속한 모든 질문(ChatQA) 목록을 조회합니다.
     * @param subCategoryId 서브 카테고리 ID
     * @return 해당 서브 카테고리의 ChatQA 리스트
     */
    @GetMapping("/sub-categories/{subCategoryId}/qas") // ✅ 경로 재수정
    public ResponseEntity<List<ChatQA>> getQuestionsBySubCategory(@PathVariable Integer subCategoryId) {
        try {
            List<ChatQA> questions = chatQARepository.findBySubCategoryId(subCategoryId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 질문(ChatQA)의 답변을 조회합니다.
     * @param qaId ChatQA ID
     * @return 답변 텍스트
     */
    @GetMapping("/answer/{qaId}")
    public ResponseEntity<String> getAnswerByQaId(@PathVariable Integer qaId) {
        try {
            return chatQARepository.findById(qaId)
                    .map(ChatQA::getAnswer)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("답변을 찾을 수 없습니다.");
        }
    }

    @PostMapping("/forbidden-words")
    public Map<String, Boolean> checkForbiddenWord(@RequestBody Map<String, String> request) {
        String inputWord = request.get("word");
        Map<String, Boolean> response = new HashMap<>();

        List<ForbiddenWord> forbiddenWords = forbiddenWordRepository.findAll();

        boolean isForbidden = forbiddenWords.stream()
                .anyMatch(forbidden -> inputWord.toLowerCase().contains(forbidden.getWord().toLowerCase()));

        response.put("isForbidden", isForbidden);
        return response;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        // ChatService의 새로운 메서드를 호출합니다.
        ChatResponse response = chatService.handleChatRequest(request);

        // 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }
}
