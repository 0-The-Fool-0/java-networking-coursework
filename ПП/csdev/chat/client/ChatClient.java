package csdev.chat.client;

import csdev.Protocol;
import csdev.chat.ChatMessage;
import csdev.chat.ChatMessageType;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Console chat client. Uses a dedicated thread for incoming messages and the main thread for user input.
 */
public final class ChatClient {

    private ChatClient() {
    }

    public static void main(String[] args) {
        // Настройка кодировки для Windows консоли
        setupConsoleEncoding();
        
        if (args.length < 1 || args.length > 3) {
            System.err.println("Использование: ChatClient <никнейм> [хост] [порт]");
            return;
        }
        String nickname = args[0];
        String host = args.length >= 2 ? args[1] : InetAddress.getLoopbackAddress().getHostName();
        int port = args.length == 3 ? Integer.parseInt(args[2]) : Protocol.PORT;
        new ChatClient().run(nickname, host, port);
    }
    
    private static void setupConsoleEncoding() {
        // Устанавливаем кодировку через системные свойства
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        
        // Пытаемся установить кодировку для потоков вывода
        try {
            java.io.PrintStream utf8Out = new java.io.PrintStream(
                new java.io.FileOutputStream(java.io.FileDescriptor.out), true, "UTF-8");
            java.io.PrintStream utf8Err = new java.io.PrintStream(
                new java.io.FileOutputStream(java.io.FileDescriptor.err), true, "UTF-8");
            System.setOut(utf8Out);
            System.setErr(utf8Err);
        } catch (java.io.UnsupportedEncodingException e) {
            // Игнорируем, если UTF-8 не поддерживается
        }
    }

    private void run(String nickname, String host, int port) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            out.writeObject(ChatMessage.login(nickname));
            out.flush();
            Object response = in.readObject();
            if (!(response instanceof ChatMessage)) {
                System.err.println("Неожиданный ответ от сервера");
                return;
            }
            ChatMessage message = (ChatMessage) response;
            if (message.getType() == ChatMessageType.LOGIN_REJECTED) {
                System.err.println("Ошибка входа: " + message.getBody());
                return;
            }
            if (message.getType() != ChatMessageType.LOGIN_ACCEPTED) {
                System.err.println("Неожиданный ответ от сервера");
                return;
            }
            System.out.println("Подключено к чату как '" + nickname + "'");
            AtomicReference<List<String>> users = new AtomicReference<>(message.getUsers());
            Thread reader = startReaderThread(in, users);
            prompt();
            String line;
            while ((line = console.readLine()) != null) {
                if ("/quit".equalsIgnoreCase(line.trim())) {
                    out.writeObject(ChatMessage.logout());
                    out.flush();
                    break;
                }
                if ("/users".equalsIgnoreCase(line.trim())) {
                    printUsers(users.get());
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    out.writeObject(ChatMessage.chat(nickname, line));
                    out.flush();
                }
                prompt();
            }
            reader.interrupt();
        } catch (EOFException eof) {
            System.err.println("Сервер закрыл соединение");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка соединения: " + e.getMessage());
        }
    }

    private Thread startReaderThread(ObjectInputStream in, AtomicReference<List<String>> users) {
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Object obj = in.readObject();
                    if (!(obj instanceof ChatMessage)) {
                        continue;
                    }
                    ChatMessage message = (ChatMessage) obj;
                        switch (message.getType()) {
                            case CHAT:
                                System.out.println("[" + message.getSender() + "]: " + message.getBody());
                                break;
                            case USER_JOINED:
                                users.set(message.getUsers());
                                System.out.println("* " + message.getBody() + " присоединился к чату");
                                break;
                            case USER_LEFT:
                                users.set(message.getUsers());
                                System.out.println("* " + message.getBody() + " покинул чат");
                                break;
                            case SERVER_SHUTDOWN:
                                System.out.println("* Сервер: " + message.getBody());
                                return;
                            default:
                                break;
                        }
                        prompt();
                    } catch (EOFException eof) {
                        System.out.println("* Сервер закрыл соединение");
                        return;
                    } catch (IOException e) {
                        System.out.println("* Соединение потеряно: " + e.getMessage());
                        return;
                    } catch (ClassNotFoundException e) {
                        System.out.println("* Неожиданные данные: " + e.getMessage());
                        return;
                    }
                }
            }
        }, "chat-client-reader");
        reader.setDaemon(true);
        reader.start();
        return reader;
    }

    private void printUsers(List<String> current) {
        if (current == null || current.isEmpty()) {
            System.out.println("Нет других подключенных пользователей");
            return;
        }
        System.out.println("Подключенные пользователи:");
        current.forEach(user -> System.out.println(" - " + user));
    }

    private void prompt() {
        System.out.print("> ");
        System.out.flush();
    }
}

