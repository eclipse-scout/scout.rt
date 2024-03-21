/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.prefs.CustomClientPreferenceId;
import org.eclipse.scout.rt.shared.prefs.ICustomClientPreferenceDo;
import org.eclipse.scout.rt.shared.services.common.prefs.IPreferences;
import org.eclipse.scout.rt.shared.services.common.prefs.Preferences;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI model customization wrapping a {@link IPreferences} object.
 */
@Bean
public class ClientUIPreferences {

  private static final Logger LOG = LoggerFactory.getLogger(ClientUIPreferences.class);

  /**
   * @return a new instance of the {@link ClientUIPreferences}
   * @throws IllegalArgumentException
   *           When no {@link IClientSession} is available in the current thread context ( {@link ISession#CURRENT}).
   */
  public static ClientUIPreferences getInstance() {
    return getInstance(ClientSessionProvider.currentSession());
  }

  /**
   * @return a new instance of the {@link ClientUIPreferences} for the specified {@link IClientSession}.
   */
  public static ClientUIPreferences getInstance(IClientSession session) {
    ClientUIPreferences preferences = BEANS.get(ClientUIPreferences.class);
    preferences.load(session);
    return preferences;
  }

  protected static final String PREFERENCES_NODE_ID = "org.eclipse.scout.rt.client";

  protected static final String TABLE_COLUMNS_CONFIGS = "table.columns.configs.";
  protected static final String TABLE_CUSTOMIZER_DATA = "table.customizer.data.";
  protected static final String TABLE_COLUMN_UIINDEX = "table.column.viewIndex.";
  protected static final String TABLE_COLUMN_WIDTH = "table.column.width.";
  protected static final String TABLE_COLUMN_VISIBLE = "table.column.visible.";
  protected static final String TABLE_COLUMN_SORT_INDEX = "table.column.sortIndex.";
  protected static final String TABLE_COLUMN_GROUPED = "table.column.grouped.";
  protected static final String TABLE_COLUMN_AGGR_FUNCTION = "table.column.aggr.function.";
  protected static final String TABLE_COLUMN_BACKGROUND_EFFECT = "table.column.background.effect.";
  protected static final String TABLE_COLUMN_SORT_ASC = "table.column.sortAsc.";
  protected static final String TABLE_COLUMN_SORT_EXPLICIT = "table.column.sortExplicit.";
  protected static final String TABLE_TILE_MODE = "table.tile.mode";
  protected static final String CALENDAR_DISPLAY_MODE = "calendar.display.mode";
  protected static final String CALENDAR_DISPLAY_CONDENSED = "calendar.display.condensed";
  protected static final String DESKTOP_COLUMN_SPLITS = "desktop.columnSplits";
  protected static final String FIELD_COLLAPSED = "fieldCollapsed";

  protected IPreferences m_prefs;

  protected ClientUIPreferences() {
  }

  protected String getUserAgentPrefix() {
    UserAgent currentUserAgent;
    if (m_prefs != null && m_prefs.userScope() instanceof IClientSession) {
      currentUserAgent = ((IClientSession) m_prefs.userScope()).getUserAgent();
    }
    else {
      currentUserAgent = UserAgentUtility.getCurrentUserAgent();
    }
    if (currentUserAgent == null) {
      return "";
    }

    String uiLayer = null;
    if (!UiLayer.UNKNOWN.equals(currentUserAgent.getUiLayer())) {
      uiLayer = currentUserAgent.getUiLayer().stringValue();
    }
    String uiDeviceType = null;
    if (!UiDeviceType.UNKNOWN.equals(currentUserAgent.getUiDeviceType())) {
      uiDeviceType = currentUserAgent.getUiDeviceType().stringValue();
    }

    return StringUtility.concatenateTokens(uiLayer, ".", uiDeviceType, ".");
  }

