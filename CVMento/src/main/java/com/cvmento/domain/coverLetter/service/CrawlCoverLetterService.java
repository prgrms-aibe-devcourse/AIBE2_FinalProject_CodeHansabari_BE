package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterData;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterResponse;
import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.repository.CrawlCoverLetterRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.global.exception.CrawlCoverLetterException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 크롤링 데이터 서비스
 */
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
     * IT 직무 합격 자소서 크롤링 및 저장
     */
    public CrawlCoverLetterResponse crawlAndSaveCoverLetters() {
        MDC.put("spanId", "crawl-execution-service");

        try {
            log.info("자소서 크롤링 서비스 시작");

            // 1. 기존 데이터 삭제 (업데이트 방식)
            MDC.put("spanId", "crawl-repository");
            long beforeCount = crawlCoverLetterRepository.count();
            crawlCoverLetterRepository.deleteAll();

            MDC.put("spanId", "crawl-execution-service");
            log.info("기존 크롤링 데이터 삭제 완료 - 삭제된 개수: {}", beforeCount);

            // 2. API 호출하여 자소서 데이터 가져오기
            String responseBody = callLinkareerAPI();

            // 3. JSON 응답 파싱하여 content 추출
            List<String> contents = parseContentsFromResponse(responseBody);

            // 4. DB에 저장
            List<CrawlCoverLetter> savedCoverLetters = saveCoverLetters(contents);

            log.info("자소서 크롤링 완료 - 수집개수: {}, 저장개수: {}",
                    contents.size(), savedCoverLetters.size());

            return CrawlCoverLetterResponse.success(
                    "자소서 크롤링이 완료되었습니다.",
                    savedCoverLetters.size()
            );

        } catch (Exception e) {
            log.error("자소서 크롤링 중 오류 발생", e);
            return CrawlCoverLetterResponse.failure(
                    "크롤링 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * Linkareer GraphQL API 호출
     */
    private String callLinkareerAPI() {
        MDC.put("spanId", "linkareer-api-client");

        log.info("Linkareer API 호출 시작 - URL: {}", API_URL);

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

        log.debug("요청 페이로드 길이: {}chars", payload.length());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        log.info("Linkareer API 요청 전송");
        ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Linkareer API 호출 실패 - 상태코드: {}", response.getStatusCode());
            throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
        }

        String responseBody = response.getBody();
        log.info("Linkareer API 응답 수신 완료 - 응답길이: {}chars",
                responseBody != null ? responseBody.length() : 0);

        log.debug("API 응답 미리보기: {}",
                responseBody != null && responseBody.length() > 200 ?
                        responseBody.substring(0, 200) + "..." : responseBody);

        return responseBody;
    }

    /**
     * API 응답 파싱(content 추출)
     */
    private List<String> parseContentsFromResponse(String responseBody) throws Exception {
        MDC.put("spanId", "json-parsing-service");

        List<String> contents = new ArrayList<>();

        log.info("API 응답 파싱 시작 - 응답길이: {}chars", responseBody.length());

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode edges = jsonNode.path("data")
                .path("coverLetters")
                .path("edges");

        if (edges.isArray()) {
            log.info("크롤링 대상 발견 - edges 개수: {}", edges.size());

            for (int i = 0; i < edges.size(); i++) {
                JsonNode edge = edges.get(i);
                JsonNode node = edge.path("node");

                if (node.has("content")) {
                    String content = node.get("content").asText();

                    if (content != null && !content.trim().isEmpty()) {
                        contents.add(content);

                        // 처음 1개만 상세 로그 (디버그용)
                        if (i == 0) {
                            String cleanedContent = cleanText(content);
                            log.debug("첫 번째 content 샘플 - 원본길이: {}, 정리후길이: {}",
                                    content.length(), cleanedContent.length());
                        }
                    }
                } else {
                    // content 없는 경우만 경고 (첫 번째만)
                    if (i == 0) {
                        log.warn("content 필드가 없는 node 발견");
                    }
                }

                // 진행상황 로그를 더 적게 (100개마다)
                if (i > 0 && i % 100 == 0) {
                    log.info("파싱 진행: {} / {} ({}%)",
                            i + 1, edges.size(), (i + 1) * 100 / edges.size());
                }
            }
        } else {
            log.warn("예상과 다른 응답 구조 - edges가 배열이 아님");
        }

        log.info("API 응답 파싱 완료 - 추출된 content: {}개", contents.size());

        return contents;
    }

    /**
     * 크롤링 데이터 저장
     */
    private List<CrawlCoverLetter> saveCoverLetters(List<String> contents) {
        MDC.put("spanId", "crawl-repository");

        List<CrawlCoverLetter> coverLetters = new ArrayList<>();

        for (String content : contents) {
            String cleanedContent = cleanText(content);
            CrawlCoverLetter coverLetter = new CrawlCoverLetter(cleanedContent);
            coverLetters.add(coverLetter);
        }

        log.info("DB 저장 시작 - 저장할 개수: {}", coverLetters.size());

        List<CrawlCoverLetter> saved = crawlCoverLetterRepository.saveAll(coverLetters);

        log.info("DB 저장 완료 - 저장된 개수: {}", saved.size());

        return saved;
    }

    /**
     * 텍스트 정리(개행·여러 공백 정규화)
     */
    private String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        return text
                .replaceAll("\\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 크롤링 데이터 전체 조회
     */
    public List<CrawlCoverLetterData> getAllCrawlCoverLetters() {
        MDC.put("spanId", "crawl-list-service");

        MDC.put("spanId", "crawl-repository");
        List<CrawlCoverLetter> coverLetters = crawlCoverLetterRepository.findAllByOrderByCreatedAtDesc();

        MDC.put("spanId", "crawl-list-service");
        log.info("크롤링 데이터 전체 조회 완료 - 개수: {}", coverLetters.size());

        return coverLetters.stream()
                .map(CrawlCoverLetterData::from)
                .collect(Collectors.toList());
    }

    /**
     * 크롤링 데이터 단건 조회
     */
    public CrawlCoverLetterData getCrawlCoverLetterById(Long id) {
        MDC.put("spanId", "crawl-detail-service");

        MDC.put("spanId", "crawl-repository");
        CrawlCoverLetter coverLetter = crawlCoverLetterRepository.findById(id)
                .orElseThrow(() -> new CrawlCoverLetterException(
                        "CRAWL_COVER_LETTER_NOT_FOUND",
                        "크롤링 데이터를 찾을 수 없습니다. ID: " + id,
                        404
                ));

        MDC.put("spanId", "crawl-detail-service");
        log.info("크롤링 데이터 개별 조회 완료 - ID: {}, 텍스트길이: {}",
                id, coverLetter.getText() != null ? coverLetter.getText().length() : 0);

        return CrawlCoverLetterData.from(coverLetter);
    }

    /**
     * 크롤링 데이터 수정
     */
    public CrawlCoverLetterData updateCrawlCoverLetter(Long id, UpdateCrawlCoverLetterRequest request, Member member) {
        MDC.put("spanId", "crawl-update-service");

        if (member == null || (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT)) {
            throw new AccessDeniedException("크롤링 데이터를 수정할 권한이 없습니다.");
        }

        MDC.put("spanId", "crawl-repository");
        CrawlCoverLetter coverLetter = crawlCoverLetterRepository.findById(id)
                .orElseThrow(() -> new CrawlCoverLetterException(
                        "CRAWL_COVER_LETTER_NOT_FOUND",
                        "크롤링 데이터를 찾을 수 없습니다. ID: " + id,
                        404
                ));

        coverLetter.updateText(request.text());
        CrawlCoverLetter savedCoverLetter = crawlCoverLetterRepository.save(coverLetter);

        MDC.put("spanId", "crawl-update-service");
        log.info("크롤링 데이터 수정 완료 - ID: {}, 수정자: {}, 새로운텍스트길이: {}",
                id, member.getMemberId(), request.text() != null ? request.text().length() : 0);

        return CrawlCoverLetterData.from(savedCoverLetter);
    }

    /**
     * 크롤링 데이터 단건 삭제
     */
    public void deleteCrawlCoverLetter(Long id) {
        MDC.put("spanId", "crawl-delete-service");

        MDC.put("spanId", "crawl-repository");
        if (!crawlCoverLetterRepository.existsById(id)) {
            throw new CrawlCoverLetterException(
                    "CRAWL_COVER_LETTER_NOT_FOUND",
                    "크롤링 데이터를 찾을 수 없습니다. ID: " + id,
                    404
            );
        }

        crawlCoverLetterRepository.deleteById(id);

        MDC.put("spanId", "crawl-delete-service");
        log.info("크롤링 데이터 개별 삭제 완료 - ID: {}", id);
    }

    /**
     * 크롤링 데이터 전체 삭제
     */
    public void deleteAllCrawlCoverLetters() {
        MDC.put("spanId", "crawl-delete-all-service");

        MDC.put("spanId", "crawl-repository");
        long count = crawlCoverLetterRepository.count();
        crawlCoverLetterRepository.deleteAll();

        MDC.put("spanId", "crawl-delete-all-service");
        log.info("크롤링 데이터 전체 삭제 완료 - 삭제된 개수: {}", count);
    }
}
