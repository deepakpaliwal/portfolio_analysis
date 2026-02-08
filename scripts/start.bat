@echo off
REM ============================================================
REM  start.bat - Start portfolio-analysis services on Windows
REM  Usage: start.bat [api|ui|batch|all]
REM  Default: all
REM ============================================================
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
set PROFILE=%PROFILE:local=%
if "%PROFILE%"=="" set PROFILE=local
set LOG_DIR=%PROJECT_ROOT%\logs
set PID_DIR=%PROJECT_ROOT%\logs\pids

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%PID_DIR%" mkdir "%PID_DIR%"

set SERVICE=%1
if "%SERVICE%"=="" set SERVICE=all

if "%SERVICE%"=="api"   goto :start_api
if "%SERVICE%"=="ui"    goto :start_ui
if "%SERVICE%"=="batch" goto :start_batch
if "%SERVICE%"=="all"   goto :start_all
if "%SERVICE%"=="-h"    goto :usage
if "%SERVICE%"=="--help" goto :usage

echo ERROR: Unknown service '%SERVICE%'
goto :usage

:start_api
echo [%date% %time%] Starting portfolio-api (profile=%PROFILE%) ...
start "portfolio-api" /B cmd /c "mvn spring-boot:run -f "%PROJECT_ROOT%\pom.xml" -pl portfolio-api -P%PROFILE% > "%LOG_DIR%\api.log" 2>&1"
echo portfolio-api started. Log: %LOG_DIR%\api.log
goto :eof

:start_ui
echo [%date% %time%] Starting portfolio-ui (Vite dev server) ...
start "portfolio-ui" /B cmd /c "cd /d "%PROJECT_ROOT%\portfolio-ui" && npm run dev > "%LOG_DIR%\ui.log" 2>&1"
echo portfolio-ui started. Log: %LOG_DIR%\ui.log
goto :eof

:start_batch
echo [%date% %time%] Starting portfolio-batch (profile=%PROFILE%) ...
start "portfolio-batch" /B cmd /c "mvn spring-boot:run -f "%PROJECT_ROOT%\pom.xml" -pl portfolio-batch -P%PROFILE% > "%LOG_DIR%\batch.log" 2>&1"
echo portfolio-batch started. Log: %LOG_DIR%\batch.log
goto :eof

:start_all
echo [%date% %time%] Starting ALL services (profile=%PROFILE%) ...
call :start_api
call :start_ui
call :start_batch
echo.
echo All services launched. Logs directory: %LOG_DIR%
goto :eof

:usage
echo Usage: %~nx0 [service]
echo.
echo Services:
echo   api     Start portfolio-api   (Spring Boot, port 8080)
echo   ui      Start portfolio-ui    (Vite dev server, port 5173)
echo   batch   Start portfolio-batch (Spring Batch/Spark, port 8082)
echo   all     Start all services (default)
echo.
echo Environment variables:
echo   PROFILE   Spring / build profile (default: local)
goto :eof
