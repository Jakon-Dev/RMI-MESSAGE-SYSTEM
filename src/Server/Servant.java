/* ---------------------------------------------------------------
Práctica 1.
Código fuente: src/Server/Servant.java
Grau Informàtica
48056711M - Marc Lapeña Riu
--------------------------------------------------------------- */

package src.Server;

import src.Client.ClientListener;
import src.MsgRMI;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import static java.lang.Boolean.TRUE;

/**
 * Implementación del servidor de mensajería mediante RMI.
 * Maneja usuarios, grupos y la comunicación entre clientes.
 */
public class Servant extends UnicastRemoteObject implements MsgRMI, Runnable {
    // Almacena usuarios registrados y sus contraseñas
    private final ConcurrentHashMap<String, String> usuarios = new ConcurrentHashMap<>();
    // Almacena los grupos y sus miembros
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> grupos = new ConcurrentHashMap<>();
    // Almacena los clientes actualmente conectados
    private final ConcurrentHashMap<String, ClientListener> clientesConectados = new ConcurrentHashMap<>();

    /**
     * Constructor del servidor.
     * Inicializa los usuarios y grupos por defecto.
     * @throws RemoteException Si hay un error en la comunicación RMI.
     */
    protected Servant() throws RemoteException {
        super();
        usuarios.put("Admin", "1234");  // Usuario administrador por defecto
        grupos.put("Logs", new CopyOnWriteArrayList<>());
        grupos.get("Logs").add("Admin");
    }

    /**
     * Ejecuta el servidor de mensajería.
     */
    @Override
    public void run() {
        System.out.println("Servidor en ejecución...");
    }

    /**
     * Método para iniciar sesión de un usuario.
     * Verifica que el usuario y la contraseña sean correctos,
     * y registra al usuario en la lista de clientes conectados.
     * @param name Nombre del usuario.
     * @param credential Contraseña del usuario.
     * @param stub Referencia remota del cliente para recibir mensajes.
     * @return true si el inicio de sesión es exitoso, false en caso contrario.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    @Override
    public boolean login(String name, String credential, ClientListener stub) throws RemoteException {
        if (!usuarios.containsKey(name)) {
            System.out.println("Intento de login con usuario inexistente: " + name);
            notifyAdmin("'Login " + name + " ****' -> Error (usuario no existe)");
            return false;
        }

        if (!usuarios.get(name).equals(credential)) {
            System.out.println("Intento de login con contraseña incorrecta para: " + name);
            notifyAdmin("'Login " + name + " ****' -> Error (contraseña incorrecta)");
            return false;
        }

        if (clientesConectados.containsKey(name)) {
            System.out.println("Usuario ya conectado: " + name);
            notifyAdmin("'Login " + name + " ****' -> Error (ya conectado)");
            return false;
        }

        clientesConectados.put(name, stub);
        System.out.println("Usuario autenticado y registrado: " + name);
        notifyAdmin("'Login " + name + " ****' -> OK");

        broadcastMessage("System", name + " se ha conectado.");
        stub.newMessage("System", name, "Bienvenido al sistema de mensajería.", LocalTime.now());
        return true;
    }



    /**
     * Método para cerrar sesión de un usuario.
     * @param name Nombre del usuario.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    @Override
    public void logout(String name) throws RemoteException {
        clientesConectados.remove(name);
        System.out.println(name + " ha salido.");
        notifyAdmin("'Logout " + name + "' -> OK");
        broadcastMessage("System", name + " se ha desconectado.");
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * @param name Nombre del usuario.
     * @param password Contraseña del usuario.
     * @return true si el usuario se creó correctamente, false si ya existe.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    @Override
    public boolean newUser(String name, String password) throws RemoteException {
        return newUser(name, password, null);
    }

    /**
     * Crea un nuevo usuario y lo añade opcionalmente a un grupo.
     * @param name Nombre del usuario.
     * @param password Contraseña del usuario.
     * @param groupName Nombre del grupo al que unirse (opcional).
     * @return true si el usuario se creó correctamente, false si ya existe.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    public boolean newUser(String name, String password, String groupName) throws RemoteException {
        if (usuarios.containsKey(name)) {
            System.out.println("El usuario ya existe: " + name);
            return false;
        }

        usuarios.put(name, password);
        System.out.println("Nuevo usuario creado: " + name);
        notifyAdmin("'NewUser " + name + " ****' -> OK");

        // Si se especifica un grupo, el usuario se une automáticamente
        if (groupName != null && !groupName.isEmpty()) {
            joinGroup(name, groupName);
        }

        return true;
    }



    /**
     * Crea un grupo de usuarios.
     * @param groupName Nombre del grupo a crear.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    @Override
    public void createGroup(String groupName) throws RemoteException {
        // Intenta crear el grupo si no existe
        if (grupos.putIfAbsent(groupName, new CopyOnWriteArrayList<>()) == null) {
            System.out.println("Grupo creado: " + groupName);
            broadcastMessage("System", "Se ha creado un nuevo grupo: " + groupName);
            notifyAdmin("'NewGroup " + groupName + "' -> OK");
        } else {
            System.out.println("El grupo ya existe.");
        }
    }

    /**
     * Método para unir a un usuario a un grupo existente.
     * @param name Nombre del usuario.
     * @param groupName Nombre del grupo al que se une.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    @Override
    public void joinGroup(String name, String groupName) throws RemoteException {
        if (!grupos.containsKey(groupName)) {
            System.out.println("Grupo no encontrado: " + groupName);
            notificarIndividual("System", name, "Grupo no encontrado: " + groupName);
            return;
        }
        grupos.computeIfAbsent(groupName, k -> new CopyOnWriteArrayList<>()).add(name);
        System.out.println(name + " se ha unido a " + groupName);
        sendMessage("System" , groupName, name + " se ha unido al grupo.", TRUE);
        notifyAdmin("'JoinGroup " + groupName + "' -> OK");
    }

    /**
     * Método para desvincular a un usuario de un grupo.
     * @param name Nombre del usuario.
     * @param groupName Nombre del grupo del que se desvincula.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    @Override
    public void unlinkGroup(String name, String groupName) throws RemoteException {
        if (grupos.containsKey(groupName) && grupos.get(groupName).contains(name)) {
            grupos.computeIfPresent(groupName, (key, members) -> {
                members.remove(name);
                return members.isEmpty() ? null : members;
            });

            System.out.println(name + " ha salido de " + groupName);
            sendMessage("System", groupName, name + " se ha ido del grupo.", TRUE);
            notifyAdmin("'UnlinkGroup " + groupName + "' -> OK");
        } else {
            System.out.println("Error: " + name + " no está en el grupo " + groupName);
            notifyAdmin("'UnlinkGroup " + groupName + "' -> Error (usuario no estaba en el grupo)");
        }
    }

    /**
     * Envía un mensaje a un usuario o grupo.
     * @param sender Nombre del remitente.
     * @param recipient Nombre del destinatario o grupo.
     * @param message Contenido del mensaje.
     * @param toGroup Indica si el mensaje es para un grupo o usuario individual.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    @Override
    public synchronized void sendMessage(String sender, String recipient, String message, boolean toGroup) throws RemoteException {
        String fullMessage = LocalTime.now() + " [" + sender + (toGroup ? ":" + recipient : "") + "]: " + message;
        System.out.println(fullMessage);

        boolean esLogAdmin = sender.equals("Admin") && recipient.equals("Logs");

        if (toGroup) {
            notificarGrupo(sender, recipient, message);
            if (!esLogAdmin) {
                notifyAdmin("'SendMsg -g " + recipient + " " + message + "' -> OK");
            }
        } else {
            notificarIndividual(sender, recipient, message);
            if (!esLogAdmin) {
                notifyAdmin("'SendMsg " + recipient + " " + message + "' -> OK");
            }
        }
    }


    /**
     * Método para enviar un mensaje a todos los usuarios conectados.
     * @param sender Nombre del remitente.
     * @param message Contenido del mensaje.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    private void broadcastMessage(String sender, String message) throws RemoteException {
        LocalTime now = LocalTime.now();
        List<String> clientesDesconectados = new ArrayList<>();

        for (Map.Entry<String, ClientListener> entry : clientesConectados.entrySet()) {
            try {
                entry.getValue().newMessage(sender, "", message, now);
            } catch (RemoteException e) {
                System.err.println("Error notificando a cliente: " + entry.getKey());
                clientesDesconectados.add(entry.getKey());  // Guardamos usuarios a eliminar
            }
        }

        // Eliminamos fuera del loop para evitar ConcurrentModificationException
        for (String cliente : clientesDesconectados) {
            clientesConectados.remove(cliente);
        }
    }

    /**
     * Método para enviar un mensaje al grupo de logs del sistema.
     * @param message Contenido del mensaje de log.
     * @throws RemoteException En caso de error en la comunicación RMI.
     */
    private void notifyAdmin(String message) throws RemoteException {
        sendMessage("Admin", "Logs", message, TRUE);
    }

