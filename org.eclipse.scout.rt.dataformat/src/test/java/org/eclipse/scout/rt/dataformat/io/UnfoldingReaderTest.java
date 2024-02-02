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

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.junit.Test;

public class UnfoldingReaderTest {

  @Test
  public void testSingleProperty() throws IOException {
    testSingleProperty("lorem:ipsum", "LOREM", "ipsum");
    testSingleProperty("\tloREM:ipsum", "\tLOREM", "ipsum");
    testSingleProperty("\nLOREM:ipsum", "LOREM", "ipsum");
    testSingleProperty("lorem:", "LOREM", "");
    testSingleProperty(":", "", "");
  }

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

  protected void testSingleProperty(String text, String key, String value) throws IOException {
    UnfoldingReader unfoldingReader = new UnfoldingReader(new StringReader(text), "utf-8");
    Property property = unfoldingReader.readProperty();
    assertEquals(key, property.getName());
    assertEquals(value, property.getValue());
  }
}
