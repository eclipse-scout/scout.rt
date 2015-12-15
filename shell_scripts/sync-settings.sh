#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

mvn org.eclipse.scout:eclipse-settings-maven-plugin:eclipse-settings -f org.eclipse.scout.rt $*
