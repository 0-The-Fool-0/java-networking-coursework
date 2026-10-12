@echo off
chcp 65001 >nul
if "%1"=="" (
    echo Использование: chat-client-run.cmd никнейм [хост] [порт]
    goto :eof
)
java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.client.ChatClient %*


