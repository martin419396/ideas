package configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.Configuration;


@Singleton
public class The {

	private static Configuration configuration;

	@Inject
	public The(Configuration configuration) {
		The.configuration = configuration;
	}
	
	public static String cassandraKeyspace() {
		return configuration.getString("cassandra.keyspace");
	}

	public static String cassandraContactPoint() {
		return configuration.getString("cassandra.contact-point");
	}
}
