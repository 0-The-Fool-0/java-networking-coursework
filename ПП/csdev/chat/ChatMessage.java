package csdev.chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Serializable DTO that travels between chat clients and the server.
 */
public final class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ChatMessageType type;
    private final String sender;
    private final String body;
    private final List<String> users;

    private ChatMessage(ChatMessageType type, String sender, String body, List<String> users) {
        this.type = Objects.requireNonNull(type, "type");
        this.sender = sender;
        this.body = body;
        if (users == null) {
            this.users = Collections.emptyList();
        } else {
            this.users = Collections.unmodifiableList(new ArrayList<String>(users));
        }
    }

    public ChatMessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public List<String> getUsers() {
        return users;
    }

    public static ChatMessage login(String nickname) {
        return new ChatMessage(ChatMessageType.LOGIN, nickname, null, null);
    }

    public static ChatMessage logout() {
        return new ChatMessage(ChatMessageType.LOGOUT_REQUEST, null, null, null);
    }

    public static ChatMessage loginAccepted(List<String> users) {
        return new ChatMessage(ChatMessageType.LOGIN_ACCEPTED, null, null, users);
    }

    public static ChatMessage loginRejected(String reason) {
        return new ChatMessage(ChatMessageType.LOGIN_REJECTED, null, reason, null);
    }

    public static ChatMessage userJoined(String nickname, List<String> users) {
        return new ChatMessage(ChatMessageType.USER_JOINED, nickname, nickname, users);
    }

    public static ChatMessage userLeft(String nickname, List<String> users) {
        return new ChatMessage(ChatMessageType.USER_LEFT, nickname, nickname, users);
    }

    public static ChatMessage chat(String sender, String text) {
        return new ChatMessage(ChatMessageType.CHAT, sender, text, null);
    }

    public static ChatMessage serverShutdown(String reason) {
        return new ChatMessage(ChatMessageType.SERVER_SHUTDOWN, null, reason, null);
    }
}

