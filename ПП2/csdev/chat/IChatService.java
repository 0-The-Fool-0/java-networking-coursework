package csdev.chat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for chat service. Provides methods for client registration,
 * message sending, and logout.
 */
public interface IChatService extends Remote {
    
    /**
     * Registers a client with the chat server.
     * 
     * @param nickname the nickname to register
     * @param callback the client callback interface for receiving messages
     * @return login result with accepted status and user list, or rejected status with reason
     * @throws RemoteException if a remote error occurs
     */
    LoginResult login(String nickname, IChatClientCallback callback) throws RemoteException;
    
    /**
     * Sends a chat message from a client to all connected clients.
     * 
     * @param nickname the sender's nickname
     * @param message the message text
     * @throws RemoteException if a remote error occurs
     */
    void sendMessage(String nickname, String message) throws RemoteException;
    
    /**
     * Logs out a client from the chat server.
     * 
     * @param nickname the nickname to logout
     * @throws RemoteException if a remote error occurs
     */
    void logout(String nickname) throws RemoteException;
    
    /**
     * Result of login attempt.
     */
    class LoginResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final boolean accepted;
        private final String reason;
        private final List<String> users;
        
        private LoginResult(boolean accepted, String reason, List<String> users) {
            this.accepted = accepted;
            this.reason = reason;
            this.users = users != null ? new java.util.ArrayList<>(users) : new java.util.ArrayList<>();
        }
        
        public static LoginResult accepted(List<String> users) {
            return new LoginResult(true, null, users);
        }
        
        public static LoginResult rejected(String reason) {
            return new LoginResult(false, reason, null);
        }
        
        public boolean isAccepted() {
            return accepted;
        }
        
        public String getReason() {
            return reason;
        }
        
        public List<String> getUsers() {
            return new java.util.ArrayList<>(users);
        }
    }
}

