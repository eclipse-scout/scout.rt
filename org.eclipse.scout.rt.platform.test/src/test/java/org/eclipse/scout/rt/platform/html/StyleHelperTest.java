/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link StyleHelper}
 *
 * @since 8.0 (in rt.client since 5.2)
 */
@RunWith(PlatformTestRunner.class)
public class StyleHelperTest {

  public static final String CLASS = "class";
  public static final String INITIAL_FIRST_CLASS = "first-class";
  public static final String INITIAL_LAST_CLASS = "last-class";
  public static final String INITIAL_CLASSES = INITIAL_FIRST_CLASS + " " + CLASS + " " + INITIAL_LAST_CLASS;
  public static final String ADDED_CLASS = "added-class";

  @Test
  public void testAddCssClassNull() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, null);
    assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassToNull() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass((String) null, ADDED_CLASS);
    assertEquals(ADDED_CLASS, newClasses);
  }

  @Test
  public void testAddCssClassEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, "");
    assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassToEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass("", ADDED_CLASS);
    assertEquals(ADDED_CLASS, newClasses);
  }

  @Test
  public void testAddCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, ADDED_CLASS);
    assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, newClasses);
  }

  @Test
  public void testAddCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClasses(INITIAL_CLASSES, "a", "b");
    assertEquals(INITIAL_CLASSES + " a b", newClasses);
  }

  @Test
  public void testAddMultipleCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass("a b c", "e b d");
    assertEquals("a b c e d", newClasses);
  }

  @Test
  public void testAddCssClassAlreadyContainedAsFirst() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, INITIAL_FIRST_CLASS);
    assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassAlreadyContainedAsLast() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, INITIAL_LAST_CLASS);
    assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassAlreadyContained() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, CLASS);
    assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassToNullCssClasses() {
    String nullClasses = null;
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(nullClasses, ADDED_CLASS);
    assertEquals(ADDED_CLASS, newClasses);
  }

  @Test
  public void testAddCssClassToEmptyCssClasses() {
    String emptyClasses = "";
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(emptyClasses, ADDED_CLASS);
    assertEquals(ADDED_CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClassNull() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, null);
    assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testRemoveCssClassToNull() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass((String) null, ADDED_CLASS);
    assertEquals("", newClasses);
  }

  @Test
  public void testRemoveCssClassEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, "");
    assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testRemoveCssClassToEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass("", ADDED_CLASS);
    assertEquals("", newClasses);
  }

  @Test
  public void testRemoveCssClassFirst() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, INITIAL_FIRST_CLASS);
    assertEquals("class " + INITIAL_LAST_CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClassLast() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, INITIAL_LAST_CLASS);
    assertEquals(INITIAL_FIRST_CLASS + " " + CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, CLASS);
    assertEquals(INITIAL_FIRST_CLASS + " " + INITIAL_LAST_CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClasses(INITIAL_CLASSES, INITIAL_FIRST_CLASS, INITIAL_LAST_CLASS);
    assertEquals(CLASS, newClasses);
  }

  @Test
  public void testAllCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass("a b c", "c b a");
    assertEquals("", newClasses);
  }

  @Test
  public void testRemoveMultipleCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass("a b c", "e b d");
    assertEquals("a c", newClasses);
  }

  @Test
  public void testToggleCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, ADDED_CLASS, true);
    assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, ADDED_CLASS, true);
    assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, ADDED_CLASS, false);
    assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testToggleMultipleCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).toggleCssClass("a b c", "e b d", true);
    assertEquals("a b c e d", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass("a b c", "e b d", true);
    assertEquals("a b c e d", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass("a b c", "e b d", false);
    assertEquals("a c", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass("a b c", "e b d", false);
    assertEquals("a c", newClasses);
  }

  @Test
  public void testFormatting() {
    String newClasses = BEANS.get(StyleHelper.class).toggleCssClass("           a\n b     c\r", "       e    b\n d       ", true);
    assertEquals("a b c e d", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass("           a\n b     c\r", "       e    b\n d       ", false);
    assertEquals("a c", newClasses);
  }

  @Test
  public void testMultipleOccurences() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass("a a b a c", "e b e d b d");
    assertEquals("a a b a c e d", newClasses); // existing duplicates are unchanged
    newClasses = BEANS.get(StyleHelper.class).removeCssClass("a a b a c", "e b e d b d");
    assertEquals("a a a c", newClasses); // existing duplicates are unchanged
  }

  @Test
  public void testHasCssClass() {
    StyleHelper helper = BEANS.get(StyleHelper.class);
    assertTrue(helper.hasCssClass("foo bar", "bar"));
    assertTrue(helper.hasCssClass("foo bar", "foo"));
    assertTrue(helper.hasCssClass("foo bar", "bar foo"));
    assertFalse(helper.hasCssClass("foo bar", "bar baz"));
    assertFalse(helper.hasCssClass("foo bar", "baz"));
    assertTrue(helper.hasCssClass("          \nfoo\r bar", "    \rfoo"));

    assertTrue(helper.hasCssClass("foo bar", null));
    assertTrue(helper.hasCssClass("foo bar", ""));
    assertFalse(helper.hasCssClass(null, "baz"));
    assertFalse(helper.hasCssClass("", "baz"));
  }
}
