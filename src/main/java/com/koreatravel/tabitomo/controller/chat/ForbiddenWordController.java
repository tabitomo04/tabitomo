package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.service.chat.ForbiddenWordService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ForbiddenWordController {

    private final ForbiddenWordService forbiddenWordService;

    public ForbiddenWordController(ForbiddenWordService forbiddenWordService) {
        this.forbiddenWordService = forbiddenWordService;
    }

    @PostMapping("/check-word")
    public Map<String, Boolean> checkForbiddenWord(@RequestBody Map<String, String> request) {
        String word = request.get("word");
        boolean isForbidden = forbiddenWordService.containsForbiddenWord(word);
        return Map.of("isForbidden", isForbidden);
    }
}