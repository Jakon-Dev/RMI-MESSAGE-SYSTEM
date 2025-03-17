@echo off
set /p SERVER_IP=Enter the server IP (default localhost):
if "%SERVER_IP%"=="" set SERVER_IP=localhost

echo Connecting to server at %SERVER_IP%...
java -cp . src/Client/Client %SERVER_IP%
pause
