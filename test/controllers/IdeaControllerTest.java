package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import models.Idea;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import com.fasterxml.jackson.databind.JsonNode;

import configuration.The;
import dal.CassandraClient;

public class IdeaControllerTest {

	Application application;

	Idea ideaFirst  = new Idea("Cat1", "Jump down");
	Idea ideaMiddle = new Idea("Cat2", "Jump up");
	Idea ideaLast   = new Idea("Cat3", "Jump all around");

	@Before
	public void setup() {
		application = new GuiceApplicationBuilder()
			.configure("cassandra.keyspace", "test")
			.build();

		Helpers.start(application);

		CassandraClient.execute("drop table ideas");

		String createStmt =
			"create table ideas (" +
			"	  received timeuuid," +
			"	  received_date text," +
			"	  author text," +
			"	  content text," +
			"	  primary key (received_date, received)" +
			") with clustering order by (received desc);";

		CassandraClient.execute(createStmt);

		CassandraClient.insert(ideaFirst);
		CassandraClient.insert(ideaMiddle);
		CassandraClient.insert(ideaLast);
	}

	@After
	public void teardown() {
		Helpers.stop(application);
	}

	@Test
	public void testPing() {
		Result result = new controllers.IdeaController().ping();
		assertEquals(OK, result.status());
		assertEquals("text/plain", result.contentType().get());
		assertEquals("utf-8", result.charset().get());
		assertTrue(contentAsString(result).equals("pong"));
	}

	@Test
	public void testGetAll() throws InterruptedException, ExecutionException {
		ArrayList<Idea> list = new ArrayList<Idea>();
		Materializer materializer = ActorMaterializer.create(The.actorSystem());

		Result result = (new controllers.IdeaController()).all();

		result.body().dataStream().runForeach(chunk -> {
			JsonNode json = Json.parse(chunk.utf8String());
			String author = json.findPath("author").textValue();
			String content = json.findPath("content").textValue();
			list.add(new Idea(author, content));
		}, materializer)
		.toCompletableFuture()
		.get(); //we need to block to make sure list is filled

		assertTrue("Resultset has expected size", list.size() == 3);
		assertTrue("Last in is first out",        list.get(0).equals(ideaLast));
		assertTrue("The middle is the middle",    list.get(1).equals(ideaMiddle));
		assertTrue("First in is last out",        list.get(2).equals(ideaFirst));
	}
}
