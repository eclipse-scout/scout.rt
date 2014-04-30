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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.shared.data.basic.table.SortSpec;

public class ColumnSet {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ColumnSet.class);

  private final AbstractTable m_table;
  private final List<IColumn<?>> m_columns;
  private final List<IColumn<?>> m_userSortColumns;
  private final List<IColumn<?>> m_permanentHeadSortColumns;
  private final List<IColumn<?>> m_permanentTailSortColumns;
  /**
   * key index to model index
   */
  private int[] m_keyIndexes = new int[0];
  /**
   * view index to model index (regardless of visibility)
   */
  private int[] m_displayableIndexes = new int[0];
  /**
   * view index to model index (only visible columns)
   */
  private int[] m_visibleIndexes = new int[0];
  /**
   * class to model
   */
  private final HashMap<Class, IColumn> m_classIndexes = new HashMap<Class, IColumn>();

  /**
   * ID to model
   */
  private final HashMap<String, IColumn> m_idIndexes = new HashMap<String, IColumn>();

  private final Map<Class<?>, Class<? extends IColumn>> m_columnReplacements;

  public ColumnSet(AbstractTable table, List<IColumn<?>> columns) {
    // process @Replace annotations
    List<Class<? extends IColumn>> columnArray = new ArrayList<Class<? extends IColumn>>(columns.size());
    for (IColumn c : columns) {
      columnArray.add(c.getClass());
    }
    Map<Class<?>, Class<? extends IColumn>> replacements = ConfigurationUtility.getReplacementMapping(columnArray);
    if (replacements.isEmpty()) {
      replacements = null;
    }
    m_columnReplacements = replacements;
    //
    m_table = table;
    m_columns = new ArrayList<IColumn<?>>(columns.size());
    m_userSortColumns = new ArrayList<IColumn<?>>();
    m_permanentHeadSortColumns = new ArrayList<IColumn<?>>();
    m_permanentTailSortColumns = new ArrayList<IColumn<?>>();
    PropertyChangeListener columnListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        IColumn c = (IColumn) e.getSource();
        if (IColumn.PROP_VIEW_COLUMN_INDEX_HINT.equals(e.getPropertyName())) {
          //ignore
        }
        else if (IColumn.PROP_VIEW_ORDER.equals(e.getPropertyName())) {
          resetColumnsViewOrder();
        }
        else {
          updateColumnStructure(c);
        }
      }
    };
    int index = 0;
    for (IColumn col : columns) {
      if (col instanceof AbstractColumn) {
        ((AbstractColumn) col).setColumnIndexInternal(index);
        ((AbstractColumn) col).setTableInternal(m_table);
      }
      rebuildHeaderCell(col);
      m_columns.add(col);
      m_classIndexes.put(col.getClass(), col);
      m_idIndexes.put(col.getColumnId(), col);
      col.addPropertyChangeListener(columnListener);
      index++;
    }
    reorganizeIndexes();
  }

  public void initialize() {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    //clean up visible column index hints, make as permutation of model indices
    int n = getColumnCount();
    TreeMap<CompositeObject, IColumn> sortMap = new TreeMap<CompositeObject, IColumn>();
    int viewIndex = 0;
    for (int modelIndex = 0; modelIndex < n; modelIndex++) {
      IColumn col = getColumn(modelIndex);
      double viewHint = col.getVisibleColumnIndexHint();
      if (viewHint < 0) {
        viewHint = col.getViewOrder();
      }
      if (viewHint < 0) {
        viewHint = viewIndex;
      }
      sortMap.put(new CompositeObject(viewHint, viewIndex), col);
      // next
      viewIndex++;
    }
    viewIndex = 0;
    for (Map.Entry<CompositeObject, IColumn> e : sortMap.entrySet()) {
      e.getValue().setVisibleColumnIndexHint(viewIndex);
      viewIndex++;
    }
    reorganizeIndexes();
    //initialize sorting
    sortMap.clear();
    int index = 0;
    for (IColumn col : getColumns()) {
      int sortIndex = -1;
      if (col.isInitialAlwaysIncludeSortAtBegin()) {
        sortIndex = col.getInitialSortIndex();
        if (sortIndex < 0) {
          LOG.warn("AlwaysIncludeSortAtBegin is set but no sort index configured. Table: " + m_table.getClass().getName());
        }
      }
      else if (col.isInitialAlwaysIncludeSortAtEnd()) {
        sortIndex = col.getInitialSortIndex();
        if (sortIndex < 0) {
          LOG.warn("AlwaysIncludeSortAtEnd is set but no sort index configured. Table: " + m_table.getClass().getName());
        }
      }
      else {
        sortIndex = prefs.getTableColumnSortIndex(col, col.getInitialSortIndex());
      }
      if (sortIndex >= 0) {
        sortMap.put(new CompositeObject(sortIndex, index), col);
      }
      index++;
    }
    //
    clearSortColumns();
    clearPermanentHeadSortColumns();
    clearPermanentTailSortColumns();
    for (IColumn col : sortMap.values()) {
      if (col.isInitialAlwaysIncludeSortAtBegin()) {
        boolean asc = col.isInitialSortAscending();
        addPermanentHeadSortColumn(col, asc);
      }
      else if (col.isInitialAlwaysIncludeSortAtEnd()) {
        boolean asc = col.isInitialSortAscending();
        addPermanentTailSortColumn(col, asc);
      }
      else {
        boolean asc = prefs.getTableColumnSortAscending(col, col.isInitialSortAscending());
        addSortColumn(col, asc);
      }
    }
    //restore explicit flag on user sort columns (after sort is built)
    for (IColumn col : getUserSortColumns()) {
      Boolean explicit = prefs.getTableColumnSortExplicit(col);
      if (explicit != null) {
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setSortExplicit(explicit.booleanValue());
      }
    }
    /*
     * ticket 93309
     * sanity check: when there is no visible column at all, reset visibilities to model init defaults
     */
    if (getVisibleColumnCount() == 0) {
      viewIndex = 0;
      for (IColumn c : getColumns()) {
        if (c.isDisplayable() && c.isInitialVisible()) {
          c.setVisible(true);
        }
        else {
          c.setVisible(false);
        }
        c.setWidth(c.getInitialWidth());
        c.setVisibleColumnIndexHint(viewIndex);
        prefs.removeTableColumnPreferences(c);
        //next
        viewIndex++;
      }
      reorganizeIndexes();
    }
    checkMultiline();
  }

  public int getColumnCount() {
    return m_columns.size();
  }

  public int getKeyColumnCount() {
    return m_keyIndexes.length;
  }

  public int getDisplayableColumnCount() {
    return m_displayableIndexes.length;
  }

  public int getVisibleColumnCount() {
    return m_visibleIndexes.length;
  }

  public int[] getKeyColumnIndexes() {
    int[] a = new int[m_keyIndexes.length];
    System.arraycopy(m_keyIndexes, 0, a, 0, a.length);
    return a;
  }

  public int[] getAllColumnIndexes() {
    int[] a = new int[m_columns.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = i;
    }
    return a;
  }

  public List<IColumn<?>> getAllColumnsInUserOrder() {
    List<IColumn<?>> visibleCols = getVisibleColumns();

    int counter = 0;
    TreeMap<CompositeObject, IColumn<?>> sortMap = new TreeMap<CompositeObject, IColumn<?>>();
    for (IColumn col : visibleCols) {
      sortMap.put(new CompositeObject(col.getVisibleColumnIndexHint(), counter++), col);
    }

    for (IColumn<?> column : getColumns()) {
      if (column.isDisplayable() && column.isVisible()) {
        //already in map
      }
      else {
        sortMap.put(new CompositeObject(column.getVisibleColumnIndexHint(), counter++), column);
      }
    }
    return CollectionUtility.arrayList(sortMap.values());
  }

  public int[] getDisplayableColumnIndexes() {
    int[] a = new int[m_displayableIndexes.length];
    System.arraycopy(m_displayableIndexes, 0, a, 0, a.length);
    return a;
  }

  public int[] getVisibleColumnIndexes() {
    int[] a = new int[m_visibleIndexes.length];
    System.arraycopy(m_visibleIndexes, 0, a, 0, a.length);
    return a;
  }

  public int getKeyColumnIndex(int keyIndex) {
    return m_keyIndexes[keyIndex];
  }

  public int getDisplayableColumnIndex(int displayableIndex) {
    return m_displayableIndexes[displayableIndex];
  }

  public int getVisibleColumnIndex(int visibleIndex) {
    return m_visibleIndexes[visibleIndex];
  }

  public IColumn getColumn(int index) {
    if (index >= 0 && index < m_columns.size()) {
      return m_columns.get(index);
    }
    else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends IColumn> T getColumnByClass(Class<T> c) {
    Class<? extends T> columnClass = getReplacingColumnClass(c);
    T col = (T) m_classIndexes.get(columnClass);
    return col;
  }

  @SuppressWarnings("unchecked")
  public <T extends IColumn> T getColumnById(String id) {
    return (T) m_idIndexes.get(id);
  }

  /**
   * Checks whether the column with the given class has been replaced by another column. If so, the replacing
   * column's class is returned. Otherwise the given class itself.
   * 
   * @param c
   * @return Returns the possibly available replacing column class for the given class.
   * @see Replace
   * @since 3.8.2
   */
  private <T extends IColumn> Class<? extends T> getReplacingColumnClass(Class<T> c) {
    if (m_columnReplacements != null) {
      @SuppressWarnings("unchecked")
      Class<? extends T> replacingColumnClass = (Class<? extends T>) m_columnReplacements.get(c);
      if (replacingColumnClass != null) {
        return replacingColumnClass;
      }
    }
    return c;
  }

  public IColumn getDisplayableColumn(int index) {
    if (index >= 0 && index < m_displayableIndexes.length) {
      return m_columns.get(m_displayableIndexes[index]);
    }
    else {
      return null;
    }
  }

  public IColumn getVisibleColumn(int index) {
    if (index >= 0 && index < m_visibleIndexes.length) {
      return m_columns.get(m_visibleIndexes[index]);
    }
    else {
      return null;
    }
  }

  public List<IColumn<?>> getColumns() {
    return CollectionUtility.arrayList(m_columns);
  }

  public List<IColumn<?>> getKeyColumns() {
    List<IColumn<?>> keyColumns = new ArrayList<IColumn<?>>(m_keyIndexes.length);
    for (int i = 0; i < m_keyIndexes.length; i++) {
      keyColumns.add(getColumn(m_keyIndexes[i]));
    }
    return keyColumns;
  }

  public List<IColumn<?>> getDisplayableColumns() {
    List<IColumn<?>> a = new ArrayList<IColumn<?>>(m_displayableIndexes.length);
    for (int i = 0; i < m_displayableIndexes.length; i++) {
      a.add(getColumn(m_displayableIndexes[i]));
    }
    return a;
  }

  public List<IColumn<?>> getVisibleColumns() {
    List<IColumn<?>> a = new ArrayList<IColumn<?>>(m_visibleIndexes.length);
    for (int i = 0; i < m_visibleIndexes.length; i++) {
      a.add(getColumn(m_visibleIndexes[i]));
    }
    return a;
  }

  public IColumn getFirstVisibleColumn() {
    if (m_visibleIndexes.length > 0) {
      return m_columns.get(m_visibleIndexes[0]);
    }
    else {
      return null;
    }
  }

  public IColumn getFirstDefinedVisibileColumn() {
    int colIdx = m_columns.size();
    for (int i = 0; i < m_visibleIndexes.length; i++) {
      if (CompareUtility.compareTo(m_visibleIndexes[i], colIdx) < 0) {
        colIdx = m_visibleIndexes[i];
      }
    }
    if (colIdx != m_columns.size()) {
      return m_columns.get(colIdx);
    }
    else {
      return null;
    }
  }

  public List<IColumn<?>> getSummaryColumns() {
    List<IColumn<?>> summaryColumns = new ArrayList<IColumn<?>>();
    for (IColumn c : getColumns()) {
      if (c.isSummary()) {
        summaryColumns.add(c);
      }
    }
    return summaryColumns;
  }

  public int getIndexFor(IColumn column) {
    return m_columns.indexOf(column);
  }

  public void moveColumnToVisibleIndex(int fromIndex, int toVisibleIndex) {
    // find view index
    int fromVisibleIndex = modelToVisibleIndex(fromIndex);
    if (fromVisibleIndex >= 0 && toVisibleIndex >= 0) {
      moveVisibleColumnToVisibleIndex(fromVisibleIndex, toVisibleIndex);
    }
  }

  public void moveVisibleColumnToVisibleIndex(int fromVisibleIndex, int toVisibleIndex) {
    if (fromVisibleIndex != toVisibleIndex) {
      IColumn fromCol = getVisibleColumn(fromVisibleIndex);
      IColumn toCol = getVisibleColumn(toVisibleIndex);
      if (fromCol != null && toCol != null) {
        boolean traversedFrom = false;
        ArrayList<IColumn<?>> list = new ArrayList<IColumn<?>>();
        for (IColumn c : getAllColumnsInUserOrder()) {
          if (c == fromCol) {
            traversedFrom = true;
            //nop
          }
          else if (c == toCol) {
            if (traversedFrom) {
              list.add(c);
              list.add(fromCol);
            }
            else {
              list.add(fromCol);
              list.add(c);
            }
          }
          else {
            list.add(c);
          }
        }
        int viewHint = 0;
        for (IColumn c : list) {
          c.setVisibleColumnIndexHint(viewHint);
          viewHint++;
        }
        reorganizeIndexes();
        fireColumnOrderChanged();
      }
    }
  }

  /**
   * set visible columns and put them in specific order
   */
  public void setVisibleColumns(Collection<? extends IColumn> columns) {
    try {
      m_table.setTableChanging(true);
      //
      List<IColumn<?>> resolvedColumns = resolveColumns(columns);
      if (columns == null) {
        columns = CollectionUtility.hashSet();
      }
      if (resolvedColumns.size() > 0 || columns.size() == 0) {
        List<IColumn<?>> newColumns = new ArrayList<IColumn<?>>();
        for (IColumn col : columns) {
          if (col.isDisplayable()) {
            // sanity check
            if (col.getInitialWidth() == 0 && col.getWidth() == 0) {
              col.setInitialWidth(60);
              col.setWidth(60);
            }
            newColumns.add(col);
          }
        }
        int viewHint = 0;
        int nextNewIndex = 0;
        for (IColumn col : getAllColumnsInUserOrder()) {
          if (newColumns.contains(col)) {
            //use next in list since the list is pre-ordered
            IColumn nextSortedCol = newColumns.get(nextNewIndex);
            nextNewIndex++;
            nextSortedCol.setVisible(true);
            nextSortedCol.setVisibleColumnIndexHint(viewHint);
          }
          else {
            col.setVisible(false);
            col.setVisibleColumnIndexHint(viewHint);
          }
          viewHint++;
        }
        reorganizeIndexes();
        List<IColumn<?>> displayableColumns = getDisplayableColumns();
        for (IColumn col : displayableColumns) {
          rebuildHeaderCell(col);
        }
        fireColumnHeadersUpdated(displayableColumns);
        fireColumnStructureChanged();
        checkMultiline();
      }
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  private int visibleToModelIndex(int visibleIndex) {
    if (visibleIndex < 0 || visibleIndex >= m_visibleIndexes.length) {
      LOG.warn("viewIndex " + visibleIndex + " out of range [" + 0 + "," + (m_visibleIndexes.length - 1) + "]");
      return -1;
    }
    return m_visibleIndexes[visibleIndex];
  }

  private int keyToModelIndex(int keyIndex) {
    if (keyIndex < 0 || keyIndex >= m_keyIndexes.length) {
      LOG.warn("keyIndex " + keyIndex + " out of range [" + 0 + "," + (m_keyIndexes.length - 1) + "]");
      return -1;
    }
    return m_keyIndexes[keyIndex];
  }

  private int modelToVisibleIndex(int index) {
    for (int i = 0; i < m_visibleIndexes.length; i++) {
      if (m_visibleIndexes[i] == index) {
        return i;
      }
    }
    return -1;
  }

  public IColumn resolveColumn(IColumn c) {
    if (c.getTable() == m_table) {
      return c;
    }
    else {
      return null;
    }
  }

  public List<IColumn<?>> resolveColumns(Collection<? extends IColumn> columns) {
    if (columns != null) {
      List<IColumn<?>> result = new ArrayList<IColumn<?>>(columns.size());
      for (IColumn col : columns) {
        IColumn resolvedCol = resolveColumn(col);
        if (resolvedCol != null) {
          result.add(resolvedCol);
        }
      }
      return result;
    }
    return CollectionUtility.emptyArrayList();
  }

  /*
   * Sorting
   */

  /**
   * @param column
   * @param multiSort
   *          true = multiple sort columns are supported, every event toggles
   *          the current column between the states ON-ASCENDING (add to tail of
   *          sort columns), ON-DESCENDING. False = the selected column is set
   *          as the (new) primary sort column, if already set it is toggled
   *          between ascending and descending
   */
  public void handleSortEvent(IColumn col, boolean multiSort) {
    col = resolveColumn(col);
    if (col == null) {
      return;
    }
    //
    try {
      m_table.setTableChanging(true);
      //
      if (multiSort) {
        if (isSortColumn(col) && col.isSortExplicit()) {
          toggleSortColumn(col);
        }
        else {
          addSortColumn(col, true);
        }
      }
      else {
        int explicitCount = 0;
        for (IColumn c : m_userSortColumns) {
          if (c.isSortExplicit()) {
            explicitCount++;
          }
        }
        if (isSortColumn(col) && col.isSortExplicit() && explicitCount == 1) {
          toggleSortColumn(col);
        }
        else {
          setSortColumn(col, true, 5);
        }
      }
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  public int getSortColumnCount() {
    return m_userSortColumns.size() + m_permanentHeadSortColumns.size() + m_permanentTailSortColumns.size();
  }

  /**
   * @return all sort columns including permanent-head, user, permanent-tail
   */
  public List<IColumn<?>> getSortColumns() {
    List<IColumn<?>> list = new ArrayList<IColumn<?>>(getSortColumnCount());
    list.addAll(m_permanentHeadSortColumns);
    list.addAll(m_userSortColumns);
    list.addAll(m_permanentTailSortColumns);
    return list;
  }

  /**
   * @return only user sort columns
   */
  public List<IColumn<?>> getUserSortColumns() {
    return CollectionUtility.arrayList(m_userSortColumns);
  }

  /**
   * @return only permanent head sort columns
   */
  public List<IColumn<?>> getPermanentHeadSortColumns() {
    return CollectionUtility.arrayList(m_permanentHeadSortColumns);
  }

  /**
   * @return only permanent tail sort columns
   */
  public List<IColumn<?>> getPermanentTailSortColumns() {
    return CollectionUtility.arrayList(m_permanentTailSortColumns);
  }

  public SortSpec getSortSpec() {
    ArrayList<IColumn<?>> sortColumns = new ArrayList<IColumn<?>>();
    for (IColumn c : getSortColumns()) {
      if (c.isSortExplicit()) {
        sortColumns.add(c);
      }
    }
    if (sortColumns.size() > 0) {
      int[] indexes = new int[sortColumns.size()];
      boolean[] asc = new boolean[sortColumns.size()];
      for (int i = 0; i < sortColumns.size(); i++) {
        indexes[i] = sortColumns.get(i).getColumnIndex();
        asc[i] = sortColumns.get(i).isSortAscending();
      }
      return new SortSpec(indexes, asc);
    }
    else {
      return null;
    }
  }

  public void setSortSpec(SortSpec spec) {
    if (spec != null) {
      for (IColumn col : m_userSortColumns) {
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setSortActive(false);
        cell.setSortAscending(false);
        cell.setSortExplicit(false);
      }
      m_userSortColumns.clear();
      List<IColumn<?>> colList = new ArrayList<IColumn<?>>();
      for (int i = 0; i < spec.size(); i++) {
        IColumn col = getColumn(spec.getColumnIndex(i));
        if (col != null && (!isSortColumn(col))) {
          HeaderCell cell = (HeaderCell) col.getHeaderCell();
          cell.setSortActive(true);
          cell.setSortExplicit(true);
          cell.setSortAscending(spec.isColumnAscending(i));
          colList.add(col);
        }
      }
      m_userSortColumns.addAll(colList);
      for (IColumn col : colList) {
        rebuildHeaderCell(col);
      }
      fireColumnHeadersUpdated(colList);
    }
    else {
      clearSortColumns();
    }
  }

  /**
   * @return true if the column is either a permanent-head, user or permanent-tail sort column
   */
  public boolean isSortColumn(IColumn col) {
    return m_permanentHeadSortColumns.contains(col) || m_userSortColumns.contains(col) || m_permanentTailSortColumns.contains(col);
  }

  /**
   * @return true if the column is a user sort column
   */
  public boolean isUserSortColumn(IColumn col) {
    return m_userSortColumns.contains(col);
  }

  /**
   * @return true if the column is a permanent-head sort column
   */
  public boolean isPermanentHeadSortColumn(IColumn col) {
    return m_permanentHeadSortColumns.contains(col);
  }

  /**
   * @return true if the column is a permanent-tail sort column
   */
  public boolean isPermanentTailSortColumn(IColumn col) {
    return m_permanentTailSortColumns.contains(col);
  }

  /**
   * @return the absolut sort index
   */
  public int getSortColumnIndex(IColumn col) {
    if (isPermanentHeadSortColumn(col)) {
      return m_permanentHeadSortColumns.indexOf(col);
    }
    if (isUserSortColumn(col)) {
      return m_permanentHeadSortColumns.size() + m_userSortColumns.indexOf(col);
    }
    if (isPermanentTailSortColumn(col)) {
      return m_permanentHeadSortColumns.size() + m_userSortColumns.size() + m_permanentTailSortColumns.indexOf(col);
    }
    return -1;
  }

  /**
   * add column at beginning of sort columns but keep sort history of max
   * keepHistoryCount last columns
   * <p>
   * The column is added as a user sort column
   */
  public void setSortColumn(IColumn<?> col, boolean ascending, int keepHistoryCount) {
    col = resolveColumn(col);
    if (col != null) {
      m_userSortColumns.remove(col);
      if (!isSortColumn(col)) {
        while (m_userSortColumns.size() > keepHistoryCount) {
          IColumn c = m_userSortColumns.remove(m_userSortColumns.size() - 1);
          HeaderCell cell = (HeaderCell) c.getHeaderCell();
          cell.setSortActive(false);
          cell.setSortExplicit(false);
          cell.setSortAscending(false);
          rebuildHeaderCell(c);
        }
        for (IColumn c : m_userSortColumns) {
          HeaderCell cell = (HeaderCell) c.getHeaderCell();
          cell.setSortExplicit(false);
          rebuildHeaderCell(c);
        }
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setSortActive(true);
        cell.setSortExplicit(true);
        cell.setSortAscending(ascending);
        m_userSortColumns.add(0, col);
        rebuildHeaderCell(col);
        fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
      }
    }
  }

  /**
   * add column to the user sort columns
   * <p>
   * see also {@link #addPermanentHeadSortColumn(IColumn, boolean)} and
   * {@link #addPermanentTailSortColumn(IColumn, boolean)}
   */
  public void addSortColumn(IColumn<?> col, boolean ascending) {
    col = resolveColumn(col);
    if (col != null) {
      m_userSortColumns.remove(col);
      if (!isSortColumn(col)) {
        for (Iterator<IColumn<?>> it = m_userSortColumns.iterator(); it.hasNext();) {
          IColumn c = it.next();
          if (!c.isSortExplicit()) {
            it.remove();
            HeaderCell cell = (HeaderCell) c.getHeaderCell();
            cell.setSortActive(false);
            cell.setSortExplicit(false);
            cell.setSortAscending(false);
            rebuildHeaderCell(c);
          }
        }
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setSortActive(true);
        cell.setSortExplicit(true);
        cell.setSortAscending(ascending);
        m_userSortColumns.add(col);
        rebuildHeaderCell(col);
        fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
      }
    }
  }

  public void toggleSortColumn(IColumn<?> col) {
    col = resolveColumn(col);
    if (col != null && isSortColumn(col) && !col.isSortPermanent()) {
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortAscending(!cell.isSortAscending());
      rebuildHeaderCell(col);
      fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
    }
  }

  private void updateColumnStructure(IColumn column) {
    reorganizeIndexes();
    checkMultiline();
    fireColumnStructureChanged();
  }

  private void resetColumnsViewOrder() {
    for (IColumn c : getColumns()) {
      c.setVisibleColumnIndexHint(-1);
    }
    reorganizeIndexes();
    fireColumnOrderChanged();
  }

  public void updateColumn(IColumn<?> column) {
    checkMultiline();
    fireColumnHeadersUpdated(CollectionUtility.hashSet(column));
  }

  public void removeSortColumn(IColumn<?> col) {
    col = resolveColumn(col);
    if (col != null) {
      m_userSortColumns.remove(col);
      if (!isSortColumn(col)) {
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setSortActive(false);
        cell.setSortExplicit(false);
        rebuildHeaderCell(col);
        fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
      }
    }
  }

  /**
   * only clears user sort columns.
   * <p>
   * see also {@link #clearPermanentHeadSortColumns()} and {@link #clearPermanentTailSortColumns()}
   */
  public void clearSortColumns() {
    if (m_userSortColumns.size() == 0) {
      return;
    }
    List<IColumn<?>> userSortColumnsBackup = new ArrayList<IColumn<?>>(m_userSortColumns);
    m_userSortColumns.clear();
    for (IColumn col : userSortColumnsBackup) {
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(false);
      cell.setSortExplicit(false);
    }
    for (IColumn c : userSortColumnsBackup) {
      rebuildHeaderCell(c);
    }
    fireColumnHeadersUpdated(userSortColumnsBackup);
  }

  public void clearPermanentHeadSortColumns() {
    if (m_permanentHeadSortColumns.size() == 0) {
      return;
    }
    List<IColumn<?>> currentColumnList = new ArrayList<IColumn<?>>(m_permanentHeadSortColumns);
    m_permanentHeadSortColumns.clear();
    for (IColumn col : currentColumnList) {
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(false);
      cell.setSortExplicit(false);
      cell.setSortPermanent(false);
    }
    for (IColumn col : currentColumnList) {
      rebuildHeaderCell(col);
    }
    fireColumnHeadersUpdated(currentColumnList);
  }

  public void clearPermanentTailSortColumns() {
    if (m_permanentTailSortColumns.size() == 0) {
      return;
    }
    List<IColumn<?>> currentColumnList = new ArrayList<IColumn<?>>(m_permanentTailSortColumns);
    m_permanentTailSortColumns.clear();
    for (IColumn col : currentColumnList) {
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(false);
      cell.setSortExplicit(false);
      cell.setSortPermanent(false);
    }
    for (IColumn col : currentColumnList) {
      rebuildHeaderCell(col);
    }
    fireColumnHeadersUpdated(currentColumnList);
  }

  public void addPermanentHeadSortColumn(IColumn<?> col, boolean ascending) {
    col = resolveColumn(col);
    if (col != null) {
      m_permanentHeadSortColumns.remove(col);
      //
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(true);
      cell.setSortExplicit(true);
      cell.setSortPermanent(true);
      cell.setSortAscending(ascending);
      m_permanentHeadSortColumns.add(col);
      rebuildHeaderCell(col);
      fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
    }
  }

  public void addPermanentTailSortColumn(IColumn<?> col, boolean ascending) {
    col = resolveColumn(col);
    if (col != null) {
      m_permanentTailSortColumns.remove(col);
      //
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(true);
      cell.setSortExplicit(true);
      cell.setSortPermanent(true);
      cell.setSortAscending(ascending);
      m_permanentTailSortColumns.add(col);
      rebuildHeaderCell(col);
      fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
    }
  }

  /**
   * mapping int array functions
   */
  private void reorganizeIndexes() {
    calculateDisplayableIndexes();
    calculateVisibleIndexes();
    calculateKeyIndexes();
  }

  private void calculateDisplayableIndexes() {
    int viewIndex = 0;
    Map<CompositeObject, Integer> displayableMap = new TreeMap<CompositeObject, Integer>();
    for (int modelIndex = 0; modelIndex < getColumnCount(); modelIndex++) {
      IColumn col = getColumn(modelIndex);
      if (col.isDisplayable()) {
        displayableMap.put(new CompositeObject(modelIndex), modelIndex);
      }
    }
    m_displayableIndexes = new int[displayableMap.size()];
    viewIndex = 0;
    for (int modelIndex : displayableMap.values()) {
      m_displayableIndexes[viewIndex++] = modelIndex;
    }
  }

  private void calculateVisibleIndexes() {
    int viewIndex = 0;
    Map<CompositeObject, Integer> visibleMap = new TreeMap<CompositeObject, Integer>();
    for (int modelIndex = 0; modelIndex < getColumnCount(); modelIndex++) {
      IColumn col = getColumn(modelIndex);
      if (col.isDisplayable() && col.isVisible()) {
        double viewHint = col.getVisibleColumnIndexHint();
        if (viewHint < 0) {
          viewHint = col.getViewOrder();
        }
        if (viewHint < 0) {
          viewHint = viewIndex;
        }
        visibleMap.put(new CompositeObject(viewHint, viewIndex), modelIndex);
        viewIndex++;
      }
    }
    m_visibleIndexes = new int[visibleMap.size()];
    viewIndex = 0;
    for (int modelIndex : visibleMap.values()) {
      m_visibleIndexes[viewIndex++] = modelIndex;
    }
  }

  private void calculateKeyIndexes() {
    List<Integer> keyIndexes = new ArrayList<Integer>();
    for (int modelIndex = 0; modelIndex < getColumnCount(); modelIndex++) {
      IColumn col = getColumn(modelIndex);
      if (col.isPrimaryKey()) {
        keyIndexes.add(modelIndex);
      }
    }
    m_keyIndexes = new int[keyIndexes.size()];
    int viewIndex = 0;
    for (int modelIndex : keyIndexes) {
      m_keyIndexes[viewIndex++] = modelIndex;
    }
  }

  private void rebuildHeaderCell(IColumn col) {
    col.decorateHeaderCell();
  }

  private void fireColumnHeadersUpdated(Collection<? extends IColumn<?>> columns) {
    TableEvent e = new TableEvent(m_table, TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
    e.setColumns(columns);
    m_table.fireTableEventInternal(e);
  }

  private void fireColumnOrderChanged() {
    TableEvent e = new TableEvent(m_table, TableEvent.TYPE_COLUMN_ORDER_CHANGED);
    m_table.fireTableEventInternal(e);
  }

  private void fireColumnStructureChanged() {
    TableEvent e = new TableEvent(m_table, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
    m_table.fireTableEventInternal(e);
  }

  private void checkMultiline() {
    if (m_table != null) {
      if (!m_table.isInitialMultilineText() && !ConfigurationUtility.isMethodOverwrite(AbstractTable.class, "getConfiguredMultilineText", null, m_table.getClass())) {
        //do automatic check for wrapping columns
        boolean m = false;
        for (IColumn col : getVisibleColumns()) {
          if (col instanceof IStringColumn && ((IStringColumn) col).isTextWrap()) {
            m = true;
            break;
          }
        }
        m_table.setMultilineText(m);
      }
    }
  }

}
