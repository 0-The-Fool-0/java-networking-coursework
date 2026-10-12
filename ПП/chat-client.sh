#!/bin/bash
# Скрипт для запуска чат-клиента на macOS/Linux

# Проверяем наличие Java
if ! command -v java &> /dev/null; then
    echo "Ошибка: Java не найдена. Установите JDK версии 8 или выше."
    exit 1
fi

# Проверяем аргументы
if [ $# -lt 1 ]; then
    echo "Использование: ./chat-client.sh <никнейм> [хост] [порт]"
    echo ""
    echo "Примеры:"
    echo "  ./chat-client.sh Иван                    # подключение к localhost:8071"
    echo "  ./chat-client.sh Анна localhost           # подключение к localhost:8071"
    echo "  ./chat-client.sh Петр 192.168.1.101      # подключение к удаленному серверу"
    echo "  ./chat-client.sh Мария 192.168.1.101 8080 # подключение с указанием порта"
    exit 1
fi

NICKNAME=$1
HOST=${2:-localhost}
PORT=${3:-8071}

# Устанавливаем кодировку UTF-8 для корректного отображения русского текста
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
export LANG=ru_RU.UTF-8
export LC_ALL=ru_RU.UTF-8

echo "Подключение к серверу $HOST:$PORT как '$NICKNAME'..."
echo ""

# Запускаем клиент
java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.client.ChatClient "$NICKNAME" "$HOST" "$PORT"

