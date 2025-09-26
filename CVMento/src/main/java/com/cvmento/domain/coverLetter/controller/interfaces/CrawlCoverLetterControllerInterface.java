package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.global.common.dto.CommonResponse;
import java.util.Map;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "자소서 크롤링", description = "자소서 크롤링 관리 API. 외부 사이트에서 합격 자소서를 수집(크롤링)하고, 관리하는 기능을 제공합니다.")
public interface CrawlCoverLetterControllerInterface {

    @Operation(
            summary = "자소서 크롤링 실행 (Sub-Backend 위임)",
            description = """
                    Sub-Backend에 자소서 크롤링 작업을 요청합니다.
                    API는 작업을 시작시키고 즉시 응답을 반환하며, 실제 크롤링은 백그라운드에서 비동기적으로 수행됩니다.

                    **처리 과정:**
                    1. Main-Backend API 호출
                    2. 현재 실행 중인 다른 Job이 있는지 확인 (중복 실행 방지)
                    3. Sub-Backend에 크롤링 Job 시작 요청
                    4. Sub-Backend는 Linkareer 사이트에서 IT 직무 합격 자소서를 크롤링하여 자체 DB에 저장

                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "크롤링 작업 시작 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "크롤링 작업 시작",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "크롤링 작업이 시작되었습니다.",
                                                      "data": {
                                                        "jobId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                                                        "task": "CRAWLING",
                                                        "status": "PENDING"
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - 이미 다른 Job이 실행 중일 경우",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이미 작업 실행 중",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "JOB_ALREADY_ACTIVE",
                                                      "message": "현재 크롤링 작업이 진행 중입니다. 작업 완료 후 다시 시도해주세요. (진행중인 작업 생성자: admin@cvmento.com)",
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한이 필요합니다",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류 - 크롤링 요청 실패",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<?>> crawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails);

    @Operation(
            summary = "크롤링 데이터 페이징 조회 (Sub-Backend 위임)",
            description = """
                    Sub-Backend에 저장된 크롤링 원본 자소서 데이터를 페이징으로 조회합니다.
                    Main-Backend는 Sub-Backend의 조회 API를 호출하여 결과를 그대로 반환합니다.

                    **페이징 및 정렬 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작, 기본값: 0)
                    - `size`: 한 페이지에 보여줄 데이터 개수 (기본값: 20)
                    - `sort`: 정렬 기준. `{필드명},{ASC|DESC}` 형식으로 전달합니다.
                      - **정렬 가능 필드**: `createdAt`(생성일), `updatedAt`(수정일)

                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "페이징 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getCrawlCoverLettersWithPagination(
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "크롤링 데이터 개별 조회 (Sub-Backend 위임)",
            description = "Sub-Backend에서 특정 ID의 크롤링된 자소서 데이터를 조회합니다. (관리자 권한 필요)",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "데이터 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Map<String, Object>>> getCrawlCoverLetterById(
            @Parameter(description = "크롤링 데이터 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "크롤링 데이터 수정 (Sub-Backend 위임)",
            description = "Sub-Backend에서 특정 ID의 크롤링된 자소서 데이터를 수정합니다. (관리자 권한 필요)",
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 자소서 텍스트",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateCrawlCoverLetterRequest.class),
                            examples = @ExampleObject(
                                    name = "크롤링 데이터 수정 요청",
                                    value = """
                                            {
                                              "text": "저는 소프트웨어 개발 분야에서 지속적인 성장을 추구하는 개발자입니다. 특히 백엔드 개발에 대한 깊은 관심을 바탕으로 다양한 프로젝트 경험을 쌓아왔으며, 문제 해결 능력과 협업 능력을 키워왔습니다."
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "데이터 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Map<String, Object>>> updateCrawlCoverLetter(
            @Parameter(description = "수정할 크롤링 데이터 ID") @PathVariable Long id,
            @RequestBody UpdateCrawlCoverLetterRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "크롤링 데이터 개별 삭제 (Sub-Backend 위임)",
            description = "Sub-Backend에서 특정 ID의 크롤링된 자소서 데이터를 삭제합니다. (관리자 권한 필요)",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "삭제 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "삭제 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "크롤링 데이터가 삭제되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "데이터 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> deleteCrawlCoverLetter(
            @Parameter(description = "삭제할 크롤링 데이터 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "크롤링 데이터 전체 삭제 (Sub-Backend 위임)",
            description = "Sub-Backend에서 크롤링된 모든 자소서 데이터를 삭제합니다. (관리자 권한 필요)",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "삭제 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "전체 삭제 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "모든 크롤링 데이터가 삭제되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> deleteAllCrawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails);
}