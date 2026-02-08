@echo off
REM ============================================================
REM  stop.bat - Stop portfolio-analysis services on Windows
REM  Usage: stop.bat [api|ui|batch|all]
REM  Default: all
REM ============================================================
setlocal enabledelayedexpansion

set SERVICE=%1
if "%SERVICE%"=="" set SERVICE=all

if "%SERVICE%"=="api"   goto :stop_api
if "%SERVICE%"=="ui"    goto :stop_ui
if "%SERVICE%"=="batch" goto :stop_batch
if "%SERVICE%"=="all"   goto :stop_all
if "%SERVICE%"=="-h"    goto :usage
if "%SERVICE%"=="--help" goto :usage

echo ERROR: Unknown service '%SERVICE%'
goto :usage

:stop_api
echo [%date% %time%] Stopping portfolio-api ...
for /f "tokens=1" %%p in ('wmic process where "commandline like '%%portfolio-api%%' and not commandline like '%%stop.bat%%'" get processid 2^>nul ^| findstr /r "[0-9]"') do (
    echo Killing PID %%p
    taskkill /PID %%p /F >nul 2>&1
)
echo portfolio-api stopped.
goto :eof

:stop_ui
echo [%date% %time%] Stopping portfolio-ui ...
for /f "tokens=1" %%p in ('wmic process where "commandline like '%%portfolio-ui%%' and not commandline like '%%stop.bat%%'" get processid 2^>nul ^| findstr /r "[0-9]"') do (
    echo Killing PID %%p
    taskkill /PID %%p /F >nul 2>&1
)
echo portfolio-ui stopped.
goto :eof

:stop_batch
echo [%date% %time%] Stopping portfolio-batch ...
for /f "tokens=1" %%p in ('wmic process where "commandline like '%%portfolio-batch%%' and not commandline like '%%stop.bat%%'" get processid 2^>nul ^| findstr /r "[0-9]"') do (
    echo Killing PID %%p
    taskkill /PID %%p /F >nul 2>&1
)
echo portfolio-batch stopped.
goto :eof

:stop_all
echo [%date% %time%] Stopping ALL services ...
call :stop_api
call :stop_ui
call :stop_batch
echo All services stopped.
goto :eof

:usage
echo Usage: %~nx0 [service]
echo.
echo Services:
echo   api     Stop portfolio-api
echo   ui      Stop portfolio-ui
echo   batch   Stop portfolio-batch
echo   all     Stop all services (default)
goto :eof
