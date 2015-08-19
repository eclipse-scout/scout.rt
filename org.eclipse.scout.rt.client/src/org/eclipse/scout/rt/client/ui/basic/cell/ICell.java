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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
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
  int ENABLED_BIT = 9;// default true, value Boolean
  int EDITABLE_BIT = 10;// default false, value Boolean
  int HTML_ENABLED_BIT = 12; //default false, value Boolean

  Object getValue();

  String getText();

  String getIconId();

  String getTooltipText();

  int getHorizontalAlignment();

  String getBackgroundColor();

  String getForegroundColor();

  FontSpec getFont();

  boolean isEnabled();

  /**
   * @return true if the cell is <b>basically</b> editable. This flag is used primarly to decorate editable cells. It
   *         does not regard
   *         the visible state of the column, table and container.
   *         To check for effective editable use {@link ITable#isCellEditable(int, int)} inside a {@link ClientJob}
   */
  boolean isEditable();

  /**
   * @return true, if the cell may contain html that needs to be rendered. false otherwise.
   */
  boolean isHtmlEnabled();

  ICellObserver getObserver();

  /**
   * the error status of the cell or <code>null</code> in case of no error.
   */
  IProcessingStatus getErrorStatus();

}
