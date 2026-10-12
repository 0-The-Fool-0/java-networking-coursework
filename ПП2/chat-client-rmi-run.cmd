@echo off
chcp 65001 >nul
if "%1"=="" (
    echo Использование: chat-client-rmi-run.cmd никнейм [хост] [порт]
    goto :eof
)
java -ea -esa -Dfile.encoding=UTF-8 -Djava.security.policy=client.policy csdev.chat.client.ChatClientRMI %*


