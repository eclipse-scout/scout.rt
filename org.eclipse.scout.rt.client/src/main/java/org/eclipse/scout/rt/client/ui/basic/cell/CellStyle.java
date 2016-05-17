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
package org.eclipse.scout.rt.client.ui.basic.cell;

import org.eclipse.scout.rt.shared.data.basic.FontSpec;

/**
 * A cell style represents the graphical properties of a particular cell. These values are kept in a different class so
 * that they can be shared between {@link Cell} instances.
 */
public class CellStyle implements ICellSpecialization {

  private String m_cssClass;
  private String m_iconId;
  private String m_backgroundColor;
  private String m_foregroundColor;
  private FontSpec m_fontSpec;
  private int m_horizontalAlignment;
  private boolean m_htmlEnabled;

  public CellStyle() {
    m_horizontalAlignment = -1;
  }

  public CellStyle(CellStyle cell) {
    setCssClass(cell.getCssClass());
    setIconId(cell.getIconId());
    setBackgroundColor(cell.getBackgroundColor());
    setForegroundColor(cell.getForegroundColor());
    setFont(cell.getFont());
    setHorizontalAlignment(cell.getHorizontalAlignment());
    setHtmlEnabled(cell.isHtmlEnabled());
  }

  @Override
  public CellStyle copy() {
    return new CellStyle(this);
  }

  @Override
  public void setEnabled(boolean enabled) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTooltipText(String tooltip) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEditable(boolean editable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getTooltipText() {
    return null;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  @Override
  public boolean isHtmlEnabled() {
    return m_htmlEnabled;
  }

  @Override
  public void setHtmlEnabled(boolean enabled) {
    m_htmlEnabled = enabled;
  }

  @Override
  public CellStyle getCellStyle() {
    return this;
  }

  @Override
  public ICellSpecialization reconcile(CellStyle cellStyle) {
    return cellStyle;
  }

  @Override
  public String getCssClass() {
    return m_cssClass;
  }

  @Override
  public void setCssClass(String cssClass) {
    m_cssClass = cssClass;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public void setIconId(String iconId) {
    m_iconId = iconId;
  }

  @Override
  public String getBackgroundColor() {
    return m_backgroundColor;
  }

  @Override
  public void setBackgroundColor(String backgroundColor) {
    m_backgroundColor = backgroundColor;
  }

  @Override
  public String getForegroundColor() {
    return m_foregroundColor;
  }

  @Override
  public void setForegroundColor(String foregroundColor) {
    m_foregroundColor = foregroundColor;
  }

  @Override
  public FontSpec getFont() {
    return m_fontSpec;
  }

  @Override
  public void setFont(FontSpec font) {
    m_fontSpec = font;
  }

  @Override
  public int getHorizontalAlignment() {
    return m_horizontalAlignment;
  }

  @Override
  public void setHorizontalAlignment(int horizontalAlignment) {
    m_horizontalAlignment = horizontalAlignment;
  }

  @Override
  public boolean isMandatory() {
    return false;
  }

  @Override
  public void setMandatory(boolean mandatory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_backgroundColor == null) ? 0 : m_backgroundColor.hashCode());
    result = prime * result + ((m_cssClass == null) ? 0 : m_cssClass.hashCode());
    result = prime * result + ((m_fontSpec == null) ? 0 : m_fontSpec.hashCode());
    result = prime * result + ((m_foregroundColor == null) ? 0 : m_foregroundColor.hashCode());
    result = prime * result + m_horizontalAlignment;
    result = prime * result + (m_htmlEnabled ? 1231 : 1237);
    result = prime * result + ((m_iconId == null) ? 0 : m_iconId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CellStyle other = (CellStyle) obj;
    if (m_backgroundColor == null) {
      if (other.m_backgroundColor != null) {
        return false;
      }
    }
    else if (!m_backgroundColor.equals(other.m_backgroundColor)) {
      return false;
    }
    if (m_cssClass == null) {
      if (other.m_cssClass != null) {
        return false;
      }
    }
    else if (!m_cssClass.equals(other.m_cssClass)) {
      return false;
    }
    if (m_fontSpec == null) {
      if (other.m_fontSpec != null) {
        return false;
      }
    }
    else if (!m_fontSpec.equals(other.m_fontSpec)) {
      return false;
    }
    if (m_foregroundColor == null) {
      if (other.m_foregroundColor != null) {
        return false;
      }
    }
    else if (!m_foregroundColor.equals(other.m_foregroundColor)) {
      return false;
    }
    if (m_horizontalAlignment != other.m_horizontalAlignment) {
      return false;
    }
    if (m_htmlEnabled != other.m_htmlEnabled) {
      return false;
    }
    if (m_iconId == null) {
      if (other.m_iconId != null) {
        return false;
      }
    }
    else if (!m_iconId.equals(other.m_iconId)) {
      return false;
    }
    return true;
  }
}
