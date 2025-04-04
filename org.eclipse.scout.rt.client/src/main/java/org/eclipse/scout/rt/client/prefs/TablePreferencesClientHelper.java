/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.prefs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.table.ITableCustomizerDo;
import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.TableClientUiPreferenceProfileDo;
import org.eclipse.scout.rt.api.data.table.TableClientUiPreferenceProfileId;
import org.eclipse.scout.rt.api.data.table.TableClientUiPreferencesDo;
import org.eclipse.scout.rt.api.data.table.TableColumnAggregationFunctionId;
import org.eclipse.scout.rt.api.data.table.TableColumnBackgroundEffectId;
import org.eclipse.scout.rt.api.data.table.TableColumnClientUiPreferenceDo;
import org.eclipse.scout.rt.api.data.table.TableColumnId;
import org.eclipse.scout.rt.api.data.table.TableId;
import org.eclipse.scout.rt.client.prefs.userfilter.UserFilterStateHelper;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.InspectorObjectIdProvider;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for working with table preferences. Also see {@link TableClientUiPreferencesDo}
 */
@ApplicationScoped
public class TablePreferencesClientHelper {

  private static final Logger LOG = LoggerFactory.getLogger(TablePreferencesClientHelper.class);

  protected final LazyValue<UserFilterStateHelper> m_userFilterHelper = new LazyValue<>(UserFilterStateHelper.class);

  public TableId getTableId(ITable table) {
    return TableId.of(table.classId());
  }

  public TableColumnId getColumnId(IColumn<?> column) {
    return TableColumnId.of(BEANS.get(InspectorObjectIdProvider.class).getIdForColumn(column));
  }

  /**
   * Resolves the column based on the give column ID and logs a warning if no column was found.
   *
   * @return The found column or <code>null</code> if not column was found.
   */
  public IColumn<?> resolveColumn(ITable table, TableColumnId columnId) {
    if (columnId == null) {
      return null;
    }

    IColumn<?> column = table.getColumnSet().getColumns().stream()
        // compare column ids without debug prefixes
        .filter(col -> columnId.equals(getColumnId(col)))
        .findFirst()
        .orElse(null);

    if (column == null) {
      /*
       * TODO jwi [23.0] maybe change this to warn as soon as value migrations are available/class Ids can be validated.
       *  For now, client preferences may contain configured columns that no longer exist, because these preferences
       *  were never cleaned up.
       */
      LOG.debug("Unable to resolve column in table {} ({}) for column id {}.", getTableId(table), table.getClass().getSimpleName(), columnId);
      return null;
    }

    return column;
  }

  protected ITableCustomizerDo getTableCustomizerData(ITable table) {
    // FIXME bsh [js-bookmark] Add Scout implementation
    //    ITableCustomizer tableCustomizer = table.getTableCustomizer();
    //    if (tableCustomizer instanceof ICoreTableCustomizer) {
    //      return ((ICoreTableCustomizer) tableCustomizer).exportUserCustomizedData();
    //    }
    //
    //    // probably no customizer at all
    //    LOG.debug("Cannot export user customized columns for table {} as it does not provide a CoreTableCustomizer. Bookmark will be stored without custom columns.", table.getClass().getSimpleName());
    return null;
  }

  public TableClientUiPreferencesDo createTablePreferencesForBookmark(ITable table) {
    return createTablePreferences(table, TableClientUiPreferenceProfileId.BOOKMARK);
  }

  public TableClientUiPreferencesDo createTablePreferences(ITable table, TableClientUiPreferenceProfileId profileId) {
    return BEANS.get(TableClientUiPreferencesDo.class)
        .withTableId(getTableId(table))
        .withTileMode(table.isTileMode())
        .withTileGlobalKey(getTileGlobalKey(table))
        .withTablePreferenceProfiles(createTablePreferenceProfile(table, profileId));
  }

  protected Map<TableClientUiPreferenceProfileId, TableClientUiPreferenceProfileDo> createTablePreferenceProfile(ITable table, TableClientUiPreferenceProfileId profileId) {
    TableClientUiPreferenceProfileDo tablePreference = BEANS.get(TableClientUiPreferenceProfileDo.class)
        .withTableCustomizerData(getTableCustomizerData(table))
        .withColumns(createTableColumnPreferences(table));

    List<IUserFilterStateDo> userFilterStates = m_userFilterHelper.get().createUserFilterStates(table);
    if (!userFilterStates.isEmpty()) {
      tablePreference.withUserFilters(userFilterStates);
    }

    Map<TableClientUiPreferenceProfileId, TableClientUiPreferenceProfileDo> map = new HashMap<>();
    map.put(profileId, tablePreference);
    return map;
  }

