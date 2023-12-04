#!/bin/bash

#
# Copyright (c) 2010, 2023 BSI Business Systems Integration AG
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Check for missing CQs for all runtime modules (excluding tests or build dependencies)
# See https://www.eclipse.org/projects/handbook/#ip-prereq-diligence
# See https://github.com/eclipse/dash-licenses

# download Eclipse dash-licenses tool
rm -r -d -f dash >/dev/null
git clone --quiet https://github.com/eclipse/dash-licenses.git dash >/dev/null
cd dash

# install Eclipse dash-licenses tool into local Maven repo
mvn clean install -DskipTests=true >/dev/null
cd ..

# dash version must match the one present in pom of the master branch
DASH_VERSION="0.0.1-SNAPSHOT"

echo "MISSING CQs OF ALL TRANSITIVE JAVA DEPENDENCIES:"
mvn dependency:list -pl="-org.eclipse.scout.rt:org.eclipse.scout.rt.ui.html.selenium,-org.eclipse.scout.rt:org.eclipse.scout.rt.platform.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.shared.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.server.commons.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.server.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.server.jaxws.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.server.jdbc.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.dataobject.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.jackson.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.mom.api.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.mom.jms.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.mail.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.security.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.rest.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.rest.jersey.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.client.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.svg.client.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.ui.html.test,-org.eclipse.scout.rt:org.eclipse.scout.rt.app.test.affix,-org.eclipse.scout.rt:org.eclipse.scout.rt.app.test" |
  grep -Poh "\S+:(system|provided|compile)" | sort | uniq | java -jar $HOME/.m2/repository/org/eclipse/dash/org.eclipse.dash.licenses/$DASH_VERSION/org.eclipse.dash.licenses-$DASH_VERSION.jar - |
  grep -v "com.google.code.findbugs/jsr305/3.0.2" | grep -v "xml-apis/xml-apis-ext/1.3.04" # don't report as there CQs available: https://dev.eclipse.org/ipzilla/show_bug.cgi?id=18738 and https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7082
echo "--------------------------------------"

cd ..

function checkJs() {
  local dir=$1
  local name=$2

  echo "MISSING CQs FOR TRANSITIVE JAVASCRIPT DEPENDENCIES OF ${name}:"
  cd $dir
  rm -r -d -f node_modules
  pnpm install --ignore-scripts --force >/dev/null
  pnpm list -P -depth=1000 --parseable | grep -Po "[^\\\\]+@[^\\\\]+" | sort | uniq |
    grep -v $name | grep -v "chartjs-plugin-datalabels@2.0.0" | # don't report as there is a CQ available: https://dev.eclipse.org/ipzilla/show_bug.cgi?id=22596
    java -jar $HOME/.m2/repository/org/eclipse/dash/org.eclipse.dash.licenses/$DASH_VERSION/org.eclipse.dash.licenses-$DASH_VERSION.jar -
  echo "--------------------------------------"
  cd ..
}

checkJs "eclipse-scout-core" "@eclipse-scout/core"
checkJs "eclipse-scout-chart" "@eclipse-scout/chart"

# remove dash download again
rm -r -d -f org.eclipse.scout.rt/dash
