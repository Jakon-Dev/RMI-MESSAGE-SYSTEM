/* ---------------------------------------------------------------
Práctica 1.
Código fuente: src/Client/ClientListener.java
Grau Informàtica
48056711M - Marc Lapeña Riu
--------------------------------------------------------------- */

package src.Client;
import java.rmi.*;
import java.time.*;

/**
 * Interfaz remota para recibir mensajes en un cliente.
 * Extiende java.rmi.Remote para permitir la comunicación remota.
 */
public interface ClientListener extends java.rmi.Remote {

    /**
     * Método remoto que se invoca cuando se recibe un nuevo mensaje.
     * @param sender Nombre del remitente del mensaje.
     * @param receiver Nombre del destinatario del mensaje.
     * @param message Contenido del mensaje.
     * @param hora Hora en la que se envió el mensaje.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    public void newMessage(String sender, String receiver, String message, LocalTime hora) throws RemoteException;
}
