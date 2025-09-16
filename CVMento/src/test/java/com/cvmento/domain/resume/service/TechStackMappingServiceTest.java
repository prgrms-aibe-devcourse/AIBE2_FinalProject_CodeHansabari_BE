package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.entity.TechStack;
import com.cvmento.domain.resume.repository.TechStackRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * TechStackMappingService의 단위 테스트.
 *
 * 정상 시나리오:
 * - 기술스택 이름으로 ID 조회 (캐시 사용)
 * - 기술스택 엔티티 조회
 * - 캐시 초기화 및 갱신
 * - 전체 기술스택 이름 목록 조회
 *
 * 비정상 시나리오:
 * - 빈 이름으로 조회
 * - 존재하지 않는 기술스택 조회
 * - 캐시 초기화 실패
 * - DB 조회 실패
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TechStackMappingService 단위 테스트")
@Slf4j
class TechStackMappingServiceTest {

    private static final String JAVA_TECH_STACK = "Java";
    private static final String SPRING_TECH_STACK = "Spring";
    private static final String REACT_TECH_STACK = "React";
    private static final String UNKNOWN_TECH_STACK = "UnknownTech";
    private static final Long JAVA_ID = 1L;
    private static final Long SPRING_ID = 2L;
    private static final Long REACT_ID = 3L;

    @Mock
    private TechStackRepository techStackRepository;

    @InjectMocks
    private TechStackMappingService techStackMappingService;

    private List<TechStack> mockTechStacks;
    private TechStack javaTechStack;
    private TechStack springTechStack;
    private TechStack reactTechStack;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        javaTechStack = createMockTechStack(JAVA_ID, JAVA_TECH_STACK);
        springTechStack = createMockTechStack(SPRING_ID, SPRING_TECH_STACK);
        reactTechStack = createMockTechStack(REACT_ID, REACT_TECH_STACK);

        mockTechStacks = List.of(javaTechStack, springTechStack, reactTechStack);

        // 캐시 초기화 (각 테스트마다 깨끗한 상태로 시작)
        ReflectionTestUtils.setField(techStackMappingService, "techStackNameToIdCache", null);

        log.info("테스트 Mock 기술스택 생성 완료: {}개", mockTechStacks.size());
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("기술스택 ID 조회 테스트")
    class FindTechStackIdTests {

