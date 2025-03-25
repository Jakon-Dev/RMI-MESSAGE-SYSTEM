/* ---------------------------------------------------------------
Práctica 1.
Código fuente: src/MsgRMI.java
Grau Informàtica
48056711M - Marc Lapeña Riu 
--------------------------------------------------------------- */

package src;
import src.Client.ClientListener;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaz remota para la gestión de mensajería mediante RMI.
 */
public interface MsgRMI extends Remote {

    /**
     * Método para iniciar sesión en el sistema.
     * @param name Nombre de usuario.
     * @param credential Contraseña del usuario.
     * @param stub Referencia remota del cliente.
     * @return true si el inicio de sesión es exitoso, false en caso contrario.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    boolean login(String name, String credential, ClientListener stub) throws RemoteException;

    /**
     * Método para cerrar sesión.
     * @param name Nombre de usuario que cierra sesión.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    void logout(String name) throws RemoteException;

    /**
     * Método para registrar un nuevo usuario en el sistema.
     * @param name Nombre de usuario.
     * @param password Contraseña del usuario.
     * @return true si el usuario se crea con éxito, false en caso contrario.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    boolean newUser(String name, String password) throws RemoteException;

    /**
     * Método para enviar un mensaje a un usuario o grupo.
     * @param name Nombre del remitente.
     * @param recipient Nombre del destinatario o grupo.
     * @param message Contenido del mensaje.
     * @param toGroup Indica si el mensaje es para un grupo.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    void sendMessage(String name, String recipient, String message, boolean toGroup) throws RemoteException;

    /**
     * Método para crear un nuevo grupo.
     * @param s Nombre del grupo a crear.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    void createGroup(String s) throws RemoteException;

    /**
     * Método para unirse a un grupo existente.
     * @param name Nombre del usuario que se une al grupo.
     * @param s Nombre del grupo.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    void joinGroup(String name, String s) throws RemoteException;

    /**
     * Método para salir de un grupo.
     * @param name Nombre del usuario que se desvincula del grupo.
     * @param s Nombre del grupo.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    void unlinkGroup(String name, String s) throws RemoteException;
}