  /**
   * Gets the splitter position from the client preferences. If the splitter position type of the current split box does
   * not match the splitter position type in the client preferences, the splitter position is no longer valid. The
   * preferences will therefore be invalidated.
   * <p>
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   *
   * @since 3.8.0
   */
  public Double getSplitterPosition(ISplitBox splitBox) {
    if (m_prefs == null) {
      return null;
    }

    String baseKey = splitBox.getCacheSplitterPositionPropertyName();
    if (baseKey == null) {
      return null;
    }

    baseKey = getUserAgentPrefix() + baseKey;
    String splitterPositionKey = baseKey + "#" + ISplitBox.PROP_SPLITTER_POSITION;
    String splitterPositionTypeKey = baseKey + "#" + ISplitBox.PROP_SPLITTER_POSITION_TYPE;
    Double splitterPosition = getPropertyDouble(splitterPositionKey);
    String splitterPositionType = m_prefs.get(splitterPositionTypeKey, null);

    // If splitter position type does not match, clear cache since it has become invalid.
    if (ObjectUtility.notEquals(splitterPositionType, splitBox.getSplitterPositionType())) {
      m_prefs.remove(splitterPositionKey);
      m_prefs.remove(splitterPositionTypeKey);
      return null;
    }

    return splitterPosition;
  }

  /**
   * Gets the minimized state of the collapsable field.
   * <p>
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   *
   * @since 9.0.0
   */
  public Boolean getSplitBoxFieldMinimized(ISplitBox splitBox) {
    if (m_prefs == null) {
      return null;
    }

    String baseKey = splitBox.getCacheSplitterPositionPropertyName();
    if (baseKey == null) {
      return null;
    }

    baseKey = getUserAgentPrefix() + baseKey;
    String minimizedKey = baseKey + "#" + ISplitBox.PROP_FIELD_MINIMIZED;
    return getPropertyBoolean(minimizedKey);
  }

  /**
   * Gets the collapsed state of the collapsable field.
   * <p>
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   *
   * @since 9.0.0
   */
  public Boolean getSplitBoxFieldCollapsed(ISplitBox splitBox) {
    return isFieldCollapsed(splitBox);
  }

  public Boolean isFieldCollapsed(IPreferenceField field) {
    if (m_prefs == null) {
      return null;
    }

    String propName = prepareCollapsedPropName(field);
    if (propName == null) {
      return null;
    }

    return getPropertyBoolean(propName);
  }

  public void setFieldCollapsed(IPreferenceField field, Boolean value) {
    if (m_prefs == null) {
      return;
    }

    String propName = prepareCollapsedPropName(field);
    if (propName == null) {
      return;
    }

    setPropertyBoolean(propName, value);
  }

  protected String prepareCollapsedPropName(IPreferenceField field) {
    return Optional.ofNullable(field.getPreferenceBaseKey())
        .map(k -> getUserAgentPrefix() + k + "#" + FIELD_COLLAPSED)
        .orElse(null);
  }

  /**
   * Sets all the split box attributes into the client preferences if {@link ISplitBox#isCacheSplitterPosition()} is set
   * to {@code true}. If {@link ISplitBox#isCacheSplitterPosition()} is set to {@code false} the client preferences will
   * be cleared (@see {@link #removeAllSplitBoxPreferences(ISplitBox)}).
   * <p>
   * Following attributes will be stored into the client preferences.
   * <ul>
   * <li>Splitter position
   * <li>Splitter position type
   * <li>Collapsable field minimized state
   * <li>Collapsable field collapsed state
   * </ul>
   * <p>
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   *
   * @since 9.0.0
   */
  public void setAllSplitBoxPreferences(ISplitBox splitBox) {
    if (m_prefs == null) {
      return;
    }

    if (!splitBox.isCacheSplitterPosition()) {
      removeAllSplitBoxPreferences(splitBox);
      return;
    }

    String baseKey = splitBox.getPreferenceBaseKey();
    if (baseKey == null) {
      return;
    }

    baseKey = getUserAgentPrefix() + baseKey;
    String splitterPositionKey = baseKey + "#" + ISplitBox.PROP_SPLITTER_POSITION;
    String splitterPositionTypeKey = baseKey + "#" + ISplitBox.PROP_SPLITTER_POSITION_TYPE;
    String minimizedKey = baseKey + "#" + ISplitBox.PROP_FIELD_MINIMIZED;

    m_prefs.putDouble(splitterPositionKey, splitBox.getSplitterPosition());
    m_prefs.put(splitterPositionTypeKey, splitBox.getSplitterPositionType());
    m_prefs.putBoolean(minimizedKey, splitBox.isFieldMinimized());

    setFieldCollapsed(splitBox, splitBox.isFieldCollapsed());
    flush();
  }

