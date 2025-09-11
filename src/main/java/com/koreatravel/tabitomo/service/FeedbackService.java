package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.dto.FeedbackRequest;
import com.koreatravel.tabitomo.entity.Feedback;
import com.koreatravel.tabitomo.entity.LearningData;
import com.koreatravel.tabitomo.repository.FeedbackRepository;
import com.koreatravel.tabitomo.repository.LearningDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final LearningDataRepository learningDataRepository; // 새로운 Repository 주입
    private final ObjectMapper objectMapper;

    // 생성자 주입
    public FeedbackService(FeedbackRepository feedbackRepository, LearningDataRepository learningDataRepository, ObjectMapper objectMapper) {
        this.feedbackRepository = feedbackRepository;
        this.learningDataRepository = learningDataRepository;
        this.objectMapper = objectMapper;
    }

    public void saveFeedback(FeedbackRequest feedbackRequest) throws JsonProcessingException {
        // 기존 피드백 저장 로직
        Feedback feedback = new Feedback();
        feedback.setRating(feedbackRequest.getRating());
        feedback.setFeedbackText(feedbackRequest.getFeedbackText());
        feedback.setTimestamp(LocalDateTime.now());
        String chatHistoryJson = objectMapper.writeValueAsString(feedbackRequest.getChatHistory());
        feedback.setChatHistory(chatHistoryJson);

        feedbackRepository.save(feedback);

        // 학습 데이터 생성 로직 호출
        generateLearningData(feedback);
    }

    private void generateLearningData(Feedback feedback) {
        int rating = feedback.getRating();
        String chatHistoryJson = feedback.getChatHistory();
        String feedbackText = feedback.getFeedbackText();

        String prompt = "";
        String expectedResponse = "";
        int importance = 0;

        try {
            // chat_history를 List<Map<String, String>>으로 변환
            List<Map<String, String>> chatHistory = objectMapper.readValue(chatHistoryJson, List.class);
            String lastUserMessage = chatHistory.stream()
                    .filter(m -> "user".equals(m.get("sender")))
                    .reduce((first, second) -> second)
                    .map(m -> m.get("message"))
                    .orElse("");
            String lastBotResponse = chatHistory.stream()
                    .filter(m -> "bot".equals(m.get("sender")))
                    .reduce((first, second) -> second)
                    .map(m -> m.get("message"))
                    .orElse("");

            switch (rating) {
                case 1:
                    prompt = "다음 채팅에서 AI가 잘못된 답변을 한 이유와 올바른 답변을 제시해줘:\n" + chatHistoryJson;
                    expectedResponse = feedbackText; // 사용자의 피드백 텍스트를 정답으로 사용
                    importance = 5;
                    break;
                case 2:
                    prompt = "다음 대화에서 AI가 놓친 맥락은 무엇이고, 이 대화에 대해 더 나은 답변을 제공해줘:\n" + chatHistoryJson;
                    expectedResponse = "사용자 의견에 따른 개선된 답변"; // AI가 직접 생성하도록 할 수도 있음
                    importance = 4;
                    break;
                case 3:
                    prompt = "다음 답변의 부족한 부분을 찾아 보완해줘:\n" + lastBotResponse;
                    expectedResponse = "사용자 의견(" + feedbackText + ")을 반영하여 보강된 답변";
                    importance = 3;
                    break;
                case 4:
                    // 간단한 보강 데이터
                    prompt = lastUserMessage;
                    expectedResponse = lastBotResponse + ". " + feedbackText + " 부분을 더 자세히 설명해줘.";
                    importance = 2;
                    break;
                case 5:
                    // 학습 데이터 생성 안함
                    return;
                default:
                    return;
            }

            LearningData learningData = new LearningData();
            learningData.setPrompt(prompt);
            learningData.setExpectedResponse(expectedResponse);
            learningData.setSourceFeedbackId(feedback.getFeedbackId());
            learningData.setImportance(importance);
            learningData.setCreatedAt(LocalDateTime.now());

            learningDataRepository.save(learningData);

        } catch (Exception e) {
            System.err.println("학습 데이터 생성 중 오류 발생: " + e.getMessage());
        }
    }
}