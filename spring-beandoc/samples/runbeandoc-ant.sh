#!/bin/sh

CLASSPATH=.:../target/dist/spring-beandoc.jar:../target/dist/spring-core.jar:../target/dist/jdom.jar:../target/dist/commons-logging.jar 
ant beandoc
