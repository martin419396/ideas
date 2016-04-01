package dal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.Idea;
import play.Logger;
import play.inject.ApplicationLifecycle;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import configuration.The;

@Singleton
public class CassandraClient {
	private static Cluster cluster;
	private static Session session;
	
	private static PreparedStatement insertStatement;
	private static PreparedStatement selectStatement;
	
	@Inject
	public CassandraClient(ApplicationLifecycle appLifecycle) {
		cluster = Cluster.builder().addContactPoint(The.cassandraContactPoint()).build();
		session = cluster.connect(The.cassandraKeyspace());
		
		Logger.info("Connected to Cassandra cluster: {}", cluster.getMetadata().getClusterName());
		Logger.info("Keyspace: {}", session.getLoggedKeyspace());

		insertStatement = session.prepare("insert into ideas (received, received_date, author, content) values (now(), ?, ?, ?)");
		selectStatement = session.prepare("select * from ideas");

		appLifecycle.addStopHook(() -> {
			session.close();
			cluster.close();
			return CompletableFuture.completedFuture(null);
		});
	}
	
	public static ResultSet insert(Idea idea) {
		String date = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
		BoundStatement bound = insertStatement.bind(date, idea.author, idea.content);
		return session.execute(bound);
	}
	
	public static Iterator<Row> getAllIdeas() {
		BoundStatement bound = selectStatement.bind();
		return session.execute(bound).iterator();
	}
	
	/*
	 * Execute custom cql. Useful for testing.
	 */
	public static ResultSet execute(String cql) {
		return session.execute(cql);
	}
}
