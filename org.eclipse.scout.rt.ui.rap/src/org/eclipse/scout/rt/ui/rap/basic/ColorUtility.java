package org.eclipse.scout.rt.ui.rap.basic;

import org.eclipse.swt.graphics.Color;

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
}
