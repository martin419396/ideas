package actors.messages;

import models.Idea;

public class ReceivedIdea {
    public final Idea idea;

    public ReceivedIdea(Idea idea) {
        this.idea = idea;
    }
}