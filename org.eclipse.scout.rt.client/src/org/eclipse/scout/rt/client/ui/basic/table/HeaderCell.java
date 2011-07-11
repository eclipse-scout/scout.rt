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
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public class HeaderCell implements IHeaderCell {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HeaderCell.class);

  private int m_columnIndex = -1;
  private boolean m_sortAscending;
  private boolean m_sortActive;
  private boolean m_sortPermanent;
  private boolean m_sortExplicit;
  private String m_iconId;
  private String m_text;
  private String m_tooltip;
  private int m_horizontalAlignment = -1;
  private String m_foregroundColor;
  private String m_backgroundColor;
  private FontSpec m_font;

  public HeaderCell() {
  }

  @Override
  public int getColumnIndex() {
    return m_columnIndex;
  }

  /**
   * do not use this internal method
   */
  public void setColumnIndexInternal(int index) {
    if (m_columnIndex < 0) {
      m_columnIndex = index;
    }
    else {
      LOG.warn(null, new IllegalAccessException("do not use this internal method"));
    }
  }

  @Override
  public boolean isSortAscending() {
    return m_sortAscending;
  }

  public void setSortAscending(boolean b) {
    m_sortAscending = b;
  }

  @Override
  public String getBackgroundColor() {
    return m_backgroundColor;
  }

  public void setBackgroundColor(String c) {
    m_backgroundColor = c;
  }

  @Override
  public FontSpec getFont() {
    return m_font;
  }

  public void setFont(FontSpec f) {
    m_font = f;
  }

  @Override
  public String getForegroundColor() {
    return m_foregroundColor;
  }

  public void setForegroundColor(String c) {
    m_foregroundColor = c;
  }

  @Override
  public int getHorizontalAlignment() {
    return m_horizontalAlignment;
  }

  public void setHorizontalAlignment(int a) {
    m_horizontalAlignment = a;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  public void setIconId(String id) {
    m_iconId = id;
  }

  @Override
  public String getText() {
    return m_text;
  }

  /**
   * this method is not in interface, use {@link #IColumn.decorateHeaderCell()} and
   * {@link #IColumn.execDecorateHeaderCell()}
   */
  public void setText(String s) {
    m_text = s;
  }

  @Override
  public String getTooltipText() {
    return m_tooltip;
  }

  public void setTooltipText(String s) {
    m_tooltip = s;
  }

  @Override
  public boolean isSortActive() {
    return m_sortActive;
  }

  public void setSortActive(boolean b) {
    m_sortActive = b;
  }

  @Override
  public boolean isSortPermanent() {
    return m_sortPermanent;
  }

  public void setSortPermanent(boolean b) {
    m_sortPermanent = b;
  }

  @Override
  public boolean isSortExplicit() {
    return m_sortExplicit;
  }

  public void setSortExplicit(boolean b) {
    m_sortExplicit = b;
  }

}
