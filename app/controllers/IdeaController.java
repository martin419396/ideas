package controllers;

import models.Idea;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import actors.messages.ReceivedIdea;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import com.fasterxml.jackson.databind.JsonNode;

import configuration.The;
import dal.CassandraClient;

public class IdeaController extends Controller {

	final ActorSelection connector = The.actorSystem().actorSelection("/user/connector");
		
    public Result ping() {
    	return ok("pong");
    }
    
	@BodyParser.Of(BodyParser.Json.class)
	public Result add() {
		JsonNode json = request().body().asJson();

		String author = json.findPath("author").textValue();
		String content = json.findPath("content").textValue();

		if (author == null || content == null) {
			return badRequest("missing author and/or content of idea");
		}
		else {
			Idea newIdea = new Idea(author, content);
			connector.tell(new ReceivedIdea(newIdea), ActorRef.noSender());
			return ok(Json.toJson(newIdea));
		}
	}

    public Result all() {
        Source<JsonNode, ?> source =
        	Source
        		.fromIterator(() -> CassandraClient.getAllIdeas())
        		.map(row -> new Idea(row.getString("author"), row.getString("content")))
        		.map(idea -> Json.toJson(idea));
        
        return ok().chunked(source.map(json -> ByteString.fromString(json.toString() + "\n")));
    }
}