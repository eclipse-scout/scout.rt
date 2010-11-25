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
package org.eclipse.scout.rt.client.ui.basic.cell;

import org.eclipse.scout.rt.shared.data.basic.FontSpec;

/**
 * A cell style represents the graphical properties of a particular cell. These values are kept in a
 * different class so that can be shared between {@link Cell} instances.
 */
public class CellStyle implements ICellSpecialization {

  private String m_iconId;
  private String m_backgroundColor;
  private String m_foregroundColor;
  private String m_fontPattern;
  private int m_horizontalAlignment;

  public CellStyle() {
    m_horizontalAlignment = -1;
  }

  public CellStyle(CellStyle cell) {
    setIconId(cell.getIconId());
    setBackgroundColor(cell.getBackgroundColor());
    setForegroundColor(cell.getForegroundColor());
    setFont(cell.getFont());
    setHorizontalAlignment(cell.getHorizontalAlignment());
  }

  public CellStyle copy() {
    return new CellStyle(this);
  }

  public void setEnabled(boolean enabled) {
    throw new UnsupportedOperationException();
  }

  public void setTooltipText(String tooltip) {
    throw new UnsupportedOperationException();
  }

  public String getTooltipText() {
    return null;
  }

  public boolean isEnabled() {
    return true;
  }

  public CellStyle getCellStyle() {
    return this;
  }

  public ICellSpecialization reconcile(CellStyle cellStyle) {
    return cellStyle;
  }

  public String getIconId() {
    return m_iconId;
  }

  public void setIconId(String iconId) {
    m_iconId = iconId;
  }

  public String getBackgroundColor() {
    return m_backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    m_backgroundColor = backgroundColor;
  }

  public String getForegroundColor() {
    return m_foregroundColor;
  }

  public void setForegroundColor(String foregroundColor) {
    m_foregroundColor = foregroundColor;
  }

  public FontSpec getFont() {
    return m_fontPattern != null ? FontSpec.parse(m_fontPattern) : null;
  }

  public void setFont(FontSpec font) {
    m_fontPattern = font != null ? font.toPattern() : null;
  }

  public int getHorizontalAlignment() {
    return m_horizontalAlignment;
  }

  public void setHorizontalAlignment(int horizontalAlignment) {
    m_horizontalAlignment = horizontalAlignment;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_backgroundColor == null) ? 0 : m_backgroundColor.hashCode());
    result = prime * result + ((m_fontPattern == null) ? 0 : m_fontPattern.hashCode());
    result = prime * result + ((m_foregroundColor == null) ? 0 : m_foregroundColor.hashCode());
    result = prime * result + m_horizontalAlignment;
    result = prime * result + ((m_iconId == null) ? 0 : m_iconId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CellStyle other = (CellStyle) obj;
    if (m_backgroundColor == null) {
      if (other.m_backgroundColor != null) return false;
    }
    else if (!m_backgroundColor.equals(other.m_backgroundColor)) return false;
    if (m_fontPattern == null) {
      if (other.m_fontPattern != null) return false;
    }
    else if (!m_fontPattern.equals(other.m_fontPattern)) return false;
    if (m_foregroundColor == null) {
      if (other.m_foregroundColor != null) return false;
    }
    else if (!m_foregroundColor.equals(other.m_foregroundColor)) return false;
    if (m_horizontalAlignment != other.m_horizontalAlignment) return false;
    if (m_iconId == null) {
      if (other.m_iconId != null) return false;
    }
    else if (!m_iconId.equals(other.m_iconId)) return false;
    return true;
  }
}
