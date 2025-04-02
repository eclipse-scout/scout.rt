/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import java.util.Collection;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("scout.TableClientUiPreferenceProfile")
@TypeVersion(Scout_25_2_001.class)
public class TableClientUiPreferenceProfileDo extends DoEntity {

  /**
   * Contains settings (width, visible etc.) for all columns including custom columns
   */
  public DoCollection<TableColumnClientUiPreferenceDo> columns() {
    return doCollection("columns");
  }

  /**
   * Most user filters are column-based, but some use several columns to filter, thus keep it in
   * {@link TableClientUiPreferenceProfileDo} instead of {@link TableColumnClientUiPreferenceDo}.
   */
  public DoCollection<IUserFilterStateDo> userFilters() {
    return doCollection("userFilters");
  }

  /**
   * Data from table customizer (i.e. custom columns).
   */
  public DoValue<ITableCustomizerDo> tableCustomizerData() {
    return doValue("tableCustomizerData");
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public TableColumnClientUiPreferenceDo getColumn(TableColumnId columnId) {
    return columns().stream()
        .filter(colPref -> colPref.getColumnId().equals(columnId))
        .findFirst()
        .orElse(null);
  }

  public boolean removeColumn(TableColumnId columnId) {
    return getColumns().removeIf(colPref -> colPref.getColumnId().equals(columnId));
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #columns()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferenceProfileDo withColumns(Collection<? extends TableColumnClientUiPreferenceDo> columns) {
    columns().updateAll(columns);
    return this;
  }

  /**
   * See {@link #columns()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferenceProfileDo withColumns(TableColumnClientUiPreferenceDo... columns) {
    columns().updateAll(columns);
    return this;
  }

  /**
   * See {@link #columns()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Collection<TableColumnClientUiPreferenceDo> getColumns() {
    return columns().get();
  }

  /**
   * See {@link #userFilters()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferenceProfileDo withUserFilters(Collection<? extends IUserFilterStateDo> userFilters) {
    userFilters().updateAll(userFilters);
    return this;
  }

  /**
   * See {@link #userFilters()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferenceProfileDo withUserFilters(IUserFilterStateDo... userFilters) {
    userFilters().updateAll(userFilters);
    return this;
  }

  /**
   * See {@link #userFilters()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Collection<IUserFilterStateDo> getUserFilters() {
    return userFilters().get();
  }

  /**
   * See {@link #tableCustomizerData()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferenceProfileDo withTableCustomizerData(ITableCustomizerDo tableCustomizerData) {
    tableCustomizerData().set(tableCustomizerData);
    return this;
  }

  /**
   * See {@link #tableCustomizerData()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public ITableCustomizerDo getTableCustomizerData() {
    return tableCustomizerData().get();
  }
}
