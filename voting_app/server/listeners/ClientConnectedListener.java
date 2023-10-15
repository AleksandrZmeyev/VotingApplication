package interns.voting_app.server.listeners;

import interns.voting_app.server.events.ClientConnectedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import interns.voting_app.server.handlers.ClientCommandHandler;
import interns.voting_app.server.handlers.ClientHandler;

import java.net.Socket;

@Component
public class ClientConnectedListener implements ApplicationListener<ClientConnectedEvent> {

    private final ClientCommandHandler clientCommandHandler;

    public ClientConnectedListener(ClientCommandHandler clientCommandHandler) {
        this.clientCommandHandler = clientCommandHandler;
    }

    @Override
    public void onApplicationEvent(ClientConnectedEvent event) {
        Socket clientSocket = event.getClientSocket();

        Thread clientHandlerThread = new Thread(new ClientHandler(clientSocket, clientCommandHandler));
        clientHandlerThread.setDaemon(true);
        clientHandlerThread.start();
    }
}
