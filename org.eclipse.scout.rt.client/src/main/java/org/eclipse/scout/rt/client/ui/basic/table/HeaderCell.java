/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderCell extends AbstractPropertyObserver implements IHeaderCell {
  private static final Logger LOG = LoggerFactory.getLogger(HeaderCell.class);

  public HeaderCell() {
    doSetColumnIndex(-1);
    setHorizontalAlignment(-1);
  }

  @Override
  public int getColumnIndex() {
    return propertySupport.getPropertyInt(PROP_COLUMN_INDEX);
  }

  /**
   * do not use this internal method
   */
  public void setColumnIndexInternal(int index) {
    int currentColIndex = getColumnIndex();
    if (currentColIndex < 0) {
      doSetColumnIndex(index);
    }
    else {
      LOG.warn(null, new IllegalAccessException("do not use this internal method"));
    }
  }

  protected void doSetColumnIndex(int index) {
    propertySupport.setPropertyInt(PROP_COLUMN_INDEX, index);
  }

  @Override
  public boolean isSortAscending() {
    return propertySupport.getPropertyBool(PROP_SORT_ASC);
  }

  @Override
  public boolean setSortAscending(boolean sortAscending) {
    return propertySupport.setPropertyBool(PROP_SORT_ASC, sortAscending);
  }

  @Override
  public String getCssClass() {
    return propertySupport.getPropertyString(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    propertySupport.setPropertyString(PROP_CSS_CLASS, cssClass);
  }

  @Override
  public boolean isHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_HTML_ENABLED);
  }

  @Override
  public boolean setHtmlEnabled(boolean htmlEnabled) {
    return propertySupport.setPropertyBool(PROP_HTML_ENABLED, htmlEnabled);
  }

  @Override
  public String getBackgroundColor() {
    return propertySupport.getPropertyString(PROP_BACKGROUND_COLOR);
  }

  @Override
  public boolean setBackgroundColor(String backgroundColor) {
    return propertySupport.setPropertyString(PROP_BACKGROUND_COLOR, backgroundColor);
  }

  @Override
  public FontSpec getFont() {
    return propertySupport.getProperty(PROP_FONT, FontSpec.class);
  }

  @Override
  public boolean setFont(FontSpec font) {
    return propertySupport.setProperty(PROP_FONT, font);
  }

  @Override
  public String getForegroundColor() {
    return propertySupport.getPropertyString(PROP_FOREGROUND_COLOR);
  }

  @Override
  public boolean setForegroundColor(String foregroundColor) {
    return propertySupport.setPropertyString(PROP_FOREGROUND_COLOR, foregroundColor);
  }

  @Override
  public int getHorizontalAlignment() {
    return propertySupport.getPropertyInt(PROP_HORIZONTAL_ALIGNMENT);
  }

  @Override
  public boolean setHorizontalAlignment(int horizontalAlignment) {
    return propertySupport.setPropertyInt(PROP_HORIZONTAL_ALIGNMENT, horizontalAlignment);
  }

  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public boolean setIconId(String iconId) {
    return propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @Override
  public String getText() {
    return propertySupport.getPropertyString(PROP_TEXT);
  }

  @Override
  public boolean setText(String text) {
    return propertySupport.setPropertyString(PROP_TEXT, text);
  }

  @Override
  public String getTooltipText() {
    return propertySupport.getPropertyString(PROP_TOOLTIP_TEXT);
  }

  @Override
  public boolean setTooltipText(String tooltipText) {
    return propertySupport.setPropertyString(PROP_TOOLTIP_TEXT, tooltipText);
  }

  @Override
  public boolean isTooltipHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_TOOLTIP_HTML_ENABLED);
  }

  @Override
  public boolean setTooltipHtmlEnabled(boolean tooltipHtmlEnabled) {
    return propertySupport.setPropertyBool(PROP_TOOLTIP_HTML_ENABLED, tooltipHtmlEnabled);
  }

  @Override
  public boolean isSortActive() {
    return propertySupport.getPropertyBool(PROP_SORT_ACTIVE);
  }

  @Override
  public boolean setSortActive(boolean sortActive) {
    return propertySupport.setPropertyBool(PROP_SORT_ACTIVE, sortActive);
  }

  @Override
  public boolean isGroupingActive() {
    return propertySupport.getPropertyBool(PROP_GROUPING_ACTIVE);
  }

  @Override
  public boolean setGroupingActive(boolean groupingActive) {
    return propertySupport.setPropertyBool(PROP_GROUPING_ACTIVE, groupingActive);
  }

  @Override
  public boolean isSortPermanent() {
    return propertySupport.getPropertyBool(PROP_SORT_PERMANENT);
  }

  @Override
  public boolean setSortPermanent(boolean sortPermanent) {
    return propertySupport.setPropertyBool(PROP_SORT_PERMANENT, sortPermanent);
  }
}
