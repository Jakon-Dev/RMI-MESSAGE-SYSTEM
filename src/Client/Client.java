/* ---------------------------------------------------------------
Práctica 1.
Código fuente: src/Client/Client.java
Grau Informàtica
48056711M - Marc Lapeña Riu
--------------------------------------------------------------- */

package src.Client;

import src.MsgRMI;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Cliente de la aplicación de mensajería RMI.
 * Permite a los usuarios autenticarse, enviar y recibir mensajes,
 * y gestionar grupos de mensajería.
 */
public class Client implements ClientListener {
    private String name = "Anonymous";
    private static MsgRMI messager;

    public static void main(String[] args) {
        try {
            // Dirección del registro RMI
            String registryHost = (args.length > 0) ? args[0] : "localhost";

            // Obtener referencia al registro
            Registry registry = LocateRegistry.getRegistry(registryHost);

            // Buscar el servicio en el registro
            MsgRMI messager = (MsgRMI) registry.lookup("Messager");

            System.out.println("Conectado al RMI server en " + registryHost);

            // Crear instancia del cliente y exportarla como objeto remoto
            Client client = new Client();
            ClientListener stub = (ClientListener) UnicastRemoteObject.exportObject(client, 0);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                String[] command = input.split(" ", 2);

                switch (command[0].toLowerCase()) {
                    case "login":
                        if (command.length < 2) {
                            System.out.println("Uso: Login <Usuario> <Contraseña>");
                        } else {
                            String[] credentials = command[1].split(" ", 2);
                            if (credentials.length < 2) {
                                System.out.println("Debe proporcionar usuario y contraseña.");
                            } else {
                                String username = credentials[0];
                                String password = credentials[1];
                                boolean success = messager.login(username, password, stub);
                                if (success) {
                                    client.name = username;
                                    System.out.println("Login exitoso.");
                                } else {
                                    System.out.println("Error en login.");
                                }
                            }
                        }
                        break;

                    case "logout":
                        messager.logout(client.name);
                        System.out.println("Sesión cerrada.");
                        break;


                    case "newuser":
                        if (command.length < 2) {
                            System.out.println("Uso: NewUser <Usuario> <Contraseña>");
                        } else {
                            String[] userParams = command[1].split(" ", 3);
                            if (userParams.length < 2) {
                                System.out.println("Debe proporcionar usuario y contraseña.");
                            } else {
                                boolean success = messager.newUser(userParams[0], userParams[1]);
                                if(userParams.length == 3 && success){
                                    String group = userParams[2];
                                    messager.createGroup(group);
                                    messager.joinGroup(userParams[0], group);
                                }
                                System.out.println(success ? "Usuario creado correctamente." : "Error: el usuario ya existe.");
                            }
                        }
                        break;

                    case "sendmsg":
                        if (command.length < 2) {
                            System.out.println("Uso: SendMsg <Usuario> [-g <Grupo>] <TextoMensaje>");
                        } else {
                            String[] parts = command[1].split(" ", 2);

                            if (parts.length < 2) {
                                System.out.println("Mensaje incompleto. Debes proporcionar un destinatario y un mensaje.");
                            } else {
                                boolean toGroup = parts[0].equals("-g");
                                String recipient;
                                String message;

                                if (toGroup) {
                                    String[] groupParts = parts[1].split(" ", 2);
                                    if (groupParts.length < 2) {
                                        System.out.println("Uso incorrecto: SendMsg -g <Grupo> <TextoMensaje>");
                                    } else {
                                        recipient = groupParts[0];  // Nombre del grupo
                                        message = groupParts[1];    // Contenido del mensaje
                                        messager.sendMessage(client.name, recipient, message, true);
                                    }
                                } else {
                                    String[] userParts = command[1].split(" ", 2);
                                    if (userParts.length < 2) {
                                        System.out.println("Uso incorrecto: SendMsg <Usuario> <TextoMensaje>");
                                    } else {
                                        recipient = userParts[0];  // Nombre del usuario
                                        message = userParts[1];    // Contenido del mensaje
                                        messager.sendMessage(client.name, recipient, message, false);
                                    }
                                }
                            }
                        }
                        break;


                    case "newgroup":
                        if (command.length < 2) {
                            System.out.println("Uso: NewGroup <Grupo>");
                        } else {
                            messager.createGroup(command[1]);
                        }
                        break;

                    case "joingroup":
                        if (command.length < 2) {
                            System.out.println("Uso: JoinGroup <Grupo>");
                        } else {
                            messager.joinGroup(client.name, command[1]);
                        }
                        break;

                    case "unlinkgroup":
                        if (command.length < 2) {
                            System.out.println("Uso: UnlinkGroup <Grupo>");
                        } else {
                            messager.unlinkGroup(client.name, command[1]);
                        }
                        break;

                    case "exit":
                        messager.logout(client.name);
                        System.out.println("Saliendo de la aplicación.");
                        scanner.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Comando no reconocido.");
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }

    /**
     * Método llamado cuando el cliente recibe un nuevo mensaje.
     * @param sender Remitente del mensaje.
     * @param receiver Destinatario del mensaje (puede ser usuario o grupo).
     * @param message Contenido del mensaje.
     * @param hora Hora en que se recibió el mensaje.
     * @throws RemoteException En caso de error en la comunicación remota.
     */
    @Override
    public void newMessage(String sender, String receiver, String message, LocalTime hora) throws RemoteException {
        hora = LocalTime.parse(hora.format(DateTimeFormatter.ofPattern("HH:mm")));

        String output = receiver.isEmpty() ?
                String.format("\n%s [%s]: %s", hora, sender, message) :
                String.format("\n%s [%s:%s]: %s", hora, sender, receiver, message);

        System.out.println(output);
        System.out.print("> ");
    }

}