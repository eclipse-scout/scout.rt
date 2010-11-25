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
/*
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA  02111-1307, USA.
 */
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Provides utility methods which may be used by a L&F and the Swing ext components.
 * This code is free software from http://geosoft.no/software/colorutil/ColorUtil.java.html
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
    if (alpha < 0) alpha = 0;
    else if (alpha > 255) alpha = 255;
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

  /**
   * Make a color darker.
   * 
   * @param color
   *          Color to mix with black.
   * @param fraction
   *          Black ratio (1.0 = complete black, 0.0 = color).
   * @return Darker color.
   */
  public static Color darker(Color color, double ratio) {
    return blend(Color.BLACK, color, ratio);
  }

  /**
   * Make a color lighter.
   * 
   * @param color
   *          Color to mix with white.
   * @param fraction
   *          White ratio (1.0 = complete white, 0.0 = color).
   * @return Lighter color.
   */
  public static Color lighter(Color color, double ratio) {
    return blend(Color.WHITE, color, ratio);
  }

  /**
   * Make an even blend between two colors.
   * 
   * @param c1
   *          First color to blend.
   * @param c2
   *          Second color to blend.
   * @return Blended color.
   */
  public static Color blend(Color color1, Color color2) {
    return blend(color1, color2, 0.5);
  }

  /**
   * Blend two colors.
   * 
   * @param color1
   *          First color to blend.
   * @param color2
   *          Second color to blend.
   * @param ratio
   *          Blend ratio. 0.5 will give even blend, 1.0 will return color1, 0.0
   *          will return color2 and so on.
   * @return Blended color.
   */
  public static Color blend(Color color1, Color color2, double ratio) {
    float r = (float) ratio;
    float ir = (float) 1.0 - r;

    float[] rgb1 = new float[3];
    float[] rgb2 = new float[3];

    color1.getColorComponents(rgb1);
    color2.getColorComponents(rgb2);

    Color color = new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);

    return color;
  }
}
