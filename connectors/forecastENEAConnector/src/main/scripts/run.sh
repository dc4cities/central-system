#!/bin/sh

#Define the classpath
ROOT=`dirname $0`
JARS=`ls ${ROOT}/jar/*.jar`

#Credential for the access to the FTP server
FTP_LOGIN=hackme
FTP_PASSWORD=hackme

FTP_URL=hackme
#Output directory for the synchronisation process

ftp_sync() {
    echo "Syncing ${FTP_URL} ..."
    echo "Entering ${ROOT}"
    cd ${ROOT}
    wget -nv -r -N -l inf --no-host-directories --user ${FTP_LOGIN} --password ${FTP_PASSWORD} ${FTP_URL}
    if [ $? -ne 0 ]; then
        echo "FAIL"
        cd -
        exit 1
    fi
    echo "Leaving ${ROOT}"
    cd -
    echo "DONE"
}

if [ $# -eq 1 ]; then
case $1 in
--sync)
        ftp_sync
        ;;
--sync-only)
        ftp_sync
        exit 0
        ;;
*)
        echo "Usage $0 [--sync| --sync-only]"
        echo "--sync sync with the remote directory and launch the program (optional)"
        echo "--sync-only sync with the remote directory and stop"
        exit 0
        ;;
esac
fi

if [ -n "$JAVA_HOME" ]; then
        JAVA=$JAVA_HOME/bin/java
else
        JAVA=java
fi

for JAR in $JARS; do
        CLASSPATH=$JAR:$CLASSPATH
done

JAVA_OPTS="-classpath $CLASSPATH"
MAIN="eu.dc4cities.connectors.ForecastENEAConnector.runner.ENEAForecastRunner"
$JAVA $JAVA_OPTS $MAIN ${ROOT}/config/eneaConnectorParams.json
