#!/bin/sh
#Shell script to create a distribution of the central system

function getVersion {
    mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"
}

VERSION=`getVersion`
echo "Generate the distribution version ${VERSION}"

if [ $# -eq 0 -o "$1" != "--unsafe" ]; then
    mvn package -U || exit 1
fi

APP_NAME="dc4c_cs-${VERSION}"


TMP_DIR=`mktemp -d -t dc4cities.XXX` || exit 1
ROOT=${TMP_DIR}/${APP_NAME}
mkdir -p ${ROOT}

cp tools/INSTALL.md ${ROOT}/INSTALL.txt

# The webapps
mkdir ${ROOT}/webapps
for m in applications/ctrl-backend; do
    cp -rf $m/target/*.war ${ROOT}/webapps/
done

#sample configuration files
mkdir -p ${ROOT}/conf
cp -r env/inria/ctrl-backend/*.json ${ROOT}/conf

mkdir -p ${ROOT}/logs

#Basic runtime for the webapps
mkdir -p ${ROOT}/lib/
cp  tools/jetty-runner.jar ${ROOT}/lib/
cp tools/*.sh ${ROOT}/

# The connectors
tar xfz connectors/forecastENEAConnector/target/*.tar.gz -C ${ROOT}/ || exit 1
mv ${ROOT}/forecastENEAConnector-${VERSION} ${ROOT}/forecastENEAConnector

CWD=`pwd`
cd ${TMP_DIR}
tar cfz ${APP_NAME}.tar.gz ${APP_NAME}
mv ${APP_NAME}.tar.gz ${CWD}
cd -
echo "Done. Distribution available in ${APP_NAME}.tar.gz"
rm -rf ${TMP_DIR}
