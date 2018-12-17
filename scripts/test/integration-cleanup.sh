#!/bin/bash

#stop mongo
echo "Stop mongod"
sudo service mongod stop

#kill vault
pgrep -f "vault"| xargs kill -9 || true

#kill delegate
pgrep -f "integration-test/wings/delegate" | xargs kill -9

#kill manager
pgrep -f "integration-test/wings/rest" | xargs kill -9

#kill le container
docker ps -a
# don't fail if the container is not presents
docker ps | grep le_local | awk '{print $1}'

# cleaning up all exited containers
docker ps -a | grep Exited | awk '{print $1}' | xargs docker rm || true

echo "about to call docker kill le_local"
docker ps | grep learning | awk '{print $1}' | xargs docker kill || true

echo "Stop and remove all other docker containers"
docker stop $(docker ps -aq) || true
docker rm $(docker ps -aq) || true



