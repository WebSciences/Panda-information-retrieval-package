#!/bin/bash
#
# 
# a script for handling the indexing and retrieval from a standard 
# TREC test collection. It also prints out the contents of the basic
# structures of Panda.

fullPath () {
	t='TEMP=`cd $TEMP; pwd`'
	for d in $*; do
		eval `echo $t | sed 's/TEMP/'$d'/g'`
	done
}

#setup PANDA_HOME
if [ ! -n "$PANDA_HOME" ]
then
	#find out where this script is running
	TEMPVAR=`dirname $0`
	#make the path abolute
	fullPath TEMPVAR
	#Panda folder is folder above
	PANDA_HOME=`dirname $TEMPVAR`
	echo "Setting PANDA_HOME to $PANDA_HOME"
fi

#setup PANDA_ETC
if [ ! -n "$PANDA_ETC" ]
then
	PANDA_ETC=$PANDA_HOME/etc
fi

#setup PANDA_VAR
if [ ! -n "$PANDA_VAR" ]
then
	PANDA_VAR=$PANDA_HOME/var
fi

#setup JAVA_HOME
if [ ! -n "$JAVA_HOME" ]
then
	#where is java?
	TEMPVAR=`which java`
	#j2sdk/bin folder is in the dir that java was in
	TEMPVAR=`dirname $TEMPVAR`
	#then java install prefix is folder above
	JAVA_HOME=`dirname $TEMPVAR`
	echo "Setting JAVA_HOME to $JAVA_HOME"
fi

#setup CLASSPATH
for jar in $PANDA_HOME/dist/*.jar; do
	if [ ! -n "$CLASSPATH" ]
	then
		CLASSPATH=$jar
	else
		CLASSPATH=$CLASSPATH:$jar
	fi
done

#JAVA_OPTIONS=

$JAVA_HOME/bin/java -Xmx512M $JAVA_OPTIONS $PANDA_OPTIONS \
	 -Dpanda.etc=$PANDA_ETC \
	 -Dpanda.var=$PANDA_VAR \
	 -Dpanda.home=$PANDA_HOME \
	 -Dpanda.setup=$PANDA_ETC/panda.properties \
	 -cp $CLASSPATH uk.ac.ucl.panda.Panda $@
