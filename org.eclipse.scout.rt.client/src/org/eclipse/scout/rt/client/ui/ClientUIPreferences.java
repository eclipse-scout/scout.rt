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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleObjectInputStream;
import org.eclipse.scout.commons.osgi.BundleObjectOutputStream;
import org.eclipse.scout.commons.prefs.UserScope;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.IUniqueColumnFilterIdentifier;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;
import org.eclipse.scout.rt.shared.services.common.prefs.IUserPreferencesStorageService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

/**
 * UI model customization wrapping a {@link org.eclipse.core.runtime.Preferences} object with its location Stored
 * in user area.
 */
public class ClientUIPreferences {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientUIPreferences.class);

  /**
   * @return a new instance of the {@link ClientUIPreferences} based on the {@link UserScope}s
   *         {@link IEclipsePreferences}
   *         <p>
   *         Exactly the same as {@link ClientUIPreferences#ClientUIPreferences()}
   */
  public static ClientUIPreferences getInstance() {
    return new ClientUIPreferences();
  }

  private static final String TABLE_CUSTOMIZER_DATA = "table.customizer.data.";
  private static final String TABLE_COLUMN_UIINDEX = "table.column.viewIndex.";
  private static final String TABLE_COLUMN_WIDTH = "table.column.width.";
  private static final String TABLE_COLUMN_VISIBLE = "table.column.visible.";
  private static final String TABLE_COLUMN_SORT_INDEX = "table.column.sortIndex.";
  private static final String TABLE_COLUMN_SORT_ASC = "table.column.sortAsc.";
  private static final String TABLE_COLUMN_SORT_EXPLICIT = "table.column.sortExplicit.";
  private static final String TABLE_COLUMN_FILTER = "table.column.filter.";
  private static final String APPLICATION_WINDOW_MAXIMIZED = "application.window.maximized";
  private static final String APPLICATION_WINDOW_BOUNDS = "application.window.bounds";
  private static final String CALENDAR_DISPLAY_MODE = "calendar.display.mode";
  private static final String CALENDAR_DISPLAY_CONDENSED = "calendar.display.condensed";
  private static final String DESKTOP_COLUMN_SPLITS = "desktop.columnSplits";

  /**
   * @deprecated to be removed in release 3.9.0
   */
  @Deprecated
  private static final String NLS_LOCALE_ISO = "nls_locale_iso";
  private static final String NLS_LOCALE_LANGUAGE = "locale.language";
  private static final String NLS_LOCALE_COUNTRY = "locale.country";

  private static final Locale HOST_LOCALE = Locale.getDefault();

  private final IEclipsePreferences m_env;

  public ClientUIPreferences() {
    m_env = SERVICES.getService(IUserPreferencesStorageService.class).loadPreferences();
  }

  public Rectangle getFormBounds(IForm form) {
    String key = form.computeCacheBoundsKey();
    if (key == null) {
      return null;
    }

    String value = m_env.get(key, "");
    if (!StringUtility.isNullOrEmpty(value)) {
      try {
        StringTokenizer tok = new StringTokenizer(value, ",");
        Rectangle r = new Rectangle(
            new Integer(tok.nextToken()).intValue(),
            new Integer(tok.nextToken()).intValue(),
            new Integer(tok.nextToken()).intValue(),
            new Integer(tok.nextToken()).intValue()
            );
        return r;
      }
      catch (Exception e) {
        LOG.warn("value=" + value, e);
      }
    }
    return null;
  }

  public void setFormBounds(IForm form, Rectangle bounds) {
    String key = form.computeCacheBoundsKey();
    if (key == null) {
      return;
    }

    if (bounds == null) {
      m_env.remove(key);
    }
    else {
      m_env.put(key, bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height);
    }
    flush();
  }

  public String getTableKey(ITable t) {
    String key = t.getClass().getName();
    String context = t.getUserPreferenceContext();
    if (context != null) {
      key += "#" + context;
    }
    return key;
  }

  public Object getTableCustomizerData(String customizerKey, Bundle loaderBundle) {
    String key = TABLE_CUSTOMIZER_DATA + customizerKey;
    byte[] serialData = m_env.getByteArray(key, null);
    if (serialData != null) {
      ObjectInputStream in = null;
      try {
        in = new BundleObjectInputStream(new ByteArrayInputStream(serialData), new Bundle[]{loaderBundle});
        Object customizerData = in.readObject();
        in.close();
        in = null;
        return customizerData;
      }
      catch (Throwable t) {
        LOG.error("Failed reading custom table data for " + key + ": " + t);
        m_env.remove(key);
        return null;
      }
      finally {
        if (in != null) {
          try {
            in.close();
          }
          catch (Throwable t) {
          }
        }
      }
    }
    else {
      return null;
    }
  }

  /**
   * store customizer data to persistent store
   */
  public void setTableCustomizerData(String customizerKey, Object customizerData) {
    String key = TABLE_CUSTOMIZER_DATA + customizerKey;
    if (customizerData != null) {
      ObjectOutputStream out = null;
      try {
        ByteArrayOutputStream serialData = new ByteArrayOutputStream();
        out = new BundleObjectOutputStream(serialData);
        out.writeObject(customizerData);
        out.close();
        out = null;
        m_env.putByteArray(key, serialData.toByteArray());
      }
      catch (Throwable t) {
        LOG.error("Failed storing custom table data for " + key, t);
        m_env.remove(key);
      }
      finally {
        if (out != null) {
          try {
            out.close();
          }
          catch (Throwable t) {
          }
        }
      }
    }
    else {
      m_env.remove(key);
    }
    //
    flush();
  }

  /**
   * @return the key prefix used to store column data
   */
  public String getColumnKey(IColumn c) {
    String key = c.getColumnId();
    if (c instanceof IUniqueColumnFilterIdentifier) {
      key = ((IUniqueColumnFilterIdentifier) c).getIdentifier() + "." + key;
    }
    if (c.getTable() != null) {
      key = getTableKey(c.getTable()) + "#" + key;
    }
    return key;
  }

  public void setTableColumnPreferences(IColumn col) {
    setTableColumnPreferences(col, true);
  }

  public void setTableColumnPreferences(IColumn col, boolean flush) {
    String keySuffix = getColumnKey(col);
    String key = TABLE_COLUMN_UIINDEX + keySuffix;
    int viewIndex = col.getVisibleColumnIndexHint();
    boolean visible = col.isVisibleInternal();
    int width = col.getWidth();
    int sortIndex = col.getSortIndex();
    boolean sortUp = col.isSortAscending();
    boolean sortExplicit = col.isSortExplicit();
    boolean filterActive = col.isColumnFilterActive();
    //
    if (viewIndex >= 0) {
      m_env.put(key, "" + viewIndex);
    }
    else {
      m_env.remove(key);
    }
    //
    key = TABLE_COLUMN_VISIBLE + keySuffix;
    if (!visible) {
      m_env.put(key, "no");
    }
    else {
      m_env.put(key, "yes");
    }
    //
    key = TABLE_COLUMN_WIDTH + keySuffix;
    if (width >= 0) {
      m_env.put(key, "" + width);
    }
    else {
      m_env.remove(key);
    }
    //
    key = TABLE_COLUMN_SORT_INDEX + keySuffix;
    if (sortIndex >= 0) {
      m_env.put(key, "" + sortIndex);
    }
    else {
      m_env.put(key, "-1");
    }
    //
    key = TABLE_COLUMN_SORT_ASC + keySuffix;
    if (sortIndex >= 0 && sortUp) {
      m_env.put(key, "true");
    }
    else {
      m_env.put(key, "false");
    }
    //
    key = TABLE_COLUMN_SORT_EXPLICIT + keySuffix;
    if (sortExplicit) {
      m_env.put(key, "true");
    }
    else {
      m_env.put(key, "false");
    }
    //
    key = TABLE_COLUMN_FILTER + keySuffix;
    if (filterActive) {
      if (col.getTable().getColumnFilterManager() != null) {
        byte[] filterData = col.getTable().getColumnFilterManager().getSerializedFilter(col);
        m_env.putByteArray(key, filterData);
      }
    }
    else {
      m_env.remove(key);
    }

    if (flush) {
      flush();
    }
  }

  public void updateTableColumnFilter(IColumn column) {
    if (column.getTable() != null && column.getTable().getColumnFilterManager() != null) {
      String keySuffix = getColumnKey(column);
      String key = TABLE_COLUMN_FILTER + keySuffix;
      byte[] value = m_env.getByteArray(key, null);
      if (value != null) {
        try {
          column.getTable().getColumnFilterManager().setSerializedFilter(value, column);
        }
        catch (Exception e) {
          LOG.warn("value=" + value, e);
        }
      }
    }
  }

  public void updateTableColumnOrder(List<IColumn<?>> columnList, int[] visibleColumnIndexHints) {
    if (columnList.size() != visibleColumnIndexHints.length) {
      throw new IllegalArgumentException("columnList.size=" + columnList.size() + " hints.length=" + visibleColumnIndexHints.length);
    }
    for (int i = 0; i < visibleColumnIndexHints.length; i++) {
      IColumn<?> c = columnList.get(i);
      int viewIndex = visibleColumnIndexHints[i];
      String keySuffix = getColumnKey(c);
      String key = TABLE_COLUMN_UIINDEX + keySuffix;
      if (viewIndex >= 0) {
        m_env.put(key, "" + viewIndex);
      }
      else {
        m_env.remove(key);
      }
    }
    //
    flush();
  }

  public void removeTableColumnPreferences(IColumn col) {
    removeTableColumnPreferences(col, true, true, true, true);
  }

  public void removeTableColumnPreferences(IColumn col, boolean visibility, boolean order, boolean sorting, boolean widths) {
    removeTableColumnPreferences(col, visibility, order, sorting, widths, true);
  }

  private void removeTableColumnPreferences(IColumn col, boolean visibility, boolean order, boolean sorting, boolean widths, boolean flush) {
    if (col != null) {
      String keySuffix = getColumnKey(col);
      if (visibility) {
        m_env.remove(TABLE_COLUMN_VISIBLE + keySuffix);
      }
      if (order) {
        m_env.remove(TABLE_COLUMN_UIINDEX + keySuffix);
      }
      if (sorting) {
        m_env.remove(TABLE_COLUMN_SORT_INDEX + keySuffix);
        m_env.remove(TABLE_COLUMN_SORT_ASC + keySuffix);
        m_env.remove(TABLE_COLUMN_SORT_EXPLICIT + keySuffix);
      }
      if (widths) {
        m_env.remove(TABLE_COLUMN_WIDTH + keySuffix);
      }

      if (flush) {
        flush();
      }
    }
  }

  public void removeAllTableColumnPreferences(ITable table, boolean visibility, boolean order, boolean sorting, boolean widths) {
    if (table == null) {
      return;
    }

    for (IColumn<?> col : table.getColumns()) {
      removeTableColumnPreferences(col, visibility, order, sorting, widths, false);
    }

    flush();
  }

  public void setAllTableColumnPreferences(ITable table) {
    if (table == null) {
      return;
    }

    for (IColumn col : table.getColumns()) {
      if (col.isDisplayable()) {
        setTableColumnPreferences(col, false);
      }
    }

    flush();
  }

  /**
   * @return true if there are any user preferences for this tables columns
   */
  public boolean hasTableColumnPreferences(ITable table) {
    if (table != null) {
      for (IColumn col : table.getColumns()) {
        String keySuffix = getColumnKey(col);
        if (m_env.get(TABLE_COLUMN_VISIBLE + keySuffix, null) != null) {
          return true;
        }
        if (m_env.get(TABLE_COLUMN_UIINDEX + keySuffix, null) != null) {
          return true;
        }
        if (m_env.get(TABLE_COLUMN_SORT_INDEX + keySuffix, null) != null) {
          return true;
        }
        if (m_env.get(TABLE_COLUMN_SORT_ASC + keySuffix, null) != null) {
          return true;
        }
        if (m_env.get(TABLE_COLUMN_SORT_EXPLICIT + keySuffix, null) != null) {
          return true;
        }
        if (m_env.get(TABLE_COLUMN_WIDTH + keySuffix, null) != null) {
          return true;
        }
      }
    }
    return false;
  }

  public int getTableColumnWidth(IColumn col, int defaultWidth) {
    String keySuffix = getColumnKey(col);
    String key = TABLE_COLUMN_WIDTH + keySuffix;
    String value = m_env.get(key, null);
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
    String keySuffix = getColumnKey(col);
    String key = TABLE_COLUMN_VISIBLE + keySuffix;
    String value = m_env.get(key, null);
    if (value != null) {
      Boolean b = TypeCastUtility.castValue(value, Boolean.class);
      return b != null ? b.booleanValue() : defaultValue;
    }
    return defaultValue;
  }

  public int getTableColumnViewIndex(IColumn col, int defaultIndex) {
    String keySuffix = getColumnKey(col);
    String key = TABLE_COLUMN_UIINDEX + keySuffix;
    String value = m_env.get(key, null);
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

  public int getTableColumnSortIndex(IColumn col, int defaultValue) {
    String keySuffix = getColumnKey(col);
    String key = TABLE_COLUMN_SORT_INDEX + keySuffix;
    String value = m_env.get(key, null);
    if (value != null) {
      Integer i = TypeCastUtility.castValue(value, Integer.class);
      return i.intValue();
    }
    return defaultValue;
  }

  public boolean getTableColumnSortAscending(IColumn col, boolean defaultValue) {
    String keySuffix = getColumnKey(col);
    String key = TABLE_COLUMN_SORT_ASC + keySuffix;
    String value = m_env.get(key, null);
    if (value != null) {
      Boolean b = TypeCastUtility.castValue(value, Boolean.class);
      return b != null ? b.booleanValue() : defaultValue;
    }
    return defaultValue;
  }

  public Boolean getTableColumnSortExplicit(IColumn col) {
    String keySuffix = getColumnKey(col);
    String key = TABLE_COLUMN_SORT_EXPLICIT + keySuffix;
    String value = m_env.get(key, null);
    if (value != null) {
      return TypeCastUtility.castValue(value, Boolean.class);
    }
    return null;
  }

  public void setApplicationWindowPreferences(BoundsSpec r, boolean maximized) {
    if (r != null) {
      String value = "" + r.x + "," + r.y + "," + r.width + "," + r.height;
      m_env.put(APPLICATION_WINDOW_BOUNDS, value);
    }
    else {
      m_env.remove(APPLICATION_WINDOW_BOUNDS);
    }
    //
    if (maximized) {
      m_env.put(APPLICATION_WINDOW_MAXIMIZED, "yes");
    }
    else {
      m_env.remove(APPLICATION_WINDOW_MAXIMIZED);
    }
    flush();
  }

  public boolean getApplicationWindowMaximized() {
    String key = APPLICATION_WINDOW_MAXIMIZED;
    String value = m_env.get(key, null);
    if (value != null) {
      return value.equalsIgnoreCase("yes");
    }
    return false;
  }

  public BoundsSpec getApplicationWindowBounds() {
    String key = APPLICATION_WINDOW_BOUNDS;
    String value = m_env.get(key, null);
    if (value != null) {
      try {
        StringTokenizer tok = new StringTokenizer(value, ",");
        BoundsSpec r = new BoundsSpec(
            new Integer(tok.nextToken()).intValue(),
            new Integer(tok.nextToken()).intValue(),
            new Integer(tok.nextToken()).intValue(),
            new Integer(tok.nextToken()).intValue()
            );
        return r;
      }
      catch (Exception e) {
        LOG.warn("value=" + value, e);
      }
    }
    return null;
  }

  public void setCalendarPreferences(int displayMode, boolean displayCondensed) {
    m_env.put(CALENDAR_DISPLAY_MODE, "" + displayMode);
    m_env.put(CALENDAR_DISPLAY_CONDENSED, "" + displayCondensed);
    flush();
  }

  public int getCalendarDisplayMode(int defaultValue) {
    String key = CALENDAR_DISPLAY_MODE;
    String value = m_env.get(key, null);
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
    String key = CALENDAR_DISPLAY_CONDENSED;
    String value = m_env.get(key, null);
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
    String value = m_env.get(propName, null);
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
    m_env.put(propName, "" + value);
    flush();
  }

  public int[] getPropertyIntArray(String propName) {
    String strVal = m_env.get(propName, null);
    if (strVal != null) {
      String[] split = strVal.split(";");
      int[] val = new int[split.length];
      for (int i = 0; i < split.length; i++) {
        val[i] = Integer.parseInt(split[i]);
      }
      return val;
    }
    return null;
  }

  public void setPropertyIntArray(String propName, int[] value) {
    StringBuilder builder = new StringBuilder();
    if (value != null) {
      for (int i = 0; i < value.length; i++) {
        builder.append(value[i]);
        if (i != value.length - 1) {
          builder.append(";");
        }
      }
    }
    m_env.put(propName, builder.toString());
    flush();
  }

  public Double getPropertyDouble(String propName) {
    String value = m_env.get(propName, null);
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
    m_env.put(propName, "" + value);
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

  public Locale getLocale() {
    // >> legacy support. To be removed in release 3.9.0.
    String strLegacy = m_env.get(NLS_LOCALE_ISO, null);
    if (strLegacy != null) {
      m_env.remove(NLS_LOCALE_ISO); // remove legacy entry
      m_env.put(NLS_LOCALE_LANGUAGE, strLegacy);
      flush();
    }
    // << legacy support. To be removed in release 3.9.0.

    String strLanguage = m_env.get(NLS_LOCALE_LANGUAGE, null);
    String strCountry = m_env.get(NLS_LOCALE_COUNTRY, null);
    if (strLanguage != null && strCountry != null) {
      return new Locale(strLanguage, strCountry);
    }
    else if (strLanguage != null) {
      return new Locale(strLanguage, ClientUIPreferences.getHostLocale().getCountry());
    }
    return null;
  }

  public void setLocale(Locale locale) {
    if (locale != null) {
      m_env.put(NLS_LOCALE_LANGUAGE, locale.getLanguage());
      m_env.put(NLS_LOCALE_COUNTRY, locale.getCountry());
    }
    else {
      m_env.remove(NLS_LOCALE_LANGUAGE);
      m_env.remove(NLS_LOCALE_COUNTRY);
    }
    flush();
  }

  /**
   * @return
   *         the startup locale of the Java Virtual Machine which typically is based on the host environment
   */
  public static Locale getHostLocale() {
    return HOST_LOCALE;
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

  protected void flush() {
    SERVICES.getService(IUserPreferencesStorageService.class).storePreferences(m_env);
  }
}
