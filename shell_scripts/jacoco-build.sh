#!/bin/bash
export MAVEN_OPTS='-Xmx256m'

# Parallel executions of maven modules and tests.
# Half of CPU core are used in to keep other half for OS and other programs.
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -B -e -V -Pcoverage-per-test -Dmaven.test.failure.ignore=true -T0.5C -DforkCount=0.5C $*
