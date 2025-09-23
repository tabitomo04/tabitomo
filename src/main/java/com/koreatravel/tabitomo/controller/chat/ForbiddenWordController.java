package com.koreatravel.tabitomo.controller.chat;

import com.koreatravel.tabitomo.service.chat.ForbiddenWordService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/chat")
public class ForbiddenWordController {

    private final ForbiddenWordService forbiddenWordService;

    public ForbiddenWordController(ForbiddenWordService forbiddenWordService) {
        this.forbiddenWordService = forbiddenWordService;
    }

    /**
     * 입력된 단어에 금지어가 포함되어 있는지 확인합니다.
     *
     * @param request "word" 키를 가진 Map
     * @return "isForbidden" 키와 boolean 값을 가진 Map
     */
    @PostMapping("/check-word")
    public Map<String, Boolean> checkForbiddenWord(@RequestBody Map<String, String> request) {
        String word = request.get("word");
        boolean isForbidden = forbiddenWordService.containsForbiddenWord(word);
        return Map.of("isForbidden", isForbidden);
    }

    /**
     * (디버깅용) 현재 서비스에 로드된 금지어 목록을 반환합니다.
     * 이 API를 호출하여 백엔드에서 금지어를 제대로 로드했는지 확인할 수 있습니다.
     *
     * @return 금지어 목록 Set
     */
    @GetMapping("/forbidden-words")
    public Set<String> getForbiddenWords() {
        return forbiddenWordService.getForbiddenWordsForDebugging();
    }
}
