@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, version 3.2.0
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars:
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a key stroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM             e.g. to debug Maven itself, use
@REM               set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@IF "%MAVEN_BATCH_ECHO%"=="on" echo %MAVEN_BATCH_ECHO%

@REM Set local scope for the variables with windows NT shell
@IF "%OS%"=="Windows_NT" @SETLOCAL
@IF "%OS%"=="Windows_NT" @SET "JAVA_OPTS=%JAVA_OPTS%"

@REM ==== START VALIDATION ====
IF NOT "%JAVA_HOME%"=="" GOTO OkJHome
FOR %%i IN (java.exe) DO SET JAVA_EXE=%%~$PATH:i
IF NOT "%JAVA_EXE%"=="" (
  FOR %%i IN ("%JAVA_EXE%") DO SET JAVA_HOME=%%~dpi..
)
IF NOT "%JAVA_HOME%"=="" GOTO OkJHome

ECHO.
ECHO Error: JAVA_HOME not found in your environment. >&2
ECHO Please set the JAVA_HOME variable in your environment to match the >&2
ECHO location of your Java installation. >&2
ECHO.
GOTO error

:OkJHome
@REM ==== END VALIDATION ====

SET MAVEN_WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
SET MAVEN_WRAPPER_PROPERTIES=%~dp0.mvn\wrapper\maven-wrapper.properties
SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

IF EXIST "%MAVEN_WRAPPER_JAR%" GOTO downloadDone

ECHO Downloading Maven Wrapper...
SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
IF NOT "%MVNW_VERBOSE%"=="" ECHO Downloading from: %DOWNLOAD_URL%

PowerShell -Command "if (-not (Test-Path '%~dp0.mvn\wrapper')) { New-Item -ItemType Directory -Path '%~dp0.mvn\wrapper' | Out-Null }; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%MAVEN_WRAPPER_JAR%'"
IF "%ERRORLEVEL%"=="0" GOTO downloadDone
ECHO ERROR: could not download maven wrapper >&2
GOTO error

:downloadDone
IF NOT EXIST "%MAVEN_WRAPPER_PROPERTIES%" (
  PowerShell -Command "Set-Content -Path '%MAVEN_WRAPPER_PROPERTIES%' -Value 'distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip'"
)

@REM Execute the Java main class
"%JAVA_HOME%\bin\java.exe" ^
  %MAVEN_OPTS% ^
  %MAVEN_DEBUG_OPTS% ^
  -classpath "%MAVEN_WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%~dp0" ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
IF ERRORLEVEL 1 GOTO error
GOTO end

:error
SET ERROR_CODE=1

:end
@IF "%OS%"=="Windows_NT" @ENDLOCAL
EXIT /B %ERROR_CODE%