  protected String getTileGlobalKey(ITable table) {
    // FIXME bsh [js-bookmark] Add Scout implementation
    //    // table has to be in tile mode (tile key is not enough as it is not cleared when switching modes)
    //    if (!table.isTileMode() || !(table instanceof ITileBeanTable) || !(((ITileBeanTable) table).getTableTileCustomizer() instanceof CoreTablePageTileCustomizer)) {
    //      return null;
    //    }
    //
    //    AbstractTableTileCustomizer<?> tableTileCustomizer = ((ITileBeanTable) table).getTableTileCustomizer();
    //    return (tableTileCustomizer != null && tableTileCustomizer.getTileBean() != null) ? tableTileCustomizer.getTileBean().getTileGlobalKey() : null;
    return null;
  }

  public List<TableColumnClientUiPreferenceDo> createTableColumnPreferences(ITable table) {
    return table.getColumnSet().getAllColumnsInUserOrder().stream()
        .map(this::createTableColumnPreference)
        .collect(Collectors.toList());
  }

  public TableColumnClientUiPreferenceDo createTableColumnPreference(IColumn<?> column) {
    List<ITableColumnPreferenceDoEntityMapper> columnMappers = BEANS.all(ITableColumnPreferenceDoEntityMapper.class);
    TableColumnClientUiPreferenceDo columnPreference = BEANS.get(TableColumnClientUiPreferenceDo.class);
    ITableColumnPreferenceDoEntityMapper columnMapper = columnMappers.stream()
        .filter(mapping -> mapping.accept(column))
        .findFirst()
        .orElseThrow();

    columnMapper.toDo(column, columnPreference);
    return columnPreference;
  }

  public void applyTableCustomizerData(IPageWithTable<?> tablePage, TableClientUiPreferenceProfileDo tableClientUiPreferenceProfileDo) {
    // FIXME bsh [js-bookmark] Add Scout implementation
    //    if (tablePage.getTable().getTableCustomizer() == null || tableClientUiPreferenceProfileDo == null || tableClientUiPreferenceProfileDo.getTableCustomizerData() == null) {
    //      return;
    //    }
    //
    //    ITable table = tablePage.getTable();
    //    ITableCustomizer tableCustomizer = table.getTableCustomizer();
    //    if (tableCustomizer instanceof ICoreTableCustomizer) {
    //      ICoreTableCustomizer coreTableCustomizer = (ICoreTableCustomizer) tableCustomizer;
    //      CoreTableCustomizerDo coreTableCustomizerDo = coreTableCustomizer.exportUserCustomizedData();
    //      // only load custom column configuration if it changed, this is mainly false if the bookmark is used to refresh the outline state
    //      // (actually opening a bookmark at original location as well as inlining in bookmarks outline should always require you to insert the custom columns,
    //      // unless you already performed that action during the current session)
    //      // re-inserting custom columns is
    //      // 1. costly
    //      // 2. it discards all rows and discarding all rows may have some unwanted side effects when trying to refresh the outline,
    //      // because the node that triggered it may suddenly be gone or detached from its outline
    //      if (!coreTableCustomizerDo.equals(tableClientUiPreferenceProfileDo.getTableCustomizerData())) {
    //        tableCustomizer.removeAllColumns();
    //        String stringData = BEANS.get(IDataObjectMapper.class).writeValue(tableClientUiPreferenceProfileDo.getTableCustomizerData());
    //        tableCustomizer.setSerializedData(stringData.getBytes(StandardCharsets.UTF_8));
    //        table.resetColumnConfiguration(); // loads the configuration from changed customizer
    //        tablePage.setChildrenLoaded(false);
    //      }
    //    }
  }

