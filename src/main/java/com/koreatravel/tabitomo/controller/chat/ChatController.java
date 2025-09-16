package com.koreatravel.tabitomo.controller.chat;

import com.koreatravel.tabitomo.domain.dto.chat.ChatRequest;
import com.koreatravel.tabitomo.domain.dto.chat.ChatResponse;
import com.koreatravel.tabitomo.domain.entity.chat.ForbiddenWord;
import com.koreatravel.tabitomo.repository.chat.ForbiddenWordRepository;
import com.koreatravel.tabitomo.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ForbiddenWordRepository forbiddenWordRepository;

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        // ChatService로 ChatRequest를 그대로 전달하여 언어 정보도 함께 넘김
        ChatResponse response = chatService.handleChatRequest(request);
        return ResponseEntity.ok(response);
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
}
