package interns.voting_app.server.services;

import interns.voting_app.server.model.CommandResult;
import interns.voting_app.server.model.Topic;
import org.springframework.stereotype.Service;
import interns.voting_app.server.model.Vote;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class VotingService {
    private CopyOnWriteArrayList<Topic> topicList = new CopyOnWriteArrayList<>();

    public Topic getTopicByName(String topicName) {
        for (Topic topic : topicList) {
            if (topicName.equalsIgnoreCase(topic.getName())) {
                return topic;
            }
        }
        return null;
    }

    public CommandResult createTopic(String topicName) {
        if (getTopicByName(topicName) != null) {
            return new CommandResult(false, "Topic with this name is already created");
        }
        Topic topic = new Topic(topicName);
        boolean success = topicList.add(topic);
        String message = success ? ("Topic '" + topicName + "' created") : ("Topic not created");
        return new CommandResult(success, message);
    }

    public CommandResult createVoteInTopic(String topicName, String voteName,
                                             String voteDescription, int answerCount,
                                             String[] answersPool, String owner
    ) {
        Topic topic = getTopicByName(topicName);
        if (topic == null) {
            return new CommandResult(false,"Topic not found");
        }
        if (topic.getVoteByName(voteName) != null) {
            return new CommandResult(false,"Vote with this name is already created");
        }
        if (answersPool.length != answerCount) {
            return new CommandResult(false,"You entered not the number of answers that indicated");
        }
        Map<String, Integer> answers = Vote.setAnswers(answersPool, answerCount);
        if (answers == null) return new CommandResult(false, "You entered several identical answer options");
        Vote createdVote = new Vote(voteName, voteDescription, answerCount, answers, owner);
        boolean success = topic.getVoteList().add(createdVote);
        String message = success ? ("Vote '" + voteName + "' created") : ("Vote not created");
        return new CommandResult(success, message);
    }

    public CommandResult vote(String topicName, String voteName, String answer) {
        Vote vote = getTopicByName(topicName).getVoteByName(voteName);
        Map<String, Integer> answers = vote.getAnswers();
        if (!answers.containsKey(answer)) {
            return new CommandResult(false, "Incorrect answer");
        }
        answers.put(answer, answers.get(answer) + 1);
        return new CommandResult(true, "Vote accepted");
    }

    public CommandResult view() {
        if (topicList.isEmpty()) {
            return new CommandResult(false, "There is no topic yet");
        }
        StringBuilder topicNames = new StringBuilder();
        for (Topic topic : topicList) {
            topicNames.append(topic.getName()).append("(").append(topic.getVoteList().size()).append(") ");
        }
        return new CommandResult(true, topicNames.toString());
    }

    public CommandResult viewTopic(String topicName) {
        Topic topic = getTopicByName(topicName);
        if (topic == null) {
            return new CommandResult(false, "Topic not found");
        }
        List<Vote> votes = topic.getVoteList();
        if (votes.isEmpty()) {
            return new CommandResult(false, "No votes in topic");
        }
        StringBuilder voteNames = new StringBuilder("Votes: ");
        for (Vote vote : votes) {
            voteNames.append(vote.getName()).append(" ");
        }
        return new CommandResult(true, voteNames.toString());
    }

    public CommandResult viewVoteInTopic(String topicName, String voteName) {
        CommandResult result = isTopicOrVoteExist(topicName, voteName);
        if (!result.isSuccess()) {
            return result;
        }
        Topic topic = getTopicByName(topicName);
        Vote vote = topic.getVoteByName(voteName);
        StringBuilder voteInfo = new StringBuilder();
        voteInfo.append("Description: ").append(vote.getDescription()).append("\n").append("answers: ");
        for (Map.Entry<String, Integer> entry : vote.getAnswers().entrySet()) {
            voteInfo.append(entry.getKey()).append("-").append(entry.getValue()).append(" ");
        }
        return new CommandResult(true, voteInfo.toString());
    }

    public CommandResult deleteVoteFromTopic(String topicName, String voteName, String owner) {
        CommandResult result = isTopicOrVoteExist(topicName, voteName);
        if (!result.isSuccess()) {
            return result;
        }
        Topic topic = getTopicByName(topicName);
        Vote vote = topic.getVoteByName(voteName);
        if (!vote.getOwner().equals(owner)) {
            return new CommandResult(false,"Access denied");
        }
        boolean success = topic.getVoteList().remove(vote);
        String message = success ? ("Vote '" + voteName + "' deleted") : ("Vote not deleted");
        return new CommandResult(success, message);
    }

    public CommandResult isTopicOrVoteExist (String topicName, String voteName) {
        Topic topic = getTopicByName(topicName);
        if (topic == null) {
            return new CommandResult(false,"Topic not found");
        }
        if (topic.getVoteByName(voteName) == null) {
            return new CommandResult(false,"Vote not found");
        }
        return new CommandResult(true, "Topic and vote found");
    }

    public CommandResult saveInFile(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(topicList);
        } catch (IOException e) {
            return new CommandResult(false, e.getMessage());
        }
        return new CommandResult(true, "Topic list saved");
    }

    public CommandResult loadFromFile(String filePath) {
        CopyOnWriteArrayList<Topic> loadingTopicList;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            loadingTopicList = (CopyOnWriteArrayList<Topic>) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            return new CommandResult(false, e.getMessage());
        }
        int newTopicCount = topicList.addAllAbsent(loadingTopicList);
        return new CommandResult(true, newTopicCount + " topics loaded");
    }
}
