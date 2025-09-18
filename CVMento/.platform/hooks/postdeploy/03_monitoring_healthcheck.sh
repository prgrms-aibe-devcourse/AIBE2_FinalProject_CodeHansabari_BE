#!/bin/bash

LOG_FILE="/var/log/monitoring-healthcheck.log"
exec > >(tee -a $LOG_FILE) 2>&1

echo "=== 모니터링 헬스체크: $(date) ==="

# 1. 서비스 상태 확인
echo "1. Grafana Agent 서비스 상태:"
sudo systemctl is-active grafana-agent
sudo systemctl is-enabled grafana-agent

# 2. 프로세스 확인
echo "2. Grafana Agent 프로세스:"
ps aux | grep grafana-agent | grep -v grep || echo "Grafana Agent 프로세스를 찾을 수 없습니다"

# 3. Spring Boot 프로세스 확인
echo "3. Spring Boot 프로세스:"
ps aux | grep java | grep -v grep || echo "Java 프로세스를 찾을 수 없습니다"

# 4. 포트 점유 확인
echo "4. 포트 사용 현황:"
echo "포트 5000 (Spring Boot):"
sudo ss -tlnp | grep :5000 || echo "포트 5000이 사용되지 않고 있습니다"
echo "포트 12345 (Grafana Agent):"
sudo ss -tlnp | grep :12345 || echo "포트 12345가 사용되지 않고 있습니다"

# 5. 엔드포인트 테스트
echo "5. 엔드포인트 테스트:"
echo "Spring Boot Health:"
curl -s -w "HTTP Status: %{http_code}\n" http://localhost:5000/actuator/health || echo "Spring Boot Health 엔드포인트 실패"

echo "Spring Boot Prometheus:"
PROMETHEUS_RESPONSE=$(curl -s -w "HTTP Status: %{http_code}\n" http://localhost:5000/actuator/prometheus)
if echo "$PROMETHEUS_RESPONSE" | grep -q "HTTP Status: 200"; then
    echo "Spring Boot Prometheus 엔드포인트 성공"
    echo "메트릭 수: $(echo "$PROMETHEUS_RESPONSE" | grep -c "^[a-zA-Z]")"
else
    echo "Spring Boot Prometheus 엔드포인트 실패: $PROMETHEUS_RESPONSE"
fi

echo "Grafana Agent Health:"
curl -s -w "HTTP Status: %{http_code}\n" http://localhost:12345/metrics || echo "Grafana Agent 메트릭 엔드포인트 실패"

# 6. 설정 파일 확인
echo "6. 설정 파일 확인:"
if [ -f /etc/grafana-agent/agent.yml ]; then
    echo "Grafana Agent 설정 파일 존재"
    echo "설정 파일 크기: $(stat -c%s /etc/grafana-agent/agent.yml) bytes"
    # 민감한 정보를 제외하고 일부 설정 확인
    grep -E "(scrape_interval|job_name|targets)" /etc/grafana-agent/agent.yml || echo "설정 파일 읽기 실패"
else
    echo "Grafana Agent 설정 파일을 찾을 수 없습니다"
fi

# 7. 로그 확인
echo "7. 최근 로그 (마지막 20줄):"
sudo journalctl -u grafana-agent --no-pager -n 20 || echo "systemd 로그를 읽을 수 없습니다"

# 8. 네트워크 연결성 테스트
echo "8. 외부 연결성 테스트:"
if [ -n "$GRAFANA_PUSH_URL" ]; then
    # URL에서 호스트 추출
    GRAFANA_HOST=$(echo "$GRAFANA_PUSH_URL" | sed -E 's|^https?://([^/]+).*|\1|')
    echo "Grafana Cloud 연결성 테스트 ($GRAFANA_HOST):"
    ping -c 3 "$GRAFANA_HOST" || echo "Grafana Cloud에 ping 실패"
else
    echo "GRAFANA_PUSH_URL 환경변수가 설정되지 않음"
fi

# 9. 디스크 사용량 확인
echo "9. 디스크 사용량:"
df -h /var/lib/grafana-agent /var/log/grafana-agent 2>/dev/null || echo "Grafana Agent 디렉토리 확인 실패"

echo "=== 모니터링 헬스체크 완료: $(date) ==="
echo "=== 헬스체크 요약 ==="
echo "Grafana Agent 서비스: $(sudo systemctl is-active grafana-agent 2>/dev/null || echo 'inactive')"
echo "Spring Boot (포트 5000): $(ss -tln | grep :5000 >/dev/null && echo 'active' || echo 'inactive')"
echo "Grafana Agent (포트 12345): $(ss -tln | grep :12345 >/dev/null && echo 'active' || echo 'inactive')"
echo "=======================================\n"