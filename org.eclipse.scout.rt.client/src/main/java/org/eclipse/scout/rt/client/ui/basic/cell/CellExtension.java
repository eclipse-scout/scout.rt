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
 * Rarely used attributes of a {@link Cell} are stored in this class. It references a probably shared instance of a
 * {@link CellStyle} class.
 * <p>
 * <b>Note</b>: Instances of this class are not expected to be shared between multiple {@link Cell}s.
 */
public class CellExtension implements ICellSpecialization {

  private String m_tooltipText;
  private boolean m_enabled;
  private boolean m_editable;
  private boolean m_mandatory;
  private CellStyle m_cellStyle;

  public CellExtension(ICellSpecialization specialization) {
    m_enabled = true;
    m_cellStyle = specialization.getCellStyle();
  }

  @Override
  public ICellSpecialization copy() {
    setCellStyle(getCellStyle().copy());
    return this;
  }

  @Override
  public ICellSpecialization reconcile(CellStyle cellStyle) {
    setCellStyle(cellStyle);
    return this;
  }

  @Override
  public String getCssClass() {
    return m_cellStyle.getCssClass();
  }

  @Override
  public void setCssClass(String cssClass) {
    m_cellStyle.setCssClass(cssClass);
  }

  @Override
  public String getTooltipText() {
    return m_tooltipText;
  }

  @Override
  public void setTooltipText(String tooltipText) {
    m_tooltipText = tooltipText;
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  @Override
  public boolean isEditable() {
    return m_editable;
  }

  @Override
  public void setEditable(boolean editable) {
    m_editable = editable;
  }

  @Override
  public CellStyle getCellStyle() {
    return m_cellStyle;
  }

  public void setCellStyle(CellStyle cellStyle) {
    m_cellStyle = cellStyle;
  }

  @Override
  public String getBackgroundColor() {
    return m_cellStyle.getBackgroundColor();
  }

  @Override
  public FontSpec getFont() {
    return m_cellStyle.getFont();
  }

  @Override
  public String getForegroundColor() {
    return m_cellStyle.getForegroundColor();
  }

  @Override
  public int getHorizontalAlignment() {
    return m_cellStyle.getHorizontalAlignment();
  }

  @Override
  public String getIconId() {
    return m_cellStyle.getIconId();
  }

  @Override
  public void setBackgroundColor(String backgroundColor) {
    m_cellStyle.setBackgroundColor(backgroundColor);
  }

  @Override
  public void setFont(FontSpec font) {
    m_cellStyle.setFont(font);
  }

  @Override
  public void setForegroundColor(String foregroundColor) {
    m_cellStyle.setForegroundColor(foregroundColor);
  }

  @Override
  public void setHorizontalAlignment(int horizontalAlignment) {
    m_cellStyle.setHorizontalAlignment(horizontalAlignment);
  }

  @Override
  public void setIconId(String iconId) {
    m_cellStyle.setIconId(iconId);
  }

  @Override
  public void setHtmlEnabled(boolean enabled) {
    m_cellStyle.setHtmlEnabled(enabled);
  }

  @Override
  public boolean isHtmlEnabled() {
    return m_cellStyle.isHtmlEnabled();
  }

  @Override
  public boolean isMandatory() {
    return m_mandatory;
  }

  @Override
  public void setMandatory(boolean mandatory) {
    m_mandatory = mandatory;
  }

}
