@echo off
REM ============================================================
REM  build.bat - Build all portfolio-analysis modules on Windows
REM  Usage: build.bat [profile]
REM  Default profile: local
REM ============================================================
setlocal

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=local

echo [%date% %time%] Building all modules (profile=%PROFILE%) ...

REM Build Maven modules
echo Building Maven modules ...
mvn clean package -f "%PROJECT_ROOT%\pom.xml" -P%PROFILE% -DskipTests
if errorlevel 1 (
    echo ERROR: Maven build failed!
    exit /b 1
)

REM Build React UI
echo Building portfolio-ui ...
cd /d "%PROJECT_ROOT%\portfolio-ui"
if not exist "node_modules" (
    echo Installing npm dependencies ...
    npm install
)
npm run build
if errorlevel 1 (
    echo ERROR: UI build failed!
    exit /b 1
)

echo.
echo Build complete!
echo   API jar:   portfolio-api\target\portfolio-api-*.jar
echo   Batch jar: portfolio-batch\target\portfolio-batch-*.jar
echo   UI dist:   portfolio-ui\dist\
