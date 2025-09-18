package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterData;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "자소서 크롤링", description = "자소서 크롤링 관리 API. 외부 사이트에서 합격 자소서를 수집(크롤링)하고, 관리하는 기능을 제공합니다.")
public interface CrawlCoverLetterControllerInterface {

    @Operation(
            summary = "자소서 크롤링 실행",
            description = """
                    Linkareer API를 호출하여 IT 직무 합격 자소서를 크롤링하고 DB에 저장합니다.
                    
                    **크롤링 대상:**
                    - 사이트: Linkareer (https://api.linkareer.com/graphql)
                    - 직무: IT
                    - 상태: PUBLISHED (공개된 자소서)
                    - 정렬: 합격일 기준 내림차순
                    - 페이지 크기: 314개
                    
                    **처리 과정:**
                    1. 기존 크롤링 데이터 삭제
                    2. Linkareer API 호출
                    3. 응답에서 content 필드 추출
                    4. DB에 저장
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "크롤링 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "크롤링 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "success": true,
                                                        "message": "자소서 크롤링이 완료되었습니다.",
                                                        "crawledCount": 150
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한이 필요합니다",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "권한 부족",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "ACCESS_DENIED",
                                                      "message": "크롤링을 실행할 권한이 없습니다. 관리자 권한이 필요합니다.",
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류 - 크롤링 중 오류 발생",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<?>> crawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails);

    @Operation(
            summary = "크롤링 데이터 페이징 조회",
            description = """
                    크롤링으로 수집된 원본 자소서 데이터를 페이징으로 조회합니다.
                    프론트엔드 관리자 페이지에서 수집된 데이터를 확인하는 용도로 사용할 수 있습니다.
                    
                    **페이징 및 정렬 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작, 기본값: 0)
                    - `size`: 한 페이지에 보여줄 데이터 개수 (기본값: 20)
                    - `sort`: 정렬 기준. `{필드명},{ASC|DESC}` 형식으로 전달합니다.
                      - **정렬 가능 필드**: `createdAt`(생성일), `updatedAt`(수정일)
                    
                    **사용 예시:**
                    - 첫 페이지 조회: `?page=0&size=20`
                    - 수정일 내림차순 정렬: `?sort=updatedAt,desc`
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "페이징 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "페이징 응답 예시",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "content": [
                                                          {
                                                            "coverLetterId": 1,
                                                            "text": "저는 소프트웨어 개발에 대한 깊은 열정을 바탕으로...",
                                                            "createdAt": "2024-01-15T10:00:00",
                                                            "updatedAt": "2024-01-15T10:00:00"
                                                          }
                                                        ],
                                                        "pageable": {
                                                          "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                          "offset": 0,
                                                          "pageNumber": 0,
                                                          "pageSize": 20,
                                                          "paged": true,
                                                          "unpaged": false
                                                        },
                                                        "totalElements": 1,
                                                        "totalPages": 1,
                                                        "last": true,
                                                        "size": 20,
                                                        "number": 0,
                                                        "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                        "numberOfElements": 1,
                                                        "first": true,
                                                        "empty": false
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Page<CrawlCoverLetterData>>> getCrawlCoverLettersWithPagination(
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "크롤링 데이터 개별 조회",
            description = "특정 ID의 크롤링된 자소서 데이터를 조회합니다. (관리자 권한 필요)",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "크롤링 데이터 상세",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "coverLetterId": 1,
                                                        "text": "저는 소프트웨어 개발에 대한 깊은 열정을 바탕으로 지속적인 성장을 추구하고 있습니다. 대학교 재학 중부터 다양한 프로젝트를 통해 실무 경험을 쌓아왔으며...",
                                                        "createdAt": "2024-01-15T10:00:00",
                                                        "updatedAt": "2024-01-15T10:00:00"
                                                      },
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
    ResponseEntity<CommonResponse<CrawlCoverLetterData>> getCrawlCoverLetterById(
            @Parameter(description = "크롤링 데이터 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "크롤링 데이터 수정",
            description = "특정 ID의 크롤링된 자소서 데이터를 수정합니다. (관리자 권한 필요)",
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
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "수정 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "coverLetterId": 1,
                                                        "text": "저는 소프트웨어 개발 분야에서 지속적인 성장을 추구하는 개발자입니다...",
                                                        "createdAt": "2024-01-15T10:00:00",
                                                        "updatedAt": "2024-01-15T14:30:00"
                                                      },
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
    ResponseEntity<CommonResponse<CrawlCoverLetterData>> updateCrawlCoverLetter(
            @Parameter(description = "수정할 크롤링 데이터 ID") @PathVariable Long id,
            @RequestBody UpdateCrawlCoverLetterRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "크롤링 데이터 개별 삭제",
            description = "특정 ID의 크롤링된 자소서 데이터를 삭제합니다. (관리자 권한 필요)",
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
            summary = "크롤링 데이터 전체 삭제",
            description = "크롤링된 모든 자소서 데이터를 삭제합니다. (관리자 권한 필요)",
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