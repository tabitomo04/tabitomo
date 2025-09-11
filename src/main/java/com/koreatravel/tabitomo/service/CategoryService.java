package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.dto.CategoryDto;
import com.koreatravel.tabitomo.dto.QaDto;
import com.koreatravel.tabitomo.entity.ChatQA;
import com.koreatravel.tabitomo.entity.MainCategory;
import com.koreatravel.tabitomo.entity.SubCategory;
import com.koreatravel.tabitomo.repository.ChatQARepository;
import com.koreatravel.tabitomo.repository.MainCategoryRepository;
import com.koreatravel.tabitomo.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ChatQARepository chatQaRepository;
    private final TranslationService translationService;

    public List<CategoryDto> getMainCategories(String lang) {
        List<MainCategory> categories = mainCategoryRepository.findAll();
        return categories.stream()
                .map(category -> {
                    String name = category.getName();
                    if (lang != null && !lang.isEmpty() && !"ko".equalsIgnoreCase(lang) && name != null) {
                        name = translationService.translateText(name, lang);
                    }
                    return new CategoryDto(category.getId(), name);
                })
                .collect(Collectors.toList());
    }

    public List<CategoryDto> getSubCategories(Integer mainCategoryId, String lang) {
        List<SubCategory> subCategories = subCategoryRepository.findByMainCategoryId(mainCategoryId);
        return subCategories.stream()
                .map(subCategory -> {
                    String name = subCategory.getName();
                    // ✅ lang에 대한 null, empty 체크 추가
                    if (lang != null && !lang.isEmpty() && !"ko".equalsIgnoreCase(lang) && name != null) {
                        name = translationService.translateText(name, lang);
                    }
                    return new CategoryDto(subCategory.getId(), name);
                })
                .collect(Collectors.toList());
    }

    public List<QaDto> getQasBySubCategoryId(Integer subCategoryId, String lang) {
        List<ChatQA> qas = chatQaRepository.findBySubCategoryId(subCategoryId);
        return qas.stream()
                .map(qa -> {
                    String question = qa.getQuestion();
                    // ✅ lang에 대한 null, empty 체크 추가
                    if (lang != null && !lang.isEmpty() && !"ko".equalsIgnoreCase(lang) && question != null) {
                        question = translationService.translateText(question, lang);
                    }
                    return new QaDto(qa.getQaId(), question);
                })
                .collect(Collectors.toList());
    }

    public Optional<ChatQA> getAnswerByQaId(Integer qaId) {
        return chatQaRepository.findById(qaId);
    }
}