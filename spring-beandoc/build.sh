#!/bin/sh
#
# build.sh
#
# build jar and place in {dist} directory, or pass params to ant
#

if [ $# == 0 ]; then
  $ANT_HOME/bin/ant dist
else
  $ANT_HOME/bin/ant $*
fi
