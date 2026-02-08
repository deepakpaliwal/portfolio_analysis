@echo off
REM ============================================================
REM  restart.bat - Restart portfolio-analysis services on Windows
REM  Usage: restart.bat [api|ui|batch|all]
REM  Default: all
REM ============================================================
setlocal

set SCRIPT_DIR=%~dp0
set SERVICE=%1
if "%SERVICE%"=="" set SERVICE=all

echo === Stopping %SERVICE% ===
call "%SCRIPT_DIR%stop.bat" %SERVICE%

echo.
echo === Starting %SERVICE% ===
call "%SCRIPT_DIR%start.bat" %SERVICE%
