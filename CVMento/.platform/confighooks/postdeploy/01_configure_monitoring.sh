#!/bin/bash

# EB 환경에서 추가 설정
LOG_FILE="/var/log/eb-monitoring-config.log"
exec > >(tee -a $LOG_FILE) 2>&1

echo "=== EB 모니터링 환경 설정: $(date) ==="

# Java 애플리케이션이 메트릭을 노출할 수 있도록 JVM 옵션 확인
echo "Java 프로세스 확인:"
ps aux | grep java | grep -v grep

# Spring Boot Actuator 엔드포인트 활성화 확인
echo "Spring Boot Actuator 엔드포인트 확인:"
sleep 10  # Spring Boot 시작 대기
curl -s http://localhost:5000/actuator | jq . || echo "Actuator 엔드포인트에 접근할 수 없거나 jq가 설치되지 않음"

# 방화벽 설정 확인 (Amazon Linux 2023에서는 firewalld 대신 iptables 사용)
echo "방화벽 상태 확인:"
sudo iptables -L -n | grep -E "(12345|5000)" || echo "특별한 방화벽 규칙 없음"

# 로그 파일 권한 설정
sudo chmod 644 /var/log/grafana-agent-*.log || true

echo "=== EB 모니터링 환경 설정 완료: $(date) ==="