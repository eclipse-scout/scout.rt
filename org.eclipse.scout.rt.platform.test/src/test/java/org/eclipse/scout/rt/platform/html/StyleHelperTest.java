/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.html;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
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
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassToNull() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass((String) null, ADDED_CLASS);
    Assert.assertEquals(ADDED_CLASS, newClasses);
  }

  @Test
  public void testAddCssClassEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, "");
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassToEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass("", ADDED_CLASS);
    Assert.assertEquals(ADDED_CLASS, newClasses);
  }

  @Test
  public void testAddCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, ADDED_CLASS);
    Assert.assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, newClasses);
  }

  @Test
  public void testAddMultipleCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass("a b c", "e b d");
    Assert.assertEquals("a b c e d", newClasses);
  }

  @Test
  public void testAddCssClassAlreadyContainedAsFirst() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, INITIAL_FIRST_CLASS);
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassAlreadyContainedAsLast() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, INITIAL_LAST_CLASS);
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassAlreadyContained() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, CLASS);
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassToNullCssClasses() {
    String nullClasses = null;
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(nullClasses, ADDED_CLASS);
    Assert.assertEquals(ADDED_CLASS, newClasses);
  }

  @Test
  public void testAddCssClassToEmptyCssClasses() {
    String emptyClasses = "";
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(emptyClasses, ADDED_CLASS);
    Assert.assertEquals(ADDED_CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClassNull() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, null);
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testRemoveCssClassToNull() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass((String) null, ADDED_CLASS);
    Assert.assertEquals("", newClasses);
  }

  @Test
  public void testRemoveCssClassEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, "");
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testRemoveCssClassToEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass("", ADDED_CLASS);
    Assert.assertEquals("", newClasses);
  }

  @Test
  public void testRemoveCssClassFirst() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, INITIAL_FIRST_CLASS);
    Assert.assertEquals("class " + INITIAL_LAST_CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClassLast() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, INITIAL_LAST_CLASS);
    Assert.assertEquals(INITIAL_FIRST_CLASS + " " + CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, CLASS);
    Assert.assertEquals(INITIAL_FIRST_CLASS + " " + INITIAL_LAST_CLASS, newClasses);
  }

  @Test
  public void testAllCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass("a b c", "c b a");
    Assert.assertEquals("", newClasses);
  }

  @Test
  public void testRemoveMultipleCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass("a b c", "e b d");
    Assert.assertEquals("a c", newClasses);
  }

  @Test
  public void testToggleCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, ADDED_CLASS, true);
    Assert.assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, ADDED_CLASS, true);
    Assert.assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, ADDED_CLASS, false);
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testToggleMultipleCssClasses() {
    String newClasses = BEANS.get(StyleHelper.class).toggleCssClass("a b c", "e b d", true);
    Assert.assertEquals("a b c e d", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass("a b c", "e b d", true);
    Assert.assertEquals("a b c e d", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass("a b c", "e b d", false);
    Assert.assertEquals("a c", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass("a b c", "e b d", false);
    Assert.assertEquals("a c", newClasses);
  }

  @Test
  public void testFormatting() {
    String newClasses = BEANS.get(StyleHelper.class).toggleCssClass("           a\n b     c\r", "       e    b\n d       ", true);
    Assert.assertEquals("a b c e d", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass("           a\n b     c\r", "       e    b\n d       ", false);
    Assert.assertEquals("a c", newClasses);
  }

  @Test
  public void testMultipleOccurences() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass("a a b a c", "e b e d b d");
    Assert.assertEquals("a a b a c e d", newClasses); // existing duplicates are unchanged
    newClasses = BEANS.get(StyleHelper.class).removeCssClass("a a b a c", "e b e d b d");
    Assert.assertEquals("a a a c", newClasses); // existing duplicates are unchanged
  }

  @Test
  public void testHasCssClass() {
    StyleHelper helper = BEANS.get(StyleHelper.class);
    Assert.assertTrue(helper.hasCssClass("foo bar", "bar"));
    Assert.assertTrue(helper.hasCssClass("foo bar", "foo"));
    Assert.assertTrue(helper.hasCssClass("foo bar", "bar foo"));
    Assert.assertFalse(helper.hasCssClass("foo bar", "bar baz"));
    Assert.assertFalse(helper.hasCssClass("foo bar", "baz"));
    Assert.assertTrue(helper.hasCssClass("          \nfoo\r bar", "    \rfoo"));

    Assert.assertTrue(helper.hasCssClass("foo bar", null));
    Assert.assertTrue(helper.hasCssClass("foo bar", ""));
    Assert.assertFalse(helper.hasCssClass(null, "baz"));
    Assert.assertFalse(helper.hasCssClass("", "baz"));
  }
}
