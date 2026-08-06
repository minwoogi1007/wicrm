package com.wio.crm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wio.crm.model.Consultation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 분석 서비스 - OpenAI API 연동
 */
@Service
public class AiAnalysisService {
    
    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;
    
    @Value("${openai.model:gpt-4}")
    private String openaiModel;
    
    @Value("${ai.report.enabled:false}")
    private boolean aiEnabled;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AiAnalysisService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * AI API 연결 테스트
     */
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        
        if (!aiEnabled) {
            result.put("success", false);
            result.put("message", "AI 기능이 비활성화되어 있습니다. (ai.report.enabled=false)");
            return result;
        }
        
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            result.put("success", false);
            result.put("message", "OpenAI API 키가 설정되지 않았습니다.");
            return result;
        }
        
        try {
            String response = callOpenAI("Say 'Hello, AI Report Service is working!'", 50);
            result.put("success", true);
            result.put("message", "API 연결 성공");
            result.put("response", response);
            result.put("model", openaiModel);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "API 연결 실패: " + e.getMessage());
            log.error("OpenAI API 테스트 실패", e);
        }
        
        return result;
    }
    
    /**
     * 상담 데이터 종합 분석
     */
    public Map<String, String> analyzeConsultations(List<Consultation> consultations) {
        Map<String, String> results = new HashMap<>();
        int totalTokens = 0;
        
        // 상담 데이터를 텍스트로 변환
        String consultationData = prepareConsultationData(consultations);
        
        try {
            // 1. 요약 분석
            log.info("요약 분석 시작...");
            String summaryPrompt = buildSummaryPrompt(consultationData);
            String summaryResult = callOpenAI(summaryPrompt, 1500);
            results.put("summary", summaryResult);
            totalTokens += estimateTokens(summaryPrompt) + estimateTokens(summaryResult);
            
            // 2. 키워드 분석
            log.info("키워드 분석 시작...");
            String keywordPrompt = buildKeywordPrompt(consultationData);
            String keywordResult = callOpenAI(keywordPrompt, 1500);
            results.put("keywords", keywordResult);
            totalTokens += estimateTokens(keywordPrompt) + estimateTokens(keywordResult);
            
            // 3. 감정 분석
            log.info("감정 분석 시작...");
            String sentimentPrompt = buildSentimentPrompt(consultationData);
            String sentimentResult = callOpenAI(sentimentPrompt, 1500);
            results.put("sentiment", sentimentResult);
            totalTokens += estimateTokens(sentimentPrompt) + estimateTokens(sentimentResult);
            
            // 4. 개선 제안
            log.info("개선 제안 생성 시작...");
            String recommendationPrompt = buildRecommendationPrompt(consultationData);
            String recommendationResult = callOpenAI(recommendationPrompt, 2000);
            results.put("recommendations", recommendationResult);
            totalTokens += estimateTokens(recommendationPrompt) + estimateTokens(recommendationResult);
            
            results.put("tokenUsed", String.valueOf(totalTokens));
            log.info("AI 분석 완료: 총 토큰 사용량={}", totalTokens);
            
        } catch (Exception e) {
            log.error("AI 분석 중 오류 발생", e);
            throw new RuntimeException("AI 분석 실패: " + e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * 상담 데이터를 분석용 텍스트로 변환
     */
    private String prepareConsultationData(List<Consultation> consultations) {
        StringBuilder sb = new StringBuilder();
        sb.append("총 상담 건수: ").append(consultations.size()).append("건\n\n");
        
        // 최대 100건만 분석 (토큰 제한)
        int limit = Math.min(consultations.size(), 100);
        
        for (int i = 0; i < limit; i++) {
            Consultation c = consultations.get(i);
            sb.append("--- 상담 ").append(i + 1).append(" ---\n");
            sb.append("유형: ").append(c.getCsType() != null ? c.getCsType() : "미분류").append("\n");
            sb.append("상담내용: ").append(c.getCsNote() != null ? c.getCsNote() : "").append("\n");
            sb.append("처리내용: ").append(c.getPrcNote() != null ? c.getPrcNote() : "").append("\n");
            sb.append("긴급여부: ").append("1".equals(c.getEmgGubn()) ? "긴급" : "일반").append("\n");
            sb.append("\n");
        }
        
        if (consultations.size() > limit) {
            sb.append("... 외 ").append(consultations.size() - limit).append("건\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 요약 분석 프롬프트
     */
    private String buildSummaryPrompt(String data) {
        return "당신은 고객 상담 데이터 분석 전문가입니다.\n\n" +
                "다음 상담 데이터를 분석하여 비즈니스 인사이트를 제공해주세요.\n\n" +
                "[상담 데이터]\n" + data + "\n\n" +
                "[분석 요청]\n" +
                "1. 전체 상담 현황 요약 (2-3문장)\n" +
                "2. 주요 이슈 3가지\n" +
                "3. 개선이 필요한 영역 2가지\n" +
                "4. 긍정적인 트렌드 2가지\n\n" +
                "결과를 다음 JSON 형식으로 반환해주세요:\n" +
                "{\"summary\": \"...\", \"mainIssues\": [...], \"improvementAreas\": [...], \"positiveTrends\": [...]}";
    }
    
    /**
     * 키워드 분석 프롬프트
     */
    private String buildKeywordPrompt(String data) {
        return "다음 상담 데이터에서 키워드를 추출해주세요.\n\n" +
                "[상담 데이터]\n" + data + "\n\n" +
                "[추출 항목]\n" +
                "1. 제품명 (빈도순 TOP 5)\n" +
                "2. 문제 유형 (배송, 품질, 환불 등)\n" +
                "3. 고객 요청 사항\n\n" +
                "결과를 다음 JSON 형식으로 반환해주세요:\n" +
                "{\"products\": [{\"name\": \"...\", \"count\": N, \"sentiment\": \"positive/negative/neutral\"}], " +
                "\"issues\": [{\"type\": \"...\", \"count\": N}], " +
                "\"requests\": [{\"request\": \"...\", \"count\": N}]}";
    }
    
    /**
     * 감정 분석 프롬프트
     */
    private String buildSentimentPrompt(String data) {
        return "다음 상담 데이터의 고객 감정을 분석해주세요.\n\n" +
                "[상담 데이터]\n" + data + "\n\n" +
                "[분석 항목]\n" +
                "1. 전체 감정 분포 (긍정/중립/부정 비율 %)\n" +
                "2. 주요 불만 사항 TOP 5\n" +
                "3. 만족도가 높은 영역\n" +
                "4. 감정 변화 추이\n\n" +
                "결과를 다음 JSON 형식으로 반환해주세요:\n" +
                "{\"distribution\": {\"positive\": N, \"neutral\": N, \"negative\": N}, " +
                "\"topComplaints\": [{\"complaint\": \"...\", \"count\": N}], " +
                "\"satisfactionAreas\": [...], \"emotionTrend\": \"상승/하락/유지\"}";
    }
    
    /**
     * 개선 제안 프롬프트
     */
    private String buildRecommendationPrompt(String data) {
        return "상담 데이터 분석 결과를 바탕으로 실행 가능한 개선 방안을 제안해주세요.\n\n" +
                "[상담 데이터]\n" + data + "\n\n" +
                "[제안 조건]\n" +
                "1. 구체적이고 실행 가능한 방안\n" +
                "2. 예상 효과 포함\n" +
                "3. 우선순위 표시 (high/medium/low)\n" +
                "4. 3-5개 제안\n\n" +
                "결과를 다음 JSON 형식으로 반환해주세요:\n" +
                "{\"recommendations\": [{\"title\": \"...\", \"description\": \"...\", " +
                "\"expectedEffect\": \"...\", \"priority\": \"high/medium/low\", " +
                "\"category\": \"product/service/process/communication\"}]}";
    }
    
    /**
     * OpenAI API 호출
     */
    private String callOpenAI(String prompt, int maxTokens) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openaiModel);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.7);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                openaiApiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } else {
            throw new RuntimeException("OpenAI API 응답 오류: " + response.getStatusCode());
        }
    }
    
    /**
     * 토큰 수 추정 (대략적인 계산)
     */
    private int estimateTokens(String text) {
        if (text == null) return 0;
        // 한글은 대략 글자당 2토큰, 영문은 4글자당 1토큰
        return text.length() / 2;
    }
}