        @Test
        @DisplayName("기술스택 이름으로 ID 조회 성공 (캐시 사용)")
        void findTechStackIdByName_WithValidName_Success() {
            log.info("=== 테스트 시작: 기술스택 이름으로 ID 조회 성공 (캐시 사용) ===");

            // Given
            given(techStackRepository.findAll()).willReturn(mockTechStacks);

            // When
            Optional<Long> javaId = techStackMappingService.findTechStackIdByName(JAVA_TECH_STACK);
            Optional<Long> springId = techStackMappingService.findTechStackIdByName(SPRING_TECH_STACK);

            // Then
            assertThat(javaId).isPresent();
            assertThat(javaId.get()).isEqualTo(JAVA_ID);
            assertThat(springId).isPresent();
            assertThat(springId.get()).isEqualTo(SPRING_ID);

            // 캐시 초기화를 위해 findAll은 한 번만 호출되어야 함
            verify(techStackRepository, times(1)).findAll();

            log.info("✅ 기술스택 ID 조회 성공 - Java: {}, Spring: {}", javaId.get(), springId.get());
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 기술스택 이름으로 조회 시 빈 Optional 반환")
        void findTechStackIdByName_WithUnknownName_ReturnEmpty() {
            log.info("=== 테스트 시작: 존재하지 않는 기술스택 이름으로 조회 시 빈 Optional 반환 ===");

            // Given
            given(techStackRepository.findAll()).willReturn(mockTechStacks);

            // When
            Optional<Long> unknownId = techStackMappingService.findTechStackIdByName(UNKNOWN_TECH_STACK);

            // Then
            assertThat(unknownId).isEmpty();

            log.info("✅ 존재하지 않는 기술스택 조회 시 빈 결과 반환 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("빈 이름으로 조회 시 빈 Optional 반환")
        void findTechStackIdByName_WithEmptyName_ReturnEmpty() {
            log.info("=== 테스트 시작: 빈 이름으로 조회 시 빈 Optional 반환 ===");

            // When & Then
            assertThat(techStackMappingService.findTechStackIdByName("")).isEmpty();
            assertThat(techStackMappingService.findTechStackIdByName("   ")).isEmpty();
            assertThat(techStackMappingService.findTechStackIdByName(null)).isEmpty();

            // 빈 이름으로는 DB 조회하지 않음
            verify(techStackRepository, never()).findAll();

            log.info("✅ 빈 이름 조회 시 빈 결과 반환 및 DB 조회 안함 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("공백이 포함된 기술스택 이름 정상 처리")
        void findTechStackIdByName_WithWhitespace_TrimAndSuccess() {
            log.info("=== 테스트 시작: 공백이 포함된 기술스택 이름 정상 처리 ===");

            // Given
            given(techStackRepository.findAll()).willReturn(mockTechStacks);

            // When
            Optional<Long> javaId = techStackMappingService.findTechStackIdByName("  Java  ");
            Optional<Long> springId = techStackMappingService.findTechStackIdByName("\tSpring\n");

            // Then
            assertThat(javaId).isPresent();
            assertThat(javaId.get()).isEqualTo(JAVA_ID);
            assertThat(springId).isPresent();
            assertThat(springId.get()).isEqualTo(SPRING_ID);

            log.info("✅ 공백 포함 기술스택 이름 trim 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("캐시 초기화 실패 시에도 빈 캐시로 동작")
        void findTechStackIdByName_WithCacheInitFailure_WorkWithEmptyCache() {
            log.info("=== 테스트 시작: 캐시 초기화 실패 시에도 빈 캐시로 동작 ===");

            // Given
            given(techStackRepository.findAll()).willThrow(new RuntimeException("DB 연결 실패"));

            // When
            Optional<Long> result = techStackMappingService.findTechStackIdByName(JAVA_TECH_STACK);

            // Then
            assertThat(result).isEmpty();

            log.info("✅ 캐시 초기화 실패 시 빈 결과 반환 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("기술스택 엔티티 조회 테스트")
    class FindTechStackEntityTests {

        @Test
        @DisplayName("기술스택 이름으로 엔티티 조회 성공")
        void findTechStackByName_WithValidName_Success() {
            log.info("=== 테스트 시작: 기술스택 이름으로 엔티티 조회 성공 ===");

            // Given
            given(techStackRepository.findByName(JAVA_TECH_STACK)).willReturn(Optional.of(javaTechStack));

            // When
            Optional<TechStack> result = techStackMappingService.findTechStackByName(JAVA_TECH_STACK);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo(JAVA_TECH_STACK);
            assertThat(result.get().getId()).isEqualTo(JAVA_ID);

            verify(techStackRepository).findByName(JAVA_TECH_STACK);

            log.info("✅ 기술스택 엔티티 조회 성공 - 이름: {}, ID: {}",
                    result.get().getName(), result.get().getId());
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 기술스택 엔티티 조회 시 빈 Optional 반환")
        void findTechStackByName_WithUnknownName_ReturnEmpty() {
            log.info("=== 테스트 시작: 존재하지 않는 기술스택 엔티티 조회 시 빈 Optional 반환 ===");

            // Given
            given(techStackRepository.findByName(UNKNOWN_TECH_STACK)).willReturn(Optional.empty());

            // When
            Optional<TechStack> result = techStackMappingService.findTechStackByName(UNKNOWN_TECH_STACK);

            // Then
            assertThat(result).isEmpty();

            verify(techStackRepository).findByName(UNKNOWN_TECH_STACK);

            log.info("✅ 존재하지 않는 기술스택 엔티티 조회 시 빈 결과 반환 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("빈 이름으로 엔티티 조회 시 빈 Optional 반환")
        void findTechStackByName_WithEmptyName_ReturnEmpty() {
            log.info("=== 테스트 시작: 빈 이름으로 엔티티 조회 시 빈 Optional 반환 ===");

            // When & Then
            assertThat(techStackMappingService.findTechStackByName("")).isEmpty();
            assertThat(techStackMappingService.findTechStackByName(null)).isEmpty();

            // 빈 이름으로는 DB 조회하지 않음
            verify(techStackRepository, never()).findByName(anyString());

            log.info("✅ 빈 이름으로 엔티티 조회 시 DB 조회 안함 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("공백이 포함된 이름으로 엔티티 조회 시 trim 처리")
        void findTechStackByName_WithWhitespace_TrimAndQuery() {
            log.info("=== 테스트 시작: 공백이 포함된 이름으로 엔티티 조회 시 trim 처리 ===");

            // Given
            given(techStackRepository.findByName(JAVA_TECH_STACK)).willReturn(Optional.of(javaTechStack));

            // When
            Optional<TechStack> result = techStackMappingService.findTechStackByName("  Java  ");

            // Then
            assertThat(result).isPresent();

            // trim된 이름으로 조회했는지 확인
            verify(techStackRepository).findByName(JAVA_TECH_STACK);

            log.info("✅ 공백 포함 이름 trim 처리 후 조회 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("캐시 관리 테스트")
    class CacheManagementTests {

        @Test
        @DisplayName("캐시 수동 갱신 성공")
        void refreshCache_Success() {
            log.info("=== 테스트 시작: 캐시 수동 갱신 성공 ===");

            // Given
            given(techStackRepository.findAll()).willReturn(mockTechStacks);

            // 첫 번째 조회로 캐시 초기화
            techStackMappingService.findTechStackIdByName(JAVA_TECH_STACK);

            // When
            techStackMappingService.refreshCache();

            // Then
            // findAll이 총 2번 호출되어야 함 (초기화 1번 + 갱신 1번)
            verify(techStackRepository, times(2)).findAll();

            // 갱신된 캐시로 정상 조회 확인
            Optional<Long> javaId = techStackMappingService.findTechStackIdByName(JAVA_TECH_STACK);
            assertThat(javaId).isPresent();
            assertThat(javaId.get()).isEqualTo(JAVA_ID);

            log.info("✅ 캐시 수동 갱신 및 재조회 성공");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("캐시 갱신 실패 시에도 빈 캐시로 동작")
        void refreshCache_FailureButWorkWithEmptyCache() {
            log.info("=== 테스트 시작: 캐시 갱신 실패 시에도 빈 캐시로 동작 ===");

            // Given
            given(techStackRepository.findAll()).willThrow(new RuntimeException("DB 오류"));

            // When
            techStackMappingService.refreshCache();

            // Then
            Optional<Long> result = techStackMappingService.findTechStackIdByName(JAVA_TECH_STACK);
            assertThat(result).isEmpty();

            log.info("✅ 캐시 갱신 실패 시 빈 캐시로 동작 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("여러 번 ID 조회 시 캐시 재사용 확인")
        void findTechStackIdByName_MultipleCalls_ReuseCache() {
            log.info("=== 테스트 시작: 여러 번 ID 조회 시 캐시 재사용 확인 ===");

            // Given
            given(techStackRepository.findAll()).willReturn(mockTechStacks);

            // When
            Optional<Long> firstCall = techStackMappingService.findTechStackIdByName(JAVA_TECH_STACK);
            Optional<Long> secondCall = techStackMappingService.findTechStackIdByName(JAVA_TECH_STACK);
            Optional<Long> thirdCall = techStackMappingService.findTechStackIdByName(SPRING_TECH_STACK);

            // Then
            assertThat(firstCall).isPresent();
            assertThat(secondCall).isPresent();
            assertThat(thirdCall).isPresent();

            // findAll은 캐시 초기화를 위해 한 번만 호출
            verify(techStackRepository, times(1)).findAll();

            log.info("✅ 캐시 재사용으로 DB 조회 최소화 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("전체 기술스택 목록 조회 테스트")
    class GetAllTechStackNamesTests {

        @Test
        @DisplayName("전체 기술스택 이름 목록 조회 성공")
        void getAllTechStackNames_Success() {
            log.info("=== 테스트 시작: 전체 기술스택 이름 목록 조회 성공 ===");

            // Given
            given(techStackRepository.findAll()).willReturn(mockTechStacks);

            // When
            List<String> allNames = techStackMappingService.getAllTechStackNames();

            // Then
            assertThat(allNames).hasSize(3);
            assertThat(allNames).containsExactly(JAVA_TECH_STACK, REACT_TECH_STACK, SPRING_TECH_STACK); // 정렬된 순서

            verify(techStackRepository).findAll();

            log.info("✅ 전체 기술스택 이름 목록 조회 성공 - {}개", allNames.size());
            log.info("조회된 기술스택: {}", allNames);
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("기술스택이 없을 때 빈 목록 반환")
        void getAllTechStackNames_WithEmptyRepository_ReturnEmptyList() {
            log.info("=== 테스트 시작: 기술스택이 없을 때 빈 목록 반환 ===");

            // Given
            given(techStackRepository.findAll()).willReturn(List.of());

            // When
            List<String> allNames = techStackMappingService.getAllTechStackNames();

            // Then
            assertThat(allNames).isEmpty();

            log.info("✅ 빈 기술스택 목록 반환 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("기술스택 이름 정렬 확인")
        void getAllTechStackNames_CheckSorting() {
            log.info("=== 테스트 시작: 기술스택 이름 정렬 확인 ===");

            // Given
            TechStack zTechStack = createMockTechStack(4L, "Zookeeper");
            TechStack aTechStack = createMockTechStack(5L, "Angular");
            List<TechStack> unsortedTechStacks = List.of(zTechStack, javaTechStack, aTechStack, springTechStack);

            given(techStackRepository.findAll()).willReturn(unsortedTechStacks);

            // When
            List<String> allNames = techStackMappingService.getAllTechStackNames();

            // Then
            assertThat(allNames).hasSize(4);
            assertThat(allNames).containsExactly("Angular", "Java", "Spring", "Zookeeper"); // 알파벳 정렬

            log.info("✅ 기술스택 이름 정렬 확인 - 정렬된 순서: {}", allNames);
            log.info("=== 테스트 완료 ===\n");
        }
    }

    // 테스트 헬퍼 메서드
    private TechStack createMockTechStack(Long id, String name) {
        TechStack techStack = mock(TechStack.class);
        given(techStack.getId()).willReturn(id);
        given(techStack.getName()).willReturn(name);
        log.debug("Mock 기술스택 생성: ID={}, Name={}", id, name);
        return techStack;
    }
}