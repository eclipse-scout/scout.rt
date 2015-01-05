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

import java.net.URL;
import java.util.List;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableContentChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCopyChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCreateTableRowDataMapperChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateRowChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDisposeTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDragChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDropChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableHyperlinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableResetColumnsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowClickChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsSelectedChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowDataMapper;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractTableExtension<TABLE extends AbstractTable> extends AbstractExtension<TABLE> implements ITableExtension<TABLE> {

  public AbstractTableExtension(TABLE owner) {
    super(owner);
  }

  @Override
  public void execHyperlinkAction(TableHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException {
    chain.execHyperlinkAction(url, path, local);
  }

  @Override
  public void execRowAction(TableRowActionChain chain, ITableRow row) throws ProcessingException {
    chain.execRowAction(row);
  }

  @Override
  public void execContentChanged(TableContentChangedChain chain) throws ProcessingException {
    chain.execContentChanged();
  }

  @Override
  public ITableRowDataMapper execCreateTableRowDataMapper(TableCreateTableRowDataMapperChain chain, Class<? extends AbstractTableRowData> rowType) throws ProcessingException {
    return chain.execCreateTableRowDataMapper(rowType);
  }

  @Override
  public void execInitTable(TableInitTableChain chain) throws ProcessingException {
    chain.execInitTable();
  }

  @Override
  public void execResetColumns(TableResetColumnsChain chain, boolean visibility, boolean order, boolean sorting, boolean widths) throws ProcessingException {
    chain.execResetColumns(visibility, order, sorting, widths);
  }

  @Override
  public void execDecorateCell(TableDecorateCellChain chain, Cell view, ITableRow row, IColumn<?> col) throws ProcessingException {
    chain.execDecorateCell(view, row, col);
  }

  @Override
  public void execDrop(TableDropChain chain, ITableRow row, TransferObject t) throws ProcessingException {
    chain.execDrop(row, t);
  }

  @Override
  public void execDisposeTable(TableDisposeTableChain chain) throws ProcessingException {
    chain.execDisposeTable();
  }

  @Override
  public void execRowClick(TableRowClickChain chain, ITableRow row, MouseButton mouseButton) throws ProcessingException {
    chain.execRowClick(row, mouseButton);
  }

  @Override
  public void execDecorateRow(TableDecorateRowChain chain, ITableRow row) throws ProcessingException {
    chain.execDecorateRow(row);
  }

  @Override
  public TransferObject execCopy(TableCopyChain chain, List<? extends ITableRow> rows) throws ProcessingException {
    return chain.execCopy(rows);
  }

  @Override
  public void execRowsSelected(TableRowsSelectedChain chain, List<? extends ITableRow> rows) throws ProcessingException {
    chain.execRowsSelected(rows);
  }

  @Override
  public TransferObject execDrag(TableDragChain chain, List<ITableRow> rows) throws ProcessingException {
    return chain.execDrag(rows);
  }
}
