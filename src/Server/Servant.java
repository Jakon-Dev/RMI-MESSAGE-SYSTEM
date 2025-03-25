package src.Server;

import src.Client.ClientListener;
import src.MsgRMI;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Boolean.TRUE;


public class Servant extends UnicastRemoteObject implements MsgRMI, Runnable {
    private final ConcurrentHashMap<String, String> usuarios = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> grupos = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClientListener> clientesConectados = new ConcurrentHashMap<>();


    protected Servant() throws RemoteException {
        super();
        usuarios.put("Admin", "1234");  // Usuario administrador por defecto
        grupos.put("Logs", new CopyOnWriteArrayList<>());
        grupos.get("Logs").add("Admin");
    }

    @Override
    public void run() {
        System.out.println("Servidor en ejecución...");
    }

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




    @Override
    public void logout(String name) throws RemoteException {
        clientesConectados.remove(name);
        System.out.println(name + " ha salido.");
        notifyAdmin("'Logout " + name + "' -> OK");
        broadcastMessage("System", name + " se ha desconectado.");
    }


    @Override
    public boolean newUser(String name, String password) throws RemoteException {
        return newUser(name, password, null);
    }

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


    @Override
    public void joinGroup(String name, String groupName) throws RemoteException {
        grupos.computeIfAbsent(groupName, k -> new CopyOnWriteArrayList<>()).add(name);
        System.out.println(name + " se ha unido a " + groupName);
        sendMessage(groupName, groupName, name + " se ha unido al grupo.", TRUE);
        notifyAdmin("'JoinGroup " + groupName + "' -> OK");
    }

    @Override
    public void unlinkGroup(String name, String groupName) throws RemoteException {
        if (grupos.containsKey(groupName) && grupos.get(groupName).contains(name)) {
            grupos.computeIfPresent(groupName, (key, members) -> {
                members.remove(name);
                return members.isEmpty() ? null : members;
            });

            System.out.println(name + " ha salido de " + groupName);
            sendMessage(groupName, groupName, name + " se ha ido del grupo.", TRUE);
            notifyAdmin("'UnlinkGroup " + groupName + "' -> OK");
        } else {
            System.out.println("Error: " + name + " no está en el grupo " + groupName);
            notifyAdmin("'UnlinkGroup " + groupName + "' -> Error (usuario no estaba en el grupo)");
        }
    }


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


    private void notifyAdmin(String message) throws RemoteException {
        sendMessage("Admin", "Logs", message, TRUE);
    }

    private void notificarGrupo(String sender, String groupName, String message) {
        LocalTime now = LocalTime.now();
        List<String> desconectados = new ArrayList<>();

        if (!grupos.containsKey(groupName)) {
            System.out.println("Grupo no encontrado: " + groupName);
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
