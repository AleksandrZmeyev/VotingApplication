package interns.voting_app.client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket serverSocket;
    private BufferedReader serverReader;
    private PrintWriter serverWriter;

    public Client(String serverAddress, int serverPort) {
        try {
            serverSocket = new Socket(serverAddress, serverPort);
            serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            serverWriter = new PrintWriter(serverSocket.getOutputStream(), true);
            System.out.println("Connected to server " + serverAddress + ":" + serverPort);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void start() {
        Thread messageReaderThread = new Thread(new MessageReader());
        messageReaderThread.setDaemon(true);
        messageReaderThread.start();

        try {
            Scanner scanner = new Scanner(System.in);
            String line;
            while (!serverSocket.isOutputShutdown()) {
                line = scanner.nextLine();
                serverWriter.println(line);

                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Closing application");
                    try {
                        serverReader.close();
                        serverWriter.close();
                        serverSocket.close();
                        scanner.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    System.exit(0);
                }
            }
            scanner.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                serverReader.close();
                serverWriter.close();
                serverSocket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private class MessageReader implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = serverReader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                try {
                    serverReader.close();
                    serverWriter.close();
                    serverSocket.close();
                } catch (IOException exception) {
                    System.out.println(exception.getMessage());
                }
                System.exit(1);
            }
        }
    }
}
