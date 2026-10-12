# Инструкция по запуску чата на macOS

## Требования

1. **Java Development Kit (JDK)** версии 8 или выше
   - Проверьте установку: `java -version`
   - Если не установлен, скачайте с [Oracle](https://www.oracle.com/java/technologies/downloads/) или установите через Homebrew: `brew install openjdk`

## Компиляция проекта

1. Откройте Terminal (Терминал)
2. Перейдите в папку проекта:
   ```bash
   cd /path/to/ПП
   ```
3. Скомпилируйте все Java файлы:
   ```bash
   javac -encoding UTF-8 $(find . -name "*.java")
   ```
   или
   ```bash
   find . -name "*.java" -print0 | xargs -0 javac -encoding UTF-8
   ```

## Запуск

### 1. Сделайте скрипты исполняемыми:
```bash
chmod +x chat-server.sh
chmod +x chat-client.sh
```

### 2. Запустите сервер:

**Вариант А: Используя скрипт (рекомендуется)**
```bash
./chat-server.sh
```

**Вариант Б: Напрямую через Java**
```bash
java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.server.ChatServer
```

**Вариант В: С указанием порта**
```bash
./chat-server.sh 8071
```

### 3. Запустите клиент(ов):

В **новом окне терминала** запустите клиент:

**Вариант А: Используя скрипт (рекомендуется)**
```bash
./chat-client.sh Иван localhost
```

**Вариант Б: Напрямую через Java**
```bash
java -ea -esa -Dfile.encoding=UTF-8 csdev.chat.client.ChatClient Иван localhost
```

**Вариант В: Подключение к удаленному серверу**
```bash
./chat-client.sh Иван 192.168.1.101 8071
```

## Пример использования

1. **Терминал 1** (сервер):
   ```bash
   cd /path/to/ПП
   ./chat-server.sh
   ```
   Вы увидите: `[время] Прослушивание порта 8071`

2. **Терминал 2** (клиент 1):
   ```bash
   cd /path/to/ПП
   ./chat-client.sh Анна localhost
   ```

3. **Терминал 3** (клиент 2):
   ```bash
   cd /path/to/ПП
   ./chat-client.sh Иван localhost
   ```

4. Теперь можно общаться! Введите сообщение и нажмите Enter.

## Команды клиента

- Просто введите текст и нажмите Enter - сообщение отправится всем
- `/users` - показать список подключенных пользователей
- `/quit` - выйти из чата

## Подключение между macOS и Windows

Если сервер запущен на Windows, а клиент на macOS:

1. Узнайте IP-адрес Windows-машины (в Windows: `ipconfig`, найдите IPv4 адрес)
2. На macOS запустите клиент с IP Windows-машины:
   ```bash
   ./chat-client.sh Иван 192.168.1.101
   ```

Если сервер на macOS, а клиент на Windows:
1. Узнайте IP-адрес macOS-машины: `ifconfig | grep "inet "`
2. На Windows используйте этот IP при запуске клиента

## Устранение проблем

### Ошибка "Could not find or load main class"
- Убедитесь, что вы находитесь в корневой папке проекта (где находится папка `csdev`)
- Проверьте, что компиляция прошла успешно (должны быть `.class` файлы)

### Проблемы с кодировкой
- macOS обычно хорошо поддерживает UTF-8 по умолчанию
- Если видите кракозябры, убедитесь, что терминал использует UTF-8:
  ```bash
  export LANG=ru_RU.UTF-8
  export LC_ALL=ru_RU.UTF-8
  ```

### Порт занят
- Если порт 8071 занят, используйте другой:
  ```bash
  ./chat-server.sh 8080
  ./chat-client.sh Иван localhost 8080
  ```

### Firewall блокирует соединение
- На macOS может потребоваться разрешить Java в настройках безопасности
- Системные настройки → Безопасность → Разрешить Java

