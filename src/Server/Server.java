package src.Server;

import src.MsgRMI;
import src.Server.Servant;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        int port = 1099;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto no válido. Usando 1099.");
            }
        }

        try {
            // Obtiene la IP local de la máquina automáticamente
            String serverIP = InetAddress.getLocalHost().getHostAddress();
            System.setProperty("java.rmi.server.hostname", serverIP);

            System.out.println("Servidor usando IP: " + serverIP);

            MsgRMI messager = new Servant();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("Messager", messager);

            System.out.println("RMI Server creado en el puerto " + port + "...");

            Thread thread = new Thread((Runnable) messager);
            thread.start();
        } catch (UnknownHostException e) {
            System.err.println("No se pudo determinar la IP de la máquina.");
        } catch (RemoteException e) {
            System.err.println("Error creando el servidor: " + e.getMessage());
        }
    }
}
