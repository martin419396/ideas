package configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.Configuration;
import akka.actor.ActorSystem;


@Singleton
public class The {

	private static ActorSystem actorSystem;
	private static Configuration configuration;

	@Inject
	public The(ActorSystem actorSystem, Configuration configuration) {
		The.actorSystem = actorSystem;
		The.configuration = configuration;
	}
	
	public static ActorSystem actorSystem() {
		return actorSystem;
	}

	public static String topic() {
		return configuration.getString("topic");
	}
	
	public static String cassandraKeyspace() {
		return configuration.getString("cassandra.keyspace");
	}

	public static String cassandraContactPoint() {
		return configuration.getString("cassandra.contact-point");
	}
	
	public static String kafkaBootstrapServers() {
		return configuration.getString("kafka.bootstrap.servers");
	}
}
