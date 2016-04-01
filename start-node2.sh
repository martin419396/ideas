#!/bin/bash

sbt -Dnode.id=2 -Dhttp.port=9001 -Dakka.remote.netty.tcp.port=2552 -Dakka.cluster.seed-nodes.0="akka.tcp://ideas@127.0.0.1:2551" -Dakka.cluster.seed-nodes.1="akka.tcp://ideas@127.0.0.1:2552" run
