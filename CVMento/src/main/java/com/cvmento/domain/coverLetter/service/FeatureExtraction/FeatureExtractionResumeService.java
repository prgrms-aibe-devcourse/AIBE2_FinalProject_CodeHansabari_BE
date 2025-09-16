package com.cvmento.domain.coverLetter.service.FeatureExtraction;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.repository.CrawlCoverLetterRepository;
import com.cvmento.domain.coverLetter.repository.RawCoverLetterFeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 특징 추출 중단/재개 서비스
 * - 중단된 지점부터 자동 재개
 * - 처리 상태 확인 및 관리
 * - 동적 지연 시간 조정
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureExtractionResumeService {

    private final CrawlCoverLetterRepository crawlRepository;
    private final RawCoverLetterFeatureRepository rawFeatureRepository;
    private final FeatureExtractionUtils utils;

    /**
     * 중단/재개 로직: 이미 처리된 자소서를 제외하고 남은 자소서 반환
     */
    public List<CrawlCoverLetter> getRemainingCoverLetters(List<CrawlCoverLetter> allCoverLetters) {
        try {
            // 1. 각 DB에서 최소 ID 확인
            Long minCrawlId = crawlRepository.findMinId();
            Long minRawFeatureId = rawFeatureRepository.findMinCoverLetterId();
            
            log.info("중단/재개 확인 - CrawlCoverLetter 최소 ID: {}, RawFeature 최소 ID: {}", 
                    minCrawlId, minRawFeatureId);
            
            // 2. 중단 여부 판단
            if (minRawFeatureId == null) {
                // 아직 처리된 특징이 없음 - 처음부터 시작
                log.info("처리된 특징이 없습니다. 처음부터 시작합니다.");
                return allCoverLetters;
            }
            
            if (minCrawlId < minRawFeatureId) {
                // 중단됨: raw feature가 더 큰 ID까지만 처리됨
                Long resumeFromId = minRawFeatureId - 1;
                log.info("중단 감지: {}부터 재개합니다", resumeFromId);
                
                // 3. 재개할 자소서 필터링 (ID 내림차순으로 정렬된 상태에서)
                List<CrawlCoverLetter> remainingCoverLetters = allCoverLetters.stream()
                        .filter(cl -> cl.getCoverLetterId() <= resumeFromId)
                        .collect(Collectors.toList());
                
                log.info("재개 대상: {}개 자소서 (ID: {} 이하)", 
                        remainingCoverLetters.size(), resumeFromId);
                return remainingCoverLetters;
            } else {
                // 완료됨: 모든 자소서 처리 완료
                log.info("모든 자소서 처리 완료");
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("중단/재개 확인 중 오류 발생", e);
            // 오류 시 전체 자소서 반환 (안전한 기본값)
            return allCoverLetters;
        }
    }

    /**
     * 성공적인 응답에 따른 지연 시간 조정
     */
    public long adjustDelayForSuccess(long responseTime, long currentDelay, long baseDelay, long maxDelay) {
        return utils.adjustDelayForSuccess(responseTime, currentDelay, baseDelay, maxDelay);
    }

    // 삭제: 상태/재개 관련 부가 메서드들 - 현행 플로우에서 미사용
}
