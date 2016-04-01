package actors.messages;

import java.io.Serializable;

import models.Idea;

public class PublishedIdea implements Serializable {
	private static final long serialVersionUID = 1L;

	public final Idea idea;

	public PublishedIdea(Idea idea) {
		this.idea = idea;
	}
}
