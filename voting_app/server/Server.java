package interns.voting_app.server;

import interns.voting_app.server.events.ClientConnectedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import interns.voting_app.server.model.CommandResult;
import interns.voting_app.server.handlers.ServerCommandHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Component
public class Server {
    private final ApplicationEventPublisher eventPublisher;
    private final ServerCommandHandler serverCommandHandler;
    private static final Logger LOGGER;
    static {
        try {
            FileInputStream loggerConfig =
                    new FileInputStream("logger.properties");
            LogManager.getLogManager().readConfiguration(loggerConfig);
            LOGGER = Logger.getLogger("ServerLogger");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static BufferedReader selfReader = new BufferedReader(new InputStreamReader(System.in));


    @Autowired
    public Server(ApplicationEventPublisher eventPublisher, ServerCommandHandler serverCommandHandler) {
        this.eventPublisher = eventPublisher;
        this.serverCommandHandler = serverCommandHandler;
    }

    public void start(int port) {
        Thread commandThread = new Thread(this::handleConsoleCommands);
        commandThread.setDaemon(true);
        commandThread.start();

        try (ServerSocket server = new ServerSocket(port)) {
            LOGGER.info("Server started at port: " + server.getLocalPort());
            while (true) {
                Socket client = server.accept();
                LOGGER.info("New connected from " + client.getInetAddress().getHostAddress());
                eventPublisher.publishEvent(new ClientConnectedEvent(this, client));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception: " + e.getMessage(), e);
        }
    }

    private void handleConsoleCommands() {
        CommandResult result;
        while (true) {
            try {
                String command = selfReader.readLine();
                result = serverCommandHandler.handleCommand(command);
                if (result.getMessage().equals("exit")) {
                    LOGGER.info("Shutting down the server");
                    System.exit(0);
                }
                LOGGER.info(result.getMessage());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception: " + e.getMessage(), e);
            }
        }
    }
}
