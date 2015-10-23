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
package org.eclipse.scout.rt.client.ui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;
import org.eclipse.scout.rt.shared.services.common.prefs.IPreferences;
import org.eclipse.scout.rt.shared.services.common.prefs.Preferences;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

/**
 * UI model customization wrapping a {@link IPreferences} object.
 */
public class ClientUIPreferences {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientUIPreferences.class);

  /**
   * @return a new instance of the {@link ClientUIPreferences}
   * @throws IllegalArgumentException
   *           When no {@link IClientSession} is available in the current thread context ( {@link ISession#CURRENT}).
   */
  public static ClientUIPreferences getInstance() {
    return new ClientUIPreferences(ClientSessionProvider.currentSession());
  }

  /**
   * @return a new instance of the {@link ClientUIPreferences}
   */
  public static ClientUIPreferences getInstance(IClientSession session) {
    return new ClientUIPreferences(session);
  }

  private static final String PREFERENCES_NODE_ID = "org.eclipse.scout.rt.client";

  private static final String TABLE_COLUMNS_CONFIGS = "table.columns.configs.";
  private static final String TABLE_CUSTOMIZER_DATA = "table.customizer.data.";
  private static final String TABLE_COLUMN_UIINDEX = "table.column.viewIndex.";
  private static final String TABLE_COLUMN_WIDTH = "table.column.width.";
  private static final String TABLE_COLUMN_VISIBLE = "table.column.visible.";
  private static final String TABLE_COLUMN_SORT_INDEX = "table.column.sortIndex.";
  private static final String TABLE_COLUMN_GROUPED = "table.column.grouped.";
  private static final String TABLE_COLUMN_AGGR_FUNCTION = "table.column.aggr.function.";
  private static final String TABLE_COLUMN_SORT_ASC = "table.column.sortAsc.";
  private static final String TABLE_COLUMN_SORT_EXPLICIT = "table.column.sortExplicit.";
  private static final String APPLICATION_WINDOW_MAXIMIZED = "application.window.maximized";
  private static final String APPLICATION_WINDOW_BOUNDS = "application.window.bounds";
  private static final String CALENDAR_DISPLAY_MODE = "calendar.display.mode";
  private static final String CALENDAR_DISPLAY_CONDENSED = "calendar.display.condensed";
  private static final String DESKTOP_COLUMN_SPLITS = "desktop.columnSplits";
  private static final String FORM_BOUNDS = "form.bounds.";

  private final IClientSession m_session;
  private IPreferences m_prefs;

  private ClientUIPreferences(IClientSession session) {
    if (session == null) {
      throw new IllegalArgumentException("No scout client session context. Calling client preferences from outside a scout client session job.");
    }
    m_session = session;
    load();
  }

  /**
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   */
  public Rectangle getFormBounds(IForm form) {
    String key = form.computeCacheBoundsKey();
    if (key == null || m_prefs == null) {
      return null;
    }

    key = getUserAgentPrefix() + FORM_BOUNDS + key;
    String value = m_prefs.get(key, "");
    if (StringUtility.isNullOrEmpty(value)) {
      key = getLegacyFormBoundsKey(form);
      value = m_prefs.get(key, "");
    }

    if (!StringUtility.isNullOrEmpty(value)) {
      try {
        StringTokenizer tok = new StringTokenizer(value, ",");
        Rectangle r = new Rectangle(new Integer(tok.nextToken()).intValue(), new Integer(tok.nextToken()).intValue(), new Integer(tok.nextToken()).intValue(), new Integer(tok.nextToken()).intValue());
        return r;
      }
      catch (Exception e) {
        LOG.warn("value=" + value, e);
      }
    }
    return null;
  }

  /**
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   */
  public void setFormBounds(IForm form, Rectangle bounds) {
    String key = form.computeCacheBoundsKey();
    if (key == null || m_prefs == null) {
      return;
    }

    key = getUserAgentPrefix() + FORM_BOUNDS + key;
    if (bounds == null) {
      m_prefs.remove(key);
    }
    else {
      m_prefs.put(key, bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height);
    }
    flush();
  }

  private String getLegacyFormBoundsKey(IForm form) {
    String key = form.computeCacheBoundsKey();
    if (key == null) {
      return null;
    }

    //Add prefix only if not already added.
    //This is mainly necessary due to backward compatibility because until 3.8.0 the prefix had to be returned by computeCacheBoundsKey
    if (!key.startsWith("form.bounds")) {
      key = "form.bounds_" + key;
    }

    //Explicitly don't consider user agent because before 3.8.0 there was no user agent and therefore the keys didn't contain this information.

    return key;
  }

  private String getUserAgentPrefix() {
    UserAgent currentUserAgent = null;
    if (getSession() != null) {
      currentUserAgent = getSession().getUserAgent();
    }
    else {
      currentUserAgent = UserAgentUtility.getCurrentUserAgent();
    }
    if (currentUserAgent == null) {
      return "";
    }

    String uiLayer = null;
    if (!UiLayer.UNKNOWN.equals(currentUserAgent.getUiLayer())) {
      uiLayer = currentUserAgent.getUiLayer().getIdentifier();
    }
    String uiDeviceType = null;
    if (!UiDeviceType.UNKNOWN.equals(currentUserAgent.getUiDeviceType())) {
      uiDeviceType = currentUserAgent.getUiDeviceType().getIdentifier();
    }

    return StringUtility.concatenateTokens(uiLayer, ".", uiDeviceType, ".");
  }

  /**
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   *
   * @since 3.8.0
   */
  public int[] getSplitterPosition(ISplitBox splitBox) {
    String baseKey = splitBox.getCacheSplitterPositionPropertyName();
    if (baseKey == null) {
      return null;
    }

    String key = getUserAgentPrefix() + baseKey;
    int[] value = getPropertyIntArray(key);
    if (value == null) {
      //If no value has been found try to load with the base key. Done due to backward compatibility.
      value = getPropertyIntArray(baseKey);
    }

    return value;
  }

  /**
   * Since this property depends on the user agent it is saved separately for each combination of
   * {@link org.eclipse.scout.rt.shared.ui.IUiLayer IUiLayer} and {@link org.eclipse.scout.rt.shared.ui.IUiDeviceType
   * IUiDeviceType}.
   *
   * @since 3.8.0
   */
  public void setSplitterPosition(ISplitBox splitBox, int[] weights) {
    String key = splitBox.getCacheSplitterPositionPropertyName();
    if (key == null) {
      return;
    }

    key = getUserAgentPrefix() + key;
    setPropertyIntArray(key, weights);
  }

  public String getTableKey(ITable t) {
    String key = t.getClass().getName();
    String context = t.getUserPreferenceContext();
    if (context != null) {
      key += "#" + context;
    }
    return key;
  }

  public Object getTableCustomizerData(String customizerKey) {
    if (m_prefs == null) {
      return null;
    }
    String key = TABLE_CUSTOMIZER_DATA + customizerKey;
    byte[] serialData = m_prefs.getByteArray(key, null);
    if (serialData != null) {
      try {
        Object customizerData = SerializationUtility.createObjectSerializer().deserialize(serialData, null);
        return customizerData;
      }
      catch (Exception t) {
        LOG.error("Failed reading custom table data for " + key + ": " + t);
        m_prefs.remove(key);
        return null;
      }
    }
    else {
      return null;
    }
  }

  public void removeTableCustomizerData(ITable table, String configName) {
    if (m_prefs == null) {
      return;
    }
    ITableCustomizer customizer = table.getTableCustomizer();
    if (customizer != null) {
      m_prefs.remove(createTableCustomizerConfigKey(table, customizer.getClass(), configName));
    }
  }

  public void setTableCustomizerData(ITable table, ITableCustomizer customizer, String configName) {
    if (m_prefs == null) {
      return;
    }
    m_prefs.putByteArray(createTableCustomizerConfigKey(table, customizer.getClass(), configName), customizer.getSerializedData());
  }

  public byte[] getTableCustomizerData(ITable table, Class customizer, String configName) {
    if (m_prefs == null) {
      return null;
    }
    return m_prefs.getByteArray(createTableCustomizerConfigKey(table, customizer, configName), null);
  }

  private String createTableCustomizerConfigKey(ITable table, Class customizer, String configName) {
    StringBuilder sb = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(configName)) {
      sb.append(configName).append(".");
    }
    sb.append(TABLE_CUSTOMIZER_DATA).append(getTableKey(table)).append("#").append(customizer.getName());
    return sb.toString();
  }

  /**
   * store customizer data to persistent store
   */
  public void setTableCustomizerData(String customizerKey, Object customizerData) {

    String key = TABLE_CUSTOMIZER_DATA + customizerKey;
    if (customizerData != null) {
      try {
        byte[] data = SerializationUtility.createObjectSerializer().serialize(customizerData);
        m_prefs.putByteArray(key, data);
      }
      catch (Exception t) {
        LOG.error("Failed storing custom table data for " + key, t);
        m_prefs.remove(key);
      }
    }
    else {
      m_prefs.remove(key);
    }
    //
    flush();
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

  public void setTableColumnPreferences(IColumn col) {
    setTableColumnPreferences(col, true);
  }

  public void setTableColumnPreferences(IColumn col, boolean flush) {
    setTableColumnPreferences(col, flush, null);
  }

  public void setTableColumnPreferences(IColumn col, boolean flush, String configName) {
    if (m_prefs == null) {
      return;
    }

    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_UIINDEX);
    int viewIndex = col.getVisibleColumnIndexHint();
    boolean visible = col.isVisibleInternal();
    int width = col.getWidth();
    int sortIndex = col.getSortIndex();
    boolean grouped = col.isGroupingActive();
    boolean sortUp = col.isSortAscending();
    String aggregationFunction = null;
    if (col instanceof INumberColumn) {
      aggregationFunction = ((INumberColumn) col).getAggregationFunction();
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
    if (aggregationFunction != null) {
      m_prefs.put(key, aggregationFunction);
    }
    else {
      m_prefs.remove(key);
    }
    if (flush) {
      flush();
    }
  }

  public void removeTableColumnPreferences(IColumn col, String configName) {
    if (m_prefs == null) {
      return;
    }

    String key = createColumnConfigKey(col, configName, TABLE_COLUMN_UIINDEX);
    m_prefs.remove(key);
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_VISIBLE);
    m_prefs.remove(key);
    //
    key = getUserAgentPrefix() + createColumnConfigKey(col, configName, TABLE_COLUMN_WIDTH);
    m_prefs.remove(key);
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_INDEX);
    m_prefs.remove(key);
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_ASC);
    m_prefs.remove(key);
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_GROUPED);
    m_prefs.remove(key);
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_AGGR_FUNCTION);
    m_prefs.remove(key);
    //
    key = createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_EXPLICIT);
    m_prefs.remove(key);

  }

  private String createColumnConfigKey(IColumn col, String configName, String propertyKey) {
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

  public void removeTableColumnPreferences(IColumn col) {
    removeTableColumnPreferences(col, true, true, true, true);
  }

  public void removeTableColumnPreferences(IColumn col, boolean visibility, boolean order, boolean sorting, boolean widths) {
    removeTableColumnPreferences(col, visibility, order, sorting, widths, true, null);
  }

  private void removeTableColumnPreferences(IColumn col, boolean visibility, boolean order, boolean sorting, boolean widths, boolean flush, String configName) {
    if (col != null && m_prefs != null) {
      if (visibility) {
        m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_VISIBLE));
      }
      if (order) {
        m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_UIINDEX));
      }
      if (sorting) {
        m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_INDEX));
        m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_ASC));
        m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_GROUPED));
        m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_AGGR_FUNCTION));
        m_prefs.remove(createColumnConfigKey(col, configName, TABLE_COLUMN_SORT_EXPLICIT));
      }
      if (widths) {
        m_prefs.remove(getUserAgentPrefix() + createColumnConfigKey(col, configName, TABLE_COLUMN_WIDTH));
      }

      if (flush) {
        flush();
      }
    }
  }

  public void removeAllTableColumnPreferences(ITable table, boolean visibility, boolean order, boolean sorting, boolean widths) {
    removeAllTableColumnPreferences(table, visibility, order, sorting, widths, null);
  }

  public void removeAllTableColumnPreferences(ITable table, boolean visibility, boolean order, boolean sorting, boolean widths, String configName) {
    if (table == null) {
      return;
    }

    for (IColumn<?> col : table.getColumns()) {
      removeTableColumnPreferences(col, visibility, order, sorting, widths, false, configName);
    }

    flush();
  }

  public void removeAllTableColumnPreferences(ITable table, String configName) {
    if (table == null || StringUtility.isNullOrEmpty(configName)) {
      return;
    }
    for (IColumn col : table.getColumns()) {
      if (col.isDisplayable()) {
        removeTableColumnPreferences(col, configName);
      }
    }
    flush();
  }

  public void setAllTableColumnPreferences(ITable table, String configName) {
    if (table == null) {
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
    return new LinkedHashSet<String>(m_prefs.getList(key, new ArrayList<String>()));
  }

  public void removeTableColumnsConfig(ITable table, String name) {
    if (m_prefs == null) {
      return;
    }
    String key = TABLE_COLUMNS_CONFIGS + getTableKey(table);
    Set<String> configs = getAllTableColumnsConfigs(table);
    configs.remove(name);
    m_prefs.putList(key, new ArrayList<String>(configs));
    removeAllTableColumnPreferences(table, true, true, true, true, name);
    removeTableCustomizerData(table, name);
  }

  public void addTableColumnsConfig(ITable table, String name) {
    if (m_prefs == null) {
      return;
    }
    String key = TABLE_COLUMNS_CONFIGS + getTableKey(table);
    Set<String> configs = getAllTableColumnsConfigs(table);
    configs.add(name);
    m_prefs.putList(key, new ArrayList<String>(configs));
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
        LOG.warn("value=" + value, e);
      }
    }
    return defaultWidth;
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
        LOG.warn("value=" + value, e);
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

  public void setApplicationWindowPreferences(BoundsSpec r, boolean maximized) {
    if (m_prefs == null) {
      return;
    }

    if (r != null) {
      String value = "" + r.x + "," + r.y + "," + r.width + "," + r.height;
      m_prefs.put(APPLICATION_WINDOW_BOUNDS, value);
    }
    else {
      m_prefs.remove(APPLICATION_WINDOW_BOUNDS);
    }
    //
    if (maximized) {
      m_prefs.put(APPLICATION_WINDOW_MAXIMIZED, "yes");
    }
    else {
      m_prefs.remove(APPLICATION_WINDOW_MAXIMIZED);
    }
    flush();
  }

  public boolean getApplicationWindowMaximized() {
    if (m_prefs == null) {
      return false;
    }

    String key = APPLICATION_WINDOW_MAXIMIZED;
    String value = m_prefs.get(key, null);
    return "yes".equalsIgnoreCase(value);
  }

  public BoundsSpec getApplicationWindowBounds() {
    if (m_prefs == null) {
      return null;
    }

    String key = APPLICATION_WINDOW_BOUNDS;
    String value = m_prefs.get(key, null);
    if (value != null) {
      try {
        StringTokenizer tok = new StringTokenizer(value, ",");
        BoundsSpec r = new BoundsSpec(new Integer(tok.nextToken()).intValue(), new Integer(tok.nextToken()).intValue(), new Integer(tok.nextToken()).intValue(), new Integer(tok.nextToken()).intValue());
        return r;
      }
      catch (Exception e) {
        LOG.warn("value=" + value, e);
      }
    }
    return null;
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
        LOG.warn("value=" + value, e);
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
        LOG.warn("value=" + value, e);
      }
    }
    return defaultValue;
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
        LOG.warn("value=" + value, e);
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
        LOG.warn("value=" + value, e);
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

  public int[][] getDesktopColumnSplits(int rowCount, int colCount) {
    //the row x col matrix is stored as flat int array, row per row, column per column
    int[] a = getPropertyIntArray(DESKTOP_COLUMN_SPLITS);
    if (a != null && a.length == rowCount * colCount) {
      int[][] splits = new int[rowCount][colCount];
      int index = 0;
      for (int r = 0; r < rowCount; r++) {
        for (int c = 0; c < colCount; c++) {
          splits[r][c] = a[index];
          index++;
        }
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
      for (int r = 0; r < rowCount; r++) {
        for (int c = 0; c < colCount; c++) {
          a[index] = splits[r][c];
          index++;
        }
      }
      setPropertyIntArray(DESKTOP_COLUMN_SPLITS, a);
    }
  }

  public static IPreferences getClientPreferences(IClientSession session) {
    try {
      return Preferences.get(session, PREFERENCES_NODE_ID);
    }
    catch (RuntimeException t) {
      LOG.error("Unable to load preferences.", t);
    }
    return null;
  }

  protected void load() {
    m_prefs = getClientPreferences(getSession());
  }

  protected void flush() {
    if (m_prefs == null) {
      return;
    }

    try {
      m_prefs.flush();
    }
    catch (RuntimeException t) {
      LOG.error("Unable to flush preferences.", t);
    }
  }

  public IClientSession getSession() {
    return m_session;
  }
}
