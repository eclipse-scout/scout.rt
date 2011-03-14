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
package org.eclipse.scout.rt.shared.data.basic;

import java.util.StringTokenizer;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class FontSpec implements java.io.Serializable {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FontSpec.class);
  public static final int STYLE_PLAIN = 0x00;
  public static final int STYLE_BOLD = 0x01;
  public static final int STYLE_ITALIC = 0x02;

  private String m_name;
  private int m_style;
  private int m_size;

  private FontSpec() {
  }

  public FontSpec(String name, int style, int size) {
    m_name = name;
    m_style = style;
    m_size = size;
  }

  public String toPattern() {
    return getName() + (isPlain() ? "-PLAIN" : "") + (isBold() ? "-BOLD" : "") + (isItalic() ? "-ITALIC" : "") + "-" + getSize();
  }

  public String getName() {
    return m_name;
  }

  public int getStyle() {
    return m_style;
  }

  public boolean isPlain() {
    return m_style == STYLE_PLAIN;
  }

  public boolean isBold() {
    return (m_style & STYLE_BOLD) != 0;
  }

  public boolean isItalic() {
    return (m_style & STYLE_ITALIC) != 0;
  }

  public int getSize() {
    return m_size;
  }

  // derivatives
  public FontSpec getPlainCopy() {
    return new FontSpec(m_name, STYLE_PLAIN, m_size);
  }

  public FontSpec getBoldCopy() {
    return new FontSpec(m_name, m_style | STYLE_BOLD, m_size);
  }

  public FontSpec getItalicCopy() {
    return new FontSpec(m_name, m_style | STYLE_ITALIC, m_size);
  }

  /**
   * Sample: Dialog-PLAIN-12
   */
  public static FontSpec parse(String pattern) {
    if (pattern == null || pattern.length() == 0) {
      return null;
    }
    else {
      FontSpec f = new FontSpec();
      StringTokenizer tok = new StringTokenizer(pattern, " -_,/.;");
      while (tok.hasMoreTokens()) {
        String s = tok.nextToken().toUpperCase();
        // styles
        if (s.equals("PLAIN")) {
          // nop
        }
        else if (s.equals("BOLD")) {
          f.m_style = f.m_style | STYLE_BOLD;
        }
        else if (s.equals("ITALIC")) {
          f.m_style = f.m_style | STYLE_ITALIC;
        }
        else {
          // size or name
          try {
            // size
            f.m_size = Integer.parseInt(s);
          }
          catch (NumberFormatException nfe) {
            // name
            f.m_name = s;
          }
        }
      }
      //if name is "null" set it to null
      if (f.m_name != null && f.m_name.equalsIgnoreCase("null")) {
        f.m_name = null;
      }
      return f;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    result = prime * result + m_size;
    result = prime * result + m_style;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    FontSpec other = (FontSpec) obj;
    if (m_name == null) {
      if (other.m_name != null) return false;
    }
    else if (!m_name.equals(other.m_name)) return false;
    if (m_size != other.m_size) return false;
    if (m_style != other.m_style) return false;
    return true;
  }
}
