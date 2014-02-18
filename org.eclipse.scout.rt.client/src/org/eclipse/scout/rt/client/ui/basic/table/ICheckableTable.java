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

import java.util.Collection;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;

/**
 * Legacy
 * 
 * @deprecated use {@link ITable} with {@link ITable#setCheckable(boolean)} Will be removed in Release 3.10.
 */
@Deprecated
public interface ICheckableTable extends ITable {

  String PROP_MULTI_CHECKABLE = "multiCheckable";

  IBooleanColumn getCheckboxColumn();

  void checkRow(ITableRow row, Boolean value) throws ProcessingException;

  void checkRow(int row, Boolean value) throws ProcessingException;

  @Override
  Collection<ITableRow> getCheckedRows();

  ITableRow getCheckedRow();

}
