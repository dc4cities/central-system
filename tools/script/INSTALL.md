# Installing DC4Cities

This document describes how-to make a clean install of the software stack on a trial.
The software stack is composed of standalone applications but also web-applications.
The distribution is organised as follow:
```
$ tree
 |- INSTALL.txt #This file
 |- lib/ #3rd party libraries used to run tools

 |- webapps/ #all the webapps
 \- forecastENEAConnector #connectors to the historical database
```

## Running DC4Cities

### Running locally

```sh
$ ./run_local.sh
```

This script will launch the webapps inside a local container using the script `start_webapps.sh`.
It will also launch the enea connector with a hourly synchronization thanks to the script `start_connector.sh`.
All the output is redirected into log files.

### Running on a production server

* Deploy the war files inside `webapps` into tomcat
* launch the connector every hour using cron. Use `run.sh --sync` to synchronise with the FTP server.
Warning, the `run.sh` script must be launched _from_ the `forecaseENEAConnector` folder

To run, the connector requires an access to the FTP server XXXX and an access to a remote port XXXX (the historical database)
