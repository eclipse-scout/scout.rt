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
package org.eclipse.scout.commons;

import java.awt.Color;

public final class ColorUtility {

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

}
