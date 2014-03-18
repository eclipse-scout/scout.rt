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
package org.eclipse.scout.rt.ui.swt.util;

import java.util.HashMap;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.basic.ColorUtility;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ColorFactory.class);
  private final Display m_display;
  private HashMap<String, Color> m_colorCache = new HashMap<String, Color>();

  public ColorFactory(Display display) {
    m_display = display;
  }

  public Color getColor(RGB rgb) {
    int iRgb = ((rgb.red & 0xFF) << 16) | ((rgb.green & 0xFF) << 8) | ((rgb.blue & 0xFF) << 0);
    String hexString = Integer.toHexString(iRgb);
    while (hexString.length() < 6) {
      hexString = "0" + hexString;
    }
    return getColor(hexString);
  }

  public Color getColor(String scoutColor) {
    if (scoutColor == null || scoutColor.length() != 6) {
      return null;
    }
    Color c = m_colorCache.get(scoutColor);
    if (c == null) {
      c = createColorFromScout(scoutColor);
      m_colorCache.put(scoutColor, c);
    }
    return c;
  }

  private Color createColorFromScout(String scoutColor) {
    if (scoutColor == null) {
      return null;
    }
    RGB rgb = ColorUtility.toRGB(scoutColor);
    return new Color(m_display, rgb);
  }

  public void dispose() {
    for (Color c : m_colorCache.values()) {
      c.dispose();
    }
    m_colorCache.clear();

  }
}
