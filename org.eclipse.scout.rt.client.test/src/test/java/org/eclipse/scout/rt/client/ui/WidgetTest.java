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
  public void testInitConfig() {
    Widget widget = new Widget();
    assertEquals(1, widget.initConfigCalls);
    assertEquals(true, widget.isInitConfigDone());
  }

  @Test
  public void testPostInitConfig() {
    Widget widget = new Widget();
    assertEquals(0, widget.postInitConfigCalls);
    assertEquals(false, widget.isPostInitConfigDone());

    widget.postInitConfig();
    assertEquals(1, widget.postInitConfigCalls);
    assertEquals(true, widget.isPostInitConfigDone());

    // Does not execute postInitConfig again
    widget.postInitConfig();
    assertEquals(1, widget.postInitConfigCalls);
    assertEquals(true, widget.isPostInitConfigDone());
  }

  @Test
  public void testInit() {
    Widget widget = new Widget();
    assertEquals(0, widget.initCalls);
    assertEquals(false, widget.isInitDone());

    widget.init();
    assertEquals(1, widget.initCalls);
    assertEquals(true, widget.isInitDone());

    // Does not execute init again
    widget.init();
    assertEquals(1, widget.initCalls);
    assertEquals(true, widget.isInitDone());
  }

  @Test
  public void testDispose() {
    Widget widget = new Widget();
    assertEquals(0, widget.disposeCalls);
    assertEquals(false, widget.isDisposeDone());

    widget.init();
    assertEquals(0, widget.disposeCalls);
    assertEquals(false, widget.isDisposeDone());

    widget.dispose();
    assertEquals(1, widget.disposeCalls);
    assertEquals(true, widget.isDisposeDone());

    // Does not execute dispose again
    widget.dispose();
    assertEquals(1, widget.disposeCalls);
    assertEquals(true, widget.isDisposeDone());
  }

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

    public int initCalls = 0;
    public int initConfigCalls;
    public int postInitConfigCalls = 0;
    public int disposeCalls = 0;

    @Override
    protected void initConfigInternal() {
      super.initConfigInternal();
      initConfigCalls++;
    }

    @Override
    protected void postInitConfigInternal() {
      super.postInitConfigInternal();
      postInitConfigCalls++;
    }

    @Override
    protected void initInternal() {
      super.initInternal();
      initCalls++;
    }

    @Override
    protected void disposeInternal() {
      disposeCalls++;
      super.disposeInternal();
    }
  }
}
