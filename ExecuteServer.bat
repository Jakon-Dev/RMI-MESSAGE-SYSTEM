@echo off
set /p PORT=Enter the port for the RMI Registry (default 1099):
if "%PORT%"=="" set PORT=1099

echo Starting RMI Registry on port %PORT%...
start rmiregistry %PORT%

timeout /t 2

echo Starting Server...
java -cp . src/Server/Server %PORT%
pause
