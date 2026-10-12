@echo off
chcp 65001 >nul
if "%1"=="" (
    echo Использование: chat-client-rmi.cmd никнейм [хост] [порт]
    goto :eof
)
start "ChatClientRMI %1" java -ea -esa -Dfile.encoding=UTF-8 -Djava.security.policy=client.policy csdev.chat.client.ChatClientRMI %*