  public void applyTableColumnPreferences(ITable table, TableClientUiPreferenceProfileDo tableClientUiPreferenceProfileDo) {
    if (tableClientUiPreferenceProfileDo == null || CollectionUtility.isEmpty(tableClientUiPreferenceProfileDo.getColumns())) {
      return;
    }

    Collection<TableColumnClientUiPreferenceDo> columnPreferences = tableClientUiPreferenceProfileDo.getColumns();
    ColumnSet columnSet = table.getColumnSet();
    List<IColumn<?>> visibleColumns = new ArrayList<>();
    SortedMap<Integer, List<IColumn<?>>> sortedColumnMap = new TreeMap<>();
    Map<IColumn<?>, TableColumnClientUiPreferenceDo> sortedColumnToColumnPreference = new HashMap<>();
    HashSet<IColumn<?>> groupedColumns = new HashSet<>();
    boolean userSortValid = true;
    for (TableColumnClientUiPreferenceDo columnPreference : columnPreferences) {
      // resolve column for column preference
      IColumn<?> col = resolveColumn(table, columnPreference.getColumnId());
      if (col == null) {
        continue;
      }

      // determine visible
      boolean columnVisible = true;
      if (BooleanUtility.nvl(columnPreference.getVisible(), true) && col.isDisplayable()) {
        col.setVisibleColumnIndexHint(columnPreference.getViewIndex());
        visibleColumns.add(col);
      }
      else {
        columnVisible = false;
      }

      // if column is visible apply user configured width, styling, sorting etc.
      if (columnVisible) {
        // set width
        if (NumberUtility.nvl(columnPreference.getWidth(), -1) > 0) {
          col.setWidth(columnPreference.getWidth());
        }
        // set aggregation functions and background effect
        if (col instanceof INumberColumn) {
          INumberColumn<?> numberCol = (INumberColumn<?>) col;
          if (columnPreference.getAggregationFunctionId() != null) {
            numberCol.setAggregationFunction(columnPreference.getAggregationFunctionId().unwrap());
          }
          if (columnPreference.getBackgroundEffectId() != null) {
            numberCol.setBackgroundEffect(columnPreference.getBackgroundEffectId().unwrap());
          }
        }
      }

      // sort order
      if (NumberUtility.nvl(columnPreference.getSortOrder(), -1) >= 0) {
        sortedColumnMap.computeIfAbsent(columnPreference.getSortOrder(), order -> new ArrayList<>()).add(col);
        sortedColumnToColumnPreference.put(col, columnPreference);
        if (columnPreference.isGroupingActive()) {
          groupedColumns.add(col);
        }
        if (ObjectUtility.notEquals(col.getSortIndex(), columnPreference.getSortOrder())) {
          userSortValid = false;
        }
        if (ObjectUtility.notEquals(col.isSortAscending(), columnPreference.isSortAscending())) {
          userSortValid = false;
        }
        if (ObjectUtility.notEquals(col.isGroupingActive(), columnPreference.isGroupingActive())) {
          userSortValid = false;
        }
      }
    }

    // set visible columns
    List<IColumn<?>> existingVisibleCols = columnSet.getVisibleColumns();
    if (!existingVisibleCols.equals(visibleColumns)) {
      columnSet.setVisibleColumns(visibleColumns);
    }

    List<IColumn<?>> sortedColumnList = sortedColumnMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    // resolve sorting
    if (!CollectionUtility.containsAll(sortedColumnList, columnSet.getUserSortColumns())) {
      userSortValid = false;
    }

    if (userSortValid) {
      Set<IColumn<?>> existingGroupedUserSortCols = new HashSet<>();
      //check if grouping is valid also:
      for (IColumn<?> c : columnSet.getUserSortColumns()) {
        if (c.isGroupingActive()) {
          existingGroupedUserSortCols.add(c);
        }
      }
      if (!groupedColumns.containsAll(existingGroupedUserSortCols)) {
        userSortValid = false;
      }
    }

    if (!userSortValid) {
      columnSet.clearSortColumns();
      boolean groupingPossible = true;
      for (IColumn<?> headSortColumn : columnSet.getPermanentHeadSortColumns()) {
        if (!headSortColumn.isVisible() || !headSortColumn.isGroupingActive()) {
          TableColumnClientUiPreferenceDo columnPreference = sortedColumnToColumnPreference.get(headSortColumn);
          if (columnPreference != null && !columnPreference.isGroupingActive()) {
            groupingPossible = false;
            break;
          }
        }
      }

      for (IColumn<?> col : sortedColumnList) {
        TableColumnClientUiPreferenceDo state = sortedColumnToColumnPreference.get(col);
        if (groupingPossible) {
          if (state.isGroupingActive()) {
            columnSet.addGroupingColumn(col, state.isSortAscending());
          }
          else {
            columnSet.addSortColumn(col, state.isSortAscending());
            groupingPossible = false;
          }
        }
        else {
          columnSet.addSortColumn(col, state.isSortAscending());
        }
      }
      table.sort();
    }

    ClientUIPreferences.getInstance().setAllTableColumnPreferences(table);
  }

  public void applyTileMode(ITable table, TableClientUiPreferencesDo tableClientUiPreferencesDo) {
    if (tableClientUiPreferencesDo == null) {
      return;
    }
    table.setTileMode(tableClientUiPreferencesDo.isTileMode());
  }

