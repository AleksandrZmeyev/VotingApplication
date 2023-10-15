package interns.voting_app.server.handlers;

import interns.voting_app.server.model.CommandResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("ServerLogger");
    private final Socket clientSocket;
    private final ClientCommandHandler clientCommandHandler;
    String username;

    public ClientHandler(Socket clientSocket,
                         ClientCommandHandler clientCommandHandler) {
        this.clientSocket = clientSocket;
        this.clientCommandHandler = clientCommandHandler;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            out.println("You need login to start work, use : login -u=username");
            String command;
            CommandResult result;
            do {
                command = in.readLine();
                result = clientCommandHandler.handleLoginCommand(command);
                out.println(result.getMessage());
            } while (!result.isSuccess());

            username = result.getMessage().split(" ")[3];
            LOGGER.info("Client " + clientSocket.getInetAddress().getHostAddress() + " logged in as " + username);

            while (!clientSocket.isClosed()) {
                command = in.readLine();
                if (command.equalsIgnoreCase("exit")) {
                    LOGGER.info(username + " disconnected");
                    break;
                }
                result = clientCommandHandler.handleCommand(command, username, in, out);
                out.println(result);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception: " + e.getMessage(), e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception: " + e.getMessage(), e);
            }
        }
    }
}
