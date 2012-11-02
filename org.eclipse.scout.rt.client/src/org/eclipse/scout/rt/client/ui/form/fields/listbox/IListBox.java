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
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * A listbox contains at least 3 columns, where index 0 is the key column, index
 * 1 is the text column and index 2 is the active-flag column
 * <p>
 * The listbox value is a Object[] where the Object[] is the set of checked keys of the listbox<br>
 * the inner table shows those rows as checked which have the key value as a part of the listbox value (Object[])
 * <p>
 * Note, that the listbox might not necessarily show all checked rows since the value of the listbox might contain
 * inactive keys that are not reflected in the listbox<br>
 * Therefore an empty listbox table is not the same as a listbox with an empty value (null)
 */
public interface IListBox<T> extends IValueField<T[]>, ICompositeField {

  /**
   * {@link boolean}
   */
  String PROP_FILTER_CHECKED_ROWS = "filterCheckedRows";
  /**
   * {@link boolean}
   */
  String PROP_FILTER_ACTIVE_ROWS = "filterActiveRows";
  /**
   * {@link boolean}
   */
  String PROP_FILTER_CHECKED_ROWS_VALUE = "filterCheckedRowsValue";
  /**
   * {@link TriState}
   */
  String PROP_FILTER_ACTIVE_ROWS_VALUE = "filterActiveRowsValue";

  ITable getTable();

  IListBoxUIFacade getUIFacade();

  /**
   * @return true: a filter is added to the listbox table that only accepts
   *         checked rows<br>
   *         Affects {@link ITable#getFilteredRows()}<br>
   *         see also {@link #getFilterCheckedRowsValue()} and {@link #setFilterCheckedRows(boolean)}
   */
  boolean isFilterCheckedRows();

  /**
   * see {@link #isFilterCheckedRows()}
   */
  void setFilterCheckedRows(boolean b);

  boolean getFilterCheckedRowsValue();

  void setFilterCheckedRowsValue(boolean b);

  /**
   * @return true: a filter is added to the listbox table that only accepts rows
   *         that are active or checked.<br>
   *         Affects {@link ITable#getFilteredRows()}<br>
   *         see also {@link #getActiveFilter()} and {@link #setActiveFilter(TriState)}
   */
  boolean isFilterActiveRows();

  /**
   * see {@link #isFilterActiveRows()}<br>
   * see also {@link #getFilterActiveRowsValue()} and {@link #setFilterActiveRowsValue(TriState)}
   */
  void setFilterActiveRows(boolean b);

  TriState getFilterActiveRowsValue();

  void setFilterActiveRowsValue(TriState t);

  void prepareLookupCall(LookupCall call) throws ProcessingException;

  /**
   * Populate table with data from data service<br>
   * all existing data in the table is discarded
   * 
   * @see execFilterTableRow
   */
  void loadListBoxData() throws ProcessingException;

  LookupCall getLookupCall();

  void setLookupCall(LookupCall call);

  Class<? extends ICodeType> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType> codeTypeClass);

  /**
   * Convenience for getting the first value of {@link #getValue()}
   * 
   * @return the first selected/checked value if any
   *         <p>
   *         By default a listbox is checkable, so its value is the array of all checked keys
   *         <p>
   *         When it is made non-checkable, its value is the array of all selected keys
   */
  T getSingleValue();

  /**
   * Convenience for setting a single value with {@link #setValue(Object)}
   */
  void setSingleValue(T value);

  int getCheckedKeyCount();

  T getCheckedKey();

  T[] getCheckedKeys();

  LookupRow getCheckedLookupRow();

  LookupRow[] getCheckedLookupRows();

  void checkKeys(T[] keys);

  void checkKey(T key);

  /**
   * @return the keys that have been checked with regard to the initial keys of
   *         the listbox. Initial keys are those after the last save or init
   */
  T[] getUncheckedKeys();

  /**
   * check all available keys, regardless of active/inactive flag
   */
  void checkAllKeys();

  /**
   * check all active keys, ignoring inactive keys
   */
  void checkAllActiveKeys();

  /**
   * check all inactive keys, ignoring active keys
   */
  void uncheckAllInactiveKeys();

  /**
   * decheck all available keys, regardless of active/inactive flag
   */
  void uncheckAllKeys();

}
