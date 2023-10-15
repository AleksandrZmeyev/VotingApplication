package interns.voting_app.server.handlers;

import interns.voting_app.server.model.CommandResult;
import interns.voting_app.server.model.Vote;
import interns.voting_app.server.services.VotingService;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Component
public class ClientCommandHandler {
    private final VotingService votingService;

    public ClientCommandHandler(VotingService votingService) {
        this.votingService = votingService;
    }

    public CommandResult handleCommand(String command, String username, BufferedReader in, PrintWriter out) throws IOException {
        String[] parts = command.split(" ");
        String action = parts[0];

        return switch (action) {
            case "login" -> new CommandResult(false, "You are already logged in as " + username);
            case "create" -> handleCreateCommand(command, username, in, out);
            case "view" -> handleViewCommand(command);
            case "vote" -> handleVoteCommand(command, in, out);
            case "delete" -> handleDeleteCommand(command, username);
            case "exit" -> handleExitCommand(command);
            default -> new CommandResult(false, "Unknown command: " + command);
        };
    }

    public CommandResult handleLoginCommand(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 2 && parts[0].equals("login")) {
            String usernameParam = parts[1];
            if (usernameParam.startsWith("-u=")) {
                String username = usernameParam.substring(3);
                return new CommandResult(true,"Logged in as: " + username);
            } else {
                return new CommandResult(false,"Invalid login command format, use : login -u=username");
            }
        } else {
            return new CommandResult(false, "Invalid login command format, use : login -u=username");
        }
    }

    private CommandResult handleCreateCommand(String command, String username, BufferedReader in, PrintWriter out) throws IOException {
        String[] parts = command.split(" ");
        if (parts.length == 3) {
            if (parts[1].equals("topic") && parts[2].startsWith("-n=")) {
                String topicName = parts[2].substring(3);
                return votingService.createTopic(topicName);
            } else if (parts[1].equals("vote") && parts[2].startsWith("-t=")) {
                String topicName = parts[2].substring(3);
                out.println("Enter vote name");
                String voteName = in.readLine();
                out.println("Enter description");
                String description = in.readLine();
                out.println("Enter answer count");
                int answerCount = Integer.parseInt(in.readLine());
                out.println("Enter answers in one line,use ';' to separate answers");
                String[] answerPool = in.readLine().split(";");
                return votingService.createVoteInTopic(topicName, voteName, description, answerCount, answerPool, username);
            } else {
                return new CommandResult(false, "Invalid create command format");
            }
        } else {
            return new CommandResult(false, "Invalid create command format");
        }
    }

    private CommandResult handleViewCommand(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 1) {
            return votingService.view();
        }
        if (parts.length == 2 && parts[1].startsWith("-t=")) {
            String topicName = parts[1].substring(3);
            return votingService.viewTopic(topicName);
        }
        if (parts.length == 3 && parts[1].startsWith("-t=") && parts[2].startsWith("-v=")) {
            String topicName = parts[1].substring(3);
            String voteName = parts[2].substring(3);
            return votingService.viewVoteInTopic(topicName, voteName);
        }
        return new CommandResult(false, "Invalid view command format");
    }

    private CommandResult handleVoteCommand(String command, BufferedReader in, PrintWriter out) throws IOException {
        String[] parts = command.split(" ");
        if (parts.length == 3 && parts[1].startsWith("-t=") && parts[2].startsWith("-v=")) {
            String topicName = parts[1].substring(3);
            String voteName = parts[2].substring(3);
            CommandResult result = votingService.isTopicOrVoteExist(topicName, voteName);
            if (!result.isSuccess()) return result;

            Vote vote = votingService.getTopicByName(topicName).getVoteByName(voteName);
            StringBuilder choose = new StringBuilder("Enter a suggested answer: ");
            for (Map.Entry<String, Integer> entry : vote.getAnswers().entrySet()) {
                choose.append(entry.getKey()).append(" | ");
            }
            out.println(choose);

            String answer = in.readLine();
            return votingService.vote(topicName, voteName, answer);
        }
        return new CommandResult(false, "Invalid vote command format");
    }

    private CommandResult handleDeleteCommand(String command, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 3 && parts[1].startsWith("-t=") && parts[2].startsWith("-v=")) {
            String topicName = parts[1].substring(3);
            String voteName = parts[2].substring(3);
            return votingService.deleteVoteFromTopic(topicName, voteName, username);
        }
        return new CommandResult(false, "Invalid delete command format");
    }

    private CommandResult handleExitCommand(String command) {
        if (command.split(" ").length > 1) {
            return new CommandResult(false, "Invalid exit command format");
        }
        return new CommandResult(true, "The user closed the application");
    }
}
