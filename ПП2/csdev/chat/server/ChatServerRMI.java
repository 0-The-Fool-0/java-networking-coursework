package csdev.chat.server;

import csdev.Protocol;
import csdev.chat.IChatClientCallback;
import csdev.chat.IChatService;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RMI-based chat server. Clients register and communicate through remote method calls.
 */
public final class ChatServerRMI extends UnicastRemoteObject implements IChatService {

    private static final long serialVersionUID = 1L;
    private static final String SERVICE_NAME = "ChatService";
    
    private final ConcurrentMap<String, ClientInfo> clients = new ConcurrentHashMap<>();
    private final int port;

    public ChatServerRMI(int port) throws RemoteException {
        super();
        this.port = port;
    }

    public static void main(String[] args) {
        // Настройка кодировки для Windows консоли
        setupConsoleEncoding();
        
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Protocol.PORT;
        
        try {
            // Создаём реестр RMI на указанном порту
            Registry registry = LocateRegistry.createRegistry(port);
            log("RMI реестр создан на порту " + port);
            
            // Создаём и экспортируем сервис
            ChatServerRMI server = new ChatServerRMI(port);
            registry.rebind(SERVICE_NAME, server);
            log("Сервис '" + SERVICE_NAME + "' зарегистрирован в RMI реестре");
            
            // Добавляем обработчик завершения
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.shutdown();
                    registry.unbind(SERVICE_NAME);
                    log("Сервис отключен от реестра");
                } catch (Exception e) {
                    log("Ошибка при завершении: " + e.getMessage());
                }
            }, "chat-server-rmi-shutdown"));
            
            log("Сервер чата запущен. Нажмите Ctrl+C для остановки.");
            
            // Держим сервер работающим
            Thread.currentThread().join();
        } catch (Exception e) {
            log("Ошибка запуска сервера: " + e.getMessage());
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

    @Override
    public LoginResult login(String nickname, IChatClientCallback callback) throws RemoteException {
        Objects.requireNonNull(nickname, "nickname");
        Objects.requireNonNull(callback, "callback");
        
        String sanitized = sanitizeNickname(nickname);
        if (sanitized == null) {
            return LoginResult.rejected(
                "Никнейм должен содержать 3-20 символов (латиница, кириллица, цифры, подчеркивание)");
        }
        
        if (clients.putIfAbsent(sanitized, new ClientInfo(sanitized, callback)) != null) {
            return LoginResult.rejected("Никнейм уже занят");
        }
        
        log(sanitized + " подключился");
        List<String> users = snapshotUsers();
        
        // Уведомляем всех клиентов о новом подключении (исключая самого нового клиента)
        broadcastUserJoined(sanitized, users);
        
        return LoginResult.accepted(users);
    }

    @Override
    public void sendMessage(String nickname, String message) throws RemoteException {
        Objects.requireNonNull(nickname, "nickname");
        Objects.requireNonNull(message, "message");
        
        if (!clients.containsKey(nickname)) {
            throw new RemoteException("Клиент не зарегистрирован: " + nickname);
        }
        
        String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        
        // Отправляем сообщение всем клиентам
        broadcastMessage(nickname, trimmed);
    }

    @Override
    public void logout(String nickname) throws RemoteException {
        Objects.requireNonNull(nickname, "nickname");
        
        ClientInfo removed = clients.remove(nickname);
        if (removed != null) {
            log(nickname + " отключился");
            List<String> users = snapshotUsers();
            broadcastUserLeft(nickname, users);
        }
    }
    
    private void broadcastMessage(String sender, String message) {
        List<ClientInfo> recipients = new ArrayList<>(clients.values());
        for (ClientInfo client : recipients) {
            try {
                client.callback.onMessage(sender, message);
            } catch (RemoteException e) {
                // Клиент недоступен, удаляем его
                log("Клиент " + client.nickname + " недоступен, отключаем");
                clients.remove(client.nickname);
            }
        }
    }
    
    private void broadcastUserJoined(String nickname, List<String> users) {
        List<ClientInfo> recipients = new ArrayList<>(clients.values());
        for (ClientInfo client : recipients) {
            // Не отправляем уведомление самому новому клиенту
            if (client.nickname.equals(nickname)) {
                continue;
            }
            try {
                client.callback.onUserJoined(nickname, users);
            } catch (RemoteException e) {
                log("Клиент " + client.nickname + " недоступен, отключаем");
                clients.remove(client.nickname);
            }
        }
    }
    
    private void broadcastUserLeft(String nickname, List<String> users) {
        List<ClientInfo> recipients = new ArrayList<>(clients.values());
        for (ClientInfo client : recipients) {
            try {
                client.callback.onUserLeft(nickname, users);
            } catch (RemoteException e) {
                log("Клиент " + client.nickname + " недоступен, отключаем");
                clients.remove(client.nickname);
            }
        }
    }
    
    private void shutdown() {
        List<String> recipients = new ArrayList<>(clients.keySet());
        for (String nickname : recipients) {
            ClientInfo client = clients.remove(nickname);
            if (client != null) {
                try {
                    client.callback.onServerShutdown("Сервер завершает работу");
                } catch (RemoteException e) {
                    // Игнорируем ошибки при отключении
                }
            }
        }
        log("Завершение работы завершено");
    }

    private List<String> snapshotUsers() {
        return new ArrayList<>(clients.keySet());
    }

    private static void log(String message) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        System.out.println("[" + ts + "] " + message);
    }
    
    private String sanitizeNickname(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return null;
        }
        // Разрешаем латиницу, кириллицу, цифры и подчеркивание
        if (!trimmed.matches("[А-Яа-яA-Za-z0-9_]+")) {
            return null;
        }
        return trimmed;
    }
    
    private static class ClientInfo {
        final String nickname;
        final IChatClientCallback callback;
        
        ClientInfo(String nickname, IChatClientCallback callback) {
            this.nickname = nickname;
            this.callback = callback;
        }
    }
}

