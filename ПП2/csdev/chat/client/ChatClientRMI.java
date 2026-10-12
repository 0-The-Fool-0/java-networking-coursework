package csdev.chat.client;

import csdev.Protocol;
import csdev.chat.IChatClientCallback;
import csdev.chat.IChatService;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RMI-based chat client. Uses remote method calls to communicate with the server
 * and implements callback interface to receive notifications.
 */
public final class ChatClientRMI extends UnicastRemoteObject implements IChatClientCallback {

    private static final long serialVersionUID = 1L;
    private static final String SERVICE_NAME = "ChatService";
    
    private final AtomicReference<List<String>> users = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private IChatService chatService;
    private String nickname;

    private ChatClientRMI() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        // Настройка кодировки для Windows консоли
        setupConsoleEncoding();
        
        if (args.length < 1 || args.length > 3) {
            System.err.println("Использование: ChatClientRMI <никнейм> [хост] [порт]");
            return;
        }
        
        String nickname = args[0];
        String host = args.length >= 2 ? args[1] : "localhost";
        int port = args.length == 3 ? Integer.parseInt(args[2]) : Protocol.PORT;
        
        try {
            new ChatClientRMI().run(nickname, host, port);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void setupConsoleEncoding() {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        
        try {
            java.io.PrintStream utf8Out = new java.io.PrintStream(
                new FileOutputStream(FileDescriptor.out), true, "UTF-8");
            java.io.PrintStream utf8Err = new java.io.PrintStream(
                new FileOutputStream(FileDescriptor.err), true, "UTF-8");
            System.setOut(utf8Out);
            System.setErr(utf8Err);
        } catch (java.io.UnsupportedEncodingException e) {
            // Игнорируем, если UTF-8 не поддерживается
        }
    }

    private void run(String nickname, String host, int port) throws Exception {
        this.nickname = nickname;
        
        // Формируем URL для поиска сервиса
        String serviceUrl = "rmi://" + host + ":" + port + "/" + SERVICE_NAME;
        
        // Ищем сервис в RMI реестре
        System.out.println("Подключение к серверу " + serviceUrl + "...");
        chatService = (IChatService) Naming.lookup(serviceUrl);
        
        // Регистрируемся на сервере
        IChatService.LoginResult result = chatService.login(nickname, this);
        
        if (!result.isAccepted()) {
            System.err.println("Ошибка входа: " + result.getReason());
            return;
        }
        
        users.set(result.getUsers());
        System.out.println("Подключено к чату как '" + nickname + "'");
        System.out.println("Введите сообщение (или '/quit' для выхода, '/users' для списка пользователей)");
        
        // Читаем ввод пользователя
        try (BufferedReader console = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            
            prompt();
            String line;
            while (running.get() && (line = console.readLine()) != null) {
                if ("/quit".equalsIgnoreCase(line.trim())) {
                    break;
                }
                if ("/users".equalsIgnoreCase(line.trim())) {
                    printUsers(users.get());
                    prompt();
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    try {
                        chatService.sendMessage(nickname, line);
                    } catch (RemoteException e) {
                        System.err.println("Ошибка отправки сообщения: " + e.getMessage());
                        break;
                    }
                }
                prompt();
            }
        } finally {
            // Выходим из чата
            try {
                if (chatService != null) {
                    chatService.logout(nickname);
                }
            } catch (RemoteException e) {
                // Игнорируем ошибки при выходе
            }
            running.set(false);
            System.out.println("Отключено от сервера");
        }
    }

    @Override
    public void onMessage(String sender, String message) throws RemoteException {
        if (running.get()) {
            System.out.println("\n[" + sender + "]: " + message);
            prompt();
        }
    }

    @Override
    public void onUserJoined(String nickname, List<String> users) throws RemoteException {
        if (running.get()) {
            this.users.set(users);
            System.out.println("\n* " + nickname + " присоединился к чату");
            prompt();
        }
    }

    @Override
    public void onUserLeft(String nickname, List<String> users) throws RemoteException {
        if (running.get()) {
            this.users.set(users);
            System.out.println("\n* " + nickname + " покинул чат");
            prompt();
        }
    }

    @Override
    public void onServerShutdown(String reason) throws RemoteException {
        if (running.get()) {
            System.out.println("\n* Сервер: " + reason);
            running.set(false);
        }
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


