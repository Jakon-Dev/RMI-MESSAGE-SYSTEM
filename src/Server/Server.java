package src.Server;

import src.MsgRMI;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static void main(String[] args) {
        int port = 1099;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto no valido. Usando 1099.");
            }
        }

        try {
            System.out.println("Creando server en el puerto " + port + "...");

            MsgRMI messager = new Servant();

            Registry registry = LocateRegistry.createRegistry(port);

            registry.rebind("Messager", messager);

            System.out.println("RMI Server creado en el puerto " + port + "...");

            Thread thread = new Thread((Runnable) messager);
            thread.start();
        } catch (RemoteException e) {
            System.err.println("Error creando el server: " + e.getMessage());
        }
    }
}
