package com.koreatravel.tabitomo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.koreatravel.tabitomo.repository.chat.ChatQARepository;
import com.koreatravel.tabitomo.repository.chat.ChatCategoryRepository;
import com.koreatravel.tabitomo.domain.dto.chat.ChatRequestDTO;
import com.koreatravel.tabitomo.domain.dto.chat.ChatResponseDTO;
import com.koreatravel.tabitomo.domain.entity.chat.ChatCategoryEntity;
import com.koreatravel.tabitomo.domain.entity.chat.ChatQAEntity;
import com.koreatravel.tabitomo.service.chat.AnswerService;
import com.koreatravel.tabitomo.service.chat.ChatService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.koreatravel.tabitomo.PathConstants;

@RestController
@RequestMapping(PathConstants.CHAT)
@RequiredArgsConstructor
public class ChatController {

    private final ChatQARepository chatQARepository;
    private final ChatCategoryRepository chatCategoryRepository; 
    private final AnswerService answerService;
    private final ChatService chatService;

    @Value("${spring.ai.vertex.ai.gemini.api-endpoint}")
    private String geminiApiEndpoint;
    
    @Value("${spring.ai.vertex.ai.project-id}")
    private String projectId;
    
    @Value("${spring.ai.vertex.ai.location}")
    private String location;
    
    @Value("${app.google.cloud.translation.api-key}")
    private String geminiApiKey;

    @GetMapping("/categories")
    public List<ChatCategoryEntity> getCategories() {
        return chatCategoryRepository.findAll();
    }
    
    @PostMapping("/ask")
    public ResponseEntity<?> chat(@RequestBody ChatRequestDTO request) {
        try {
            // 1. 채팅 요청 처리 (ChatService의 handleChatRequest 메서드 사용)
            ChatResponseDTO response = chatService.handleChatRequest(request);
            
            // 2. 언어 처리 (요청에 언어가 있는 경우)
            if (request.getLanguage() != null) {
                String processedLanguage = processLanguageCode(request.getLanguage());
                response.setLanguage(processedLanguage);
            }
            
            // 3. 응답 반환
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private String processLanguageCode(String lang) {
        return chatService.processLanguageCode(lang);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ChatQAEntity>> getQuestionsByCategory(@PathVariable Integer categoryId) {
        try {
            List<ChatQAEntity> questions = chatQARepository.findByCategoryId(categoryId);
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
                    .map(ChatQAEntity::getAnswer)
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
        
        // Use chatService to check for forbidden words
        boolean isForbidden = chatService.containsForbiddenWord(inputWord);
        
        response.put("isForbidden", isForbidden);
        return response;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatResponseDTO> sendMessage(@RequestBody ChatRequestDTO request) {
        // ChatService의 새로운 메서드를 호출합니다.
        ChatResponseDTO response = chatService.handleChatRequest(request);

        // 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }
}
