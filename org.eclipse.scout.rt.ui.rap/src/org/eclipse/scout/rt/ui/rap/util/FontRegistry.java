/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.util;

import java.util.HashMap;

import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FontRegistry {

  private final Display m_display;
  private HashMap<String, Font> m_fontCache = new HashMap<String, Font>();

  public FontRegistry(Display display) {
    m_display = display;
  }

  /**
   * @return a font based on templateFont with style, name and size from scoutFont (if not null).
   *         The result is cached for re-use. Dispose is done automatically and must not be done by the caller
   */
  public Font getFont(FontSpec scoutFont, Font templateFont) {
    if (scoutFont == null) {
      return templateFont;
    }
    return getFont(templateFont, scoutFont.getName(), scoutToSwtStyle(scoutFont.getStyle()), scoutFont.getSize() > 0 ? scoutFont.getSize() : null);
  }

  /**
   * @return a font based on templateFont with different style, name and size (if not null).
   *         The result is cached for re-use. Dispose is done automatically and must not be done by the caller
   */
  public Font getFont(Font templateFont, String newName, Integer newStyle, Integer newSize) {
    if (newName == null && newStyle == null && newSize == null) {
      return templateFont;
    }
    FontData[] fds = templateFont.getFontData();
    FontData fd = (fds != null && fds.length > 0 ? fds[0] : null);
    if (newName == null || newName.equalsIgnoreCase("null")) {
      if (fd != null) {
        newName = fd.getName();
      }
      else {
        newName = "Dialog";
      }
    }
    if (newStyle == null) {
      newStyle = fd != null ? fd.getStyle() : SWT.NORMAL;
    }
    if (newSize == null || newSize.intValue() <= 0) {
      newSize = fd != null ? fd.getHeight() : 11;
    }
    //
    String cacheKey = newName + "_" + newSize + "_" + newStyle;
    Font font = m_fontCache.get(cacheKey);
    if (font == null) {
      font = new Font(m_display, newName, newSize, newStyle);
      m_fontCache.put(cacheKey, font);
    }
    return font;
  }

  public static int scoutToSwtStyle(int scoutStyle) {
    int style = SWT.NORMAL;
    if ((scoutStyle & FontSpec.STYLE_BOLD) != 0) {
      style = style | SWT.BOLD;
    }
    if ((scoutStyle & FontSpec.STYLE_ITALIC) != 0) {
      style = style | SWT.ITALIC;
    }
    return style;
  }

  public void dispose() {
    for (Font f : m_fontCache.values()) {
      if (f != null && !f.isDisposed() && f.getDevice() != null) {
        f.dispose();
      }
    }
    m_fontCache.clear();
  }

}
