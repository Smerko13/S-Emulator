@echo off
setlocal

REM === Same base URL for build-time ===
set SERVER_BASE_URL=http://localhost:8080/S_Emulator_Server
set VITE_SERVER_BASE_URL=%SERVER_BASE_URL%
set REACT_APP_SERVER_BASE_URL=%SERVER_BASE_URL%

echo ========================================
echo S-Emulator Web Client - Production Build
echo ========================================
echo.
echo Server URL: %SERVER_BASE_URL%
echo.

echo [1/3] Installing dependencies...
call npm install || goto :error

echo.
echo [2/3] Building production bundle...
call npm run build || goto :error

echo.
echo [3/3] Starting preview server...
echo.
echo The web client will open at: http://localhost:4173
echo Make sure the Tomcat server is running at: %SERVER_BASE_URL%
echo.
call npm run preview
goto :eof

:error
echo.
echo ========================================
echo ERROR: Build/preview failed
echo ========================================
echo.
pause
exit /b 1

