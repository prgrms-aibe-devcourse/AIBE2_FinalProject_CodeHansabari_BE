package com.cvmento.domain.coverLetter.scheduler;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//@Component
//@RequiredArgsConstructor
//@Slf4j
public class CoverLetterCleanupScheduler {

//    private final CoverLetterRepository coverLetterRepository;
//
//    private static final int RETENTION_DAYS = 30; // 30일 후 하드 삭제
//
//    /**
//     * 매일 새벽 2시에 실행되는 하드 삭제 스케줄러
//     * 삭제된 지 30일이 지난 자소서들을 완전히 삭제
//     */
//    @Scheduled(cron = "0 0 2 * * *")
//    @Transactional
//    public void cleanupDeletedCoverLetters() {
//        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
//
//        List<CoverLetter> toDelete = coverLetterRepository
//                .findByStatusAndUpdatedAtBefore(CoverLetterStatus.DELETED, cutoffDate);
//
//        if (toDelete.isEmpty()) {
//            log.info("하드 삭제 대상 자소서가 없습니다.");
//            return;
//        }
//
//        log.info("하드 삭제 대상 자소서 {}개 발견 ({}일 이전 삭제)", toDelete.size(), RETENTION_DAYS);
//
//        int deletedCount = 0;
//        for (CoverLetter coverLetter : toDelete) {
//            try {
//                // cascade = CascadeType.ALL로 설정되어 있어서
//                // 연관된 CoverLetterQna도 함께 삭제됨
//                coverLetterRepository.delete(coverLetter);
//                deletedCount++;
//
//                log.debug("자소서 하드 삭제 완료 - ID: {}, 제목: {}",
//                        coverLetter.getCoverLetterId(), coverLetter.getTitle());
//
//            } catch (Exception e) {
//                log.error("자소서 하드 삭제 실패 - ID: {}", coverLetter.getCoverLetterId(), e);
//            }
//        }
//
//        log.info("하드 삭제 완료 - 총 {}개 자소서 삭제", deletedCount);
//    }
}