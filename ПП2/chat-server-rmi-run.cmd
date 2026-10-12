@echo off
chcp 65001 >nul
set PORT=%1
if "%PORT%"=="" (
    java -ea -esa -Dfile.encoding=UTF-8 -Djava.security.policy=server.policy csdev.chat.server.ChatServerRMI
) else (
    java -ea -esa -Dfile.encoding=UTF-8 -Djava.security.policy=server.policy csdev.chat.server.ChatServerRMI %PORT%
)


