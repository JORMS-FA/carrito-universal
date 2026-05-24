$ErrorActionPreference = "Stop"

$repoRoot = $PSScriptRoot
$gradleVersion = "9.3.1"
$androidCommandLineToolsVersion = "14742923"
$androidSdkDir = Join-Path $repoRoot ".android-sdk"
$distDir = Join-Path $repoRoot "dist-local"
$outputApk = Join-Path $distDir "carrito-universal-opt.apk"
$localJvmArgs = "-Xmx1536m -Dfile.encoding=UTF-8 -XX:MaxMetaspaceSize=512m -XX:ReservedCodeCacheSize=128m -XX:ActiveProcessorCount=2 -XX:CICompilerCount=2"

function Invoke-NativeCommand {
    param(
        [Parameter(Mandatory = $true)][string] $FilePath,
        [Parameter(ValueFromRemainingArguments = $true)][string[]] $Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $FilePath $($Arguments -join ' ')"
    }
}

function Get-GradleCommand {
    $gradlew = Join-Path $repoRoot "gradlew.bat"
    if (Test-Path -LiteralPath $gradlew) {
        return $gradlew
    }

    $systemGradle = Get-Command gradle -ErrorAction SilentlyContinue
    if ($systemGradle) {
        return $systemGradle.Source
    }

    $gradleHome = Join-Path $repoRoot ".gradle\local-gradle\gradle-$gradleVersion"
    $gradleBat = Join-Path $gradleHome "bin\gradle.bat"
    if (Test-Path -LiteralPath $gradleBat) {
        return $gradleBat
    }

    $downloadDir = Join-Path $repoRoot ".gradle\local-distributions"
    $zipPath = Join-Path $downloadDir "gradle-$gradleVersion-bin.zip"
    $extractDir = Join-Path $repoRoot ".gradle\local-gradle"
    $downloadUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"

    New-Item -ItemType Directory -Force -Path $downloadDir | Out-Null
    New-Item -ItemType Directory -Force -Path $extractDir | Out-Null

    if (-not (Test-Path -LiteralPath $zipPath)) {
        Write-Host "Downloading Gradle $gradleVersion..."
        Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath
    }

    Write-Host "Extracting Gradle $gradleVersion..."
    Expand-Archive -LiteralPath $zipPath -DestinationPath $extractDir -Force

    if (-not (Test-Path -LiteralPath $gradleBat)) {
        throw "Gradle $gradleVersion could not be prepared at $gradleBat"
    }

    return $gradleBat
}

function Initialize-AndroidSdk {
    $existingSdk = $env:ANDROID_HOME
    if ([string]::IsNullOrWhiteSpace($existingSdk)) {
        $existingSdk = $env:ANDROID_SDK_ROOT
    }

    if (-not [string]::IsNullOrWhiteSpace($existingSdk) -and (Test-Path -LiteralPath $existingSdk)) {
        $script:androidSdkDir = $existingSdk
    }

    $cmdlineToolsBin = Join-Path $androidSdkDir "cmdline-tools\latest\bin"
    $sdkManager = Join-Path $cmdlineToolsBin "sdkmanager.bat"

    if (-not (Test-Path -LiteralPath $sdkManager)) {
        $downloadDir = Join-Path $repoRoot ".gradle\android-sdk-downloads"
        $zipPath = Join-Path $downloadDir "commandlinetools-win-$androidCommandLineToolsVersion`_latest.zip"
        $downloadUrl = "https://dl.google.com/android/repository/commandlinetools-win-$androidCommandLineToolsVersion`_latest.zip"
        $extractTemp = Join-Path $androidSdkDir "cmdline-tools\_tmp"
        $latestDir = Join-Path $androidSdkDir "cmdline-tools\latest"

        New-Item -ItemType Directory -Force -Path $downloadDir | Out-Null
        New-Item -ItemType Directory -Force -Path $extractTemp | Out-Null

        if (-not (Test-Path -LiteralPath $zipPath)) {
            Write-Host "Downloading Android command line tools..."
            Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath
        }

        Write-Host "Extracting Android command line tools..."
        if (Test-Path -LiteralPath $latestDir) {
            Remove-Item -LiteralPath $latestDir -Recurse -Force
        }
        if (Test-Path -LiteralPath $extractTemp) {
            Remove-Item -LiteralPath $extractTemp -Recurse -Force
        }
        New-Item -ItemType Directory -Force -Path $extractTemp | Out-Null
        Expand-Archive -LiteralPath $zipPath -DestinationPath $extractTemp -Force
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $latestDir) | Out-Null
        Move-Item -LiteralPath (Join-Path $extractTemp "cmdline-tools") -Destination $latestDir
        Remove-Item -LiteralPath $extractTemp -Recurse -Force
    }

    if (-not (Test-Path -LiteralPath $sdkManager)) {
        throw "Android sdkmanager could not be prepared at $sdkManager"
    }

    $env:ANDROID_HOME = $androidSdkDir
    $env:ANDROID_SDK_ROOT = $androidSdkDir

    $sdkDirForProperties = ($androidSdkDir -replace "\\", "/")
    Set-Content -LiteralPath (Join-Path $repoRoot "local.properties") -Value "sdk.dir=$sdkDirForProperties" -Encoding ASCII

    Write-Host "Accepting Android SDK licenses..."
    $yesAnswers = ("y`n" * 100)
    $yesAnswers | & $sdkManager --sdk_root=$androidSdkDir --licenses | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "Android SDK license acceptance failed."
    }

    Write-Host "Installing Android SDK packages..."
    Invoke-NativeCommand $sdkManager --sdk_root=$androidSdkDir "platform-tools" "platforms;android-36" "build-tools;36.0.0"
}

