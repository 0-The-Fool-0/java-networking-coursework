@echo off
chcp 65001 >nul
if "%1"=="" (
    echo Использование: chat-client.cmd никнейм [хост] [порт]
    goto :eof
)
start "ChatClient %1" java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.client.ChatClient %*