  /**
   * Removes all the split box attributes from the client preferences.
   * <p>
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   *
   * @since 9.0.0
   */
  public void removeAllSplitBoxPreferences(ISplitBox splitBox) {
    String baseKey = splitBox.getCacheSplitterPositionPropertyName();
    if (baseKey == null) {
      return;
    }

    baseKey = getUserAgentPrefix() + baseKey;
    String splitterPositionKey = baseKey + "#" + ISplitBox.PROP_SPLITTER_POSITION;
    String splitterPositionTypeKey = baseKey + "#" + ISplitBox.PROP_SPLITTER_POSITION_TYPE;
    String minimizedKey = baseKey + "#" + ISplitBox.PROP_FIELD_MINIMIZED;
    String collapsedKey = prepareCollapsedPropName(splitBox);

    m_prefs.remove(splitterPositionKey);
    m_prefs.remove(splitterPositionTypeKey);
    m_prefs.remove(minimizedKey);
    m_prefs.remove(collapsedKey);
  }

  public String getTableKey(ITable t) {
    String key = t.getClass().getName();
    String context = t.getUserPreferenceContext();
    if (context != null) {
      key += "#" + context;
    }
    return key;
  }

  public void removeTableCustomizerData(ITableCustomizer customizer, String configName) {
    if (m_prefs == null || customizer == null || customizer.getPreferencesKey() == null) {
      return;
    }
    m_prefs.remove(createTableCustomizerConfigKey(customizer, configName));
  }

  protected void renameTableCustomizerData(ITableCustomizer customizer, String oldConfigName, String newConfigName) {
    if (m_prefs == null || customizer == null || customizer.getPreferencesKey() == null) {
      return;
    }

    String oldKey = createTableCustomizerConfigKey(customizer, oldConfigName);
    String newKey = createTableCustomizerConfigKey(customizer, newConfigName);
    renameEntry(oldKey, newKey);
  }

  protected void renameEntry(String oldKey, String newKey) {
    String entry = m_prefs.get(oldKey, null);
    if (entry != null) {
      m_prefs.remove(oldKey);
      m_prefs.put(newKey, entry);
    }
  }

  public void setTableCustomizerData(ITableCustomizer customizer, String configName) {
    if (m_prefs == null || customizer == null || customizer.getPreferencesKey() == null) {
      return;
    }
    m_prefs.putByteArray(createTableCustomizerConfigKey(customizer, configName), customizer.getSerializedData());
  }

  public byte[] getTableCustomizerData(ITableCustomizer customizer, String configName) {
    if (m_prefs == null || customizer == null || customizer.getPreferencesKey() == null) {
      return null;
    }
    return m_prefs.getByteArray(createTableCustomizerConfigKey(customizer, configName), null);
  }

