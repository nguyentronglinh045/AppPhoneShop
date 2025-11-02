# Script để chạy ứng dụng Android trên emulator
# Sử dụng: .\scripts\run-app.ps1

Write-Host "=== Starting Android App Launcher ===" -ForegroundColor Cyan

# Đường dẫn đến local.properties
$projectRoot = Split-Path $PSScriptRoot
$localPropertiesPath = Join-Path $projectRoot "local.properties"
$templatePath = Join-Path $projectRoot "local.properties.template"

# Kiểm tra và tạo local.properties nếu chưa có
if (-not (Test-Path $localPropertiesPath)) {
    Write-Host "`nWARNING: local.properties not found!" -ForegroundColor Yellow
    Write-Host "Attempting to create from template..." -ForegroundColor Yellow
    
    if (Test-Path $templatePath) {
        # Tìm Android SDK path phổ biến
        $possiblePaths = @(
            "$env:LOCALAPPDATA\Android\Sdk",
            "$env:USERPROFILE\AppData\Local\Android\Sdk",
            "C:\Android\Sdk"
        )
        
        $foundSdk = $null
        foreach ($path in $possiblePaths) {
            if (Test-Path $path) {
                $foundSdk = $path
                Write-Host "Found Android SDK at: $foundSdk" -ForegroundColor Green
                break
            }
        }
        
        if ($foundSdk) {
            # Tạo local.properties với đường dẫn đã tìm thấy
            $escapedPath = $foundSdk -replace '\\', '\\\\'
            $escapedPath = $escapedPath -replace ':', '\:'
            "sdk.dir=$escapedPath" | Out-File -FilePath $localPropertiesPath -Encoding UTF8
            Write-Host "Created local.properties successfully!" -ForegroundColor Green
        } else {
            Write-Host "`nERROR: Cannot auto-detect Android SDK!" -ForegroundColor Red
            Write-Host "Please create local.properties manually:" -ForegroundColor Yellow
            Write-Host "  1. Copy-Item local.properties.template local.properties" -ForegroundColor Cyan
            Write-Host "  2. Edit local.properties and set your SDK path" -ForegroundColor Cyan
            Write-Host "`nCommon SDK locations:" -ForegroundColor Yellow
            Write-Host "  - $env:LOCALAPPDATA\Android\Sdk" -ForegroundColor Gray
            Write-Host "  - C:\Android\Sdk" -ForegroundColor Gray
            exit 1
        }
    } else {
        Write-Host "`nERROR: local.properties.template not found!" -ForegroundColor Red
        Write-Host "Cannot create local.properties automatically." -ForegroundColor Yellow
        exit 1
    }
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

$emulatorPath = Join-Path $androidSdk "emulator\emulator.exe"
$adbPath = Join-Path $androidSdk "platform-tools\adb.exe"

if (-not (Test-Path $emulatorPath)) {
    Write-Host "ERROR: Emulator not found at $emulatorPath" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $adbPath)) {
    Write-Host "ERROR: ADB not found at $adbPath" -ForegroundColor Red
    exit 1
}

# Bước 1: Liệt kê danh sách AVDs
Write-Host "`n[1/7] Listing available AVDs..." -ForegroundColor Green
$avdList = & $emulatorPath -list-avds
if ($LASTEXITCODE -ne 0 -or $avdList.Count -eq 0) {
    Write-Host "ERROR: No AVDs found. Please create an AVD first." -ForegroundColor Red
    exit 1
}

Write-Host "Available AVDs:" -ForegroundColor Yellow
$avdList | ForEach-Object { Write-Host "  - $_" }

# Chọn AVD đầu tiên (ưu tiên phone)
$selectedAvd = $avdList[0]
foreach ($avd in $avdList) {
    if ($avd -match "phone|pixel|nexus") {
        $selectedAvd = $avd
        break
    }
}

Write-Host "Selected AVD: $selectedAvd" -ForegroundColor Cyan

# Bước 2: Khởi động emulator
Write-Host "`n[2/7] Starting emulator..." -ForegroundColor Green
$emulatorProcess = Start-Process -FilePath $emulatorPath -ArgumentList "-avd", $selectedAvd -PassThru -WindowStyle Normal
Start-Sleep -Seconds 3

# Bước 3: Chờ emulator khởi động
Write-Host "`n[3/7] Waiting for emulator to boot..." -ForegroundColor Green
& $adbPath wait-for-device
Write-Host "Device connected!" -ForegroundColor Cyan

# Chờ thêm để emulator boot hoàn toàn
Write-Host "Waiting for boot to complete..." -ForegroundColor Yellow
$bootComplete = $false
$maxAttempts = 60
$attempts = 0

while (-not $bootComplete -and $attempts -lt $maxAttempts) {
    $bootStatus = & $adbPath shell getprop sys.boot_completed 2>$null
    if ($bootStatus -match "1") {
        $bootComplete = $true
    } else {
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 2
        $attempts++
    }
}

if (-not $bootComplete) {
    Write-Host "`nWARNING: Boot check timeout, but continuing anyway..." -ForegroundColor Yellow
} else {
    Write-Host "`nEmulator booted successfully!" -ForegroundColor Green
}

Start-Sleep -Seconds 3

# Bước 4: Build APK
Write-Host "`n[4/7] Building APK..." -ForegroundColor Green
Set-Location (Split-Path $PSScriptRoot)
& .\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build successful!" -ForegroundColor Green

# Bước 5: Cài đặt APK
Write-Host "`n[5/7] Installing APK..." -ForegroundColor Green
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

# Bước 6: Chạy activity chính
Write-Host "`n[6/7] Launching app..." -ForegroundColor Green
$packageName = "com.example.phoneshopapp"
$mainActivity = ".LoginActivity"
& $adbPath shell am start -n "$packageName/$mainActivity"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to launch app!" -ForegroundColor Red
    exit 1
}

# Bước 7: Hiển thị log
Write-Host "`n[7/7] App launched successfully! 🎉" -ForegroundColor Green
Write-Host "`nMonitoring app logs (Ctrl+C to stop)..." -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

# Hiển thị logcat với filter cho package
& $adbPath logcat -s "AndroidRuntime:E" "*:W" | Select-String -Pattern $packageName -Context 0,3
