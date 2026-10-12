@echo off
chcp 65001 >nul
set PORT=%1
if "%PORT%"=="" (
    set PORT=
)
if "%PORT%"=="" (
    start "ChatServerRMI" java -ea -esa -Dfile.encoding=UTF-8 -Djava.security.policy=server.policy csdev.chat.server.ChatServerRMI
) else (
    start "ChatServerRMI" java -ea -esa -Dfile.encoding=UTF-8 -Djava.security.policy=server.policy csdev.chat.server.ChatServerRMI %PORT%
)


