#!/bin/bash

sbt -Dnode.id=3 -Dhttp.port=9002 -Dakka.remote.netty.tcp.port=2553 -Dakka.cluster.seed-nodes.0="akka.tcp://ideas@127.0.0.1:2551" -Dakka.cluster.seed-nodes.1="akka.tcp://ideas@127.0.0.1:2552" run
