package com.cvmento.domain.coverLetter.enums;

/**
 * 비동기 처리 상태 정보
 */
public enum JobStatus {
    /** 대기 상태 (기본값) */
    PENDING,    // 대기 중
    /** 진행 상태 */
    PROCESSING,
    /** 완료 상태*/
    COMPLETED,
    /** 실패 상태*/
    FAILED
}
