package dal;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.Idea;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import play.inject.ApplicationLifecycle;
import play.libs.Json;
import configuration.The;

@Singleton
public class KafkaClient {

	private static Producer<String, String> producer;

	@Inject
	public KafkaClient(ApplicationLifecycle appLifecycle) {
		Properties props = new Properties();
		props.put("bootstrap.servers", The.kafkaBootstrapServers());
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		producer = new KafkaProducer<>(props);

		appLifecycle.addStopHook(() -> {
			producer.close();
			return CompletableFuture.completedFuture(null);
		});
	}

	public static Future<RecordMetadata> send(Idea idea) {
		return producer.send(new ProducerRecord<String, String>(The.topic(), Json.toJson(idea).toString()));
	}
}
