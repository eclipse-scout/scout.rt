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
package org.eclipse.scout.rt.client.ui.basic.table.columnfilter;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * Used by {@link DefaultTableColumnFilterManager}
 */
public interface ITableColumnFilter<T> extends ITableRowFilter {

  IColumn<T> getColumn();

  void setColumn(IColumn column);

  Set<T> getSelectedValues();

  void setSelectedValues(Set<T> set);

  List<LookupRow> createHistogram();

  boolean isEmpty();

}
