package src;
import src.Client.ClientListener;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MsgRMI extends Remote {

    void addClient(ClientListener stub, String name) throws RemoteException;

    boolean login(String name, String credential) throws RemoteException;

    void logout(String name) throws RemoteException;

    boolean newUser(String name, String password) throws RemoteException;

    void sendMessage(String name, String recipient, String message, boolean toGroup) throws RemoteException;

    void createGroup(String s) throws RemoteException;

    void joinGroup(String name, String s) throws RemoteException;

    void unlinkGroup(String name, String s) throws RemoteException;
}