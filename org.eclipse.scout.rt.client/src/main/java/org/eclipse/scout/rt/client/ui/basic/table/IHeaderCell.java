/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public interface IHeaderCell extends IStyleable, IPropertyObserver {

  String PROP_COLUMN_INDEX = "columnIndex";
  String PROP_SORT_ASC = "sortAsc";
  String PROP_SORT_ACTIVE = "sortActive";
  String PROP_SORT_PERMANENT = "sortPermanent";
  String PROP_GROUPING_ACTIVE = "groupingActive";
  String PROP_TEXT = "text";
  String PROP_ICON_ID = "iconId";
  String PROP_TOOLTIP_TEXT = "tooltipText";
  String PROP_TOOLTIP_HTML_ENABLED = "tooltipHtmlEnabled";
  String PROP_HORIZONTAL_ALIGNMENT = "horizontalAlignment";
  String PROP_HTML_ENABLED = "htmlEnabled";
  String PROP_MENU_ENABLED = "menuEnabled";
  String PROP_BACKGROUND_COLOR = "backgroundColor";
  String PROP_FOREGROUND_COLOR = "foregroundColor";
  String PROP_FONT = "font";

  /**
   * @return The column index as present in the code. This index has nothing to do with the current view index of this
   * column in the table.
   */
  int getColumnIndex();

  /**
   * @return If this column is sorted ascending
   * @see #isSortActive()
   * @see #isSortPermanent()
   */
  boolean isSortAscending();

  /**
   * @param sortAscending
   *     if this column is sorted ascending
   * @return {@code true} if the value has been changed, {@code false} otherwise.
   */
  boolean setSortAscending(boolean sortAscending);

  /**
   * @return If sorting for this column is active.
   * @see #isSortAscending()
   * @see #isSortPermanent()
   */
  boolean isSortActive();

  /**
   * @param sortActive
   *     If sorting should be active for this column.
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   */
  boolean setSortActive(boolean sortActive);

  /**
   * @return true if column is either a permanent head sort column or a permanent tail sort column.<br>
   * This means that the column remains sort column unless explicitly removed using
   * {@link ColumnSet#clearPermanentHeadSortColumns()} or {@link ColumnSet#clearPermanentTailSortColumns()}
   */
  boolean isSortPermanent();

  /**
   * @param sortPermanent
   *     if this column should be sorted permanent
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   * @see #isSortPermanent()
   */
  boolean setSortPermanent(boolean sortPermanent);

  /**
   * @return If grouping is active for this column.
   */
  boolean isGroupingActive();

  /**
   * @param groupingActive
   *     If grouping is active for this column.
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   */
  boolean setGroupingActive(boolean groupingActive);

  /**
   * @return The header text of this column.
   */
  String getText();

  /**
   * @param text
   *     the new header text for this column. May contain HTML tags to format it if {@link #isHtmlEnabled()} is
   *     active. In that case it is the responsibility of the caller to ensure only safe HTML is inserted (XSS).
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   * @see #isHtmlEnabled()
   */
  boolean setText(String text);

  /**
   * @return The icon id of this column header.
   */
  String getIconId();

  /**
   * @param iconId
   *     The icon id of this column header.
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   */
  boolean setIconId(String iconId);

  /**
   * @return The tooltip text of this header.
   */
  String getTooltipText();

  /**
   * @param tooltipText
   *     The tooltip text of this header. May contain HTML tags to format it if {@link #isTooltipHtmlEnabled()} is
   *     active. In that case it is the responsibility of the caller to ensure only safe HTML is inserted (XSS).
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   * @see #isTooltipHtmlEnabled()
   */
  boolean setTooltipText(String tooltipText);

  /**
   * @return If HTML is enabled for the tooltip text.
   * @see #setTooltipText(String)
   */
  boolean isTooltipHtmlEnabled();

  /**
   * @param tooltipHtmlEnabled
   *     If HTML is enabled for the tooltip text. If enabled, it is the responsibility of the caller of
   *     {@link #setTooltipText(String)} to ensure only safe HTML is inserted (XSS).
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   * @see #setTooltipText(String)
   */
  boolean setTooltipHtmlEnabled(boolean tooltipHtmlEnabled);

  /**
   * @see IColumn#getHorizontalAlignment()
   */
  int getHorizontalAlignment();

  /**
   * @param horizontalAlignment
   *     The new horizontal alignment. See {@link IColumn#getHorizontalAlignment()}
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   * @see IColumn#getHorizontalAlignment()
   */
  boolean setHorizontalAlignment(int horizontalAlignment);

  /**
   * @return If HTML is enabled for the header text.
   * @see #setText(String)
   */
  boolean isHtmlEnabled();

  /**
   * @param htmlEnabled
   *     If HTML is enabled for the header text. If enabled, it is the responsibility of the caller of
   *     {@link #setText(String)} to ensure only safe HTML is inserted (XSS).
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   * @see #setText(String)
   */
  boolean setHtmlEnabled(boolean htmlEnabled);

  /**
   * @return If menu is enabled for the header cell.
   */
  boolean isMenuEnabled();

  /**
   * @param menuEnabled
   *     If menu is enabled for the header cell.
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   */
  boolean setMenuEnabled(boolean menuEnabled);

  /**
   * @return The background color of the header
   */
  String getBackgroundColor();

  /**
   * @param backgroundColor
   *     The background color of the header.
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   */
  boolean setBackgroundColor(String backgroundColor);

  /**
   * @return The font color of the header.
   */
  String getForegroundColor();

  /**
   * @param foregroundColor
   *     The font color of the header
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   */
  boolean setForegroundColor(String foregroundColor);

  /**
   * @return The font of the header
   */
  FontSpec getFont();

  /**
   * @param font
   *     The font of the header or {@code null} to use the default font.
   * @return @return {@code true} if the value has been changed, {@code false} otherwise.
   */
  boolean setFont(FontSpec font);
}
