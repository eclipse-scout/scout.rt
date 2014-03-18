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
package org.eclipse.scout.rt.ui.swt.basic;

import java.util.regex.Matcher;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for Colors in SWT
 */
public class ColorUtility {
  /**
   * Converts a {@link Color} to a hexadecimal representation.
   * <p>
   * Example: Color.RED --> "#ff0000"
   * <p>
   * Note: the hexadecimal representation is lowercase
   * 
   * @return hexadecimal representation of {@link Color} in lowercase. Returns <code>null</code> if parameter is
   *         <code>null</code>
   * @since 4.0-M7
   */
  public static String createStringFromColor(Color c) {
    if (c == null) {
      return null;
    }
    return org.eclipse.scout.commons.ColorUtility.rgbToText(c.getRed(), c.getGreen(), c.getBlue());
  }

  public static Color createColor(Display display, String hex) {
    RGB rgb = toRGB(hex);
    if (rgb != null) {
      return new Color(display, rgb);
    }
    return null;
  }

  public static RGB toRGB(String hex) {
    if (StringUtility.isNullOrEmpty(hex)) {
      return null;
    }
    Matcher matcher = org.eclipse.scout.commons.ColorUtility.HEX_COLOR_PATTERN.matcher(hex);
    if (matcher.matches()) {
      hex = matcher.group(2);
      int red = Integer.parseInt(hex.substring(0, 2), 16);
      int green = Integer.parseInt(hex.substring(2, 4), 16);
      int blue = Integer.parseInt(hex.substring(4, 6), 16);
      return new RGB(red, green, blue);

    }
    else {
      throw new IllegalArgumentException("Input '" + hex + "' must be a hex number with 6 digits. ");
    }
  }
}
