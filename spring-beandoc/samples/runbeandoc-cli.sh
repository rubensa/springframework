#!/bin/sh

if [ $# != 1 ]; then
  echo Usage: ./runbeandoc-cli.sh [location of beandoc.properties]
  exit 99;
fi

mkdir ../target/beandoc
CLASSPATH=.:../target/dist/spring-beandoc.jar:../target/dist/spring-core.jar:../target/dist/jdom.jar:../target/dist/commons-logging.jar
java -cp $CLASSPATH org.springframework.beandoc.client.BeanDocClient $1
