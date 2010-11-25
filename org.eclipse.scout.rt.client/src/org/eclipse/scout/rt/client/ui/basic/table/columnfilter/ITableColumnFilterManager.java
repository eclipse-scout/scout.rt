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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * A handler to perform table content filtering when {@link ITable#isColumnFilterEnabled()} is activated
 */
public interface ITableColumnFilterManager {

  boolean isEnabled();

  void setEnabled(boolean enabled);

  <T> ITableColumnFilter<T> getFilter(IColumn<T> col);

  /**
   * @return the display texts of all filters contained in the column filter manager
   */
  List<String> getDisplayTexts();

  void showFilterForm(IColumn col) throws ProcessingException;

  void reset() throws ProcessingException;
}
