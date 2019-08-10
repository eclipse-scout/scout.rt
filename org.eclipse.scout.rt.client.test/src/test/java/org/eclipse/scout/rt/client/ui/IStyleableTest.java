/*
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class IStyleableTest {

  public static final String CLASS = "class";
  public static final String INITIAL_FIRST_CLASS = "first-class";
  public static final String INITIAL_LAST_CLASS = "last-class";
  public static final String INITIAL_CLASSES = INITIAL_FIRST_CLASS + " " + CLASS + " " + INITIAL_LAST_CLASS;
  public static final String ADDED_CLASS = "added-class";

  @Test
  public void testAddCssClassStylable() {
    IStyleable stylable = new Cell();
    stylable.setCssClass(INITIAL_CLASSES);
    stylable.addCssClass(ADDED_CLASS);
    Assert.assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, stylable.getCssClass());
  }

  @Test
  public void testRemoveCssClassStylable() {
    IStyleable stylable = new Cell();
    stylable.setCssClass(INITIAL_CLASSES);
    stylable.removeCssClass(CLASS);
    Assert.assertEquals(INITIAL_FIRST_CLASS + " " + INITIAL_LAST_CLASS, stylable.getCssClass());
  }

  @Test
  public void testToggleCssClassStylable() {
    IStyleable stylable = new Cell();
    stylable.setCssClass(INITIAL_CLASSES);
    stylable.toggleCssClass(ADDED_CLASS, true);
    Assert.assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, stylable.getCssClass());
    stylable.toggleCssClass(ADDED_CLASS, true);
    Assert.assertEquals(INITIAL_CLASSES + " " + ADDED_CLASS, stylable.getCssClass());
    stylable.toggleCssClass(ADDED_CLASS, false);
    Assert.assertEquals(INITIAL_CLASSES, stylable.getCssClass());
  }
}
