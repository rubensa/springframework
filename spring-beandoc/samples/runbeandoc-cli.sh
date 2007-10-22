#!/bin/sh

if [ $# != 1 ]; then
  echo Usage: ./runbeandoc-cli.sh [location of beandoc.properties]
  exit 99;
fi

mkdir ../target/sample-output
CLASSPATH=.:../target/dist/spring-beandoc.jar:../target/dist/spring-beans.jar:../target/dist/spring-core.jar:../target/dist/spring-context.jar:../target/dist/jdom.jar:../target/dist/commons-logging.jar
java -cp $CLASSPATH org.springframework.beandoc.client.BeanDocClient --properties $1
