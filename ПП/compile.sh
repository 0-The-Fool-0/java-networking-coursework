#!/bin/bash
# Скрипт для компиляции проекта на macOS/Linux

# Проверяем наличие Java
if ! command -v java &> /dev/null; then
    echo "Ошибка: Java не найдена. Установите JDK версии 8 или выше."
    exit 1
fi

# Проверяем наличие javac
if ! command -v javac &> /dev/null; then
    echo "Ошибка: javac не найден. Установите JDK (не только JRE)."
    exit 1
fi

echo "Поиск Java файлов..."
echo "Компиляция с кодировкой UTF-8..."
echo ""

# Находим все .java файлы и компилируем
find . -name "*.java" -print0 | xargs -0 javac -encoding UTF-8

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Компиляция завершена успешно!"
    echo "Теперь можно запускать сервер и клиентов."
else
    echo ""
    echo "✗ Ошибка при компиляции!"
    echo "Проверьте сообщения об ошибках выше."
    exit 1
fi

