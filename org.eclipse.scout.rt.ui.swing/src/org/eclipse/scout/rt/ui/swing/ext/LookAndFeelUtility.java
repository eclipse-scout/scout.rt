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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Provides utility methods which may be used by a L&F and the Swing ext components.
 */
public final class LookAndFeelUtility {

  private LookAndFeelUtility() {
  }

  /**
   * Makes a color translucent by changing its alpha value.
   * 
   * @param color
   *          Color to make translucent
   * @param ratio
   *          Translucent ratio (0 = translucent, 100 = opaque)
   * @return Color with new alpha value
   */
  public static Color translucent(Color color, int ratio) {
    int alpha = (int) Math.round(ratio * 2.5);
    if (alpha < 0) {
      alpha = 0;
    }
    else if (alpha > 255) {
      alpha = 255;
    }
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
  }

  /**
   * Draws a focus border.
   * 
   * @param g
   * @param x
   * @param y
   * @param w
   * @param h
   */
  public static void drawFocus(Graphics g, int x, int y, int w, int h) {
    Graphics2D g2d = (Graphics2D) g;
    Stroke old = g2d.getStroke();
    g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[]{1f, 2f}, 1));
    g2d.drawLine(x, y, x + w - 1, y);
    g2d.drawLine(x, y + h, x + w - 1, y + h);
    g2d.drawLine(x, y, x, y + h - 1);
    g2d.drawLine(x + w, y, x + w, y + h - 1);
    g2d.setStroke(old);
  }

}
