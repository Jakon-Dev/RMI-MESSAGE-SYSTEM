package src.Server;

import src.Client.ClientListener;
import src.MsgRMI;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.util.*;

public class Servant extends UnicastRemoteObject implements MsgRMI, Runnable {
    private final Vector<ClientListener> listaListeners = new Vector<>();
    private final Map<String, String> usuarios = new HashMap<>();
    private final Map<String, List<String>> grupos = new HashMap<>();

    protected Servant() throws RemoteException {
        super();
        usuarios.put("Admin", "1234");  // Usuario administrador por defecto
        grupos.put("Logs", new ArrayList<>());
        grupos.get("Logs").add("Admin");
    }

    @Override
    public void run() {
        System.out.println("Servidor en ejecución...");
    }

    @Override
    public void addClient(ClientListener stub, String name) throws RemoteException {
        if (!usuarios.containsKey(name)) {
            System.out.println("Usuario no registrado: " + name);
            return;
        }
        listaListeners.add(stub);
        System.out.println("Cliente " + name + " registrado.");
    }

    @Override
    public boolean login(String name, String credential) throws RemoteException {
        if (usuarios.containsKey(name) && usuarios.get(name).equals(credential)) {
            System.out.println("Usuario autenticado: " + name);
            notificarListeners("Admin", "Logs", "Login " + name + " **** -> OK");
            return true;
        }
        System.out.println("Error de autenticación para " + name);
        notificarListeners("Admin", "Logs", "Login " + name + " **** -> Error");
        return false;
    }

    @Override
    public void logout(String name) throws RemoteException {
        listaListeners.removeIf(listener -> {
            try {
                return listener.equals(name);
            } catch (Exception e) {
                return false;
            }
        });
        System.out.println(name + " ha salido.");
        notificarListeners("Admin", "Logs", "Logout " + name + " -> OK");
    }

    @Override
    public boolean newUser(String name, String password) throws RemoteException {
        if (usuarios.containsKey(name)) {
            System.out.println("El usuario ya existe: " + name);
            return false;
        }
        usuarios.put(name, password);
        System.out.println("Nuevo usuario creado: " + name);
        notificarListeners("Admin", "Logs", "NewUser " + name + " **** -> OK");
        return true;
    }

    @Override
    public void sendMessage(String name, String recipient, String message, boolean toGroup) throws RemoteException {
        if(toGroup){
            name = name + " | " + recipient;
        }
        String fullMessage = LocalTime.now() + " [" + name + "]: " + message;
        System.out.println(fullMessage);

        if (toGroup) {
            if (grupos.containsKey(recipient)) {
                for (String member : grupos.get(recipient)) {
                    notificarListeners(name, member, message);
                }
            } else {
                System.out.println("Grupo no encontrado: " + recipient);
            }
        } else {
            notificarListeners(name, recipient, message);
        }
    }

    @Override
    public void createGroup(String groupName) throws RemoteException {
        if (!grupos.containsKey(groupName)) {
            grupos.put(groupName, new ArrayList<>());
            System.out.println("Grupo creado: " + groupName);
            notificarListeners("Admin", "Logs", "NewGroup " + groupName + " -> OK");
        } else {
            System.out.println("El grupo ya existe.");
        }
    }

    @Override
    public void joinGroup(String name, String groupName) throws RemoteException {
        grupos.computeIfAbsent(groupName, k -> new ArrayList<>()).add(name);
        System.out.println(name + " se ha unido a " + groupName);
        notificarListeners("Admin", "Logs", "JoinGroup " + groupName + " -> OK");
    }

    @Override
    public void unlinkGroup(String name, String groupName) throws RemoteException {
        if (grupos.containsKey(groupName) && grupos.get(groupName).remove(name)) {
            System.out.println(name + " ha salido de " + groupName);
            notificarListeners("Admin", "Logs", "UnlinkGroup " + groupName + " -> OK");
        } else {
            System.out.println("Error al salir del grupo.");
        }
    }

    private void notificarListeners(String sender, String receiver, String message) {
        for (ClientListener listener : listaListeners) {
            try {
                listener.newMessage(sender, receiver, message, LocalTime.now());
            } catch (RemoteException e) {
                System.err.println("Error notificando a cliente: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            Servant server = new Servant();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("Messager", server);
            System.out.println("Servidor listo.");
            new Thread(server).start();
        } catch (RemoteException e) {
            System.err.println("Error iniciando servidor: " + e.getMessage());
        }
    }
}
