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
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Color;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class ColorUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ColorUtility.class);

  private ColorUtility() {
  }

  /**
   * Make a color darker.
   * 
   * @param color
   *          Color to mix with black.
   * @param ratio
   *          Black ratio (1.0 = complete black, 0.0 = color).
   * @return Darker color.
   */
  public static Color darker(Color color, float ratio) {
    return mergeColors(Color.BLACK, ratio, color, 1 - ratio);
  }

  /**
   * Make a color lighter.
   * 
   * @param color
   *          Color to mix with white.
   * @param ratio
   *          White ratio (1.0 = complete white, 0.0 = color).
   * @return Lighter color.
   */
  public static Color lighter(Color color, float ratio) {
    return mergeColors(Color.WHITE, ratio, color, 1 - ratio);
  }

  /**
   * Merges two colors. The two floating point arguments specify "how much" of the corresponding color is added to the
   * resulting color. Both arguments should (but don't have to) add to <code>1.0</code>.
   * <p>
   * This method is null-safe. If one of the given colors is <code>null</code>, the other color is returned (unchanged).
   */
  public static Color mergeColors(Color a, float fa, Color b, float fb) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    return new Color(
        (fa * a.getRed() + fb * b.getRed()) / (fa + fb) / 255f,
        (fa * a.getGreen() + fb * b.getGreen()) / (fa + fb) / 255f,
        (fa * a.getBlue() + fb * b.getBlue()) / (fa + fb) / 255f);
  }

  /**
   * Multiplies two colors.
   * <p>
   * For each RGB component, the resulting value is calculated as <code>(max(a, b) / 255) * min(a, b)</code> (where
   * <code>a</code> and <code>b</code> are the corresponding RGB components of color 1 or color 2, respectively.
   * Multiplying a color with white will return the color unchanged. Multiplying a color with black will return black.
   * <p>
   * This method is null-safe. If one of the given colors is <code>null</code>, the other color is returned (unchanged).
   */
  public static Color multiplyColors(Color color1, Color color2) {
    if (color1 == null) {
      return color2;
    }
    if (color2 == null) {
      return color1;
    }
    int redHigh = Math.max(color1.getRed(), color2.getRed());
    int redLow = Math.min(color1.getRed(), color2.getRed());
    int greenHigh = Math.max(color1.getGreen(), color2.getGreen());
    int greenLow = Math.min(color1.getGreen(), color2.getGreen());
    int blueHigh = Math.max(color1.getBlue(), color2.getBlue());
    int blueLow = Math.min(color1.getBlue(), color2.getBlue());
    return new Color(
        Math.round(((float) redHigh / 255f) * (float) redLow),
        Math.round(((float) greenHigh / 255f) * (float) greenLow),
        Math.round(((float) blueHigh / 255f) * (float) blueLow));
  }

  /**
   * Create the color from the given string
   */
  public static Color createColor(String c) {
    if (StringUtility.isNullOrEmpty(c)) {
      return null;
    }
    c = c.replaceAll("^(0x|0X|#)", "");
    try {
      return new Color(Integer.parseInt(c, 16));
    }
    catch (NumberFormatException nfe) {
      LOG.warn("invalid color code: " + c, nfe);
      return null;
    }
  }

  /**
   * Converts a {@link Color} to a hexadecimal representation.
   * <p>
   * Example: Color.RED --> "#ff0000"
   * <p>
   * Note: the hexadecimal representation is lowercase
   * 
   * @return hexadecimal representation of {@link Color} in lowercase. Returns <code>null</code> if parameter is
   *         <code>null</code>
   * @since 3.10.0-M5
   */
  public static String createStringFromColor(Color c) {
    if (c == null) {
      return null;
    }
    return org.eclipse.scout.commons.ColorUtility.rgbToText(c.getRed(), c.getGreen(), c.getBlue());
  }

}
