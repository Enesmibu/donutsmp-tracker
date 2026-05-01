@echo off

SET GRADLE_VERSION=8.11.1
SET GRADLE_DIR=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin\gradle-%GRADLE_VERSION%
SET GRADLE_BIN=%GRADLE_DIR%\bin\gradle.bat
SET GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip
SET GRADLE_ZIP=%TEMP%\gradle-%GRADLE_VERSION%-bin.zip

where gradle >nul 2>&1
IF %ERRORLEVEL% EQU 0 (
    gradle %*
    EXIT /B %ERRORLEVEL%
)

IF EXIST "%GRADLE_BIN%" (
    "%GRADLE_BIN%" %*
    EXIT /B %ERRORLEVEL%
)

echo Gradle bulunamadi, indiriliyor (%GRADLE_VERSION%)...
mkdir "%GRADLE_DIR%" 2>nul

powershell -Command "Invoke-WebRequest -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'"
powershell -Command "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin'"

"%GRADLE_BIN%" %*
EXIT /B %ERRORLEVEL%
