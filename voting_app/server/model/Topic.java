package interns.voting_app.server.model;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Topic implements Serializable {
    private final String name;
    private CopyOnWriteArrayList<Vote> voteList = new CopyOnWriteArrayList<>();

    public Topic(String topicName) {
        this.name = topicName;
    }

    public String getName() {
        return name;
    }

    public List<Vote> getVoteList() {
        return voteList;
    }

    public Vote getVoteByName(String voteName) {
        for (Vote vote : voteList) {
            if (voteName.equalsIgnoreCase(vote.getName())) {
                return vote;
            }
        }
        return null;
    }
}
