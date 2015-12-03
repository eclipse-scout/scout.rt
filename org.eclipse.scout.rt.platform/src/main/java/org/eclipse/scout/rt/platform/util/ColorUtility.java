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
package org.eclipse.scout.rt.platform.util;

import java.awt.Color;
import java.util.regex.Pattern;

public final class ColorUtility {

  public static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^(\\#|0X|0x)?([0-9A-Fa-f]{6})$");

  // public constants
  public static final String RED = "ff0000";
  public static final String GREEN = "00ff00";
  public static final String BLUE = "0000ff";
  public static final String BLACK = "000000";
  public static final String WHITE = "ffffff";
  public static final String CYAN = "00ffff";
  public static final String YELLOW = "ffff00";
  public static final String MAGENTA = "ff00ff";

  private ColorUtility() {
  }

  public static int hsb2rgb(int hsbHex) {
    int hue = (hsbHex >> 16) & 0xff;
    int saturation = (hsbHex >> 8) & 0xff;
    int brightness = (hsbHex) & 0xff;
    return hsb2rgb(hue, saturation, brightness);
  }

  public static int hsb2rgb(float hue, float saturation, float brightness) {
    return Color.HSBtoRGB(hue, saturation, brightness);
  }

  /**
   * Converts a color's RGB presentation to a textual hexadecimal representation.
   * <p>
   * Example: r = 255, g = 0, b = 0 --> "#ff0000"
   * <p>
   * Note: the hexadecimal representation is lowercase
   * 
   * @return hexadecimal representation of RGB in lowercase.
   * @since 4.0-M7
   */
  public static String rgbToText(int red, int green, int blue) {
    return String.format("#%02x%02x%02x", red, green, blue);
  }

}
