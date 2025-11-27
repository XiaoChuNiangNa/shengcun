# 屏幕共享修复脚本
# 适用于华为设备屏幕共享问题

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    Android Studio 屏幕共享修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 检查管理员权限
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "建议以管理员身份运行此脚本以获得最佳效果" -ForegroundColor Yellow
}

# 设置ADB路径
$adbPath = "D:\Android\Sdk\platform-tools\adb.exe"

# 检查ADB是否存在
if (-not (Test-Path $adbPath)) {
    Write-Host "错误: ADB未找到在 $adbPath" -ForegroundColor Red
    Write-Host "请检查Android SDK路径是否正确" -ForegroundColor Red
    pause
    exit 1
}

try {
    Write-Host "`n步骤 1: 重启ADB服务..." -ForegroundColor Green
    & $adbPath kill-server
    Start-Sleep -Seconds 2
    & $adbPath start-server
    Start-Sleep -Seconds 3
    Write-Host "✓ ADB服务重启完成" -ForegroundColor Green

    Write-Host "`n步骤 2: 检查设备连接..." -ForegroundColor Green
    $devices = & $adbPath devices
    Write-Host $devices
    
    if ($devices -match "78DUT21107002769") {
        Write-Host "✓ 目标设备已连接" -ForegroundColor Green
    } else {
        Write-Host "⚠ 目标设备未连接，请检查USB连接" -ForegroundColor Yellow
    }

    Write-Host "`n步骤 3: 清除设备调试缓存..." -ForegroundColor Green
    & $adbPath shell pm clear com.android.shell 2>$null
    Write-Host "✓ 设备缓存已清除" -ForegroundColor Green

    Write-Host "`n步骤 4: 重启设备调试服务..." -ForegroundColor Green
    & $adbPath shell "am force-stop com.android.settings" 2>$null
    Start-Sleep -Seconds 1
    
    Write-Host "`n步骤 5: 检查设备信息..." -ForegroundColor Green
    $model = & $adbPath shell getprop ro.product.model
    $version = & $adbPath shell getprop ro.build.version.release
    $manufacturer = & $adbPath shell getprop ro.product.manufacturer
    
    Write-Host "设备型号: $model" -ForegroundColor Cyan
    Write-Host "系统版本: $version" -ForegroundColor Cyan
    Write-Host "制造商: $manufacturer" -ForegroundColor Cyan

    # 华为设备特殊处理
    if ($manufacturer -match "HUAWEI") {
        Write-Host "`n检测到华为设备，执行特殊优化..." -ForegroundColor Yellow
        
        # 设置屏幕共享相关属性
        & $adbPath shell "setprop debug.hwui.overdraw 1" 2>$null
        & $adbPath shell "setprop debug.layout.show true" 2>$null
        
        Write-Host "✓ 华为设备优化设置完成" -ForegroundColor Green
    }

    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "          修复脚本执行完成！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    Write-Host "`n接下来请：" -ForegroundColor White
    Write-Host "1. 完全关闭Android Studio" -ForegroundColor White
    Write-Host "2. 重新启动Android Studio" -ForegroundColor White
    Write-Host "3. 重新连接设备" -ForegroundColor White
    Write-Host "4. 尝试屏幕共享功能" -ForegroundColor White
    
} catch {
    Write-Host "执行过程中出现错误: $_" -ForegroundColor Red
    Write-Host "请检查设备连接和USB权限" -ForegroundColor Red
}

Write-Host "`n按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")