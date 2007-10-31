#!/bin/sh

# thanks to Tomcat for the startup stuff..

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
 
PRGDIR=`dirname "$PRG"`

if [ $# != 1 ]; then
  echo Usage: $0 /path/to/beandoc.properties
  exit 99;
fi

for lib in `ls -1 $PRGDIR/../lib/*.jar`; do
  OUR_CP=$lib:$OUR_CP
done

EXEC="java -cp ${OUR_CP}. org.springframework.beandoc.client.BeanDocClient --properties $1"
echo $EXEC
exec $EXEC
