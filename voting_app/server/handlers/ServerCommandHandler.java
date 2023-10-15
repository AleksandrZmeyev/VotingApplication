package interns.voting_app.server.handlers;

import interns.voting_app.server.model.CommandResult;
import org.springframework.stereotype.Component;
import interns.voting_app.server.services.VotingService;

@Component
public class ServerCommandHandler {
    private final VotingService votingService;

    public ServerCommandHandler(VotingService votingService) {
        this.votingService = votingService;
    }

    public CommandResult handleCommand(String command) {
        String[] parts = command.split(" ");
        String action = parts[0];

        return switch (action) {
            case "load" -> handleLoadCommand(command);
            case "save" -> handleSaveCommand(command);
            case "exit" -> handleExitCommand();
            default -> new CommandResult(false, "Unknown command: " + command);
        };
    }

    private CommandResult handleLoadCommand(String command) {
        String[] parts = command.split(" ");
        if (parts.length != 2) {
            return new CommandResult(false, "Invalid load command format");
        }
        return votingService.loadFromFile(parts[1]);
    }

    private CommandResult handleSaveCommand(String command) {
        String[] parts = command.split(" ");
        if (parts.length != 2) {
            return new CommandResult(false, "Invalid save command format");
        }
        return votingService.saveInFile(parts[1]);
    }

    private CommandResult handleExitCommand() {
        return new CommandResult(true, "exit");
    }
}
