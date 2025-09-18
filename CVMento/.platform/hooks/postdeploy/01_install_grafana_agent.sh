#!/bin/bash

# 로그 파일 설정
LOG_FILE="/var/log/grafana-agent-install.log"
exec > >(tee -a $LOG_FILE) 2>&1

echo "=== Grafana Agent 설치 시작: $(date) ==="

# 현재 사용자 확인
echo "현재 사용자: $(whoami)"
echo "현재 디렉토리: $(pwd)"

# Grafana Agent가 이미 설치되어 있는지 확인
if command -v grafana-agent &> /dev/null; then
    echo "Grafana Agent가 이미 설치되어 있습니다."
    grafana-agent --version
else
    echo "Grafana Agent 설치 중..."

    # Amazon Linux 2023용 시스템 업데이트
    sudo dnf update -y

    # Grafana Agent 다운로드 및 설치 (최신 버전)
    AGENT_VERSION="v0.38.1"
    AGENT_URL="https://github.com/grafana/agent/releases/download/${AGENT_VERSION}/grafana-agent-linux-amd64.zip"

    # 임시 디렉토리에서 작업
    cd /tmp

    # 필요한 패키지 설치 (Amazon Linux 2023은 dnf 사용)
    if ! command -v wget &> /dev/null; then
        sudo dnf install -y wget
    fi

    if ! command -v unzip &> /dev/null; then
        sudo dnf install -y unzip
    fi

    # 네트워크 도구도 함께 설치 (디버깅용)
    sudo dnf install -y net-tools procps-ng

    # Agent 다운로드
    echo "Grafana Agent 다운로드 중: $AGENT_URL"
    wget -O grafana-agent.zip "$AGENT_URL"

    if [ $? -eq 0 ]; then
        echo "다운로드 완료"

        # 압축 해제
        unzip -o grafana-agent.zip

        # 실행 권한 부여 및 시스템 경로로 이동
        chmod +x grafana-agent-linux-amd64
        sudo mv grafana-agent-linux-amd64 /usr/local/bin/grafana-agent

        # 설치 확인
        if grafana-agent --version; then
            echo "Grafana Agent 설치 완료"
        else
            echo "Grafana Agent 설치 실패"
            exit 1
        fi

        # 임시 파일 정리
        rm -f grafana-agent.zip
    else
        echo "Grafana Agent 다운로드 실패"
        exit 1
    fi
fi

# grafana 사용자 생성 (없는 경우)
if ! id "grafana" &>/dev/null; then
    echo "grafana 사용자 생성 중..."
    sudo useradd --system --no-create-home --shell /bin/false grafana
fi

# 설정 파일 디렉토리 생성
sudo mkdir -p /etc/grafana-agent
sudo mkdir -p /var/lib/grafana-agent
sudo mkdir -p /var/log/grafana-agent

# 권한 설정
sudo chown -R grafana:grafana /var/lib/grafana-agent
sudo chown -R grafana:grafana /var/log/grafana-agent

echo "=== Grafana Agent 설치 완료: $(date) ==="