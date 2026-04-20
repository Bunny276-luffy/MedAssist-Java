@echo off
title MedAssist Launcher
color 0A
echo.
echo  =========================================================
echo    MedAssist - AI Medication Adherence System
echo    Launcher v1.0
echo  =========================================================
echo.

:: ── Set JAVA_HOME ─────────────────────────────────────────────────────────────
SET "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

IF NOT EXIST "%JAVA_EXE%" (
    echo  [ERROR] Java not found at: %JAVA_HOME%
    echo  Please install JDK 17 from https://adoptium.net/
    pause
    exit /b 1
)
echo  [OK] Java found: %JAVA_HOME%

:: ── Check Maven ────────────────────────────────────────────────────────────────
SET "MVN_HOME=F:\tools\apache-maven-3.9.6"
SET "MVN_EXE=%MVN_HOME%\bin\mvn.cmd"

IF NOT EXIST "%MVN_EXE%" (
    echo.
    echo  [INFO] Maven not found. Downloading Maven 3.9.6...
    echo  [INFO] This only happens once. Please wait...
    echo.
    PowerShell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\maven396.zip'"
    IF ERRORLEVEL 1 (
        echo  [ERROR] Download failed. Check your internet connection.
        pause
        exit /b 1
    )
    echo  [INFO] Extracting Maven...
    PowerShell -Command "Expand-Archive '%TEMP%\maven396.zip' -DestinationPath 'F:\tools\' -Force"
    IF ERRORLEVEL 1 (
        echo  [ERROR] Extraction failed.
        pause
        exit /b 1
    )
    echo  [OK] Maven installed to F:\tools\apache-maven-3.9.6
)

echo  [OK] Maven found: %MVN_HOME%
echo.

:: ── Set up PATH ────────────────────────────────────────────────────────────────
SET "PATH=%JAVA_HOME%\bin;%MVN_HOME%\bin;%PATH%"
SET "PROJECT_DIR=F:\projects\java\medassist"

cd /d "%PROJECT_DIR%"

:: ── Compile ────────────────────────────────────────────────────────────────────
echo  [STEP 1/2] Compiling MedAssist...
echo.
call "%MVN_EXE%" compile -q
IF ERRORLEVEL 1 (
    echo.
    echo  [ERROR] Compilation failed. Check the error messages above.
    pause
    exit /b 1
)
echo  [OK] Compilation successful!
echo.

:: ── Run ────────────────────────────────────────────────────────────────────────
echo  [STEP 2/2] Launching MedAssist...
echo.
call "%MVN_EXE%" exec:java -Dexec.mainClass=com.medassist.MedAssistApp
IF ERRORLEVEL 1 (
    echo.
    echo  [ERROR] Application exited with an error.
    pause
    exit /b 1
)

echo.
echo  [INFO] MedAssist closed normally.
pause
