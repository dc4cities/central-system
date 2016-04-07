#!/bin/sh

./start_webapps.sh > webapps.log 2>&1 &
./start_connector.sh > connector.log 2>&1 &
echo "Launched. See webapps.log and connector.log for informations."
wait
