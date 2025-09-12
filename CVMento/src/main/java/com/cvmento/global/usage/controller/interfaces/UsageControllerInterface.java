package com.cvmento.global.usage.controller.interfaces;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.usage.dto.TokenUsageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "토큰 사용량 관리", description = "토큰 사용량 조회 및 관리 API")
public interface UsageControllerInterface {

    @Operation(
            summary = "현재 토큰 사용량 조회",
            description = """
                사용자의 현재 토큰 보유량과 관련 정보를 조회합니다.
                
                **토큰 시스템 안내:**
                - 토큰은 다양한 AI 기능 사용 시 소모됩니다
                - 매 2시간마다 자동으로 토큰이 충전됩니다 (00:00, 02:00, 04:00...)
                - 최대 40개까지 보유 가능하며, 한 번에 10개씩 충전됩니다
                
                **기능별 토큰 소모량:**
                - AI 자소서 첨삭: 5토큰
                - 모의 면접 자동질문생성: 3토큰  
                - 커스텀 프롬프트 면접 답변생성: 1토큰
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 사용량 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "토큰이 있는 경우",
                                            summary = "토큰이 충분히 있는 일반적인 상황",
                                            value = """
                                            {
                                                "success": true,
                                                "message": "토큰 사용량 조회 성공",
                                                "data": {
                                                    "remainingTokens": 25,
                                                    "maxTokens": 40,
                                                    "nextRefillTime": "2025-09-12T14:00:00",
                                                    "refillAmount": 10
                                                },
                                                "errorCode": null,
                                                "canRetry": null,
                                                "timestamp": "2025-09-12T12:30:00"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "토큰이 부족한 경우",
                                            summary = "토큰을 많이 사용하여 얼마 남지 않은 상황",
                                            value = """
                                            {
                                                "success": true,
                                                "message": "토큰 사용량 조회 성공",
                                                "data": {
                                                    "remainingTokens": 2,
                                                    "maxTokens": 40,
                                                    "nextRefillTime": "2025-09-12T14:00:00",
                                                    "refillAmount": 10
                                                },
                                                "errorCode": null,
                                                "canRetry": null,
                                                "timestamp": "2025-09-12T12:30:00"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "timestamp": "2025-09-12T12:30:00",
                                        "status": 500,
                                        "error": "Internal Server Error",
                                        "errorCode": "INTERNAL_ERROR",
                                        "message": "서버 처리 중 오류가 발생했습니다",
                                        "errors": {}
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<CommonResponse<TokenUsageInfo>> getTokenUsage(UserDetails userDetails);
}