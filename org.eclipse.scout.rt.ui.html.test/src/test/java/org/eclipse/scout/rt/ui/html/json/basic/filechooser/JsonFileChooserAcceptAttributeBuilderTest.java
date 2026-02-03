/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.basic.filechooser;

import java.util.Set;

import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Assert;
import org.junit.Test;

public class JsonFileChooserAcceptAttributeBuilderTest {

  @Test
  public void testNop() {
    Assert.assertEquals(Set.of(), new JsonFileChooserAcceptAttributeBuilder()
        .build());
  }

  @Test
  public void testNull() {
    Assert.assertEquals(Set.of(), new JsonFileChooserAcceptAttributeBuilder()
        .withType(null)
        .build());
  }

  @Test
  public void testNulls() {
    Assert.assertEquals(Set.of(), new JsonFileChooserAcceptAttributeBuilder()
        .withTypes(null)
        .build());
  }

  @Test
  public void testExt1() {
    Assert.assertEquals(Set.of("text/plain"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("txt")
        .build());
  }

  @Test
  public void testExt2() {
    Assert.assertEquals(Set.of("text/plain"), new JsonFileChooserAcceptAttributeBuilder()
        .withType(".txt")
        .build());
  }

  @Test
  public void testExt3() {
    Assert.assertEquals(Set.of("text/plain"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("*.txt")
        .build());
  }

  @Test
  public void testMime() {
    Assert.assertEquals(Set.of("text/plain"), new JsonFileChooserAcceptAttributeBuilder()
        .withType(MimeType.TXT.getType())
        .build());
  }

  @Test
  public void testExtWithCsv() {
    Assert.assertEquals(Set.of(".csv"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("csv")
        .build());
  }

  @Test
  public void testMimeWithCsv() {
    Assert.assertEquals(Set.of(".csv"), new JsonFileChooserAcceptAttributeBuilder()
        .withType(MimeType.CSV.getType())
        .build());
  }

  @Test
  public void testMimeWithJs() {
    Assert.assertEquals(Set.of(".js", ".mjs"), new JsonFileChooserAcceptAttributeBuilder()
        .withType(MimeType.JS.getType())
        .build());
  }

  @Test
  public void testExtWithJs() {
    Assert.assertEquals(Set.of(".js", ".mjs"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("js")
        .build());
  }

  @Test
  public void testExtWithMjs() {
    Assert.assertEquals(Set.of(".js", ".mjs"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("mjs")
        .build());
  }

  @Test
  public void testUnknownMime() {
    Assert.assertEquals(Set.of("foo/bar"), new JsonFileChooserAcceptAttributeBuilder()
        .withType("foo/bar")
        .build());
  }

  @Test
  public void testMultiple() {
    Assert.assertEquals(Set.of(".js", ".mjs", "image/png", "video/avi"), new JsonFileChooserAcceptAttributeBuilder()
        .withTypes(CollectionUtility.arrayList(MimeType.JS.getType(), MimeType.PNG.getType(), MimeType.AVI.getType()))
        .build());
  }
}
