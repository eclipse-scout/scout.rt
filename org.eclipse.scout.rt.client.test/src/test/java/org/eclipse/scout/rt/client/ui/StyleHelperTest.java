/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Junit test for {@link StyleHelper}
 *
 * @since 5.2
 */
@RunWith(PlatformTestRunner.class)
public class StyleHelperTest {

  public static final String INITIAL_FIRST_CLASS = "first-class";
  public static final String INITIAL_LAST_CLASS = "last-class";
  public static final String INITIAL_CLASSES = INITIAL_FIRST_CLASS + " class " + INITIAL_LAST_CLASS;

  @Test
  public void testAddCssClassNull() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, null);
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassEmpty() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, "");
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, "added-class");
    Assert.assertEquals(INITIAL_CLASSES + " added-class", newClasses);
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
    String newClasses = BEANS.get(StyleHelper.class).addCssClass(INITIAL_CLASSES, "class");
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testAddCssClassStylable() {
    IStyleable stylable = new Cell();
    stylable.setCssClass(INITIAL_CLASSES);
    BEANS.get(StyleHelper.class).addCssClass(stylable, "added-class");
    Assert.assertEquals(INITIAL_CLASSES + " added-class", stylable.getCssClass());
  }

  @Test
  public void testRemoveCssClassFirst() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, INITIAL_FIRST_CLASS);
    Assert.assertEquals("class " + INITIAL_LAST_CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClassLast() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, INITIAL_LAST_CLASS);
    Assert.assertEquals(INITIAL_FIRST_CLASS + " class", newClasses);
  }

  @Test
  public void testRemoveCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).removeCssClass(INITIAL_CLASSES, "class");
    Assert.assertEquals(INITIAL_FIRST_CLASS + " " + INITIAL_LAST_CLASS, newClasses);
  }

  @Test
  public void testRemoveCssClassStylable() {
    IStyleable stylable = new Cell();
    stylable.setCssClass(INITIAL_CLASSES);
    BEANS.get(StyleHelper.class).removeCssClass(stylable, "class");
    Assert.assertEquals(INITIAL_FIRST_CLASS + " " + INITIAL_LAST_CLASS, stylable.getCssClass());
  }

  @Test
  public void testToggleCssClass() {
    String newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, "added-class", true);
    Assert.assertEquals(INITIAL_CLASSES + " added-class", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, "added-class", true);
    Assert.assertEquals(INITIAL_CLASSES + " added-class", newClasses);
    newClasses = BEANS.get(StyleHelper.class).toggleCssClass(INITIAL_CLASSES, "added-class", false);
    Assert.assertEquals(INITIAL_CLASSES, newClasses);
  }

  @Test
  public void testToggleCssClassStylable() {
    IStyleable stylable = new Cell();
    stylable.setCssClass(INITIAL_CLASSES);
    BEANS.get(StyleHelper.class).toggleCssClass(stylable, "added-class", true);
    Assert.assertEquals(INITIAL_CLASSES + " added-class", stylable.getCssClass());
    BEANS.get(StyleHelper.class).toggleCssClass(stylable, "added-class", true);
    Assert.assertEquals(INITIAL_CLASSES + " added-class", stylable.getCssClass());
    BEANS.get(StyleHelper.class).toggleCssClass(stylable, "added-class", false);
    Assert.assertEquals(INITIAL_CLASSES, stylable.getCssClass());
  }
}
