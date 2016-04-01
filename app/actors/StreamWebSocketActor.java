package actors;

import configuration.The;
import play.Logger;
import play.libs.Json;
import actors.messages.PublishedIdea;
import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.ExtendedActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;

public class StreamWebSocketActor extends UntypedActor {

    private final ActorRef out;
    private final ActorRef mediator;
    private final Boolean onlyJson;

    public static Props props(ActorRef out) {
        return Props.create(StreamWebSocketActor.class, out, true);
    }

    public static Props props4status(ActorRef out) {
        return Props.create(StreamWebSocketActor.class, out, false);
    }

    public StreamWebSocketActor(ActorRef out, Boolean onlyJson) {
        this.out = out;
        this.onlyJson = onlyJson;
        mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(The.topic(), getSelf()), getSelf()); //TODO externalize
    }

    public void onReceive(Object msg) throws Exception {
        if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            Logger.info("{} now subscribing to ideas", getSelf());
        }
        else if (msg instanceof PublishedIdea) {
            if (onlyJson) {
                out.tell(Json.toJson(((PublishedIdea) msg).idea), getSelf());
            }
            else {
                Address address = getSender().path().address();
                String hostPort = address.host().isDefined() ? address.hostPort() : ((ExtendedActorSystem) getContext().system()).provider().getDefaultAddress().hostPort();
                out.tell(Json.toJson(((PublishedIdea) msg).idea) + " received by " + hostPort, getSelf());				
            }
        }
        else {
            unhandled(msg);
        }
    }

    public void postStop() throws Exception {
        Logger.info("{} stopping...", getSelf());
        mediator.tell(new DistributedPubSubMediator.Unsubscribe(The.topic(), getSelf()), getSelf());
    }
}