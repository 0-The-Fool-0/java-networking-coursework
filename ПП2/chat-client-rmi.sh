#!/bin/bash
# Скрипт для запуска RMI чат-клиента на macOS/Linux

# Проверяем наличие Java
if ! command -v java &> /dev/null; then
    echo "Ошибка: Java не найдена. Установите JDK версии 8 или выше."
    exit 1
fi

# Проверяем аргументы
if [ $# -lt 1 ]; then
    echo "Использование: ./chat-client-rmi.sh <никнейм> [хост] [порт]"
    echo ""
    echo "Примеры:"
    echo "  ./chat-client-rmi.sh Иван                    # подключение к localhost:8071"
    echo "  ./chat-client-rmi.sh Анна localhost           # подключение к localhost:8071"
    echo "  ./chat-client-rmi.sh Петр 192.168.1.101      # подключение к удаленному серверу"
    echo "  ./chat-client-rmi.sh Мария 192.168.1.101 8080 # подключение с указанием порта"
    exit 1
fi

NICKNAME=$1
HOST=${2:-localhost}
PORT=${3:-8071}

# Устанавливаем кодировку UTF-8 для корректного отображения русского текста
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
export LANG=ru_RU.UTF-8
export LC_ALL=ru_RU.UTF-8

# Проверяем наличие файла политики безопасности
if [ ! -f "client.policy" ]; then
    echo "Предупреждение: файл client.policy не найден. Создаю файл с разрешениями..."
    cat > client.policy << 'EOF'
grant {
    permission java.security.AllPermission;
};
EOF
    echo "Файл client.policy создан."
fi

echo "Подключение к RMI серверу $HOST:$PORT как '$NICKNAME'..."
echo ""

# Запускаем клиент
java -ea -esa -Dfile.encoding=UTF-8 -Djava.security.policy=client.policy csdev.chat.client.ChatClientRMI "$NICKNAME" "$HOST" "$PORT"

