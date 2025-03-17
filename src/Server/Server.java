package src.Server;

import src.MsgRMI;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Server {
    public static void main(String[] args) {
        try {
            System.out.println("Cargando Servicio RMI...");

            // Crear la instancia del servicio
            MsgRMI messager = new Servant();

            // Crear y obtener el registro RMI
            Registry registry = LocateRegistry.createRegistry(1099);

            // Registrar el objeto remoto
            registry.rebind("Messager", messager);

            System.out.println("Servidor RMI listo...");

            Thread thread = new Thread((Runnable) messager);
            thread.start();
        } catch (RemoteException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}