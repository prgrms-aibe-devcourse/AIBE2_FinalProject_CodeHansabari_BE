package com.cvmento.global.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * LLM 응답 파싱 공통 유틸리티
 */
@Slf4j
public class LlmParsingUtil {

    /**
     * 마크다운 코드 블록 제거 (```json...``` 또는 ```...``` 패턴)
     *
     * @param text 원본 텍스트
     * @return 코드 블록이 제거된 텍스트
     */
    public static String removeMarkdownCodeBlocks(String text) {
        if (text == null) {
            return null;
        }

        text = text.trim();

        // ```로 시작하고 끝나는 경우
        if (text.startsWith("```") && text.endsWith("```")) {
            String[] lines = text.split("\n");

            if (lines.length >= 3) {
                // 첫 줄(```json)과 마지막 줄(```) 제거
                String[] contentLines = Arrays.copyOfRange(lines, 1, lines.length - 1);
                String result = String.join("\n", contentLines).trim();

                log.debug("마크다운 코드 블록 제거됨 - 라인수: {} -> {}",
                        lines.length, contentLines.length);

                return result;
            } else if (lines.length == 1) {
                // 한 줄에 ```json{"key":"value"}``` 형태
                String content = text.substring(3, text.length() - 3);
                if (content.startsWith("json")) {
                    content = content.substring(4).trim();
                }
                return content;
            }
        }

        return text;
    }
}