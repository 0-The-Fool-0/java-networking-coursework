package csdev.chat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for client callbacks. The server uses this to notify clients
 * about events such as new messages, user joins/leaves, etc.
 */
public interface IChatClientCallback extends Remote {
    
    /**
     * Called by the server to deliver a chat message to the client.
     * 
     * @param sender the nickname of the message sender
     * @param message the message text
     * @throws RemoteException if a remote error occurs
     */
    void onMessage(String sender, String message) throws RemoteException;
    
    /**
     * Called by the server when a user joins the chat.
     * 
     * @param nickname the nickname of the user who joined
     * @param users the updated list of all connected users
     * @throws RemoteException if a remote error occurs
     */
    void onUserJoined(String nickname, List<String> users) throws RemoteException;
    
    /**
     * Called by the server when a user leaves the chat.
     * 
     * @param nickname the nickname of the user who left
     * @param users the updated list of all connected users
     * @throws RemoteException if a remote error occurs
     */
    void onUserLeft(String nickname, List<String> users) throws RemoteException;
    
    /**
     * Called by the server when it is shutting down.
     * 
     * @param reason the shutdown reason
     * @throws RemoteException if a remote error occurs
     */
    void onServerShutdown(String reason) throws RemoteException;
}

