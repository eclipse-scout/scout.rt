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
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * Interface extension to {@link ITable} that provides additional methods for importing and exporting the table's
 * contents into {@link AbstractTableFieldBeanData}s.
 * 
 * @since 3.8.2
 */
public interface ITable2 extends ITable {

  /**
   * Exports the contents of this table into the given {@link AbstractTableFieldBeanData}. The mapping from
   * {@link IColumn}s to {@link AbstractTableRowData} properties is based on the property name and the
   * {@link IColumn#getColumnId()}.
   * 
   * @param target
   * @throws ProcessingException
   */
  void exportToTableBeanData(AbstractTableFieldBeanData target) throws ProcessingException;

  /**
   * Imports the contents of the given {@link AbstractTableFieldBeanData}. The mapping from {@link AbstractTableRowData}
   * properties to {@link IColumn}s is based on the property name and the {@link IColumn#getColumnId()}.
   * 
   * @param source
   * @throws ProcessingException
   */
  void importFromTableBeanData(AbstractTableFieldBeanData source) throws ProcessingException;
}
