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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.eclipse.scout.rt.platform.html.HTML.*;
import static org.eclipse.scout.rt.platform.util.StringUtility.isNullOrEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ICompactHandler;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

public class TableCompactHandler implements ICompactHandler {
  private ITable m_table;
  private TableListener m_tableListener;
  private Map<String, Object> m_oldStates;
  private boolean m_useOnlyVisibleColumns;
  private int m_maxContentLines;

  public TableCompactHandler(ITable table) {
    m_table = table;
    m_oldStates = new HashMap<>();
    setUseOnlyVisibleColumns(true);
    setMaxContentLines(3);
  }

  public ITable getTable() {
    return m_table;
  }

  public boolean isUseOnlyVisibleColumns() {
    return m_useOnlyVisibleColumns;
  }

  public void setUseOnlyVisibleColumns(boolean useOnlyVisibleColumns) {
    m_useOnlyVisibleColumns = useOnlyVisibleColumns;
  }

  public int getMaxContentLines() {
    return m_maxContentLines;
  }

  public void setMaxContentLines(int maxContentLines) {
    m_maxContentLines = maxContentLines;
  }

  @Override
  public void handle(boolean compact) {
    if (compact) {
      compactColumns(true);
      attachTableListener();
    }
    else {
      detachTableListener();
      compactColumns(false);
    }
    adjustTable(compact);
    if (compact) {
      updateValues(getTable().getRows());
    }
  }

  protected void adjustTable(boolean compact) {
    if (compact) {
      cacheAndSetProperty(ITable.PROP_HEADER_VISIBLE, () -> getTable().isHeaderVisible(), () -> getTable().setHeaderVisible(false));
      cacheAndSetProperty(ITable.PROP_AUTO_RESIZE_COLUMNS, () -> getTable().isAutoResizeColumns(), () -> getTable().setAutoResizeColumns(true));
    }
    else {
      resetProperty(ITable.PROP_HEADER_VISIBLE, (value) -> getTable().setHeaderVisible(value), Boolean.class);
      resetProperty(ITable.PROP_AUTO_RESIZE_COLUMNS, (value) -> getTable().setAutoResizeColumns(value), Boolean.class);
    }
  }

  protected void cacheAndSetProperty(String propertyName, Supplier getter, Runnable setter) {
    m_oldStates.putIfAbsent(propertyName, getter.get());
    setter.run();
  }

  protected <T> void resetProperty(String propertyName, Consumer<T> setter, Class<T> type) {
    if (m_oldStates.containsKey(propertyName)) {
      setter.accept(type.cast(m_oldStates.get(propertyName)));
      m_oldStates.remove(propertyName);
    }
  }

