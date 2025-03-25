package src.Server;

import src.MsgRMI;
import src.Server.Servant;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) throws UnknownHostException {
        int port = 1099;

        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            // Crear la instancia del servicio
            MsgRMI messager = new Servant();

            // Crear y obtener el registro RMI
            Registry registry = LocateRegistry.createRegistry(port);

            // Registrar el objeto remoto
            registry.rebind("Messager", messager);

            System.out.println("Servidor RMI listo...");

            Thread thread = new Thread((Runnable) messager);
            thread.start();
        } catch (RemoteException e) {
            System.err.println("Error creando el servidor: " + e.getMessage());
        }
    }
}
