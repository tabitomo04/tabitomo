package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.dto.chat.CategoryDto;
import com.koreatravel.tabitomo.dto.chat.QaDto;
import com.koreatravel.tabitomo.entity.chat.ChatQA;
import com.koreatravel.tabitomo.entity.chat.MainCategory;
import com.koreatravel.tabitomo.entity.chat.SubCategory;
import com.koreatravel.tabitomo.repository.chat.ChatQARepository;
import com.koreatravel.tabitomo.repository.chat.MainCategoryRepository;
import com.koreatravel.tabitomo.repository.chat.SubCategoryRepository;
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

    // 언어에 따라 동적으로 MainCategory 이름을 가져오는 헬퍼 메서드
    private String getCategoryName(MainCategory category, String lang) {
        if ("en".equalsIgnoreCase(lang)) {
            return category.getNameEn();
        } else if ("ja".equalsIgnoreCase(lang)) {
            return category.getNameJa();
        }
        return category.getNameKo();
    }

    // 언어에 따라 동적으로 SubCategory 이름을 가져오는 헬퍼 메서드
    private String getSubCategoryName(SubCategory subCategory, String lang) {
        if ("en".equalsIgnoreCase(lang)) {
            return subCategory.getNameEn();
        } else if ("ja".equalsIgnoreCase(lang)) {
            return subCategory.getNameJa();
        }
        return subCategory.getNameKo();
    }

    // 언어에 따라 동적으로 ChatQA 질문을 가져오는 헬퍼 메서드
    // 이 헬퍼 메서드는 더 이상 사용되지 않습니다.
    private String getQaQuestion(ChatQA qa, String lang) {
        if ("en".equalsIgnoreCase(lang)) {
            return qa.getQuestionEn();
        } else if ("ja".equalsIgnoreCase(lang)) {
            return qa.getQuestionJa();
        }
        return qa.getQuestionKo();
    }

    public List<CategoryDto> getMainCategories(String lang) {
        List<MainCategory> categories = mainCategoryRepository.findAll();
        return categories.stream()
                .map(category -> new CategoryDto(category.getId(), category.getNameKo(), category.getNameEn(), category.getNameJa()))
                .collect(Collectors.toList());
    }

    public List<CategoryDto> getSubCategories(Integer mainCategoryId, String lang) {
        List<SubCategory> subCategories = subCategoryRepository.findByMainCategoryId(mainCategoryId);
        return subCategories.stream()
                .map(subCategory -> new CategoryDto(subCategory.getId(), subCategory.getNameKo(), subCategory.getNameEn(), subCategory.getNameJa()))
                .collect(Collectors.toList());
    }

    public List<QaDto> getQasBySubCategoryId(Integer subCategoryId, String lang) {
        List<ChatQA> qas = chatQaRepository.findBySubCategoryId(subCategoryId);
        return qas.stream()
                // QaDto의 변경된 생성자 시그니처에 맞게 모든 언어별 질문 필드 전달
                .map(qa -> new QaDto(qa.getQaId(), qa.getQuestionKo(), qa.getQuestionEn(), qa.getQuestionJa()))
                .collect(Collectors.toList());
    }

    public Optional<ChatQA> getAnswerByQaId(Integer qaId) {
        return chatQaRepository.findById(qaId);
    }
}
