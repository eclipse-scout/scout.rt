#set( $symbol_dollar = '$' )
#set( $symbol_pound = '#' )
#set( $symbol_escape = '\' )

REM expected working directory is the one containing the bin, conf and lib directories

REM set JAVA_BIN here unless defined as an environment variable
IF "%JAVA_BIN%" == "" set JAVA_BIN=java

set JVM_ARGS=-Xms64m -Xmx512m -XX:-OmitStackTraceInFastThrow -Dscout.app.port=8080
set CLASSPATH=conf/;lib/*

%JAVA_BIN% %JVM_ARGS% -classpath %CLASSPATH% org.eclipse.scout.rt.app.Application
