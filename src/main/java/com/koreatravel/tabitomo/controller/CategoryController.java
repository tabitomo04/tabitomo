package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.dto.CategoryDto;
import com.koreatravel.tabitomo.dto.QaDto;
import com.koreatravel.tabitomo.entity.ChatQA;
import com.koreatravel.tabitomo.service.CategoryService;
import com.koreatravel.tabitomo.service.TranslationService;
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
    private final TranslationService translationService;

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

    // 이 엔드포인트를 제거하고 ChatController의 기존 엔드포인트로 통합하는 것을 권장합니다.
    @GetMapping("/answer/{qaId}")
    public ResponseEntity<String> getAnswerByQaId(@PathVariable Integer qaId, @RequestParam(required = false) String lang) {
        Optional<ChatQA> optionalChatQa = categoryService.getAnswerByQaId(qaId);
        if (optionalChatQa.isPresent()) {
            // ChatQA 엔티티에 'answer' 필드가 올바르게 정의되어 있는지 확인하세요.
            String answer = optionalChatQa.get().getAnswer();
            if (!"ko".equalsIgnoreCase(lang) && answer != null) {
                answer = translationService.translateText(answer, lang);
            }
            return ResponseEntity.ok(answer);
        }
        return ResponseEntity.notFound().build();
    }
}
