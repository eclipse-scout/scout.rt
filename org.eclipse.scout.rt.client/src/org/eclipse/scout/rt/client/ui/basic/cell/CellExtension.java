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
 * Rarely used attributes of a {@link Cell} are stored in this class. It references a probably shared instance of a
 * {@link CellStyle} class.
 * <p>
 * <b>Note</b>: Instances of this class are not expected to be shared between multiple {@link Cell}s.
 */
public class CellExtension implements ICellSpecialization {

  private String m_tooltipText;
  private boolean m_enabled;
  private boolean m_editable;
  private CellStyle m_cellStyle;

  public CellExtension(ICellSpecialization specialization) {
    m_enabled = true;
    m_cellStyle = specialization.getCellStyle();
  }

  public ICellSpecialization copy() {
    setCellStyle(getCellStyle().copy());
    return this;
  }

  public ICellSpecialization reconcile(CellStyle cellStyle) {
    setCellStyle(cellStyle);
    return this;
  }

  public String getTooltipText() {
    return m_tooltipText;
  }

  public void setTooltipText(String tooltipText) {
    m_tooltipText = tooltipText;
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  public boolean isEditable() {
    return m_editable;
  }

  public void setEditable(boolean editable) {
    m_editable = editable;
  }

  public CellStyle getCellStyle() {
    return m_cellStyle;
  }

  public void setCellStyle(CellStyle cellStyle) {
    m_cellStyle = cellStyle;
  }

  public String getBackgroundColor() {
    return m_cellStyle.getBackgroundColor();
  }

  public FontSpec getFont() {
    return m_cellStyle.getFont();
  }

  public String getForegroundColor() {
    return m_cellStyle.getForegroundColor();
  }

  public int getHorizontalAlignment() {
    return m_cellStyle.getHorizontalAlignment();
  }

  public String getIconId() {
    return m_cellStyle.getIconId();
  }

  public void setBackgroundColor(String backgroundColor) {
    m_cellStyle.setBackgroundColor(backgroundColor);
  }

  public void setFont(FontSpec font) {
    m_cellStyle.setFont(font);
  }

  public void setForegroundColor(String foregroundColor) {
    m_cellStyle.setForegroundColor(foregroundColor);
  }

  public void setHorizontalAlignment(int horizontalAlignment) {
    m_cellStyle.setHorizontalAlignment(horizontalAlignment);
  }

  public void setIconId(String iconId) {
    m_cellStyle.setIconId(iconId);
  }
}
