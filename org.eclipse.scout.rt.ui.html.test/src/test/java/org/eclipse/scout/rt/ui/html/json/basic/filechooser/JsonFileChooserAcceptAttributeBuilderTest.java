/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.basic.filechooser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.resource.MimeType;
import org.junit.Assert;
import org.junit.Test;

public class JsonFileChooserAcceptAttributeBuilderTest {

  @Test
  public void testNop() {
    Assert.assertEquals(setOf(), new JsonFileChooserAcceptAttributeBuilder()
        .build());
  }

  @Test
  public void testNull() {
    Assert.assertEquals(setOf(), new JsonFileChooserAcceptAttributeBuilder()
        .withType(null)
        .build());
  }

  @Test
  public void testNulls() {
    Assert.assertEquals(setOf(), new JsonFileChooserAcceptAttributeBuilder()
        .withTypes(null)
        .build());
  }

  @Test
  public void testExt1() {
    Assert.assertEquals(setOf("text/plain"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("txt")
        .build());
  }

  @Test
  public void testExt2() {
    Assert.assertEquals(setOf("text/plain"), new JsonFileChooserAcceptAttributeBuilder()
        .withType(".txt")
        .build());
  }

  @Test
  public void testExt3() {
    Assert.assertEquals(setOf("text/plain"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("*.txt")
        .build());
  }

  @Test
  public void testMime() {
    Assert.assertEquals(setOf("text/plain"), new JsonFileChooserAcceptAttributeBuilder()
        .withType(MimeType.TEXT_PLAIN.getType())
        .build());
  }

  @Test
  public void testExtWithCsv() {
    Assert.assertEquals(setOf(".csv"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("csv")
        .build());
  }

  @Test
  public void testMimeWithCsv() {
    Assert.assertEquals(setOf(".csv"), new JsonFileChooserAcceptAttributeBuilder()
        .withType(MimeType.CSV.getType())
        .build());
  }

  @Test
  public void testUnknownMime() {
    Assert.assertEquals(setOf("foo/bar"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("foo/bar")
        .build());
  }

  private static Set<String> setOf(String... elements) {
    return new HashSet<>(Arrays.asList(elements));
  }
}
