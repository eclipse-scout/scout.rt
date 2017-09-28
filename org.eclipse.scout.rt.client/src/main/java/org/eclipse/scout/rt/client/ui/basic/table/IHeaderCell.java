/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public interface IHeaderCell {

  int getColumnIndex();

  boolean isSortAscending();

  boolean isSortActive();

  /**
   * @return true if column is either a permanent head sort column or a permanent tail sort column.<br>
   *         This means that the column remains sort column unlesss explicitly removed using
   *         {@link ColumnSet#clearPermanentHeadSortColumns()} or {@link ColumnSet#clearPermanentTailSortColumns()}
   */
  boolean isSortPermanent();

  boolean isGroupingActive();

  /**
   * there is no setText method in interface,<br>
   * use {@link #IColumn.decorateHeaderCell()} and {@link #IColumn.execDecorateHeaderCell()}
   */
  String getText();

  String getIconId();

  String getTooltipText();

  int getHorizontalAlignment();

  String getCssClass();

  boolean isHtmlEnabled();

  String getBackgroundColor();

  String getForegroundColor();

  FontSpec getFont();

}
