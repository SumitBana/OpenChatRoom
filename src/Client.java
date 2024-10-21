import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private static final int PORT = 6699;
    private static final String HOSTNAME = "localhost"; // Replace with the server's hostname or IP address

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOSTNAME, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) 
        {
            System.out.println("Connection Established");
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            out.println(username); // Send username to the server

            // Thread to listen for incoming messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine())!= null) {
                        if (message.contains("has joined the room")) {
                            String joinedUsername = message.split(" from ")[0];
                            System.out.println("\n--> " + joinedUsername + " has joined the room.\n");
                        } else if (message.contains("has left the room")) {
                            String leftUsername = message.split(" from ")[0];
                            System.out.println("\n--> " + leftUsername + " has left the room.\n");
                        } else {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection Lost");
                    
                }
            }).start();

            try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException ex) {}
            
            // Send messages to the server
            while (true) {
                String message = scanner.nextLine();
                System.out.print(username + ": "+message+"\n");
                out.println(message);
                if (message.equalsIgnoreCase("!exit")) 
                {
                    System.out.println("Exiting the Application");
                    System.exit(0);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
        }
    }
}