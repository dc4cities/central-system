#!/bin/sh

ARGS=""
for w in `ls webapps`; do
    p=`basename ${w} .war`
    ARGS="${ARGS} --path /${p} webapps/${w}"
done
CWD=`pwd`

java -jar lib/jetty-runner.jar ${ARGS}
