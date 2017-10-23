#!/usr/bin/env bash

docker run -it --net=host -e "xpack.security.enabled=false" -e "discovery.type=single-node" -v cloud-metrics-elasticsearch:/usr/share/elasticsearch/data  docker.elastic.co/elasticsearch/elasticsearch:5.6.3
docker run -d --net=host grafana/grafana