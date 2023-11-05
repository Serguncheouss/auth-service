#!/bin/bash

git pull

git checkout "$1"

mvn clean
mvn install -DskipTests

rc=$?
# if maven failed, then we will not deploy new version.
if [ $rc -ne 0 ] ; then
  echo Could not perform mvn clean install, exit code [$rc]; exit $rc
fi

echo "COMMIT=$1" >> ./target/.env
for arg in "${@:2}"
do
  echo "$arg" >> ./target/.env
done

# Ensure, that docker-compose stopped
docker-compose --env-file ./target/.env stop

# Start new deployment with provided env vars in ./target/.env file
docker-compose --env-file ./target/.env up --build -d