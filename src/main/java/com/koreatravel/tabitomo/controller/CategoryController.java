package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.dto.CategoryDto;
import com.koreatravel.tabitomo.dto.QaDto;
import com.koreatravel.tabitomo.entity.ChatQA;
import com.koreatravel.tabitomo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/main-categories")
    public ResponseEntity<List<CategoryDto>> getMainCategories(@RequestParam(required = false) String lang) {
        List<CategoryDto> mainCategories = categoryService.getMainCategories(lang);
        return ResponseEntity.ok(mainCategories);
    }

    @GetMapping("/sub-categories/{mainCategoryId}")
    public ResponseEntity<List<CategoryDto>> getSubCategories(@PathVariable Integer mainCategoryId, @RequestParam(required = false) String lang) {
        List<CategoryDto> subCategories = categoryService.getSubCategories(mainCategoryId, lang);
        return ResponseEntity.ok(subCategories);
    }

    @GetMapping("/sub-categories/{subCategoryId}/qas")
    public ResponseEntity<List<QaDto>> getQasBySubCategoryId(@PathVariable Integer subCategoryId, @RequestParam(required = false) String lang) {
        List<QaDto> qas = categoryService.getQasBySubCategoryId(subCategoryId, lang);
        return ResponseEntity.ok(qas);
    }

    @GetMapping("/answer/{qaId}")
    public ResponseEntity<String> getAnswerByQaId(@PathVariable Integer qaId, @RequestParam(required = false) String lang) {
        Optional<ChatQA> optionalChatQa = categoryService.getAnswerByQaId(qaId);
        if (optionalChatQa.isPresent()) {
            ChatQA chatQa = optionalChatQa.get();
            String answer;
            switch (lang != null ? lang.toLowerCase() : "ko") {
                case "en":
                    answer = chatQa.getAnswerEn();
                    break;
                case "ja":
                    answer = chatQa.getAnswerJa();
                    break;
                default:
                    answer = chatQa.getAnswerKo();
                    break;
            }
            if (answer == null || answer.isEmpty()) {
                // 해당 언어의 답변이 없을 경우 기본 언어(한국어)로 대체
                answer = chatQa.getAnswerKo();
            }
            return ResponseEntity.ok(answer);
        }
        return ResponseEntity.notFound().build();
    }
}