    /**
     * Método para notificar un mensaje a todos los miembros de un grupo.
     * @param sender Nombre del remitente.
     * @param groupName Nombre del grupo.
     * @param message Contenido del mensaje.
     */
    private void notificarGrupo(String sender, String groupName, String message) {
        LocalTime now = LocalTime.now();
        List<String> desconectados = new ArrayList<>();

        if (!grupos.containsKey(groupName)) {
            System.out.println("Grupo no encontrado: " + groupName);
            notificarIndividual("System", sender, "Grupo no encontrado: " + groupName);
            return;
        }

        if (!grupos.get(groupName).contains(sender) && !Objects.equals(sender, "System")){
            System.out.println(sender + " no está en el grupo: " + groupName);
            notificarIndividual("System", sender, "No perteneces al grupo: " + groupName);
            return;
        }

        for (String miembro : grupos.get(groupName)) {
            ClientListener listener = clientesConectados.get(miembro);
            if (listener != null) {
                try {
                    listener.newMessage(sender, groupName, message, now);
                } catch (RemoteException e) {
                    System.err.println("Error notificando a miembro del grupo: " + miembro);
                    desconectados.add(miembro);
                }
            }
        }

        desconectados.forEach(clientesConectados::remove);
    }

    /**
     * Método para enviar un mensaje a un usuario específico.
     * @param sender Nombre del remitente.
     * @param receiver Nombre del destinatario.
     * @param message Contenido del mensaje.
     */
    private void notificarIndividual(String sender, String receiver, String message) {
        LocalTime now = LocalTime.now();

        ClientListener listener = clientesConectados.get(receiver);
        if (listener != null) {
            try {
                listener.newMessage(sender, "", message, now);
            } catch (RemoteException e) {
                System.err.println("Error notificando a cliente individual: " + receiver);
                clientesConectados.remove(receiver);
            }
        }
    }


}
