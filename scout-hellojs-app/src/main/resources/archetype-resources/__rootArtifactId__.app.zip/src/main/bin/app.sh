#set( $symbol_dollar = '$' )
#set( $symbol_pound = '#' )
#set( $symbol_escape = '\' )
${symbol_pound}!/bin/bash

${symbol_pound} expected working directory is the one containing the bin, conf and lib directories

${symbol_pound} set JAVA_BIN here unless defined as an environment variable
if [ -z ${symbol_dollar}JAVA_BIN ]; then
  JAVA_BIN="java"
fi
if [ -z ${symbol_dollar}APP_TMP ]; then
  APP_TMP="temp"
fi

JVM_ARGS="
-Xms64m -Xmx512m
-XX:-OmitStackTraceInFastThrow
-Djava.io.tmpdir=${symbol_dollar}APP_TMP
-Dscout.app.port=8082
"
CLASSPATH="conf/:lib/*"

mkdir -p logs
mkdir -p ${symbol_dollar}APP_TMP

${symbol_dollar}JAVA_BIN ${symbol_dollar}JVM_ARGS -classpath ${symbol_dollar}CLASSPATH org.eclipse.scout.rt.app.Application >> logs/${simpleArtifactName}.out 2>&1 &
echo ${symbol_dollar}! > bin/${simpleArtifactName}.pid
