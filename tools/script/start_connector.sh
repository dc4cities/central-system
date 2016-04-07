#!/bin/sh
HOUR=3600


while [ 1 ]; do
    echo "Launching the connector"
    cd forecastENEAConnector
    ./run.sh --sync
    cd -
    echo "Sleeping for one hour"
    sleep ${HOUR}
done