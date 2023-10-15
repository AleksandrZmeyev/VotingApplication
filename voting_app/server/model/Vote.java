package interns.voting_app.server.model;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Vote implements Serializable {

    private final String owner;
    private final String name;
    private final String description;
    private final int answerCount;
    private Map<String, Integer> answers;

    public Vote(String voteName,
                String voteDescription,
                int answerCount,
                Map<String, Integer> answers,
                String owner) {
        this.name = voteName;
        this.description = voteDescription;
        this.answerCount = answerCount;
        this.answers = answers;
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getAnswers() {
        return answers;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public static Map<String, Integer> setAnswers(String[] answers, int answersCount) {
        Map<String, Integer> answersPool = new ConcurrentHashMap<>();
        for (int i = 0; i < answersCount; i++) {
            if (answersPool.containsKey(answers[i])) {
                return null;
            }
            if (!answers[i].isEmpty()) {
                answersPool.put(answers[i], 0);
            }
        }
        return answersPool;
    }


}
