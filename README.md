# Ideas
A Play Framework application with Akka DistributedPubSub, Cassandra and Kafka.  

## Prerequisites
- [sbt](http://www.scala-sbt.org/download.html)
- Cassandra cluster with keyspaces demo and test. Run ```ideas.cql``` on both keyspaces to set up required tables. Check that ```cassandra.contact-point``` in ```application.conf``` defines a valid contact point for your Cassandra cluster.
- Kafka cluster. Add a topic with a name as defined by ```topic``` in application.conf.

## Usage
Run application tests:
```
sbt test
```
Run application with single node, simply:
```
sbt run
```
Check that the application is up and running with ```curl http://localhost:9000/ping```.

Run application with three nodes:
```
./start-node1.sh
```
```
./start-node2.sh
```
```
./start-node3.sh
```
each in its own terminal window.
Open ```http://localhost:9000/status``` and watch this page and the log messages in the terminal windows when posting ideas like this:
```
curl --header "Content-type: application/json" --request POST --data '{"author": "Ricardo", "content": "Let us play with long balls"}' http://localhost:9000/add
```
The page at ```http://localhost:9000/status``` will only show the ten most recently received ideas. To get all ideas ever posted, go to ```http://localhost:9000/all```.

With the three nodes running, the port numbers 9000, 9001 and 9002 may be used interchangebly in the URL's mentioned above. The three nodes form a cluster.

## Mechanics
This section describes some implementation details.

The json data received at http://localhost:9000/add is processed by method add in class controllers.IdeaController (the mapping is established in conf/routes). Deserializaton of the json data into an instance of class models.Idea is done using Jackson. If that fails a bad request response is sent to the client. Otherwise the newly created models.Idea instance is serialized and returned as json to the client. Meanwhile, and more importantly, that newly created models.Idea instance is sent to the Connector actor (actors.Connector.java). It is the responsibility of this actor to propagate the newly received idea to listening clients (/stream (websocket) and /status (javascript using the websocket)) and to other nodes in the cluster. It does so by means of the Akka DistributedPubSub Extension. The node receiving the data takes care of persistence by connecting to a Cassandra cluster using class dal.CassandreClient. For kicks the idea is also published to a Kafka cluster (using dal.KafkaClient).

## TODO
- interact with cassandra and kafka in a non-blocking way
- expand documentation
- add more tests
