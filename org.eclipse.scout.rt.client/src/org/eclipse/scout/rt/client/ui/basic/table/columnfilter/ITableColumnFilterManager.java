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

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * A handler to perform table content filtering when {@link ITable#isColumnFilterEnabled()} is activated
 */
public interface ITableColumnFilterManager {

  boolean isEnabled();

  void setEnabled(boolean enabled);

  <T> ITableColumnFilter<T> getFilter(IColumn<T> col);

  byte[] getSerializedFilter(IColumn col);

  void setSerializedFilter(byte[] filterData, IColumn col);

  /**
   * @return a collection of all filters of all rows
   */
  Collection<ITableColumnFilter> getFilters();

  /**
   * clears all filters from the table, sets and applies them again
   */
  void refresh();

  /**
   * @return the display texts of all filters contained in the column filter manager
   */
  List<String> getDisplayTexts();

  /**
   * Shows a form to create or modify a column filter
   * 
   * @param col
   *          the filtered column
   * @param showAsPopupDialog
   *          true: {@link IForm.DISPLAY_HINT_POPUP_DIALOG} will be used for display of the form
   * @throws ProcessingException
   */
  void showFilterForm(IColumn col, boolean showAsPopupDialog) throws ProcessingException;

  void reset() throws ProcessingException;

  /**
   * Adds a column filter listener.
   * <p/>
   * <b>Note:</b> The method does not check whether a listener is already in the listener list.
   * 
   * @param listener
   * @since 3.8.0
   */
  void addListener(TableColumnFilterListener listener);

  /**
   * Removes the given table column listener.
   * 
   * @param listener
   * @since 3.8.0
   */
  void removeListener(TableColumnFilterListener listener);
}
