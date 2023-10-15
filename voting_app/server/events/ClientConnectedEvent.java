package interns.voting_app.server.events;

import org.springframework.context.ApplicationEvent;

import java.net.Socket;

public class ClientConnectedEvent extends ApplicationEvent {

    private final Socket clientSocket;

    public ClientConnectedEvent(Object source, Socket clientSocket) {
        super(source);
        this.clientSocket = clientSocket;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
