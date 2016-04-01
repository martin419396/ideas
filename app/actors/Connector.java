package actors;

import models.Idea;
import play.Logger;
import actors.messages.PublishedIdea;
import actors.messages.ReceivedIdea;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import configuration.The;
import dal.CassandraClient;

public class Connector extends UntypedActor {

	private final ActorRef mediator;
		
    public Connector() {
    	mediator = DistributedPubSub.get(getContext().system()).mediator();
    	mediator.tell(new DistributedPubSubMediator.Subscribe(The.topic(), getSelf()), getSelf());
    }
    
	public void onReceive(Object msg) {
		if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
			Logger.info("{} subscribing to {}", getSelf(), The.topic());
		}
		else if (msg instanceof ReceivedIdea) {
			Idea idea = ((ReceivedIdea) msg).idea;
			Logger.info("New idea by {} from {} saying '{}'", idea.author, getSender(), idea.content);
			mediator.tell(new DistributedPubSubMediator.Publish(The.topic(), new PublishedIdea(idea)), getSelf());
			Logger.info("Persisting to Cassandra...");
			CassandraClient.insert(idea);
			Logger.info("Publishing to Kafka...");
			//TODO Kafka
		}
		else if (msg instanceof PublishedIdea) {
			Idea idea = ((PublishedIdea) msg).idea;
			if (getSender().equals(getSelf())) {
				Logger.info("Idea by {} from myself saying '{}'", idea.author, idea.content);
			}
			else {
				Logger.info("Idea by {} from {} saying '{}'", idea.author, getSender(), idea.content);
			}
		}
		else {
			unhandled(msg);
		}
	}

	public void postStop() throws Exception {
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(The.topic(), getSelf()), getSelf());
	}
}
