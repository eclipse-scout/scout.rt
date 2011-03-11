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

public final class ColorUtility {

  private ColorUtility() {
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

}
