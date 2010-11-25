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

import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class FontRegistry {

  private final Display m_display;
  private HashMap<String, Font> m_fontCache = new HashMap<String, Font>();

  public FontRegistry(Display display) {
    m_display = display;
  }

  public Font getFont(FontSpec scoutFont, Font templateFont) {
    if (scoutFont == null) {
      return templateFont;
    }
    Font font = m_fontCache.get(scoutFont.toPattern());
    if (font == null) {
      String name = scoutFont.getName();
      if (name == null || name.equalsIgnoreCase("null")) {
        if (templateFont != null) {
          name = templateFont.getFontData()[0].getName();
        }
        else {
          name = "Dialog";
        }
      }
      int style = SWT.NORMAL;
      if (scoutFont.isBold()) {
        style = style | SWT.BOLD;
      }
      if (scoutFont.isItalic()) {
        style = style | SWT.ITALIC;
      }
      int size = scoutFont.getSize();
      if (size <= 0) {
        if (templateFont != null) {
          size = templateFont.getFontData()[0].getHeight();
        }
        else {
          size = 11;
        }
      }
      font = new Font(m_display, name, size, style);
      m_fontCache.put(scoutFont.toPattern(), font);
    }
    return font;

  }

  public void dispose() {
    for (Font f : m_fontCache.values()) {
      f.dispose();
    }
    m_fontCache.clear();
  }

}
