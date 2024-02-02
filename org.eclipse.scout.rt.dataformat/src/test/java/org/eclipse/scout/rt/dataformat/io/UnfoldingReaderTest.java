/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataformat.io;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class UnfoldingReaderTest {

  @Test
  public void testInvalidFormat() throws IOException {
    testInvalidFormat("");
    testInvalidFormat("with spaces");
    testInvalidFormat("   with leading spaces");
    testInvalidFormat("multi\nline");
    testInvalidFormat("\nwith\nleading\nmulti\nline");
    testInvalidFormat("\t\ttest\t\twith\tleading\ttabs\t");
  }

  protected void testInvalidFormat(String text) throws IOException {
    UnfoldingReader unfoldingReader = new UnfoldingReader(new StringReader(text), "utf-8");
    assertNull(unfoldingReader.readProperty());

  }
}
