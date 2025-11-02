# Script wrapper thong minh de chay Android app
# Su dung: .\scripts\app.ps1
# Tu dong phat hien emulator da chay chua va chon script phu hop

param(
    [switch]$Force  # Buoc khoi dong lai emulator
)

Write-Host "=== Smart Android App Runner ===" -ForegroundColor Cyan

$scriptDir = $PSScriptRoot
$projectRoot = Split-Path $scriptDir
$localPropertiesPath = Join-Path $projectRoot "local.properties"

# Kiem tra local.properties
if (-not (Test-Path $localPropertiesPath)) {
    Write-Host "`nFirst time setup detected. Running full initialization..." -ForegroundColor Yellow
    & "$scriptDir\run-app.ps1"
    exit $LASTEXITCODE
}

# Doc SDK path
$androidSdk = $null
$content = Get-Content $localPropertiesPath -ErrorAction SilentlyContinue
foreach ($line in $content) {
    if ($line -match "sdk\.dir=(.+)") {
        $androidSdk = $matches[1].Trim() -replace '\\(.)', '$1'
        break
    }
}

# Fallback to ANDROID_HOME
if (-not $androidSdk -or $androidSdk -eq "YOUR_ANDROID_SDK_PATH_HERE") {
    $androidSdk = $env:ANDROID_HOME
}

# Neu van khong tim thay SDK hoac khong hop le, chay full script
if (-not $androidSdk) {
    Write-Host "`nAndroid SDK not configured. Running full initialization..." -ForegroundColor Yellow
    & "$scriptDir\run-app.ps1"
    exit $LASTEXITCODE
}

$adbPath = Join-Path $androidSdk "platform-tools\adb.exe"

# Kiem tra adb co ton tai khong
if (-not (Test-Path $adbPath)) {
    Write-Host "`nADB not found. Running full initialization..." -ForegroundColor Yellow
    & "$scriptDir\run-app.ps1"
    exit $LASTEXITCODE
}

# Kiem tra xem emulator co dang chay khong
Write-Host "`nChecking for running emulator..." -ForegroundColor Cyan

try {
    $devicesOutput = & $adbPath devices 2>&1
    $devices = $devicesOutput | Select-String -Pattern "emulator-\d+"
    
    if ($devices -and -not $Force) {
        # Emulator da chay -> Reload nhanh
        Write-Host "Emulator is running. Using quick reload..." -ForegroundColor Green
        Write-Host ""
        & "$scriptDir\build-and-reload.ps1"
    } else {
        # Emulator chua chay hoac Force -> Full run
        if ($Force) {
            Write-Host "Force flag detected. Running full initialization..." -ForegroundColor Yellow
        } else {
            Write-Host "No emulator running. Starting full process..." -ForegroundColor Yellow
        }
        Write-Host ""
        & "$scriptDir\run-app.ps1"
    }
} catch {
    Write-Host "Error checking emulator status: $_" -ForegroundColor Red
    Write-Host "Running full initialization..." -ForegroundColor Yellow
    & "$scriptDir\run-app.ps1"
}

exit $LASTEXITCODE


