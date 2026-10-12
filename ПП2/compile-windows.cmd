@echo off
chcp 65001 >nul
echo ========================================
echo    КОМПИЛЯЦИЯ RMI-ЧАТА
echo ========================================
echo.

REM Проверка наличия Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Java не найдена!
    echo Установите JDK версии 8 или выше с сайта:
    echo https://www.oracle.com/java/technologies/downloads/
    echo или https://adoptium.net/
    pause
    exit /b 1
)

REM Проверка наличия компилятора
javac -version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Компилятор javac не найден!
    echo Установите полный JDK (не только JRE)!
    pause
    exit /b 1
)

echo [1/3] Проверка Java...
java -version
javac -version
echo.

echo [2/3] Поиск Java файлов...
echo.

echo [3/3] Компиляция с кодировкой UTF-8...
echo.

REM Компиляция всех Java файлов
javac -encoding UTF-8 csdev\chat\*.java csdev\chat\server\*.java csdev\chat\client\*.java 2>compile_errors.txt

if errorlevel 1 (
    echo.
    echo [ОШИБКА] Ошибки при компиляции!
    echo Содержимое ошибок:
    echo ========================================
    type compile_errors.txt
    echo ========================================
    del compile_errors.txt
    pause
    exit /b 1
)

del compile_errors.txt 2>nul

echo.
echo ========================================
echo    КОМПИЛЯЦИЯ ЗАВЕРШЕНА УСПЕШНО!
echo ========================================
echo.
echo Теперь можно запускать сервер и клиентов:
echo   - chat-server-rmi-run.cmd
echo   - chat-client-rmi-run.cmd ^<никнейм^>
echo.
pause


