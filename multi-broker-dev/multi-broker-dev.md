# Multi broker dev environment

Script to pull confluent docker images

```sh
CE_VERSION=5.0.0
for IMAGE in cp-zookeeper cp-enterprise-kafka cp-schema-registry \
    cp-kafka-connect cp-kafka-rest cp-ksql-server cp-ksql-cli \
    cp-enterprise-control-center
do
    docker image pull confluentinc/${IMAGE}:${CE_VERSION}
done
```

Create a folder confluent-ops in your home directory for the labs and navigate to it and use docker compose to up the cluster

```sh
$ mkdir -p ~/confluent-ops && cd ~/confluent-ops
$ docker-compose up -d

# Monitoring
$ docker stats

# Exec into cluster
$ docker-compose exec base /bin/bash
```