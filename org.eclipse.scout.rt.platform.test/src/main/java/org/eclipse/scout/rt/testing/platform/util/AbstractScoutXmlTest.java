/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public abstract class AbstractScoutXmlTest {

  @Test
  public void testScoutXml() throws IOException {
    ScoutXmlTestSupport test = createScoutXmlTestSupport();
    getPathExclusions().forEach(test::addPathExclusion);
    test.doTest();
    test.failOnError();
  }

  protected ScoutXmlTestSupport createScoutXmlTestSupport() {
    return BEANS.get(ScoutXmlTestSupport.class);
  }

  protected List<Path> getPathExclusions() {
    return Collections.emptyList();
  }
}
