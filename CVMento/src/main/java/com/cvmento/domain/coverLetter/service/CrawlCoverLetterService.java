package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.repository.CrawlCoverLetterRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CrawlCoverLetterService {

    private static final String API_URL = "https://api.linkareer.com/graphql";
    private final CrawlCoverLetterRepository crawlCoverLetterRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Linkareer API를 호출하여 IT 직무 합격 자소서를 크롤링하고 DB에 저장
     */
    public CrawlCoverLetterResponse crawlAndSaveCoverLetters() {
        try {
            log.info("자소서 크롤링 시작");
            
            // 1. 기존 데이터 삭제 (업데이트 방식)
            crawlCoverLetterRepository.deleteAll();
            log.info("기존 크롤링 데이터 삭제 완료");
            
            // 2. API 호출하여 자소서 데이터 가져오기
            String responseBody = callLinkareerAPI();
            
            // 3. JSON 응답 파싱하여 content 추출
            List<String> contents = parseContentsFromResponse(responseBody);
            
            // 4. DB에 저장
            List<CrawlCoverLetter> savedCoverLetters = saveCoverLetters(contents);
            
            log.info("자소서 크롤링 완료: {}개 저장", savedCoverLetters.size());
            
            return CrawlCoverLetterResponse.builder()
                    .success(true)
                    .message("자소서 크롤링이 완료되었습니다.")
                    .crawledCount(savedCoverLetters.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("자소서 크롤링 중 오류 발생", e);
            return CrawlCoverLetterResponse.builder()
                    .success(false)
                    .message("크롤링 중 오류가 발생했습니다: " + e.getMessage())
                    .crawledCount(0)
                    .build();
        }
    }
    
    /**
     * Linkareer GraphQL API 호출
     */
    private String callLinkareerAPI() {
        log.info("=== Linkareer API 호출 시작 ===");
        log.info("API URL: {}", API_URL);
        
        String payload = """
            {
              "operationName": "CoverLetterList",             
              "variables": {
                "filterBy": {
                  "organizationName": "",
                  "role": "IT",
                  "keyword": "",
                  "types": ["ALL"],
                  "status": "PUBLISHED"
                },
                "orderBy": {
                  "field": "PASSED_AT",
                  "direction": "DESC"
                },
                "pagination": {
                  "page": 1,
                  "pageSize": 314
                }
              },
              "extensions": {
                "persistedQuery": {
                  "version": 1,
                  "sha256Hash": "5c0cd537900df7638b0a29b29264285662fde73d1ac48c06403987051b144fde"
                }
              }
            }
            """;

        log.info("요청 페이로드: {}", payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("요청 헤더: {}", headers);

        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        log.info("API 호출 중...");
        ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        log.info("API 응답 상태 코드: {}", response.getStatusCode());
        log.info("API 응답 헤더: {}", response.getHeaders());

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("API 호출 실패: {}", response.getStatusCode());
            throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
        }

        String responseBody = response.getBody();
        log.info("API 응답 본문 길이: {} characters", responseBody != null ? responseBody.length() : 0);
        
        if (responseBody != null && responseBody.length() > 200) {
            log.info("API 응답 미리보기: {}...", responseBody.substring(0, 200));
        } else {
            log.info("API 응답: {}", responseBody);
        }

        return responseBody;
    }
    
    /**
     * API 응답에서 content 필드 추출
     */
    private List<String> parseContentsFromResponse(String responseBody) throws Exception {
        List<String> contents = new ArrayList<>();
        
        log.info("=== API 응답 파싱 시작 ===");
        log.info("응답 본문 길이: {} characters", responseBody.length());
        
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        log.info("JSON 파싱 성공");
        
        JsonNode edges = jsonNode.path("data")
                .path("coverLetters")
                .path("edges");
        
        log.info("edges 경로 탐색 완료");
        log.info("edges 타입: {}", edges.getNodeType());
        log.info("edges가 배열인가: {}", edges.isArray());
        
        if (edges.isArray()) {
            log.info("edges 배열 크기: {}", edges.size());
            
            for (int i = 0; i < edges.size(); i++) {
                JsonNode edge = edges.get(i);
                JsonNode node = edge.path("node");
                
                log.info("=== {}번째 edge 분석 ===", i + 1);
                log.info("node 존재 여부: {}", !node.isMissingNode());
                log.info("node 타입: {}", node.getNodeType());
                
                if (node.has("content")) {
                    String content = node.get("content").asText();
                    log.info("content 필드 존재: true");
                    log.info("content 길이: {} characters", content != null ? content.length() : 0);
                    log.info("content 내용 미리보기: {}", 
                        content != null && content.length() > 100 ? 
                        content.substring(0, 100) + "..." : content);
                    
                    if (content != null && !content.trim().isEmpty()) {
                        contents.add(content);
                        log.info("content 추가됨 (현재 총 {}개)", contents.size());
                    } else {
                        log.info("content가 null이거나 빈 문자열이어서 추가되지 않음");
                    }
                } else {
                    log.info("content 필드가 존재하지 않음");
                    log.info("node의 모든 필드: {}", node.fieldNames());
                }
                
                // 처음 3개만 상세 로그 출력 (저장은 계속 진행)
                if (i >= 2) {
                    log.info("처음 3개만 상세 로그를 출력하므로 나머지는 간단히 처리");
                    // 간단한 진행상황 로그
                    if (i % 50 == 0) {
                        log.info("진행상황: {} / {} 처리 완료", i + 1, edges.size());
                    }
                }
            }
        } else {
            log.warn("edges가 배열이 아님. edges 내용: {}", edges);
        }
        
        log.info("=== 파싱 완료 ===");
        log.info("최종 추출된 content 개수: {}", contents.size());
        
        return contents;
    }
    
    /**
     * 크롤링한 자소서들을 DB에 저장
     */
    private List<CrawlCoverLetter> saveCoverLetters(List<String> contents) {
        List<CrawlCoverLetter> coverLetters = new ArrayList<>();
        
        for (String content : contents) {
            CrawlCoverLetter coverLetter = new CrawlCoverLetter(content);
            coverLetters.add(coverLetter);
        }
        
        return crawlCoverLetterRepository.saveAll(coverLetters);
    }
    
    /**
     * 크롤링 결과 응답 DTO
     */
    public static class CrawlCoverLetterResponse {
        private boolean success;
        private String message;
        private int crawledCount;
        
        // Builder 패턴
        public static CrawlCoverLetterResponseBuilder builder() {
            return new CrawlCoverLetterResponseBuilder();
        }
        
        public static class CrawlCoverLetterResponseBuilder {
            private CrawlCoverLetterResponse response = new CrawlCoverLetterResponse();
            
            public CrawlCoverLetterResponseBuilder success(boolean success) {
                response.success = success;
                return this;
            }
            
            public CrawlCoverLetterResponseBuilder message(String message) {
                response.message = message;
                return this;
            }
            
            public CrawlCoverLetterResponseBuilder crawledCount(int crawledCount) {
                response.crawledCount = crawledCount;
                return this;
            }
            
            public CrawlCoverLetterResponse build() {
                return response;
            }
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getCrawledCount() { return crawledCount; }
    }
}
