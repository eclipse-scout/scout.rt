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
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.userfilter.AbstractUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;

public class ColumnUserFilterState extends AbstractUserFilterState implements IUserFilterState {
  private static final long serialVersionUID = 1L;
  public static final String TYPE = "column";

  private IColumn<String> m_column;
  private Set<Object> m_selectedValues;

  public ColumnUserFilterState(IColumn<String> column) {
    m_column = column;
    setType("column");
  }

  public IColumn<String> getColumn() {
    return m_column;
  }

  @SuppressWarnings("unchecked")
  public void setColumn(IColumn column) {
    m_column = column;
  }

  public Set<Object> getSelectedValues() {
    return m_selectedValues;
  }

  public void setSelectedValues(Set<Object> selectedValues) {
    m_selectedValues = selectedValues;
  }

  public boolean isEmpty() {
    return (m_selectedValues == null || m_selectedValues.isEmpty());
  }

  @Override
  public Object createKey() {
    return createKeyForColumn(getColumn());
  }

  public static Object createKeyForColumn(IColumn<?> column) {
    return column;
  }
}
