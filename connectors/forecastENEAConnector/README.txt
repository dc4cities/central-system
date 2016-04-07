# How-to run the connector

## Prerequisite

    - a Java 1.7 runtime
    - wget to sync with the FTP server of HP that contains the data

## Run

The shell script `run.sh` launches the connector.
    - Using option '--sync' it syncs data then launch the connector.
    - Using option '--sync-only' the synchronisation is made and the program
      quit

Before starting the program, it is a good idea to do `run.sh --sync-only` to
synchronize the date (this may take a while). Then just launch `run.sh --sync'
when needed.

This program must execute itself regularly, typically every hour. Create
a crontab entry to do so:

$ crontab -e
0 * * * * path_to_forecaster/run.sh



