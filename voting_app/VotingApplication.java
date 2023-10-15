package interns.voting_app;

import interns.voting_app.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import interns.voting_app.client.Client;

@SpringBootApplication
public class VotingApplication {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Use: java -jar voting-app.jar server|client");
            System.exit(1);
        }

        String mode = args[0];

        if ("server".equals(mode)) {
            startServer(args);
        } else if ("client".equals(mode)) {
            String serverAddress = "localhost";
            startClient(serverAddress);
        } else {
            System.out.println("Invalid mode. Use 'server' or 'client <serverAddress>'.");
            System.exit(1);
        }
    }

    private static void startServer(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(VotingApplication.class, args);
        Server server = context.getBean(Server.class);
        server.start(8080);
    }

    private static void startClient(String serverAddress) {
        Client client = new Client(serverAddress, 8080);
        client.start();
    }

}
