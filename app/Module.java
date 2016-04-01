import play.libs.akka.AkkaGuiceSupport;
import actors.Connector;

import com.google.inject.AbstractModule;

import configuration.The;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public class Module extends AbstractModule implements AkkaGuiceSupport {

    @Override
    public void configure() {
        bind(The.class).asEagerSingleton();
        bind(dal.CassandraClient.class).asEagerSingleton();
        bind(dal.KafkaClient.class).asEagerSingleton();
        bindActor(Connector.class, "connector");
    }

}
