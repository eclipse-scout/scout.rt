/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.shared.data.basic.table.SortSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnSet {
  private static final Logger LOG = LoggerFactory.getLogger(ColumnSet.class);

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
  private final Map<Class, IColumn> m_classIndexes = new HashMap<>();

  /**
   * ID to model
   */
  private final Map<String, IColumn> m_idIndexes = new HashMap<>();

  private final Map<Class<?>, Class<? extends IColumn>> m_columnReplacements;

  private final P_ColumnListener m_columnListener = new P_ColumnListener();

  public ColumnSet(AbstractTable table, List<IColumn<?>> columns) {
    // process @Replace annotations
    List<Class<? extends IColumn>> columnArray = new ArrayList<>(columns.size());
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
    m_columns = new ArrayList<>(columns.size());
    m_userSortColumns = new ArrayList<>();
    m_permanentHeadSortColumns = new ArrayList<>();
    m_permanentTailSortColumns = new ArrayList<>();

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
      index++;
    }
    reorganizeIndexes();
  }

  public void initialize() {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    //clean up visible column index hints, make as permutation of model indices
    int n = getColumnCount();
    SortedMap<CompositeObject, IColumn> sortMap = new TreeMap<>();
    int viewIndex = 0;
    for (int modelIndex = 0; modelIndex < n; modelIndex++) {
      IColumn col = getColumn(modelIndex);
      double viewHint = col.getVisibleColumnIndexHint();
      if (viewHint < 0) {
        viewHint = col.getOrder();
      }
      if (viewHint < 0) {
        viewHint = viewIndex;
      }
      sortMap.put(new CompositeObject(viewHint, viewIndex), col);
      // next
      viewIndex++;
    }
    viewIndex = 0;
    for (Entry<CompositeObject, IColumn> e : sortMap.entrySet()) {
      e.getValue().setVisibleColumnIndexHint(viewIndex);
      viewIndex++;
    }
    reorganizeIndexes();
    applySortingAndGrouping(null);
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
        prefs.removeAllTableColumnPreferences(c, null, true);
        //next
        viewIndex++;
      }
      reorganizeIndexes();
    }
    checkMultiline();
  }

  protected void initColumns() {
    for (IColumn<?> c : getColumns()) {
      try {
        c.initColumn();
      }
      catch (Exception t) {
        LOG.error("Could not init column {}", c, t);
      }
    }
    initialize();
    for (IColumn<?> c : getColumns()) {
      c.removePropertyChangeListener(m_columnListener);
      c.addPropertyChangeListener(m_columnListener);
    }
  }

  protected void disposeColumns() {
    for (IColumn<?> c : getColumns()) {
      try {
        c.disposeColumn();
        c.removePropertyChangeListener(m_columnListener);
      }
      catch (Exception t) {
        LOG.error("Could not dispose column {}", c, t);
      }
    }
  }

  private static class P_SortingAndGroupingConfig {
    private int m_sortIndex;
    private boolean m_ascending;
    private boolean m_grouped;
    private String m_aggregationFunction;

    public int getSortIndex() {
      return m_sortIndex;
    }

    public void setSortIndex(int sortIndex) {
      m_sortIndex = sortIndex;
    }

    public boolean isAscending() {
      return m_ascending;
    }

    public void setAscending(boolean ascending) {
      m_ascending = ascending;
    }

    public boolean isGrouped() {
      return m_grouped;
    }

    public void setGrouped(boolean grouped) {
      m_grouped = grouped;
    }

    public String getAggregationFunction() {
      return m_aggregationFunction;
    }

    public void setAggregationFunction(String aggregationFunction) {
      m_aggregationFunction = aggregationFunction;
    }

  }

  private P_SortingAndGroupingConfig createSortingAndGroupingConfig(IColumn<?> col, String configName) {
    if (col.isInitialAlwaysIncludeSortAtBegin() || col.isInitialAlwaysIncludeSortAtEnd()) {
      return createSortingAndGroupingConfig(col);
    }
    P_SortingAndGroupingConfig config = new P_SortingAndGroupingConfig();
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    config.setSortIndex(prefs.getTableColumnSortIndex(col, col.getInitialSortIndex(), configName));
    config.setAscending(prefs.getTableColumnSortAscending(col, col.isInitialSortAscending(), configName));
    config.setGrouped(prefs.getTableColumnGrouped(col, col.isInitialGrouped(), configName));
    if (col instanceof INumberColumn) {
      config.setAggregationFunction(prefs.getTableColumnAggregationFunction(col, ((INumberColumn) col).getInitialAggregationFunction(), configName));
    }
    return config;
  }

  private P_SortingAndGroupingConfig createSortingAndGroupingConfig(IColumn<?> col) {
    P_SortingAndGroupingConfig config = new P_SortingAndGroupingConfig();
    config.setSortIndex(col.getInitialSortIndex());
    config.setAscending(col.isInitialSortAscending());
    config.setGrouped(col.isInitialGrouped());
    if (col instanceof INumberColumn) {
      config.setAggregationFunction(((INumberColumn) col).getInitialAggregationFunction());
    }
    return config;
  }

  private void applySortingAndGroupingInternal(Map<IColumn, P_SortingAndGroupingConfig> columnConfigs) {
    SortedMap<CompositeObject, IColumn> sortMap = new TreeMap<>();
    int index = 0;
    for (IColumn col : getColumns()) {

      int sortIndex = -1;
      if (col.isInitialAlwaysIncludeSortAtBegin()) {
        sortIndex = col.getInitialSortIndex();
        if (sortIndex < 0) {
          LOG.warn("AlwaysIncludeSortAtBegin is set but no sort index configured. Table: {}", m_table.getClass().getName());
        }
      }
      else if (col.isInitialAlwaysIncludeSortAtEnd()) {
        sortIndex = col.getInitialSortIndex();
        if (sortIndex < 0) {
          LOG.warn("AlwaysIncludeSortAtEnd is set but no sort index configured. Table: {}", m_table.getClass().getName());
        }
      }
      else {
        sortIndex = columnConfigs.get(col).getSortIndex();
      }
      if (sortIndex >= 0) {
        sortMap.put(new CompositeObject(sortIndex, index), col);
      }

      //aggregation function:
      if (col instanceof INumberColumn) {
        ((INumberColumn) col).setAggregationFunction(columnConfigs.get(col).getAggregationFunction());
      }

      index++;
    }

    clearSortColumns();
    clearPermanentHeadSortColumns();
    clearPermanentTailSortColumns();
    boolean asc;
    for (IColumn col : sortMap.values()) {
      if (col.isInitialAlwaysIncludeSortAtBegin()) {
        asc = col.isInitialSortAscending();
        addPermanentHeadSortColumn(col, asc);
      }
      else if (col.isInitialAlwaysIncludeSortAtEnd()) {
        asc = col.isInitialSortAscending();
        addPermanentTailSortColumn(col, asc);
      }
      else {
        asc = columnConfigs.get(col).isAscending();
        addSortColumn(col, asc);
      }
    }

    applyGrouping(columnConfigs);

  }

  public void resetSortingAndGrouping() {
    Map<IColumn, P_SortingAndGroupingConfig> columnConfigs = new HashMap<>();
    for (IColumn<?> col : getColumns()) {
      columnConfigs.put(col, createSortingAndGroupingConfig(col));
    }
    applySortingAndGroupingInternal(columnConfigs);
  }

  public void applySortingAndGrouping(String configName) {

    //build config map:
    Map<IColumn, P_SortingAndGroupingConfig> columnConfigs = new HashMap<>();
    for (IColumn<?> col : getColumns()) {
      columnConfigs.put(col, createSortingAndGroupingConfig(col, configName));
    }

    applySortingAndGroupingInternal(columnConfigs);
    m_table.sort();
  }

  /**
   * Applies the grouping property of columns. </br>
   * <b>Important:</b> This is already called by {@link ColumnSet#applySorting()}. </br>
   * If you do call it, Make sure sorting is applied first.
   *
   * @param configName
   */
  private void applyGrouping(Map<IColumn, P_SortingAndGroupingConfig> columnConfigs) {
    //all columns that are not sort columns but have grouping property set will not be grouped:
    for (IColumn<?> col : getColumns()) {
      if (col.getSortIndex() < 0 && col.isGroupingActive()) {
        LOG.warn("Column marked as grouped but no sort index set. Column will not be grouped");
        ((HeaderCell) col.getHeaderCell()).setGroupingActive(false);
      }
    }

    //First: head sort columns:
    applyGroupingConfiguration(columnConfigs, getPermanentHeadSortColumns(), true);

    //Custom sort columns
    boolean valid = isUserColumnGroupingAllowed();
    applyGroupingConfiguration(columnConfigs, getUserSortColumns(), valid);
  }

  /**
   * Applies the grouping state of columns (if valid)
   *
   * @param configName
   * @param columns
   * @param initiallyValid
   */
  private void applyGroupingConfiguration(Map<IColumn, P_SortingAndGroupingConfig> columnConfigs, List<IColumn<?>> columns, boolean initiallyValid) {
    boolean allPreviousColumnsGroupedAndVisible = initiallyValid;
    boolean grouped;
    for (IColumn<?> col : columns) {
      grouped = columnConfigs.get(col).isGrouped();
      HeaderCell headerCell = (HeaderCell) col.getHeaderCell();
      if (allPreviousColumnsGroupedAndVisible) {
        if (grouped && col.isVisible()) {
          headerCell.setGroupingActive(true);
        }
        else {
          headerCell.setGroupingActive(false);
          allPreviousColumnsGroupedAndVisible = false;
        }
      }
      else {
        if (grouped) {
          LOG.warn("Invalid column grouping config. " +
              "Column marked as grouped but there are non-grouped or non-visible sort columns with smaller sort index. " +
              "Grouping will be set to false.");
        }
        headerCell.setGroupingActive(false);
      }
    }
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
    SortedMap<CompositeObject, IColumn<?>> sortMap = new TreeMap<>();
    for (IColumn col : visibleCols) {
      counter++;
      sortMap.put(new CompositeObject(col.getVisibleColumnIndexHint(), counter), col);
    }

    for (IColumn<?> column : getColumns()) {
      if (column.isDisplayable() && column.isVisible()) {
        //already in map
      }
      else {
        counter++;
        sortMap.put(new CompositeObject(column.getVisibleColumnIndexHint(), counter), column);
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
   * Checks whether the column with the given class has been replaced by another column. If so, the replacing column's
   * class is returned. Otherwise the given class itself.
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
    List<IColumn<?>> keyColumns = new ArrayList<>(m_keyIndexes.length);
    for (int m_keyIndexe : m_keyIndexes) {
      keyColumns.add(getColumn(m_keyIndexe));
    }
    return keyColumns;
  }

  public List<IColumn<?>> getDisplayableColumns() {
    List<IColumn<?>> a = new ArrayList<>(m_displayableIndexes.length);
    for (int m_displayableIndexe : m_displayableIndexes) {
      a.add(getColumn(m_displayableIndexe));
    }
    return a;
  }

  public List<IColumn<?>> getVisibleColumns() {
    List<IColumn<?>> a = new ArrayList<>(m_visibleIndexes.length);
    for (int m_visibleIndexe : m_visibleIndexes) {
      a.add(getColumn(m_visibleIndexe));
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
    for (int m_visibleIndexe : m_visibleIndexes) {
      if (Integer.compare(m_visibleIndexe, colIdx) < 0) {
        colIdx = m_visibleIndexe;
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
    List<IColumn<?>> summaryColumns = new ArrayList<>();
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
        List<IColumn<?>> list = new ArrayList<>();
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
      if (!resolvedColumns.isEmpty() || columns.isEmpty()) {
        List<IColumn<?>> newColumns = new ArrayList<>();
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
      List<IColumn<?>> result = new ArrayList<>(columns.size());
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
   * Sorting and Grouping
   */

  /**
   * @param col
   * @param multiSort
   *          True: Multiple sort columns are supported, which means the given column is added to the list of sort
   *          columns, if not added yet.<br>
   *          False: The selected column is set as the (new) primary sort column.<br>
   * @param ascending
   *          Specifies the new sort direction
   */
  public void handleSortEvent(IColumn col, boolean multiSort, boolean ascending) {
    col = resolveColumn(col);
    if (col == null) {
      return;
    }
    //
    try {
      m_table.setTableChanging(true);
      //
      if (multiSort) {
        if (isSortColumn(col)) {
          updateSortColumn(col, ascending);
        }
        else {
          addSortColumn(col, ascending);
        }
      }
      else {
        for (IColumn c : m_userSortColumns) {
          if (c != col) {
            ((HeaderCell) c.getHeaderCell()).setGroupingActive(false);
          }
        }
        if (isSortColumn(col) && getSortColumnCount() == 1) {
          updateSortColumn(col, ascending);
        }
        else {
          setSortColumn(col, ascending);
          ((HeaderCell) col.getHeaderCell()).setGroupingActive(false);
        }
      }
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  /**
   * Checks if the user may define a grouping column. This depends on the defined permanend head sort columns.
   * Specifically, for a user group column to make sense, all permanent head sort columns must be both visible and
   * grouped.
   */
  public boolean isUserColumnGroupingAllowed() {
    for (IColumn<?> col : getPermanentHeadSortColumns()) {
      if (!(col.isVisible() && col.isGroupingActive())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks for a specific column if it may be grouped.
   */
  public boolean isGroupingAllowed(IColumn<?> col) {
    if (isPermanentTailSortColumn(col)) {
      return false;
    }

    //all previous head sort cols must be visible and grouped.
    //col itself must be visible
    for (IColumn<?> other : getPermanentHeadSortColumns()) {
      if (other == col) {
        return col.isVisible();
      }
      if (!(other.isVisible() && other.isGroupingActive())) {
        return false;
      }
    }

    //all head sort cols are grouped and visible.
    return col.isVisible();
  }

  /**
   * Handles the case when a column with grouping active becomes invisible. Since grouping is done in the ui only, an
   * invisible grouped column does not make sense. (Ui only knows visible columns) </br>
   * Also, if there are other grouped columns, the invisible state would lead to wrong grouping. </br>
   * This is because firstly, the ui cannot group values it does not see, and </br>
   * secondly, the implicit remaining sorting would falsify the visible groups. Therefore, both grouping and sorting
   * must be removed.
   *
   * @param col
   * @return
   */
  public void onGroupedColumnInvisible(IColumn<?> col) {
    m_table.setTableChanging(true);
    try {
      if (!isGroupingColumn(col) || (col.isVisible())) {
        return;
      }
      // removes both sorting and grouping:
      removeGroupColumn(col);
      m_table.onGroupedColumnInvisible(col);
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  public boolean isGroupingColumn(IColumn<?> col) {
    IColumn rCol = resolveColumn(col);
    return rCol != null && isSortColumn(rCol) && rCol.isGroupingActive();
  }

  public void updateGroupingColumn(IColumn<?> col, boolean ascending) {
    col = resolveColumn(col);
    if (!isGroupingColumn(col) || col.isSortPermanent()) {
      return;
    }

    HeaderCell cell = (HeaderCell) col.getHeaderCell();
    cell.setSortAscending(ascending);
    rebuildHeaderCell(col);
    fireColumnHeadersUpdated(CollectionUtility.hashSet(col));

  }

  /**
   * Adds an additional grouping column. This is done by inserting it as a user-sort column just after other grouped
   * columns, but before other the other user sort columns.
   *
   * @param col
   * @param ascending
   */
  public void addGroupingColumn(IColumn<?> col, boolean ascending) {
    col = resolveColumn(col);
    if (col == null) {
      return;
    }

    if (!isGroupingAllowed(col)) {
      return;
    }

    if (isPermanentHeadSortColumn(col)) {
      //we have assured that grouping is allowed,
      //in case of permanent head sort column, we need
      //only set the grouping property.
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setGroupingActive(true);
      return;
    }

    m_userSortColumns.remove(col);

    //find the first correct sort index for this grouping column,
    //which is the first index that is not grouped
    int index = 0;
    for (IColumn<?> column : m_userSortColumns) {
      if (!column.isGroupingActive()) {
        break;
      }
      index++;
    }

    if (index == 0) {
      LOG.warn("Multi grouping event, but no column currently grouped");
    }

    if (!isSortColumn(col)) {
      m_userSortColumns.add(index, col);
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(true);
      cell.setSortAscending(ascending);
      cell.setGroupingActive(true);
      fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
    }

  }

  /**
   * Adds column at first position of user sort columns, and marks it as grouped. </br>
   * Other columns that were previously grouped are un-grouped and will no longer be sorted. </br>
   * Other sort-columns that were previously sorted will be shifted accordingly, but will still be sorted. </br>
   *
   * @param col
   * @param ascending
   */
  public void setGroupingColumn(IColumn<?> col, boolean ascending) {
    col = resolveColumn(col);
    if (col == null) {
      return;
    }

    if (!isGroupingAllowed(col)) {
      return;
    }

    m_userSortColumns.remove(col);
    List<IColumn<?>> toBeRemoved = new ArrayList<>();
    for (IColumn<?> column : getSortColumns()) {
      if (column.isGroupingActive()) {
        toBeRemoved.add(column);
      }
    }

    for (IColumn<?> victim : toBeRemoved) {
      removeGroupColumn(victim);
    }

    if (!isSortColumn(col)) {
      m_userSortColumns.add(0, col);
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(true);
      cell.setSortAscending(ascending);
      cell.setGroupingActive(true);
    }
    else {
      //permanent sort column
      if (isPermanentHeadSortColumn(col)) {
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setGroupingActive(true);
      }
    }

    fireColumnHeadersUpdated(CollectionUtility.hashSet(col));

  }

  public void handleGroupingEvent(IColumn col, boolean multiGroup, boolean ascending) {
    col = resolveColumn(col);
    if (col == null) {
      return;
    }
    if (!isGroupingAllowed(col)) {
      return;
    }
    //
    try {
      m_table.setTableChanging(true);
      if (multiGroup) {
        if (col.isGroupingActive()) {
          updateGroupingColumn(col, ascending);
        }
        else {
          addGroupingColumn(col, ascending);
        }
      }
      else {
        setGroupingColumn(col, ascending);
      }
    }
    finally {
      m_table.setTableChanging(false);
    }

  }

  public void removeGroupColumn(IColumn<?> col) {

    col = resolveColumn(col);
    if (col == null) {
      return;
    }

    if (isPermanentHeadSortColumn(col)) {
      List<IColumn<?>> toBeRemoved = new ArrayList<>();
      //must remove all following grouping columns as well.
      boolean after = false;
      for (IColumn<?> other : getSortColumns()) {
        if (other == col) {
          after = true;
        }
        if (after) {
          ((HeaderCell) other.getHeaderCell()).setGroupingActive(false);
          toBeRemoved.add(other);
        }
      }

      for (IColumn<?> c : toBeRemoved) {
        removeSortColumn(c);
      }
    }
    else {
      //remove from user cols and set grouped to false
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setGroupingActive(false);
      removeSortColumn(col);
    }

  }

  public void setAggregationFunction(INumberColumn<?> col, String f) {
    col.setAggregationFunction(f);
  }

  public int getSortColumnCount() {
    return m_userSortColumns.size() + m_permanentHeadSortColumns.size() + m_permanentTailSortColumns.size();
  }

  /**
   * @return all sort columns including permanent-head, user, permanent-tail
   */
  public List<IColumn<?>> getSortColumns() {
    List<IColumn<?>> list = new ArrayList<>(getSortColumnCount());
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
   * @return number of columns with grouping active
   */
  public int getGroupedColumnCount() {
    return getGroupedColumns().size();
  }

  /**
   * @return a list of all grouped columns, in sort order.
   */
  public List<IColumn<?>> getGroupedColumns() {
    List<IColumn<?>> result = new ArrayList<>();
    for (IColumn<?> c : getSortColumns()) {
      if (c.isGroupingActive()) {
        result.add(c);
      }
    }
    return result;
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
    List<IColumn<?>> sortColumns = new ArrayList<>();
    sortColumns.addAll(getSortColumns());
    if (!sortColumns.isEmpty()) {
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
      }
      m_userSortColumns.clear();
      List<IColumn<?>> colList = new ArrayList<>();
      for (int i = 0; i < spec.size(); i++) {
        IColumn col = getColumn(spec.getColumnIndex(i));
        if (col != null && (!isSortColumn(col))) {
          HeaderCell cell = (HeaderCell) col.getHeaderCell();
          cell.setSortActive(true);
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
   * The column is added as a user sort column
   */
  public void setSortColumn(IColumn<?> col, boolean ascending) {
    col = resolveColumn(col);
    if (col != null) {
      clearSortColumns();
      if (!isSortColumn(col)) {
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setSortActive(true);
        cell.setSortAscending(ascending);
        cell.setGroupingActive(false);
        m_userSortColumns.add(col);
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
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setSortActive(true);
        cell.setSortAscending(ascending);
        m_userSortColumns.add(col);
        rebuildHeaderCell(col);
        fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
      }
    }
  }

  public void updateSortColumn(IColumn<?> col, boolean ascending) {
    col = resolveColumn(col);
    if (col == null || !isSortColumn(col) || col.isSortPermanent() || col.isSortAscending() == ascending) {
      return;
    }

    HeaderCell cell = (HeaderCell) col.getHeaderCell();
    cell.setSortAscending(ascending);
    rebuildHeaderCell(col);
    fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
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
    if (col == null) {
      return;
    }

    m_table.setTableChanging(true);
    try {
      m_userSortColumns.remove(col);
      if (!isSortColumn(col)) {
        HeaderCell cell = (HeaderCell) col.getHeaderCell();
        cell.setSortActive(false);
        cell.setGroupingActive(false);
        rebuildHeaderCell(col);
        fireColumnHeadersUpdated(CollectionUtility.hashSet(col));
      }
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  /**
   * only clears user sort columns.
   * <p>
   * see also {@link #clearPermanentHeadSortColumns()} and {@link #clearPermanentTailSortColumns()}
   */
  public void clearSortColumns() {
    if (m_userSortColumns.isEmpty()) {
      return;
    }
    List<IColumn<?>> userSortColumnsBackup = new ArrayList<>(m_userSortColumns);
    m_userSortColumns.clear();
    for (IColumn col : userSortColumnsBackup) {
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(false);
      cell.setGroupingActive(false);
    }
    for (IColumn c : userSortColumnsBackup) {
      rebuildHeaderCell(c);
    }
    fireColumnHeadersUpdated(userSortColumnsBackup);
  }

  public void clearPermanentHeadSortColumns() {
    if (m_permanentHeadSortColumns.isEmpty()) {
      return;
    }
    List<IColumn<?>> currentColumnList = new ArrayList<>(m_permanentHeadSortColumns);
    m_permanentHeadSortColumns.clear();
    for (IColumn col : currentColumnList) {
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(false);
      cell.setSortPermanent(false);
      cell.setGroupingActive(false);
    }
    for (IColumn col : currentColumnList) {
      rebuildHeaderCell(col);
    }
    fireColumnHeadersUpdated(currentColumnList);
  }

  public void clearPermanentTailSortColumns() {
    if (m_permanentTailSortColumns.isEmpty()) {
      return;
    }
    List<IColumn<?>> currentColumnList = new ArrayList<>(m_permanentTailSortColumns);
    m_permanentTailSortColumns.clear();
    for (IColumn col : currentColumnList) {
      HeaderCell cell = (HeaderCell) col.getHeaderCell();
      cell.setSortActive(false);
      cell.setSortPermanent(false);
      cell.setGroupingActive(false);
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
    Map<CompositeObject, Integer> displayableMap = new TreeMap<>();
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
    Map<CompositeObject, Integer> visibleMap = new TreeMap<>();
    for (int modelIndex = 0; modelIndex < getColumnCount(); modelIndex++) {
      IColumn col = getColumn(modelIndex);
      if (col.isDisplayable() && col.isVisible()) {
        double viewHint = col.getVisibleColumnIndexHint();
        if (viewHint < 0) {
          viewHint = col.getOrder();
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
    List<Integer> keyIndexes = new ArrayList<>();
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

  private void fireColumnAggregationChanged(IColumn<?> c) {
    Assertions.assertInstance(c, INumberColumn.class, "ColumnAggregation is only supported on NumberColumns.");
    TableEvent e = new TableEvent(m_table, TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED);
    e.setColumns(CollectionUtility.arrayList(c));
    m_table.fireTableEventInternal(e);
  }

  private void fireColumnBackgroundEffectChanged(IColumn<?> c) {
    Assertions.assertInstance(c, INumberColumn.class, "BackgroundEffect is only supported on NumberColumns.");
    TableEvent e = new TableEvent(m_table, TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED);
    e.setColumns(CollectionUtility.arrayList(c));
    m_table.fireTableEventInternal(e);
  }

  private void fireColumnStructureChanged() {
    TableEvent e = new TableEvent(m_table, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
    m_table.fireTableEventInternal(e);
  }

  private void checkMultiline() {
    if (m_table != null
        && !m_table.isInitialMultilineText()
        && !ConfigurationUtility.isMethodOverwrite(AbstractTable.class, "getConfiguredMultilineText", null, m_table.getClass())) {
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

  private class P_ColumnListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent e) {
      IColumn c = (IColumn) e.getSource();

      if (IColumn.PROP_VIEW_COLUMN_INDEX_HINT.equals(e.getPropertyName())) {
        return;
      }
      if (IColumn.PROP_ORDER.equals(e.getPropertyName())) {
        resetColumnsViewOrder();
        return;
      }
      if (INumberColumn.PROP_AGGREGATION_FUNCTION.equals(e.getPropertyName())) {
        fireColumnAggregationChanged(c);
        return;
      }
      if (INumberColumn.PROP_BACKGROUND_EFFECT.equals(e.getPropertyName())) {
        fireColumnBackgroundEffectChanged(c);
        return;
      }
      if (c.isGroupingActive() && IColumn.PROP_VISIBLE.equals(e.getPropertyName())) {
        onGroupedColumnInvisible(c); //also notifies table and to invalidate sorting.
      }
      //default:
      updateColumnStructure(c);
    }
  }

}
