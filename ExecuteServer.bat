@echo off
set /p PORT=Enter the port for the RMI Registry (default 1099):
if "%PORT%"=="" set PORT=1099

set /p IP=Enter the ip for the RMI Registry (default 192.168.100.1):
if "%IP%"=="" set IP=192.168.100.1


timeout /t 2

echo Starting Server on port %PORT% and IP %IP%...
java -Djava.rmi.server.hostname=%IP% src.Server.Server %PORT%
pause
