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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import org.eclipse.scout.rt.client.mobile.ui.basic.table.columns.IRowSummaryColumn;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.form.ITableRowFormProvider;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;

/**
 * @since 3.9.0
 */
public interface IMobileTable extends ITable {

  String PROP_DRILL_DOWN_STYLE_MAP = "drillDownStyleMap";
  String PROP_AUTO_CREATE_TABLE_ROW_FORM = "autoCreateTableRowForm";
  String PROP_DEFAULT_DRILL_DOWN_STYLE = IRowSummaryColumn.PROP_DEFAULT_DRILL_DOWN_STYLE;
  String PROP_PAGING_ENABLED = "pagingEnabled";
  String PROP_PAGE_SIZE = "pageSize";
  String PROP_PAGE_INDEX = "pageIndex";
  String PROP_TABLE_ROW_FORM_PROVIDER = "tableRowFormProvider";

  String DRILL_DOWN_STYLE_NONE = IRowSummaryColumn.DRILL_DOWN_STYLE_NONE;
  String DRILL_DOWN_STYLE_ICON = IRowSummaryColumn.DRILL_DOWN_STYLE_ICON;
  String DRILL_DOWN_STYLE_BUTTON = IRowSummaryColumn.DRILL_DOWN_STYLE_BUTTON;

  String getDefaultDrillDownStyle();

  void setDefaultDrillDownStyle(String defaultDrillDownStyle);

  DrillDownStyleMap getDrillDownStyleMap();

  void setDrillDownStyleMap(DrillDownStyleMap drillDownStyleMap);

  boolean isAutoCreateTableRowForm();

  void setAutoCreateTableRowForm(boolean autoCreateRowForm);

  boolean isPagingEnabled();

  void setPagingEnabled(boolean enabled);

  void setPageSize(int pageSize);

  int getPageSize();

  void setPageIndex(int index);

  int getPageIndex();

  int getPageCount();

  ITableRowFormProvider getTableRowFormProvider();

  void setTableRowFormProvider(ITableRowFormProvider provider);

  @Override
  IMobileTableUiFacade getUIFacade();
}