function Initialize-DebugKeystore {
    $debugKeystore = Join-Path $repoRoot "debug.keystore"
    $debugKeystoreBase64 = Join-Path $repoRoot "debug.keystore.base64"

    if (Test-Path -LiteralPath $debugKeystore) {
        return
    }

    if (-not (Test-Path -LiteralPath $debugKeystoreBase64)) {
        throw "debug.keystore is missing and debug.keystore.base64 was not found."
    }

    Write-Host "Decoding debug keystore..."
    $encoded = (Get-Content -LiteralPath $debugKeystoreBase64 -Raw).Trim()
    [IO.File]::WriteAllBytes($debugKeystore, [Convert]::FromBase64String($encoded))
}

function Initialize-ReleaseSigning {
    $releaseKeyDir = Join-Path $env:USERPROFILE ".codex\release-keys\carrito-universal"
    $releaseKeystore = Join-Path $releaseKeyDir "my-upload-key.jks"
    $releaseSecrets = Join-Path $releaseKeyDir "github-actions-release-secrets.txt"

    if (-not (Test-Path -LiteralPath $releaseKeystore) -or -not (Test-Path -LiteralPath $releaseSecrets)) {
        Write-Host "Release signing key was not found locally. The APK will use debug signing and cannot update the GitHub Release install."
        $env:LOCAL_RELEASE_SIGN_WITH_UPLOAD_KEY = "false"
        return
    }

    $secrets = @{}
    Get-Content -LiteralPath $releaseSecrets | ForEach-Object {
        $idx = $_.IndexOf("=")
        if ($idx -gt 0) {
            $secrets[$_.Substring(0, $idx)] = $_.Substring($idx + 1)
        }
    }

    $env:KEYSTORE_PATH = $releaseKeystore
    $env:STORE_PASSWORD = $secrets["RELEASE_STORE_PASSWORD"]
    $env:KEY_PASSWORD = $secrets["RELEASE_KEY_PASSWORD"]
    $env:LOCAL_RELEASE_SIGN_WITH_UPLOAD_KEY = "true"

    Write-Host "Using release signing key, so this APK can update GitHub Release installs."
}

Push-Location $repoRoot
try {
    $gradle = Get-GradleCommand
    Initialize-AndroidSdk
    Initialize-DebugKeystore
    Initialize-ReleaseSigning

    Write-Host "Cleaning previous build..."
    Invoke-NativeCommand $gradle clean --no-daemon --no-parallel --max-workers=1 "-Dorg.gradle.jvmargs=$localJvmArgs"

    Write-Host "Building optimized local APK..."
    Invoke-NativeCommand $gradle :app:assembleLocalRelease --no-daemon --no-parallel --max-workers=1 "-Dorg.gradle.jvmargs=$localJvmArgs"

    New-Item -ItemType Directory -Force -Path $distDir | Out-Null

    $apkCandidates = @(
        Get-ChildItem -Path (Join-Path $repoRoot "app\build\outputs\apk\localRelease") -Filter "*.apk" -File -ErrorAction SilentlyContinue
        Get-ChildItem -Path (Join-Path $repoRoot "app\build\outputs\apk") -Filter "*local*.apk" -File -Recurse -ErrorAction SilentlyContinue
    ) | Where-Object { $_ -ne $null } | Sort-Object LastWriteTime -Descending

    $apk = $apkCandidates | Select-Object -First 1
    if (-not $apk) {
        throw "No localRelease APK was found under app\build\outputs\apk."
    }

    Copy-Item -LiteralPath $apk.FullName -Destination $outputApk -Force

    Write-Host ""
    Write-Host "Local APK ready:"
    Write-Host $outputApk
} finally {
    Pop-Location
}
