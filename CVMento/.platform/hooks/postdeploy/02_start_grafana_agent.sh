#!/bin/bash

# 로그 파일 설정
LOG_FILE="/var/log/grafana-agent-start.log"
exec > >(tee -a $LOG_FILE) 2>&1

echo "=== Grafana Agent 시작 스크립트: $(date) ==="

# Spring Boot 애플리케이션이 준비될 때까지 대기
echo "Spring Boot 애플리케이션 대기 중..."
SPRING_BOOT_READY=false
RETRY_COUNT=0
MAX_RETRIES=60  # 5분 대기

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s http://localhost:5000/actuator/health > /dev/null 2>&1; then
        echo "Spring Boot 애플리케이션이 준비되었습니다."
        SPRING_BOOT_READY=true
        break
    fi

    echo "Spring Boot 애플리케이션 대기 중... ($((RETRY_COUNT + 1))/$MAX_RETRIES)"
    sleep 5
    RETRY_COUNT=$((RETRY_COUNT + 1))
done

if [ "$SPRING_BOOT_READY" = false ]; then
    echo "경고: Spring Boot 애플리케이션이 준비되지 않았지만 Grafana Agent를 시작합니다."
fi

# 환경변수 확인
echo "환경변수 확인:"
echo "GRAFANA_PUSH_URL: ${GRAFANA_PUSH_URL:0:30}..."
echo "GRAFANA_USERNAME: ${GRAFANA_USERNAME}"
echo "GRAFANA_PASSWORD: ${GRAFANA_PASSWORD:0:5}..."

# 설정 파일 복사 및 환경변수 치환
echo "설정 파일 복사 중..."
sudo cp /var/app/current/agent-production.yml /etc/grafana-agent/agent.yml

# 환경변수 치환
sudo sed -i "s|\${GRAFANA_PUSH_URL}|${GRAFANA_PUSH_URL}|g" /etc/grafana-agent/agent.yml
sudo sed -i "s|\${GRAFANA_USERNAME}|${GRAFANA_USERNAME}|g" /etc/grafana-agent/agent.yml
sudo sed -i "s|\${GRAFANA_PASSWORD}|${GRAFANA_PASSWORD}|g" /etc/grafana-agent/agent.yml

# 권한 설정
sudo chown grafana:grafana /etc/grafana-agent/agent.yml
sudo chmod 640 /etc/grafana-agent/agent.yml

# systemd 서비스 파일 생성
echo "systemd 서비스 파일 생성 중..."
sudo tee /etc/systemd/system/grafana-agent.service > /dev/null <<EOF
[Unit]
Description=Grafana Agent
Documentation=https://grafana.com/docs/agent/
Wants=network-online.target
After=network-online.target
Requires=network.target

[Service]
Type=simple
User=grafana
Group=grafana
ExecStart=/usr/local/bin/grafana-agent --config.file=/etc/grafana-agent/agent.yml --config.expand-env --server.http.address=0.0.0.0:12345
Restart=always
RestartSec=5
StandardOutput=journal
StandardError=journal
SyslogIdentifier=grafana-agent
WorkingDirectory=/var/lib/grafana-agent

# 환경변수 설정
Environment=GRAFANA_PUSH_URL=${GRAFANA_PUSH_URL}
Environment=GRAFANA_USERNAME=${GRAFANA_USERNAME}
Environment=GRAFANA_PASSWORD=${GRAFANA_PASSWORD}

# 보안 설정
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/var/lib/grafana-agent
ReadWritePaths=/var/log/grafana-agent

[Install]
WantedBy=multi-user.target
EOF

# systemd 데몬 리로드
sudo systemctl daemon-reload

# 기존 grafana-agent 프로세스 종료
echo "기존 grafana-agent 프로세스 확인 및 종료..."
sudo pkill -f grafana-agent || true
sudo systemctl stop grafana-agent || true

# 잠시 대기
sleep 3

# 서비스 시작 및 활성화
echo "Grafana Agent 서비스 시작 중..."
sudo systemctl enable grafana-agent
sudo systemctl start grafana-agent

# 서비스 상태 확인
echo "서비스 상태 확인:"
sudo systemctl status grafana-agent --no-pager

# 프로세스 확인
echo "프로세스 확인:"
ps aux | grep grafana-agent | grep -v grep || echo "프로세스를 찾을 수 없습니다"

# 포트 확인 (Amazon Linux 2023에서는 ss 명령어 권장)
echo "포트 12345 확인:"
sudo ss -tlnp | grep :12345 || echo "포트 12345가 열려있지 않습니다"

echo "포트 5000 확인 (Spring Boot):"
sudo ss -tlnp | grep :5000 || echo "포트 5000이 열려있지 않습니다"

# Spring Boot 메트릭 엔드포인트 확인
echo "Spring Boot 메트릭 엔드포인트 확인:"
curl -s http://localhost:5000/actuator/prometheus | head -10 || echo "Spring Boot 메트릭 엔드포인트에 접근할 수 없습니다"

# 로그 확인
echo "최근 로그:"
sudo journalctl -u grafana-agent --no-pager -n 10

echo "=== Grafana Agent 시작 스크립트 완료: $(date) ==="