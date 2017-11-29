/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WidgetTest {

  @Test
  public void testAddCssClass() {
    IWidget widget = new Widget();
    widget.addCssClass("custom-class");
    assertEquals("custom-class", widget.getCssClass());

    widget.addCssClass("another-class1 another-class2");
    assertEquals("custom-class another-class1 another-class2", widget.getCssClass());

    // Does not add the same class twice
    widget.addCssClass("another-class1");
    assertEquals("custom-class another-class1 another-class2", widget.getCssClass());

    widget.addCssClass("another-class2 another-class1");
    assertEquals("custom-class another-class1 another-class2", widget.getCssClass());
  }

  @Test
  public void testRemoveCssClass() {
    IWidget widget = new Widget();
    widget.setCssClass("cls1 cls2 cls3");
    assertEquals("cls1 cls2 cls3", widget.getCssClass());

    widget.removeCssClass("cls2");
    assertEquals("cls1 cls3", widget.getCssClass());

    widget.removeCssClass("cls3 cls1");
    assertEquals("", widget.getCssClass());
  }

  protected class Widget extends AbstractWidget {

  }
}