  protected String createTableCustomizerConfigKey(ITableCustomizer customizer, String configName) {
    StringBuilder sb = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(configName)) {
      sb.append(configName).append(".");
    }
    sb.append(TABLE_CUSTOMIZER_DATA).append(customizer.getPreferencesKey());
    return sb.toString();
  }

  /**
   * @return the key prefix used to store column data
   */
  public String getColumnKey(IColumn c) {
    String key = c.getColumnId();
    if (c.getTable() != null) {
      key = getTableKey(c.getTable()) + "#" + key;
    }
    return key;
  }

  protected boolean isTableClientUiPreferencesEnabled(IColumn col) {
    if (m_prefs == null) {
      return false;
    }
    return col.getTable().isClientUiPreferencesEnabled();
  }

  public void setTableColumnPreferences(IColumn col) {
    setTableColumnPreferences(col, true);
  }

  public void setTableColumnPreferences(IColumn col, boolean flush) {
    setTableColumnPreferences(col, flush, null);
  }

  public void setTableColumnPreferences(IColumn col, boolean flush, String configName) {
    if (!isTableClientUiPreferencesEnabled(col)) {
      return;
    }

    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_UIINDEX);
    int viewIndex = col.getVisibleColumnIndexHint();
    boolean visible = col.isVisible(IDimensions.VISIBLE);
    int width = col.getWidth();
    int sortIndex = col.getSortIndex();
    boolean grouped = col.isGroupingActive();
    boolean sortUp = col.isSortAscending();
    String aggregationFunction = null;
    if (col instanceof INumberColumn) {
      aggregationFunction = ((INumberColumn) col).getAggregationFunction();
    }
    String backgroundEffect = null;
    if (col instanceof INumberColumn) {
      backgroundEffect = ((INumberColumn) col).getBackgroundEffect();
    }

    //
    if (viewIndex >= 0) {
      m_prefs.put(key, "" + viewIndex);
    }
    else {
      m_prefs.remove(key);
    }
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_VISIBLE);
    if (!visible) {
      m_prefs.put(key, "no");
    }
    else {
      m_prefs.put(key, "yes");
    }
    //
    key = getUserAgentPrefix() + createColumnConfigKey(col, configName, TABLE_COLUMN_WIDTH);
    if (width >= 0) {
      m_prefs.put(key, "" + width);
    }
    else {
      m_prefs.remove(key);
    }
    // lazy legacy migration: remove sort explicit state if it exists.
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_EXPLICIT);
    m_prefs.remove(key);
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_INDEX);
    if (sortIndex >= 0) {
      m_prefs.put(key, "" + sortIndex);
    }
    else {
      m_prefs.put(key, "-1");
    }
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_ASC);
    if (sortIndex >= 0 && sortUp) {
      m_prefs.put(key, "true");
    }
    else {
      m_prefs.put(key, "false");
    }
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_GROUPED);
    if (grouped) {
      m_prefs.put(key, "true");
    }
    else {
      m_prefs.put(key, "false");
    }
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_AGGR_FUNCTION);
    m_prefs.put(key, aggregationFunction);
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_BACKGROUND_EFFECT);
    m_prefs.put(key, backgroundEffect);

    if (flush) {
      flush();
    }
  }

  public void renameTableColumnPreferences(IColumn col, String oldConfigName, String newConfigName) {
    if (col == null || StringUtility.isNullOrEmpty(oldConfigName) || StringUtility.isNullOrEmpty(newConfigName)) {
      throw new IllegalArgumentException();
    }
    renameEntry(createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_UIINDEX), createColumnConfigKey(col, newConfigName, TABLE_COLUMN_UIINDEX));
    renameEntry(createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_VISIBLE), createColumnConfigKey(col, newConfigName, TABLE_COLUMN_VISIBLE));
    renameEntry(getUserAgentPrefix() + createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_WIDTH), getUserAgentPrefix() + createColumnConfigKey(col, newConfigName, TABLE_COLUMN_WIDTH));
    renameEntry(createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_SORT_INDEX), createColumnConfigKey(col, newConfigName, TABLE_COLUMN_SORT_INDEX));
    renameEntry(createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_SORT_ASC), createColumnConfigKey(col, newConfigName, TABLE_COLUMN_SORT_ASC));
    renameEntry(createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_GROUPED), createColumnConfigKey(col, newConfigName, TABLE_COLUMN_GROUPED));
    renameEntry(createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_AGGR_FUNCTION), createColumnConfigKey(col, newConfigName, TABLE_COLUMN_AGGR_FUNCTION));
    renameEntry(createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_SORT_EXPLICIT), createColumnConfigKey(col, newConfigName, TABLE_COLUMN_SORT_EXPLICIT));
    renameEntry(createColumnConfigKey(col, oldConfigName, TABLE_COLUMN_BACKGROUND_EFFECT), createColumnConfigKey(col, newConfigName, TABLE_COLUMN_BACKGROUND_EFFECT));
  }

  protected String createColumnConfigKey(IColumn col, String configName, String propertyKey) {
    StringBuilder sb = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(configName)) {
      sb.append(configName).append(".");
    }
    sb.append(propertyKey).append(getColumnKey(col));
    return sb.toString();
  }

  public void updateTableColumnOrder(List<IColumn<?>> columnList, int[] visibleColumnIndexHints) {
    if (columnList.size() != visibleColumnIndexHints.length) {
      throw new IllegalArgumentException("columnList.size=" + columnList.size() + " hints.length=" + visibleColumnIndexHints.length);
    }
    if (m_prefs == null) {
      return;
    }
    for (int i = 0; i < visibleColumnIndexHints.length; i++) {
      IColumn<?> c = columnList.get(i);
      int viewIndex = visibleColumnIndexHints[i];
      String key = createColumnConfigKey(c, null, TABLE_COLUMN_UIINDEX);
      if (viewIndex >= 0) {
        m_prefs.put(key, "" + viewIndex);
      }
      else {
        m_prefs.remove(key);
      }
    }
    //
    flush();
  }

  public void removeAllTableColumnPreferences(IColumn col, String configName, boolean flush) {
    if (m_prefs == null) {
      return;
    }
    // visible
    m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_VISIBLE));
    // order
    m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_UIINDEX));
    // sorting
    m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_INDEX));
    m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_ASC));
    m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_GROUPED));
    m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_AGGR_FUNCTION));
    m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_EXPLICIT));
    // width
    m_prefs.remove(getUserAgentPrefix() + createColumnConfigKey(col, configName, TABLE_COLUMN_WIDTH));
    // background effect
    m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_BACKGROUND_EFFECT));

    if (flush) {
      flush();
    }

  }

  public void removeAllTableColumnPreferences(ITable table, String configName) {
    if (table == null || StringUtility.isNullOrEmpty(configName)) {
      throw new IllegalArgumentException();
    }
    for (IColumn col : table.getColumns()) {
      if (col.isDisplayable()) {
        removeAllTableColumnPreferences(col, configName, false);
      }
    }
    flush();
  }

  protected void renameAllTableColumnPreferences(ITable table, String oldConfigName, String newConfigName) {
    if (table == null || StringUtility.isNullOrEmpty(oldConfigName) || StringUtility.isNullOrEmpty(newConfigName)) {
      throw new IllegalArgumentException();
    }
    for (IColumn col : table.getColumns()) {
      if (col.isDisplayable()) {
        renameTableColumnPreferences(col, oldConfigName, newConfigName);
      }
    }
    flush();
  }

  public void setAllTableColumnPreferences(ITable table, String configName) {
    if (table == null || !table.isClientUiPreferencesEnabled()) {
      return;
    }
    for (IColumn col : table.getColumns()) {
      if (col.isDisplayable()) {
        setTableColumnPreferences(col, false, configName);
      }
    }
    flush();

  }

  public void setAllTableColumnPreferences(ITable table) {
    setAllTableColumnPreferences(table, null);
  }

  public Set<String> getAllTableColumnsConfigs(ITable table) {
    if (m_prefs == null) {
      return null;
    }
    String key = TABLE_COLUMNS_CONFIGS + getTableKey(table);
    return new LinkedHashSet<>(m_prefs.getList(key, new ArrayList<>()));
  }

  public void removeTableColumnsConfig(ITable table, String name) {
    if (m_prefs == null) {
      return;
    }
    String key = TABLE_COLUMNS_CONFIGS + getTableKey(table);
    Set<String> configs = getAllTableColumnsConfigs(table);
    configs.remove(name);
    m_prefs.putList(key, new ArrayList<>(configs));
    removeAllTableColumnPreferences(table, name);
    removeTableCustomizerData(table.getTableCustomizer(), name);
  }

  public void renameTableColumnsConfig(ITable table, String oldName, String newName) {
    if (m_prefs == null) {
      return;
    }
    String key = TABLE_COLUMNS_CONFIGS + getTableKey(table);
    Set<String> configs = getAllTableColumnsConfigs(table);
    configs.remove(oldName);
    configs.add(newName);
    m_prefs.putList(key, new ArrayList<>(configs));
    renameAllTableColumnPreferences(table, oldName, newName);
    renameTableCustomizerData(table.getTableCustomizer(), oldName, newName);
  }

  public void addTableColumnsConfig(ITable table, String name) {
    if (m_prefs == null) {
      return;
    }
    String key = TABLE_COLUMNS_CONFIGS + getTableKey(table);
    Set<String> configs = getAllTableColumnsConfigs(table);
    configs.add(name);
    m_prefs.putList(key, new ArrayList<>(configs));
  }

  /**
   * @return true if there are any user preferences for this tables columns
   */
  public boolean hasTableColumnPreferences(ITable table) {
    if (table != null && m_prefs != null) {
      for (IColumn col : table.getColumns()) {
        String keySuffix = getColumnKey(col);
        if (m_prefs.get(TABLE_COLUMN_VISIBLE + keySuffix, null) != null) {
          return true;
        }
        if (m_prefs.get(TABLE_COLUMN_UIINDEX + keySuffix, null) != null) {
          return true;
        }
        if (m_prefs.get(TABLE_COLUMN_SORT_INDEX + keySuffix, null) != null) {
          return true;
        }
        if (m_prefs.get(TABLE_COLUMN_SORT_ASC + keySuffix, null) != null) {
          return true;
        }
        if (m_prefs.get(TABLE_COLUMN_WIDTH + keySuffix, null) != null) {
          return true;
        }
        if (m_prefs.get(TABLE_COLUMN_GROUPED + keySuffix, null) != null) {
          return true;
        }
        if (m_prefs.get(TABLE_COLUMN_AGGR_FUNCTION + keySuffix, null) != null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   */
  public int getTableColumnWidth(IColumn col, int defaultWidth) {
    return getTableColumnWidth(col, defaultWidth, null);
  }

  public int getTableColumnWidth(IColumn col, int defaultWidth, String configName) {
    if (m_prefs == null) {
      return defaultWidth;
    }

    String baseKey = createColumnConfigKey(col, configName, TABLE_COLUMN_WIDTH);
    String key = getUserAgentPrefix() + baseKey;

    String value = m_prefs.get(key, null);
    if (value == null) {
      //If no value has been found try to load with the base key. Done due to backward compatibility.
      value = m_prefs.get(baseKey, null);
    }
    if (value != null) {
      try {
        return Integer.parseInt(value);
      }
      catch (Exception e) {
        LOG.warn("could not get table column width for [{}]. Loaded value '{}'", col.getClass().getName(), value, e);
      }
    }
    return defaultWidth;
  }

  public String getTableColumnBackgroundEffect(IColumn col, String defaultValue, String configName) {
    if (m_prefs == null) {
      return defaultValue;
    }
    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_BACKGROUND_EFFECT);
    return m_prefs.get(key, defaultValue);
  }

  public boolean getTableColumnVisible(IColumn col, boolean defaultValue) {
    return getTableColumnVisible(col, defaultValue, null);
  }

  public boolean getTableColumnVisible(IColumn col, boolean defaultValue, String configName) {
    if (m_prefs == null) {
      return defaultValue;
    }
    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_VISIBLE);
    String value = m_prefs.get(key, null);
    if (value != null) {
      Boolean b = TypeCastUtility.castValue(value, Boolean.class);
      return b != null ? b.booleanValue() : defaultValue;
    }
    return defaultValue;
  }

  public int getTableColumnViewIndex(IColumn col, int defaultIndex) {
    return getTableColumnViewIndex(col, defaultIndex, null);
  }

  public int getTableColumnViewIndex(IColumn col, int defaultIndex, String configName) {
    if (m_prefs == null) {
      return defaultIndex;
    }

    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_UIINDEX);
    String value = m_prefs.get(key, null);
    if (value != null) {
      try {
        return Integer.parseInt(value);
      }
      catch (Exception e) {
        LOG.warn("could not get table column view index for [{}]. Loaded value '{}'", col.getClass().getName(), value, e);
      }
    }
    return defaultIndex;
  }

  public int getTableColumnSortIndex(IColumn col, int defaultValue, String configName) {
    if (m_prefs == null) {
      return defaultValue;
    }

    //first, check if an explicit flag is set.
    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_EXPLICIT);
    String value = m_prefs.get(key, null);
    if (value != null) {
      Boolean b = TypeCastUtility.castValue(value, Boolean.class);
      if (b != null && !b.booleanValue()) {
        // Lazy legacy migration: if explicit was set to false,
        // we ignore the stored sort index and return the default instead.
        return defaultValue;
      }
    }

    key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_INDEX);
    value = m_prefs.get(key, null);
    if (value != null) {
      Integer i = TypeCastUtility.castValue(value, Integer.class);
      return i.intValue();
    }
    return defaultValue;
  }

  public boolean getTableColumnGrouped(IColumn col, boolean defaultValue, String configName) {
    if (m_prefs == null) {
      return defaultValue;
    }

    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_GROUPED);
    String value = m_prefs.get(key, null);
    if (value != null) {
      Boolean b = TypeCastUtility.castValue(value, Boolean.class);
      return b != null ? b.booleanValue() : defaultValue;
    }
    return defaultValue;
  }

  public String getTableColumnAggregationFunction(IColumn col, String defaultValue, String configName) {
    if (m_prefs == null) {
      return defaultValue;
    }

    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_AGGR_FUNCTION);
    String value = m_prefs.get(key, null);
    if (value != null) {
      String s = TypeCastUtility.castValue(value, String.class);
      return s;
    }
    return defaultValue;
  }

  public boolean getTableColumnSortAscending(IColumn col, boolean defaultValue, String configName) {
    if (m_prefs == null) {
      return defaultValue;
    }

    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_ASC);
    String value = m_prefs.get(key, null);
    if (value != null) {
      Boolean b = TypeCastUtility.castValue(value, Boolean.class);
      return b != null ? b.booleanValue() : defaultValue;
    }
    return defaultValue;
  }

  public void setCalendarPreferences(int displayMode, boolean displayCondensed) {
    if (m_prefs == null) {
      return;
    }

    m_prefs.put(CALENDAR_DISPLAY_MODE, "" + displayMode);
    m_prefs.put(CALENDAR_DISPLAY_CONDENSED, "" + displayCondensed);
    flush();
  }

  public int getCalendarDisplayMode(int defaultValue) {
    if (m_prefs == null) {
      return defaultValue;
    }

    String key = CALENDAR_DISPLAY_MODE;
    String value = m_prefs.get(key, null);
    if (value != null) {
      try {
        return Integer.parseInt(value);
      }
      catch (Exception e) {
        LOG.warn("could not get calendar display mode. Loaded value '{}'", value, e);
      }
    }
    return defaultValue;
  }

  public boolean getCalendarDisplayCondensed(boolean defaultValue) {
    if (m_prefs == null) {
      return defaultValue;
    }

    String key = CALENDAR_DISPLAY_CONDENSED;
    String value = m_prefs.get(key, null);
    if (value != null) {
      try {
        return TypeCastUtility.castValue(value, Boolean.class);
      }
      catch (Exception e) {
        LOG.warn("could not get calendar display condensed. Loaded value '{}'", value, e);
      }
    }
    return defaultValue;
  }

  public boolean getTableTileMode(ITable table, boolean defaultValue) {
    if (m_prefs == null) {
      return defaultValue;
    }

    String key = getTableKey(table) + "#" + TABLE_TILE_MODE;
    return BooleanUtility.nvl(getPropertyBoolean(key));
  }

  public void setTableTileMode(ITable table, boolean value) {
    String key = getTableKey(table) + "#" + TABLE_TILE_MODE;
    setPropertyBoolean(key, value);
  }

  public int getPropertyInteger(String propName, int defaultValue, boolean setDefaultAsProperty) {
    if (m_prefs == null) {
      return defaultValue;
    }

    String value = m_prefs.get(propName, null);
    if (value != null) {
      try {
        return TypeCastUtility.castValue(value, Integer.class);
      }
      catch (Exception e) {
        LOG.warn("could not get integer property '{}'. Loaded value '{}'", propName, value, e);
        if (setDefaultAsProperty) {
          setPropertyInteger(propName, defaultValue);
        }
      }
    }
    else {
      if (setDefaultAsProperty) {
        setPropertyInteger(propName, defaultValue);
      }
    }
    return defaultValue;
  }

  public void setPropertyInteger(String propName, int value) {
    if (m_prefs == null) {
      return;
    }

    m_prefs.put(propName, "" + value);
    flush();
  }

  public int[] getPropertyIntArray(String propName) {
    if (m_prefs == null) {
      return null;
    }

    String strVal = m_prefs.get(propName, null);
    if (!StringUtility.hasText(strVal)) {
      return null;
    }

    String[] split = strVal.split(";");
    int[] val = new int[split.length];
    for (int i = 0; i < split.length; i++) {
      val[i] = Integer.parseInt(split[i]);
    }
    return val;
  }

  public void setPropertyIntArray(String propName, int[] value) {
    if (m_prefs == null) {
      return;
    }

    StringBuilder builder = new StringBuilder();
    if (value != null) {
      for (int i = 0; i < value.length; i++) {
        builder.append(value[i]);
        if (i != value.length - 1) {
          builder.append(";");
        }
      }
    }
    m_prefs.put(propName, builder.toString());
    flush();
  }

  public Double getPropertyDouble(String propName) {
    if (m_prefs == null) {
      return null;
    }

    String value = m_prefs.get(propName, null);
    if (value != null) {
      try {
        return TypeCastUtility.castValue(value, Double.class);
      }
      catch (Exception e) {
        LOG.warn("could not get double property '{}'. Loaded value '{}'", propName, value, e);
      }
    }
    return null;
  }

  public void setPropertyDouble(String propName, Double value) {
    if (m_prefs == null) {
      return;
    }

    m_prefs.put(propName, "" + value);
    flush();
  }

  public Boolean getPropertyBoolean(String propName) {
    if (m_prefs == null) {
      return null;
    }

    Boolean result = null;
    String value = m_prefs.get(propName, null);
    if (value != null) {
      if ("true".equalsIgnoreCase(value)) {
        result = true;
      }
      else if ("false".equalsIgnoreCase(value)) {
        result = false;
      }
    }
    return result;
  }

  public void setPropertyBoolean(String propName, boolean value) {
    if (m_prefs == null) {
      return;
    }

    m_prefs.put(propName, Boolean.toString(value));
    flush();
  }

  public String getPropertyString(String propName) {
    if (m_prefs == null) {
      return null;
    }

    return m_prefs.get(propName, null);
  }

  public void setPropertyString(String propName, String value) {
    if (m_prefs == null) {
      return;
    }

    m_prefs.put(propName, value);
    flush();
  }

  public int[][] getDesktopColumnSplits(int rowCount, int colCount) {
    //the row x col matrix is stored as flat int array, row per row, column per column
    int[] a = getPropertyIntArray(DESKTOP_COLUMN_SPLITS);
    if (a != null && a.length == rowCount * colCount) {
      int[][] splits = new int[rowCount][colCount];
      for (int r = 0; r < rowCount; r++) {
        System.arraycopy(a, r * colCount, splits[r], 0, colCount);
      }
      return splits;
    }
    return null;
  }

  public void setDesktopColumnSplits(int[][] splits) {
    //the row x col matrix is stored as flat int array, row per row, column per column
    if (splits != null) {
      int rowCount = splits.length;
      int colCount = splits[0].length;
      int[] a = new int[rowCount * colCount];
      int index = 0;
      for (int[] split : splits) {
        for (int c = 0; c < colCount; c++) { // NOSONAR
          a[index] = split[c];
          index++;
        }
      }
      setPropertyIntArray(DESKTOP_COLUMN_SPLITS, a);
    }
  }

  /**
   * Sets a custom client preference data object for the given custom client preference ID.
   *
   * @param customClientPreferenceId
   *          Required ID
   * @param customClientPreference
   *          Might be <code>null</code> to remove an already stored custom client preference data object.
   */
  public void setCustomClientPreference(CustomClientPreferenceId customClientPreferenceId, ICustomClientPreferenceDo customClientPreference) {
    assertNotNull(customClientPreferenceId, "customClientPreferenceId is required");
    if (m_prefs == null) {
      return;
    }

    if (customClientPreference == null) {
      m_prefs.remove(customClientPreferenceId.unwrapAsString());
    }
    else {
      m_prefs.put(customClientPreferenceId.unwrapAsString(), BEANS.get(IDataObjectMapper.class).writeValue(customClientPreference));
    }
    flush();
  }

  /**
   * @return A custom client preference data object for the given custom client preference ID, or <code>null</code> if
   *         no custom client preference is available for the given ID.
   */
  public <T extends ICustomClientPreferenceDo> T getCustomClientPreference(CustomClientPreferenceId customClientPreferenceId, Class<T> customClientPreferenceClass) {
    if (m_prefs == null) {
      return null;
    }

    String customClientPreferenceJson = m_prefs.get(customClientPreferenceId.unwrapAsString(), null);
    return BEANS.get(IDataObjectMapper.class).readValue(customClientPreferenceJson, customClientPreferenceClass);
  }

  public static IPreferences getClientPreferences(IClientSession session) {
    try {
      return Preferences.get(session, PREFERENCES_NODE_ID);
    }
    catch (RuntimeException | PlatformError t) {
      LOG.error("Unable to load preferences.", t);
    }
    return null;
  }

  protected void load(IClientSession session) {
    m_prefs = getClientPreferences(session);
  }

  protected void flush() {
    if (m_prefs == null) {
      return;
    }

    try {
      m_prefs.flush();
    }
    catch (RuntimeException | PlatformError t) {
      LOG.error("Unable to flush preferences.", t);
    }
  }
}
