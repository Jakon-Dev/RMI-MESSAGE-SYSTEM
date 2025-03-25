/* ---------------------------------------------------------------
Práctica 1.
Código fuente: src/Server/Server.java
Grau Informàtica
48056711M - Marc Lapeña Riu
--------------------------------------------------------------- */

package src.Server;

import src.MsgRMI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Clase principal del servidor RMI.
 * Inicia el registro RMI, crea una instancia del servicio y lo publica.
 */
public class Server {
    public static void main(String[] args) throws UnknownHostException {
        int port = 1099; // Puerto por defecto para RMI

        try {
            // Si se pasa un argumento, se usa como puerto
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            // Crear la instancia del servicio RMI
            MsgRMI messager = new Servant();

            // Crear y obtener el registro RMI en el puerto especificado
            Registry registry = LocateRegistry.createRegistry(port);

            // Registrar el objeto remoto con el nombre "Messager"
            registry.rebind("Messager", messager);

            System.out.println("Servidor RMI listo...");

            // Iniciar el servidor en un hilo separado
            Thread thread = new Thread((Runnable) messager);
            thread.start();
        } catch (RemoteException e) {
            System.err.println("Error creando el servidor: " + e.getMessage());
        }
    }
}
