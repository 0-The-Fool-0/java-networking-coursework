@echo off
chcp 65001 >nul
set PORT=%1
if "%PORT%"=="" (
    set PORT=
)
if "%PORT%"=="" (
    start "ChatServer" java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.server.ChatServer
) else (
    start "ChatServer" java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.server.ChatServer %PORT%
)

