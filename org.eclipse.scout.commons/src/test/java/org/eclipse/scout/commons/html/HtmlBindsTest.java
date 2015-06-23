/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

/**
 * Tests for {@link HtmlBinds}
 */
public class HtmlBindsTest {

  @Test
  public void testBindInput() {
    String testInput = "test";
    HtmlBinds binds = new HtmlBinds();
    IHtmlBind newBind = binds.put(testInput);
    Object bindValue = binds.getBindValue(newBind.toString());
    assertEquals(testInput, bindValue);
  }

  @Test
  public void testGetReplacements_EmptyBinds() {
    HtmlBinds binds = new HtmlBinds();
    HtmlBinds binds2 = new HtmlBinds();
    Map<String, String> replacements = binds.getReplacements(binds2);
    assertTrue(replacements.isEmpty());
  }

  @Test
  public void testGetReplacements() {
    HtmlBinds binds = new HtmlBinds();
    binds.put("a");
    HtmlBinds binds2 = new HtmlBinds();
    binds2.put("b");
    Map<String, String> replacements = binds.getReplacements(binds2);
    Entry<String, String> firstEntry = replacements.entrySet().iterator().next();
    assertEquals(":b__0", firstEntry.getKey());
    assertEquals(":b__1", firstEntry.getValue());
  }

  @Test
  public void testGetRepl2() {
    HtmlBinds binds = new HtmlBinds();
    binds.put("a");
    binds.put("b");
    HtmlBinds binds2 = new HtmlBinds();
    binds2.put("c");
    Map<String, String> replacements = binds.getReplacements(binds2);
    Entry<String, String> firstEntry = replacements.entrySet().iterator().next();
    assertEquals(":b__0", firstEntry.getKey());
    assertEquals(":b__2", firstEntry.getValue());
  }

  @Test
  public void testGetRepl3() {
    HtmlBinds binds = new HtmlBinds();
    binds.put("a");
    HtmlBinds binds2 = new HtmlBinds();
    binds2.put("b");
    binds2.put("c");
    Map<String, String> replacements = binds.getReplacements(binds2);
    Iterator<Entry<String, String>> iter = replacements.entrySet().iterator();
    Entry<String, String> firstEntry = iter.next();
    Entry<String, String> secondEntry = iter.next();
    assertEquals(":b__1", firstEntry.getKey());
    assertEquals(":b__2", firstEntry.getValue());
    assertEquals(":b__0", secondEntry.getKey());
    assertEquals(":b__1", secondEntry.getValue());
  }
}
