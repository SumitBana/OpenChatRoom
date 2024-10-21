import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 6699;
    private static final Set<PrintWriter> clientWriters = new CopyOnWriteArraySet<>();
    private static final ConcurrentHashMap<PrintWriter, String> clientUsernames = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PrintWriter, String> clientIpAddresses = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            // Accept clients and handle their messages
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                    synchronized (clientWriters) {
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        clientWriters.add(out);

                        // Get client username
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String clientUsername = in.readLine();
                        clientUsernames.put(out, clientUsername);
                        clientIpAddresses.put(out, clientSocket.getInetAddress().getHostAddress());

                        // Notify all clients about the new client
                        broadcast(clientUsername + " from " + clientSocket.getInetAddress().getHostAddress() + " has joined the room.", true, clientUsername);

                        System.out.println(clientUsername + " from " + clientSocket.getInetAddress().getHostAddress() + " has joined the room.");

                        // Handle client messages
                        new Thread(() -> handleClientMessages(in, out, clientUsername, clientSocket)).start();
                    }
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating server socket: " + e.getMessage());
        }
    }

    private static void handleClientMessages(BufferedReader in, PrintWriter out, String clientUsername, Socket clientSocket) {
        try {
            String message;
            while ((message = in.readLine())!= null) {
                System.out.println(clientUsername + ": " + message);
                broadcast(clientUsername + ": " + message, false, clientUsername);
            }
        } catch (IOException e) {
        } finally {
            clientWriters.remove(out);
            clientUsernames.remove(out);
            clientIpAddresses.remove(out);

            // Notify all clients about the disconnected client
            broadcast(clientUsername + " from " + clientSocket.getInetAddress().getHostAddress() + " has left the room.", true, clientUsername);

            System.out.println(clientUsername + " from " + clientSocket.getInetAddress().getHostAddress() + " has left the room.");

            try {
                clientSocket.close();
            } catch (IOException ex) {
                System.err.println("Error closing client socket: " + ex.getMessage());
            }
        }
    }

    private static void broadcast(String message, boolean includeSelf, String selfUsername) {
        for (PrintWriter writer : clientWriters) {
            if (includeSelf ||!clientUsernames.get(writer).equals(selfUsername)) {
                writer.println(message);
            }
        }
    }
}