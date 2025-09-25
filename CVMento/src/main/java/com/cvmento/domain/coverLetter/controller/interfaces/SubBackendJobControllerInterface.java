package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import java.util.Map;


@Tag(name = "Sub-Backend Jobs", description = "Sub-Backend의 작업 조회 API")
@SecurityRequirement(name = "cookieAuth")
public interface SubBackendJobControllerInterface {

	@Operation(
			summary = "Sub-Backend Job 목록 조회",
			description = """
					Sub-Backend에서 실행되는 Job(크롤링, 특징추출 등)의 목록을 페이징으로 조회합니다.                  
                    - **전체 조회**: `GET /api/jobs`
                    - **필터링 조회**: `GET /api/jobs/{filter}`
                    
                    **필터링 가능 값:**
                    - **Job 상태**: `PENDING`, `RUNNING`, `COMPLETED`, `FAILED`
                    - **Job 타입**: `CRAWLING`, `FEATURE_EXTRACTION`, `DEDUPLICATION`
                    (필터 값은 대소문자를 구분하지 않습니다.)
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
					""",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "조회 성공",
							content = @Content(schema = @Schema(implementation = CommonResponse.class))
					),
					@ApiResponse(
							responseCode = "500",
							description = "서버 오류 - Job 조회 실패",
							content = @Content(schema = @Schema(implementation = CommonResponse.class))
					)
			}
	)
	ResponseEntity<CommonResponse<Page<Map<String, Object>>>> listJobs(
			@Parameter(
					name = "filter",
					description = "Job 상태 또는 타입으로 필터링하기 위한 값 (옵션)",
					examples = {
							@ExampleObject(name = "상태 필터링", value = "RUNNING"),
							@ExampleObject(name = "타입 필터링", value = "CRAWLING")
					}
			)
			String filter,
			@Parameter(hidden = true) Pageable pageable
	);
}