  protected void compactColumns(boolean compact) {
    for (IColumn<?> column : getTable().getColumnSet().getDisplayableColumns()) {
      column.setCompacted(compact);
    }
    TableEvent e = new TableEvent(m_table, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
    if (m_table instanceof AbstractTable) {
      // Necessary to inform the ui if compact state changes while table is already displayed
      ((AbstractTable) m_table).fireTableEventInternal(e);
    }
  }

  private void attachTableListener() {
    if (m_tableListener == null) {
      m_tableListener = new P_TableListener();
      getTable().addTableListener(m_tableListener, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED, TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
    }
  }

  private void detachTableListener() {
    if (m_tableListener != null) {
      getTable().removeTableListener(m_tableListener, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED, TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
      m_tableListener = null;
    }
  }

  public void updateValues(List<ITableRow> rows) {
    if (rows.size() == 0) {
      return;
    }
    List<IColumn<?>> columns = getColumns();
    rows.forEach(row -> updateValue(columns, row));
  }

  protected void updateValue(List<IColumn<?>> columns, ITableRow row) {
    row.setCompactValue(buildValue(columns, row));
  }

  @SuppressWarnings("StringConcatenationInLoop")
  protected String buildValue(List<IColumn<?>> columns, ITableRow row) {
    return buildValue(createBean(columns, row));
  }

  protected CompactBean createBean(List<IColumn<?>> columns, ITableRow row) {
    CompactBean bean = new CompactBean();
    processColumns(columns, row, bean);
    postProcessBean(bean);
    return bean;
  }

  protected void processColumns(List<IColumn<?>> columns, ITableRow row, CompactBean bean) {
    for (int i = 0; i < columns.size(); i++) {
      processColumn(columns.get(i), i, row, bean);
    }
  }

  protected List<IColumn<?>> getColumns() {
    return getTable().getColumnSet().getColumns().stream().filter(this::acceptColumn).collect(Collectors.toList());
  }

  protected boolean acceptColumn(IColumn<?> column) {
    return !isUseOnlyVisibleColumns() || column.isVisible();
  }

  protected void processColumn(IColumn<?> column, int index, ITableRow row, CompactBean bean) {
    updateBean(bean, column, index, row);
  }

  /**
   * @param bean
   *          the bean for the current row
   * @param column
   *          the currently processed column
   * @param index
   *          visible column index of the currently processed column
   * @param row
   *          the current row
   */
  protected void updateBean(CompactBean bean, IColumn<?> column, int index, ITableRow row) {
    if (acceptColumnForTitle(column, index)) {
      bean.setTitleLine(createCompactLine(column, index, row));
    }
    else if (acceptColumnForTitleSuffix(column, index)) {
      bean.setTitleSuffixLine(createCompactLine(column, index, row));
    }
    else if (acceptColumnForSubtitle(column, index)) {
      bean.setSubtitleLine(createCompactLine(column, index, row));
    }
    else {
      bean.addContentLine(createCompactLine(column, index, row));
    }
  }

  protected boolean acceptColumnForTitle(IColumn<?> column, int index) {
    return index == 0;
  }

  protected boolean acceptColumnForSubtitle(IColumn<?> column, int index) {
    return index == 1;
  }

  protected boolean acceptColumnForTitleSuffix(IColumn<?> column, int index) {
    return false;
  }

  protected CompactLine createCompactLine(IColumn<?> column, int index, ITableRow row) {
    IHeaderCell headerCell = null;
    if (showLabel(column, index, row)) {
      headerCell = column.getHeaderCell();
    }
    ICell cell = row.getCell(column);
    CompactLine line = new CompactLine(headerCell, cell);
    adaptCompactLine(line, column, headerCell, cell);
    return line;
  }

  protected boolean showLabel(IColumn<?> column, int index, ITableRow row) {
    return !acceptColumnForTitle(column, index) && !acceptColumnForSubtitle(column, index) && !acceptColumnForTitleSuffix(column, index);
  }

  protected void adaptCompactLine(CompactLine line, IColumn<?> column, IHeaderCell headerCell, ICell cell) {
    // can be overridden to adapt line
  }

  protected void postProcessBean(CompactBean bean) {
    bean.transform(true, getMaxContentLines());

    // If only title is set move it to content. A title without content does not look good.
    if (!isNullOrEmpty(bean.getTitle()) && isNullOrEmpty(bean.getSubtitle()) && isNullOrEmpty(bean.getTitleSuffix()) && isNullOrEmpty(bean.getContent())) {
      bean.setContent(bean.getTitle());
      bean.setTitle("");
    }
  }

  protected String buildValue(CompactBean bean) {
    String hasHeader = StringUtility.hasText(bean.getTitle() + bean.getTitleSuffix() + bean.getSubtitle()) ? "has-header" : "";
    IHtmlElement moreLink = StringUtility.hasText(bean.getMoreContent()) ? div(span(TEXTS.get("More")).addCssClass("more-link link")).cssClass("compact-cell-more") : null;
    String value = HTML.fragment(
        div(
            div(
                span(raw(bean.getTitle())).cssClass("left"),
                span(raw(bean.getTitleSuffix())).cssClass("right")).cssClass("compact-cell-title"),
            div(raw(bean.getSubtitle())).cssClass("compact-cell-subtitle")).cssClass("compact-cell-header"),
        div(raw(bean.getContent())).cssClass("compact-cell-content " + hasHeader),
        div(raw(bean.getMoreContent())).cssClass("compact-cell-more-content hidden"),
        moreLink).toHtml();
    return value;
  }

  protected class P_TableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      List<ITableRow> rows = e.getRows();
      if (ObjectUtility.isOneOf(e.getType(), TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED, TableEvent.TYPE_COLUMN_HEADERS_UPDATED)) {
        rows = getTable().getRows();
      }
      updateValues(rows);
    }
  }
}
