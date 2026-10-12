package csdev.chat;

/**
 * Defines all packet types that are exchanged between chat clients and the server.
 */
public enum ChatMessageType {
    /**
     * Client -> Server: requests to register under the provided nickname.
     */
    LOGIN,

    /**
     * Server -> Client: login accepted, the payload includes the current user list.
     */
    LOGIN_ACCEPTED,

    /**
     * Server -> Client: login rejected, payload contains human readable reason.
     */
    LOGIN_REJECTED,

    /**
     * Server -> Client: another participant joined, payload contains nickname.
     */
    USER_JOINED,

    /**
     * Server -> Client: a participant left the chat, payload contains nickname.
     */
    USER_LEFT,

    /**
     * Client -> Server and Server -> Client: regular chat message.
     */
    CHAT,

    /**
     * Client -> Server: requests graceful logout.
     */
    LOGOUT_REQUEST,

    /**
     * Server -> Client: server is going to shutdown.
     */
    SERVER_SHUTDOWN
}

