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
package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnCompleteEditChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnDecorateHeaderCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnDisposeColumnChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnInitColumnChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnParseValueChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnPrepareEditChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnValidateValueChain;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IColumnExtension<VALUE, OWNER extends AbstractColumn<VALUE>> extends IExtension<OWNER> {

  void execCompleteEdit(ColumnCompleteEditChain<VALUE> chain, ITableRow row, IFormField editingField);

  void execInitColumn(ColumnInitColumnChain<VALUE> chain);

  VALUE execParseValue(ColumnParseValueChain<VALUE> chain, ITableRow row, Object rawValue);

  VALUE execValidateValue(ColumnValidateValueChain<VALUE> chain, ITableRow row, VALUE rawValue);

  IFormField execPrepareEdit(ColumnPrepareEditChain<VALUE> chain, ITableRow row);

  void execDecorateHeaderCell(ColumnDecorateHeaderCellChain<VALUE> chain, HeaderCell cell);

  void execDecorateCell(ColumnDecorateCellChain<VALUE> chain, Cell cell, ITableRow row);

  void execDisposeColumn(ColumnDisposeColumnChain<VALUE> chain);

}
