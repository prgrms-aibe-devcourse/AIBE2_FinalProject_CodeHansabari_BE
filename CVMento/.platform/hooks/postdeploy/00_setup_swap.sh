#!/bin/bash

# 로그 파일 설정
LOG_FILE="/var/log/swap-setup.log"
exec > >(tee -a $LOG_FILE) 2>&1

echo "=== Swap 메모리 설정 시작: $(date) ==="

# 현재 메모리 및 swap 상태 확인
echo "현재 메모리 상태:"
free -h

echo "현재 swap 상태:"
sudo swapon --show

# swap 파일이 이미 존재하는지 확인
if [ -f /swapfile ]; then
    echo "Swap 파일이 이미 존재합니다."

    # 활성화되어 있는지 확인
    if sudo swapon --show | grep -q "/swapfile"; then
        echo "Swap이 이미 활성화되어 있습니다."
        echo "기존 swap 정보:"
        sudo swapon --show
    else
        echo "Swap 파일은 존재하지만 활성화되지 않음. 활성화 중..."
        sudo swapon /swapfile
    fi
else
    echo "Swap 파일이 존재하지 않음. 새로 생성 중..."

    # 사용 가능한 디스크 공간 확인
    echo "디스크 사용량 확인:"
    df -h /

    # swap 파일 생성 (1GB)
    echo "1GB swap 파일 생성 중... (시간이 걸릴 수 있습니다)"
    sudo dd if=/dev/zero of=/swapfile bs=1M count=1024

    if [ $? -eq 0 ]; then
        echo "Swap 파일 생성 완료"

        # 권한 설정
        sudo chmod 600 /swapfile
        echo "Swap 파일 권한 설정 완료"

        # swap 파일 포맷
        sudo mkswap /swapfile
        echo "Swap 파일 포맷 완료"

        # swap 활성화
        sudo swapon /swapfile
        echo "Swap 활성화 완료"

    else
        echo "ERROR: Swap 파일 생성 실패"
        exit 1
    fi
fi

# /etc/fstab에 영구 설정 추가 (중복 방지)
if ! grep -q "/swapfile" /etc/fstab; then
    echo "fstab에 swap 설정 추가 중..."
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    echo "fstab 설정 완료"
else
    echo "fstab에 이미 swap 설정이 존재합니다"
fi

# swappiness 설정 (선택사항 - 메모리가 부족할 때만 swap 사용)
echo "swappiness 설정 확인 및 조정..."
CURRENT_SWAPPINESS=$(cat /proc/sys/vm/swappiness)
echo "현재 swappiness: $CURRENT_SWAPPINESS"

# swappiness를 10으로 설정 (기본값 60보다 낮게 - RAM 우선 사용)
if [ "$CURRENT_SWAPPINESS" -ne 10 ]; then
    echo "swappiness를 10으로 설정 중..."
    echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
    sudo sysctl vm.swappiness=10
    echo "swappiness 설정 완료"
else
    echo "swappiness가 이미 적절히 설정되어 있습니다"
fi

# 최종 상태 확인
echo "=== 최종 메모리 상태 ==="
free -h
echo ""
echo "Swap 상세 정보:"
sudo swapon --show
echo ""
echo "디스크 사용량:"
df -h /

echo "=== Swap 메모리 설정 완료: $(date) ==="

# 시스템 리소스 요약
echo "=== 시스템 리소스 요약 ==="
echo "총 RAM: $(free -h | grep Mem | awk '{print $2}')"
echo "사용 가능한 RAM: $(free -h | grep Mem | awk '{print $7}')"
echo "총 Swap: $(free -h | grep Swap | awk '{print $2}')"
echo "=============================="