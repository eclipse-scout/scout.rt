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
package org.eclipse.scout.rt.ui.swing.internal;

import java.awt.Graphics;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Reflect wrapper to SwingUtilities2 in order to prevent incompatibility issues
 * when upgrading to higher java versions before 1.6
 * com.sun.java.swing.SwingUtilities2 with 1.6 sun.swing.SwingUtilities2
 */
@SuppressWarnings("unchecked")
public final class SwingUtilities2Wrapper {
  private static Class SwingUtilities2Class;
  private static Method drawStringUnderlineCharAtMethod;

  private SwingUtilities2Wrapper() {
  }

  static {
    String jv = System.getProperty("java.version");
    ClassLoader cl = SwingUtilities.class.getClassLoader();
    try {
      if (jv.startsWith("1.6")) {
        SwingUtilities2Class = Class.forName("sun.swing.SwingUtilities2", true, cl);
      }
      else {
        SwingUtilities2Class = Class.forName("com.sun.java.swing.SwingUtilities2", true, cl);
      }
      drawStringUnderlineCharAtMethod = SwingUtilities2Class.getMethod("drawStringUnderlineCharAt", JComponent.class, Graphics.class, String.class, int.class, int.class, int.class);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void drawStringUnderlineCharAt(JComponent c, Graphics g, String text, int underlinedIndex, int x, int y) {
    try {
      drawStringUnderlineCharAtMethod.invoke(null, c, g, text, underlinedIndex, x, y);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
