/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public abstract class AbstractXmlUtilityFactoryTest {

  @Test
  public void testNoFactoriesInCode() throws IOException {
    XmlFactoriesTestSupport test = new XmlFactoriesTestSupport();
    getFileExclusions().forEach(test::addFileExclusion);
    test.doTest();
    test.failOnError();
  }

  protected List<Pattern> getFileExclusions() {
    return Collections.emptyList();
  }
}
