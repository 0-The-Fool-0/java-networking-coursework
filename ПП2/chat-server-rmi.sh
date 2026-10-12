#!/bin/bash
# Скрипт для запуска RMI чат-сервера на macOS/Linux

# Проверяем наличие Java
if ! command -v java &> /dev/null; then
    echo "Ошибка: Java не найдена. Установите JDK версии 8 или выше."
    exit 1
fi

PORT=${1:-8071}

# Устанавливаем кодировку UTF-8 для корректного отображения русского текста
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
export LANG=ru_RU.UTF-8
export LC_ALL=ru_RU.UTF-8

# Проверяем наличие файла политики безопасности
if [ ! -f "server.policy" ]; then
    echo "Предупреждение: файл server.policy не найден. Создаю файл с разрешениями..."
    cat > server.policy << 'EOF'
grant {
    permission java.security.AllPermission;
};
EOF
    echo "Файл server.policy создан."
fi

echo "Запуск RMI чат-сервера на порту $PORT..."
echo "Нажмите Ctrl+C для остановки сервера."
echo ""

# Запускаем сервер
java -ea -esa -Dfile.encoding=UTF-8 -Djava.security.policy=server.policy csdev.chat.server.ChatServerRMI "$PORT"


