/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.cell;

import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public interface ICell {
  int OBSERVER_BIT = 0;// tree, table, matrix etc.
  int VALUE_BIT = 1;
  int TEXT_BIT = 2;
  int ICON_ID_BIT = 3;
  int TOOLTIP_BIT = 4;
  int H_ALIGN_BIT = 5;// default -1, value byte
  int BG_COLOR_BIT = 6;
  int FG_COLOR_BIT = 7;
  int FONT_BIT = 8;
  int EDITABLE_BIT = 10;// default false, value Boolean
  int CSS_CLASS_BIT = 11;
  int HTML_ENABLED_BIT = 12; //default false, value Boolean
  int MANDATORY_BIT = 13; //default false, value Boolean
  int ERROR_STATUS_BIT = 14;

  Object getValue();

  /**
   * @return the cell text
   * <p>
   * This can be plain text or html text depending on {@link #isHtmlEnabled()}
   * <p>
   * In order to get human readable plain text call {@link #toPlainText()}
   */
  String getText();

  /**
   * @return the cell text in plain text format
   * <p>
   * If {@link #isHtmlEnabled()} is set to false then this is the same as {@link #getText()}. Else
   * {@link HtmlHelper#toPlainText(String)} is called by {@link HTML}{@link #toPlainText()}.
   * @since 9.0
   */
  default String toPlainText() {
    String text = getText();
    if (text != null && isHtmlEnabled()) {
      text = HTML.raw(text).toPlainText();
    }
    return text;
  }

  String getCssClass();

  String getIconId();

  String getTooltipText();

  int getHorizontalAlignment();

  String getBackgroundColor();

  String getForegroundColor();

  FontSpec getFont();

  boolean isMandatory();

  boolean isEditable();

  /**
   * @return true, if the cell may contain html that needs to be rendered. false otherwise.
   */
  boolean isHtmlEnabled();

  ICellObserver getObserver();

  /**
   * the error status of the cell or <code>null</code> in case of no error.
   */
  IMultiStatus getErrorStatus();

  /**
   * @return true if the content is valid, no error status is set on field and mandatory property is met.
   */
  boolean isContentValid();

  boolean isMandatoryFulfilled();
}
