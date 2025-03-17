package src.Client;
import java.rmi.*;
import java.time.*;

public interface ClientListener extends java.rmi.Remote{
    public void newMessage(String sender, String reciever, String message, LocalTime hora ) throws RemoteException;
}
