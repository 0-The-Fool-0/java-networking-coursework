@echo off
chcp 65001 >nul
set PORT=%1
if "%PORT%"=="" (
    java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.server.ChatServer
) else (
    java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.server.ChatServer %PORT%
)


