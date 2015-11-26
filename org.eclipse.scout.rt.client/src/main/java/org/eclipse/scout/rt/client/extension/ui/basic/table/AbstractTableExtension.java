/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.basic.table;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableContentChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCopyChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCreateTableRowDataMapperChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateRowChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDisposeTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDragChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDropChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableResetColumnsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowClickChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsCheckedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsSelectedChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowDataMapper;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractTableExtension<TABLE extends AbstractTable> extends AbstractExtension<TABLE> implements ITableExtension<TABLE> {

  public AbstractTableExtension(TABLE owner) {
    super(owner);
  }

  @Override
  public void execAppLinkAction(TableAppLinkActionChain chain, String ref) {
    chain.execAppLinkAction(ref);
  }

  @Override
  public void execRowAction(TableRowActionChain chain, ITableRow row) {
    chain.execRowAction(row);
  }

  @Override
  public void execContentChanged(TableContentChangedChain chain) {
    chain.execContentChanged();
  }

  @Override
  public ITableRowDataMapper execCreateTableRowDataMapper(TableCreateTableRowDataMapperChain chain, Class<? extends AbstractTableRowData> rowType) {
    return chain.execCreateTableRowDataMapper(rowType);
  }

  @Override
  public void execInitTable(TableInitTableChain chain) {
    chain.execInitTable();
  }

  @Override
  public void execResetColumns(TableResetColumnsChain chain, boolean visibility, boolean order, boolean sorting, boolean widths) {
    chain.execResetColumns(visibility, order, sorting, widths);
  }

  @Override
  public void execDecorateCell(TableDecorateCellChain chain, Cell view, ITableRow row, IColumn<?> col) {
    chain.execDecorateCell(view, row, col);
  }

  @Override
  public void execDrop(TableDropChain chain, ITableRow row, TransferObject t) {
    chain.execDrop(row, t);
  }

  @Override
  public void execDisposeTable(TableDisposeTableChain chain) {
    chain.execDisposeTable();
  }

  @Override
  public void execRowClick(TableRowClickChain chain, ITableRow row, MouseButton mouseButton) {
    chain.execRowClick(row, mouseButton);
  }

  @Override
  public void execDecorateRow(TableDecorateRowChain chain, ITableRow row) {
    chain.execDecorateRow(row);
  }

  @Override
  public TransferObject execCopy(TableCopyChain chain, List<? extends ITableRow> rows) {
    return chain.execCopy(rows);
  }

  @Override
  public void execRowsSelected(TableRowsSelectedChain chain, List<? extends ITableRow> rows) {
    chain.execRowsSelected(rows);
  }

  @Override
  public void execRowsChecked(TableRowsCheckedChain chain, List<? extends ITableRow> rows) {
    chain.execRowsChecked(rows);
  }

  @Override
  public TransferObject execDrag(TableDragChain chain, List<ITableRow> rows) {
    return chain.execDrag(rows);
  }
}
