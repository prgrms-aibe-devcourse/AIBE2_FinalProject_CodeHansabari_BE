package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.SubBackendJobControllerInterface;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.subBackend.client.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class SubBackendJobController implements SubBackendJobControllerInterface {

	private final JobClient jobClient;

	@GetMapping({ "", "/{filter}" })
	@Override
	public ResponseEntity<CommonResponse<Page<Map<String, Object>>>> listJobs(
			@PathVariable(value = "filter", required = false) String filter,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		log.info("Job 목록 조회 요청 - filter: {}, page: {}, size: {}, sort: {}",
				filter, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

		try {
			String sortString = pageable.getSort().stream()
					.map(order -> order.getProperty() + ":" + order.getDirection().name())
					.collect(Collectors.joining(","));

			Page<Map<String, Object>> jobsPage = jobClient.getJobs(
					pageable.getPageNumber(),
					pageable.getPageSize(),
					sortString,
					filter
			);

			log.info("Sub-Backend 로부터 Job 목록 조회 완료 - 총 {}개", jobsPage.getTotalElements());
			return ResponseEntity.ok(CommonResponse.success(jobsPage));

		} catch (Exception e) {
			log.error("Sub-Backend Job 목록 조회 실패", e);
			return ResponseEntity.internalServerError()
					.body(CommonResponse.error("JOB_FETCH_ERROR", "Job 목록 조회에 실패했습니다: " + e.getMessage()));
		}
	}
}