  //  public void applyTileBean(ITable table, TableClientUiPreferencesDo tableClientUiPreferencesDo) {
  //    if (tableClientUiPreferencesDo == null || StringUtility.isNullOrEmpty(tableClientUiPreferencesDo.getTileGlobalKey()) || !(table instanceof ITileBeanTable)) {
  //      return;
  //    }
  //
  //    AbstractTableTileCustomizer<?> tableTileCustomizer = ((ITileBeanTable) table).getTableTileCustomizer();
  //    if (tableTileCustomizer == null || CollectionUtility.isEmpty(tableTileCustomizer.getTileBeans())) {
  //      return;
  //    }
  //    String tileGlobalKey = tableClientUiPreferencesDo.getTileGlobalKey();
  //    TileBean currTileBean = tableTileCustomizer.getTileBeans()
  //        .stream()
  //        .filter(Objects::nonNull)
  //        .filter(tileBean -> tileGlobalKey.equals(tileBean.getTileGlobalKey()))
  //        .findFirst()
  //        .orElse(tableTileCustomizer.getTileBeans().get(0));
  //    tableTileCustomizer.setTileBean(currTileBean);
  //  }

  @ApplicationScoped
  public interface ITableColumnPreferenceDoEntityMapper {

    boolean accept(IColumn<?> column);

    void toDo(IColumn<?> source, TableColumnClientUiPreferenceDo doEntity);
  }

  @Order(999_999) // always last
  @ApplicationScoped
  public static class DefaultTableColumnPreferenceDoEntityMapper extends AbstractDoEntityMapper<TableColumnClientUiPreferenceDo, IColumn<?>> implements ITableColumnPreferenceDoEntityMapper {

    @Override
    public boolean accept(IColumn<?> column) {
      return true;
    }

    @Override
    protected void initMappings(DoEntityMappings<TableColumnClientUiPreferenceDo, IColumn<?>> mappings) {
      mappings
          .with(TableColumnClientUiPreferenceDo::columnId, this::getColumnId)
          .with(TableColumnClientUiPreferenceDo::width, IColumn::getWidth)
          .with(TableColumnClientUiPreferenceDo::visible, IColumn::isVisible)
          .with(TableColumnClientUiPreferenceDo::viewIndex, IColumn::getVisibleColumnIndexHint)
          .with(TableColumnClientUiPreferenceDo::sortOrder, IColumn::getSortIndex)
          .with(TableColumnClientUiPreferenceDo::sortAscending, IColumn::isSortAscending)
          .with(TableColumnClientUiPreferenceDo::groupingActive, IColumn::isGroupingActive);
    }

    protected TableColumnId getColumnId(IColumn<?> col) {
      return BEANS.get(TablePreferencesClientHelper.class).getColumnId(col);
    }
  }

  @Order(1000)
  @ApplicationScoped
  public static class NumberTableColumnPreferenceDoEntityMapper extends AbstractDoEntityMapper<TableColumnClientUiPreferenceDo, INumberColumn<?>> implements ITableColumnPreferenceDoEntityMapper {

    @Override
    public boolean accept(IColumn<?> column) {
      return column instanceof INumberColumn<?>;
    }

    @Override
    public void toDo(IColumn<?> source, TableColumnClientUiPreferenceDo doEntity) {
      toDo((INumberColumn<?>) source, doEntity);
    }

    @Override
    protected void initMappings(DoEntityMappings<TableColumnClientUiPreferenceDo, INumberColumn<?>> mappings) {
      mappings
          .with(TableColumnClientUiPreferenceDo::columnId, this::getColumnId)
          .with(TableColumnClientUiPreferenceDo::width, IColumn::getWidth)
          .with(TableColumnClientUiPreferenceDo::visible, IColumn::isVisible)
          .with(TableColumnClientUiPreferenceDo::viewIndex, IColumn::getVisibleColumnIndexHint)
          .with(TableColumnClientUiPreferenceDo::sortOrder, IColumn::getSortIndex)
          .with(TableColumnClientUiPreferenceDo::sortAscending, IColumn::isSortAscending)
          .with(TableColumnClientUiPreferenceDo::groupingActive, IColumn::isGroupingActive)
          .with(TableColumnClientUiPreferenceDo::aggregationFunctionId, col -> TableColumnAggregationFunctionId.of(col.getAggregationFunction()))
          .with(TableColumnClientUiPreferenceDo::backgroundEffectId, col -> TableColumnBackgroundEffectId.of(col.getBackgroundEffect()));
    }

    protected TableColumnId getColumnId(IColumn<?> col) {
      return BEANS.get(TablePreferencesClientHelper.class).getColumnId(col);
    }
  }
}
