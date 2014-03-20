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
