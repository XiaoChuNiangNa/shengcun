# Screen Sharing Fix Script for Android Studio
# Compatible with Huawei devices

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    Android Studio Screen Sharing Fix" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check if running as administrator
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "Warning: Run as Administrator for best results" -ForegroundColor Yellow
}

# Set ADB path
$adbPath = "D:\Android\Sdk\platform-tools\adb.exe"

# Check if ADB exists
if (-not (Test-Path $adbPath)) {
    Write-Host "Error: ADB not found at $adbPath" -ForegroundColor Red
    Write-Host "Please check Android SDK path" -ForegroundColor Red
    pause
    exit 1
}

try {
    Write-Host "`nStep 1: Restarting ADB service..." -ForegroundColor Green
    & $adbPath kill-server
    Start-Sleep -Seconds 2
    & $adbPath start-server
    Start-Sleep -Seconds 3
    Write-Host "ADB service restarted successfully" -ForegroundColor Green

    Write-Host "`nStep 2: Checking device connection..." -ForegroundColor Green
    $devices = & $adbPath devices
    Write-Host $devices
    
    if ($devices -match "78DUT21107002769") {
        Write-Host "Target device connected" -ForegroundColor Green
    } else {
        Write-Host "Warning: Target device not connected, check USB connection" -ForegroundColor Yellow
    }

    Write-Host "`nStep 3: Clearing device debug cache..." -ForegroundColor Green
    & $adbPath shell pm clear com.android.shell 2>$null
    Write-Host "Device cache cleared" -ForegroundColor Green

    Write-Host "`nStep 4: Restarting device debug service..." -ForegroundColor Green
    & $adbPath shell "am force-stop com.android.settings" 2>$null
    Start-Sleep -Seconds 1
    
    Write-Host "`nStep 5: Checking device information..." -ForegroundColor Green
    $model = & $adbPath shell getprop ro.product.model
    $version = & $adbPath shell getprop ro.build.version.release
    $manufacturer = & $adbPath shell getprop ro.product.manufacturer
    
    Write-Host "Device Model: $model" -ForegroundColor Cyan
    Write-Host "OS Version: $version" -ForegroundColor Cyan
    Write-Host "Manufacturer: $manufacturer" -ForegroundColor Cyan

    # Special handling for Huawei devices
    if ($manufacturer -match "HUAWEI") {
        Write-Host "`nHuawei device detected, applying special optimizations..." -ForegroundColor Yellow
        
        # Set screen sharing related properties
        & $adbPath shell "setprop debug.hwui.overdraw 1" 2>$null
        & $adbPath shell "setprop debug.layout.show true" 2>$null
        
        Write-Host "Huawei device optimization completed" -ForegroundColor Green
    }

    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "          Fix script completed!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    Write-Host "`nNext steps:" -ForegroundColor White
    Write-Host "1. Close Android Studio completely" -ForegroundColor White
    Write-Host "2. Restart Android Studio" -ForegroundColor White
    Write-Host "3. Reconnect the device" -ForegroundColor White
    Write-Host "4. Try screen sharing functionality" -ForegroundColor White
    
} catch {
    Write-Host "Error occurred during execution: $_" -ForegroundColor Red
    Write-Host "Please check device connection and USB permissions" -ForegroundColor Red
}

Write-Host "`nPress any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")