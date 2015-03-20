/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.window;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.scout.rt.ui.swing.ext.JRootPaneEx;

/**
 * Test methods for {@link ISwingScoutView}
 */
public abstract class AbstractSwingScoutViewTest {

  /**
   * After a visibility change {@link JRootPaneEx#notifyVisibleChanged(java.awt.Component)} is called, which is
   * triggering reflow. <br>
   * To test synchronously, we call reflow directly.
   */
  protected void reflow(JRootPaneEx rootPane) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method reflowMethod = rootPane.getClass().getDeclaredMethod("reflow", (Class[]) null);
    reflowMethod.setAccessible(true);
    reflowMethod.invoke(rootPane, new Object[]{});
  }

  /**
   * @return some test dimensions larger than screen size
   */
  protected Dimension getLargeTestDimensions() {
    final int manyPixels = 10000000;
    return new Dimension(manyPixels, manyPixels);
  }

}
