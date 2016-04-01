package dal;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import models.Idea;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

public class CassandraClientTest {

	Application application;

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
	}

	@After
	public void teardown() {
		Helpers.stop(application);
	}

	@Test
	public void testWriteAndRead() {
		Idea ideaFirst  = new Idea("Cat1", "Jump down");
		Idea ideaMiddle = new Idea("Cat2", "Jump up");
		Idea ideaLast   = new Idea("Cat3", "Jump all around");

		CassandraClient.insert(ideaFirst);
		CassandraClient.insert(ideaMiddle);
		CassandraClient.insert(ideaLast);

		ArrayList<Idea> list = new ArrayList<Idea>();
		CassandraClient.getAllIdeas().forEachRemaining(row ->
			list.add(new Idea(row.getString("author"), row.getString("content")))
		);

		assertTrue("Resultset has expected size", list.size() == 3);
		assertTrue("Last in is first out",        list.get(0).equals(ideaLast));
		assertTrue("The middle is the middle",    list.get(1).equals(ideaMiddle));
		assertTrue("First in is last out",        list.get(2).equals(ideaFirst));
	}
}
