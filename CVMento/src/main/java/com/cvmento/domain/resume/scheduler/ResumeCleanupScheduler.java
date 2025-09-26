package com.cvmento.domain.resume.scheduler;

import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.cvmento.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeCleanupScheduler {

    private final ResumeRepository resumeRepository;

    private static final int RETENTION_DAYS = 30;

    /**
     * 매일 새벽 3시에 실행되는 이력서 하드 삭제 스케줄러
     * 삭제된 지 30일이 지난 이력서들을 완전히 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupDeletedResumes() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        List<Resume> toDelete = resumeRepository
                .findByStatusAndUpdatedAtBefore(ResumeStatus.DELETED, cutoffDate);

        if (toDelete.isEmpty()) {
            log.info("하드 삭제 대상 이력서가 없습니다.");
            return;
        }

        log.info("하드 삭제 대상 이력서 {}개 발견 ({}일 이전 삭제)", toDelete.size(), RETENTION_DAYS);

        int deletedCount = 0;
        for (Resume resume : toDelete) {
            try {
                // cascade = CascadeType.ALL로 설정되어 있어서
                // 연관된 모든 하위 엔티티들도 함께 삭제됨
                // (Education, Career, Project, Training 등)
                resumeRepository.delete(resume);
                deletedCount++;

                log.debug("이력서 하드 삭제 완료 - ID: {}, 제목: {}",
                        resume.getId(), resume.getTitle());

            } catch (Exception e) {
                log.error("이력서 하드 삭제 실패 - ID: {}", resume.getId(), e);
            }
        }

        log.info("이력서 하드 삭제 완료 - 총 {}개 삭제", deletedCount);
    }
}