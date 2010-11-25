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
package org.eclipse.scout.rt.shared.services.common.bookmark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CompositeObject;

public class TablePageState extends AbstractPageState implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  private CompositeObject m_expandedChildPrimaryKey;
  private List<CompositeObject> m_selectedChildrenPrimaryKeys;
  private String m_searchFormState;
  private boolean m_searchFilterComplete;
  private String m_searchFilterState;
  private List<TableColumnState> m_visibleColumns;

  public TablePageState() {
  }

  protected TablePageState(TablePageState state) {
    super(state);
    if (state.m_selectedChildrenPrimaryKeys != null) {
      this.m_selectedChildrenPrimaryKeys = new ArrayList<CompositeObject>(state.m_selectedChildrenPrimaryKeys);
    }
    this.m_expandedChildPrimaryKey = state.m_expandedChildPrimaryKey;
    this.m_searchFormState = state.m_searchFormState;
    this.m_searchFilterState = state.m_searchFilterState;
    this.m_searchFilterComplete = state.m_searchFilterComplete;
    if (state.m_visibleColumns != null) {
      this.m_visibleColumns = new ArrayList<TableColumnState>();
      for (TableColumnState col : state.m_visibleColumns) {
        this.m_visibleColumns.add(new TableColumnState(col));
      }
    }
  }

  public List<CompositeObject> getSelectedChildrenPrimaryKeys() {
    if (m_selectedChildrenPrimaryKeys == null) return Collections.emptyList();
    else return Collections.unmodifiableList(m_selectedChildrenPrimaryKeys);
  }

  public void setSelectedChildrenPrimaryKeys(List<CompositeObject> list) {
    if (list == null) {
      m_selectedChildrenPrimaryKeys = null;
    }
    else {
      m_selectedChildrenPrimaryKeys = new ArrayList<CompositeObject>(list);
    }
  }

  public CompositeObject getExpandedChildPrimaryKey() {
    return m_expandedChildPrimaryKey;
  }

  public void setExpandedChildPrimaryKey(CompositeObject pk) {
    m_expandedChildPrimaryKey = pk;
  }

  /**
   * @return xml of form content {@link org.eclipse.scout.rt.client.ui.form.IForm#setXML(String)}
   */
  public String getSearchFormState() {
    return m_searchFormState;
  }

  /**
   * set xml of form content {@link org.eclipse.scout.rt.client.ui.form.IForm#setXML(String)}
   */
  public void setSearchFormState(String xml) {
    m_searchFormState = xml;
  }

  public boolean isSearchFilterComplete() {
    return m_searchFilterComplete;
  }

  /**
   * @return CRC of serialized search filter data
   */
  public String getSearchFilterState() {
    return m_searchFilterState;
  }

  /**
   * @param complete
   *          if search was completed (i.e. is valid and can be run)
   * @param state
   *          CRC of serialized search filter data
   */
  public void setSearchFilterState(boolean complete, String state) {
    m_searchFilterComplete = complete;
    m_searchFilterState = state;
  }

  public List<TableColumnState> getVisibleColumns() {
    if (m_visibleColumns == null) return null;
    else return Collections.unmodifiableList(m_visibleColumns);
  }

  public void setVisibleColumns(List<TableColumnState> cols) {
    if (cols == null) {
      m_visibleColumns = null;
    }
    else {
      m_visibleColumns = new ArrayList<TableColumnState>(cols);
    }
  }

  @Override
  public Object clone() {
    return new TablePageState(this);
  }

}
