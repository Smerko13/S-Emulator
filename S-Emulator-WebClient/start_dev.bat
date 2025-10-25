@echo off
setlocal enabledelayedexpansion

REM === Configuration (edit if needed) ===
set SERVER_BASE_URL=http://localhost:8080/S_Emulator_Server
REM React/Vite read env vars differently; we export both:
set VITE_SERVER_BASE_URL=%SERVER_BASE_URL%
set REACT_APP_SERVER_BASE_URL=%SERVER_BASE_URL%

echo ========================================
echo S-Emulator Web Client - Development Mode
echo ========================================
echo.
echo Server URL: %SERVER_BASE_URL%
echo.

echo [1/2] Installing dependencies...
call npm install || goto :error

echo.
echo [2/2] Starting development server...
echo.
echo The web client will open at: http://localhost:5173
echo Make sure the Tomcat server is running at: %SERVER_BASE_URL%
echo.
call npm run dev
goto :eof

:error
echo.
echo ========================================
echo ERROR: Failed to start web client
echo ========================================
echo.
echo Please check:
echo - Node.js is installed (run: node --version)
echo - npm is installed (run: npm --version)
echo - You have internet connection for downloading dependencies
echo.
pause
exit /b 1

