package csdev.chat.server;

import csdev.Protocol;
import csdev.chat.ChatMessage;
import csdev.chat.ChatMessageType;

import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multithreaded TCP chat server. One handler thread per connected client.
 */
public final class ChatServer {

    private final int port;
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final ConcurrentMap<String, ClientConnection> clients = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        // Настройка кодировки для Windows консоли
        setupConsoleEncoding();
        
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Protocol.PORT;
        ChatServer server = new ChatServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "chat-server-shutdown"));
        server.start();
    }
    
    private static void setupConsoleEncoding() {
        // Устанавливаем кодировку через системные свойства
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        
        // Пытаемся установить кодировку для потоков вывода
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

    private void start() {
        log("Прослушивание порта " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                clientPool.execute(new ClientConnection(socket));
            }
        } catch (IOException e) {
            log("Сервер остановлен: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        List<String> recipients = new ArrayList<>(clients.keySet());
        for (String nickname : recipients) {
            ClientConnection connection = clients.remove(nickname);
            if (connection != null) {
                connection.send(ChatMessage.serverShutdown("Сервер завершает работу"));
                connection.closeSilently();
            }
        }
        clientPool.shutdownNow();
        log("Завершение работы завершено");
    }

    private void broadcast(ChatMessage message) {
        clients.forEach((nick, connection) -> connection.send(message));
    }

    private List<String> snapshotUsers() {
        return new ArrayList<>(clients.keySet());
    }

    private void log(String message) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        System.out.println("[" + ts + "] " + message);
    }

    private final class ClientConnection implements Runnable {

        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String nickname;
        private volatile boolean active = true;

        private ClientConnection(Socket socket) {
            this.socket = Objects.requireNonNull(socket, "socket");
        }

        @Override
        public void run() {
            try {
                initializeStreams();
                if (!performHandshake()) {
                    return;
                }
                listenForMessages();
            } catch (IOException | ClassNotFoundException e) {
                log("Ошибка соединения: " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        private void initializeStreams() throws IOException {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        }

        private boolean performHandshake() throws IOException, ClassNotFoundException {
            Object obj = in.readObject();
            if (!(obj instanceof ChatMessage)) {
                send(ChatMessage.loginRejected("Сначала необходимо войти"));
                return false;
            }
            ChatMessage message = (ChatMessage) obj;
            if (message.getType() != ChatMessageType.LOGIN) {
                send(ChatMessage.loginRejected("Сначала необходимо войти"));
                return false;
            }
            String desiredNick = sanitizeNickname(message.getSender());
            if (desiredNick == null) {
                send(ChatMessage.loginRejected("Никнейм должен содержать 3-20 символов (латиница, кириллица, цифры, подчеркивание)"));
                return false;
            }
            if (clients.putIfAbsent(desiredNick, this) != null) {
                send(ChatMessage.loginRejected("Никнейм уже занят"));
                return false;
            }
            nickname = desiredNick;
            log(nickname + " подключился с " + socket.getRemoteSocketAddress());
            List<String> users = snapshotUsers();
            send(ChatMessage.loginAccepted(users));
            broadcast(ChatMessage.userJoined(nickname, users));
            return true;
        }

        private void listenForMessages() throws IOException, ClassNotFoundException {
            while (active) {
                Object obj;
                try {
                    obj = in.readObject();
                } catch (EOFException e) {
                    break;
                }
                if (!(obj instanceof ChatMessage)) {
                    continue;
                }
                ChatMessage message = (ChatMessage) obj;
                if (message.getType() == ChatMessageType.LOGOUT_REQUEST) {
                    break;
                }
                if (message.getType() == ChatMessageType.CHAT) {
                    String payload = message.getBody();
                    if (payload != null) {
                        String trimmed = payload.trim();
                        if (!trimmed.isEmpty()) {
                            broadcast(ChatMessage.chat(nickname, trimmed));
                        }
                    }
                }
            }
        }

        private void disconnect() {
            if (nickname != null && clients.remove(nickname, this)) {
                List<String> users = snapshotUsers();
                broadcast(ChatMessage.userLeft(nickname, users));
                log(nickname + " отключился");
            }
            closeSilently();
            active = false;
        }

        private void closeSilently() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        private void send(ChatMessage message) {
            synchronized (this) {
                if (!active || out == null) {
                    return;
                }
                try {
                    out.writeObject(message);
                    out.flush();
                } catch (IOException e) {
                    active = false;
                }
            }
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
    }
}

