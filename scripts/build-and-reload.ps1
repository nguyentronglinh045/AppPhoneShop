# Script để build và reload app nhanh (khi emulator đã chạy)
# Sử dụng: .\scripts\build-and-reload.ps1

Write-Host "=== Quick Build & Reload ===" -ForegroundColor Cyan

# Đường dẫn đến local.properties
$projectRoot = Split-Path $PSScriptRoot
$localPropertiesPath = Join-Path $projectRoot "local.properties"

# Kiểm tra local.properties
if (-not (Test-Path $localPropertiesPath)) {
    Write-Host "`nERROR: local.properties not found!" -ForegroundColor Red
    Write-Host "Please run .\scripts\run-app.ps1 first to auto-create local.properties" -ForegroundColor Yellow
    Write-Host "Or create it manually:" -ForegroundColor Yellow
    Write-Host "  Copy-Item local.properties.template local.properties" -ForegroundColor Cyan
    exit 1
}

# Đọc Android SDK từ local.properties
$androidSdk = $null
Write-Host "Reading Android SDK path from local.properties..." -ForegroundColor Yellow
$content = Get-Content $localPropertiesPath
foreach ($line in $content) {
    if ($line -match "sdk\.dir=(.+)") {
        # Xử lý đường dẫn: loại bỏ escape characters (C\: -> C:, \\ -> \)
        $androidSdk = $matches[1].Trim() -replace '\\(.)', '$1'
        Write-Host "Found SDK: $androidSdk" -ForegroundColor Green
        break
    }
}

# Fallback sang ANDROID_HOME nếu không tìm thấy trong local.properties
if (-not $androidSdk -or $androidSdk -eq "YOUR_ANDROID_SDK_PATH_HERE") {
    Write-Host "SDK path not configured in local.properties, checking ANDROID_HOME..." -ForegroundColor Yellow
    $androidSdk = $env:ANDROID_HOME
}

if (-not $androidSdk) {
    Write-Host "`nERROR: Android SDK not found!" -ForegroundColor Red
    Write-Host "Please edit local.properties and set the correct SDK path" -ForegroundColor Yellow
    Write-Host "Format: sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk" -ForegroundColor Cyan
    exit 1
}

Write-Host "Using Android SDK: $androidSdk" -ForegroundColor Cyan

$adbPath = Join-Path $androidSdk "platform-tools\adb.exe"

if (-not (Test-Path $adbPath)) {
    Write-Host "ERROR: ADB not found at $adbPath" -ForegroundColor Red
    exit 1
}

# Kiểm tra emulator đã chạy chưa
Write-Host "`n[1/5] Checking for running emulator..." -ForegroundColor Green
$devices = & $adbPath devices | Select-String -Pattern "emulator-\d+"
if (-not $devices) {
    Write-Host "ERROR: No emulator is running!" -ForegroundColor Red
    Write-Host "Please start emulator first using .\scripts\run-app.ps1" -ForegroundColor Yellow
    exit 1
}
Write-Host "Emulator found!" -ForegroundColor Cyan

# Build APK
Write-Host "`n[2/5] Building APK..." -ForegroundColor Green
Set-Location (Split-Path $PSScriptRoot)
& .\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build successful!" -ForegroundColor Green

# Đóng app hiện tại (nếu đang chạy)
Write-Host "`n[3/5] Stopping current app instance..." -ForegroundColor Green
$packageName = "com.example.phoneshopapp"
& $adbPath shell am force-stop $packageName
Start-Sleep -Seconds 1

# Cài đặt APK
Write-Host "`n[4/5] Installing APK..." -ForegroundColor Green
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "ERROR: APK not found at $apkPath" -ForegroundColor Red
    exit 1
}

& $adbPath install -r $apkPath
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Installation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Installation successful!" -ForegroundColor Green

# Chạy lại app
Write-Host "`n[5/5] Launching app..." -ForegroundColor Green
$mainActivity = ".LoginActivity"
& $adbPath shell am start -n "$packageName/$mainActivity"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to launch app!" -ForegroundColor Red
    exit 1
}

Write-Host "`nApp reloaded successfully! 🚀" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan
