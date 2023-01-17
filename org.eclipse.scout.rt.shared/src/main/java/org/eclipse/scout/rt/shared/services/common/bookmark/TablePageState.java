/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.bookmark;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompositeObject;

public class TablePageState extends AbstractPageState {
  private static final long serialVersionUID = 1L;

  private CompositeObject m_expandedChildPrimaryKey;
  private List<CompositeObject> m_selectedChildrenPrimaryKeys;
  private String m_searchFormState;
  private boolean m_searchFilterComplete;
  private String m_searchFilterState;
  private byte[] m_tableCustomizerData;
  private byte[] m_userFilterData;
  /**
   * legacy, replaced by m_availableColumns
   */
  private List<TableColumnState> m_visibleColumns;
  /**
   * available columns at point in time when bookmark was created
   */
  private List<TableColumnState> m_availableColumns;

  private boolean m_tileMode;

  public TablePageState() {
    super();
  }

  protected TablePageState(TablePageState state) {
    super(state);
    if (state.m_selectedChildrenPrimaryKeys != null) {
      this.m_selectedChildrenPrimaryKeys = new ArrayList<>(state.m_selectedChildrenPrimaryKeys);
    }
    this.m_expandedChildPrimaryKey = state.m_expandedChildPrimaryKey;
    this.m_searchFormState = state.m_searchFormState;
    this.m_searchFilterState = state.m_searchFilterState;
    this.m_searchFilterComplete = state.m_searchFilterComplete;
    this.m_tableCustomizerData = state.m_tableCustomizerData;
    this.m_userFilterData = state.m_userFilterData;
    if (state.m_visibleColumns != null) {
      this.m_visibleColumns = new ArrayList<>();
      for (TableColumnState col : state.m_visibleColumns) {
        this.m_visibleColumns.add(new TableColumnState(col));
      }
    }
    if (state.m_availableColumns != null) {
      this.m_availableColumns = new ArrayList<>();
      for (TableColumnState col : state.m_availableColumns) {
        this.m_availableColumns.add(new TableColumnState(col));
      }
    }
    this.m_tileMode = state.m_tileMode;
  }

  public List<CompositeObject> getSelectedChildrenPrimaryKeys() {
    return CollectionUtility.arrayList(m_selectedChildrenPrimaryKeys);
  }

  public void setSelectedChildrenPrimaryKeys(List<CompositeObject> list) {
    if (list == null) {
      m_selectedChildrenPrimaryKeys = null;
    }
    else {
      m_selectedChildrenPrimaryKeys = new ArrayList<>(list);
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

  public List<TableColumnState> getAvailableColumns() {
    return CollectionUtility.arrayList(m_availableColumns);
  }

  public void setAvailableColumns(List<TableColumnState> cols) {
    m_visibleColumns = null;
    if (cols == null) {
      m_availableColumns = null;
    }
    else {
      m_availableColumns = new ArrayList<>(cols);
    }
  }

  /**
   * @return serialized TableCustomizer data
   */
  public byte[] getTableCustomizerData() {
    return m_tableCustomizerData;
  }

  /**
   * @param tableCustomizer
   *          serialized TableCustomizer data for persistent storage with bookmark
   */
  public void setTableCustomizerData(byte[] tableCustomizer) {
    m_tableCustomizerData = tableCustomizer;
  }

  public byte[] getUserFilterData() {
    return m_userFilterData;
  }

  public void setUserFilterData(byte[] userFilterData) {
    m_userFilterData = userFilterData;
  }

  @Override
  public TablePageState copy() {
    return new TablePageState(this);
  }

  public boolean isTileMode() {
    return m_tileMode;
  }

  public void setTileMode(boolean tileMode) {
    m_tileMode = tileMode;
  }
}
