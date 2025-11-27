@echo off
chcp 65001 >nul
echo ============================================
echo    scrcpy 屏幕共享启动器 - 华为设备专用
echo ============================================
echo.

echo 正在启动 scrcpy 连接到华为设备...
echo 设备ID: 78DUT21107002769
echo.

REM 尝试不同的启动方式
echo 方法1: 基本连接...
scrcpy -s 78DUT21107002769 --max-size 1200 --stay-awake

if %errorlevel% neq 0 (
    echo.
    echo 基本连接失败，尝试方法2...
    echo 方法2: 兼容模式...
    scrcpy --max-size 1080 --bit-rate 2M --no-audio --stay-awake
)

if %errorlevel% neq 0 (
    echo.
    echo 兼容模式失败，尝试方法3...
    echo 方法3: 极简模式...
    scrcpy --no-audio
)

echo.
echo 连接已断开
pause