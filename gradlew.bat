@echo off
where gradle >nul 2>nul
if %errorlevel% neq 0 (
  echo Gradle is required but not installed.
  exit /b 1
)
gradle %*
