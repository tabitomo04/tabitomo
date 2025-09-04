package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.dto.ChatRequest;
import com.koreatravel.tabitomo.dto.ChatResponse;
import com.koreatravel.tabitomo.entity.ChatCategory;
import com.koreatravel.tabitomo.entity.ChatQA;
import com.koreatravel.tabitomo.entity.ForbiddenWord;
import com.koreatravel.tabitomo.repository.ChatCategoryRepository;
import com.koreatravel.tabitomo.repository.ChatQARepository;
import com.koreatravel.tabitomo.repository.ForbiddenWordRepository;
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
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatQARepository chatQARepository;
    private final ForbiddenWordRepository forbiddenWordRepository;
    private final ChatCategoryRepository chatCategoryRepository; // ChatCategoryRepository 추가
    private final AnswerService answerService;
    private final ChatService chatService;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @GetMapping("/categories")
    public List<ChatCategory> getCategories() {
        return chatCategoryRepository.findAll(); // ChatCategory 목록을 직접 반환하도록 수정
    }

    @GetMapping("/qa/{categoryId}")
    public ResponseEntity<List<ChatQA>> getQuestionsByCategory(@PathVariable Integer categoryId) {
        try {
            List<ChatQA> questions = chatQARepository.findByCategoryId(categoryId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    @PostMapping("/check-word")
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
