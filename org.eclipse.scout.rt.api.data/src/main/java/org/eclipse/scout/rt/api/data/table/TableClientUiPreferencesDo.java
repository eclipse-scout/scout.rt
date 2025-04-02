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

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("scout.TableClientUiPreferences")
@TypeVersion(Scout_25_2_001.class)
public class TableClientUiPreferencesDo extends DoEntity {

  public DoValue<TableId> tableId() {
    return doValue("tableId");
  }

  /**
   * Identifier used to distinguish preferences for multiple instances of the same table.
   * <p>
   * For bookmarks this value is not set.
   */
  public DoValue<String> userPreferenceContext() {
    return doValue("userPreferenceContext");
  }

  /**
   * Note that this may be <code>true</code> even if there is no value for {@link #tileGlobalKey()}, because some tables
   * in tile mode implement tiles themselves.
   */
  public DoValue<Boolean> tileMode() {
    return doValue("tileMode");
  }

  public DoValue<String> tileGlobalKey() {
    return doValue("tileGlobalKey");
  }

  public DoValue<Map<TableClientUiPreferenceProfileId, TableClientUiPreferenceProfileDo>> tablePreferenceProfiles() {
    return doValue("tablePreferenceProfiles");
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public TableClientUiPreferenceProfileDo getGlobalTablePreferenceProfile() {
    return getTablePreferenceProfile(TableClientUiPreferenceProfileId.GLOBAL);
  }

  public TableClientUiPreferenceProfileDo getBookmarkedTablePreferenceProfile() {
    return getTablePreferenceProfile(TableClientUiPreferenceProfileId.BOOKMARK);
  }

  public TableClientUiPreferenceProfileDo getTablePreferenceProfile(TableClientUiPreferenceProfileId id) {
    return getTablePreferenceProfiles() != null ? getTablePreferenceProfiles().get(id) : null;
  }

  public void putTablePreferenceProfile(TableClientUiPreferenceProfileId id, TableClientUiPreferenceProfileDo prefs) {
    Map<TableClientUiPreferenceProfileId, TableClientUiPreferenceProfileDo> profiles = getTablePreferenceProfiles();
    if (profiles == null) {
      profiles = new HashMap<>();
      withTablePreferenceProfiles(profiles);
    }
    profiles.put(id, prefs);
  }

  public boolean removeTablePreferenceProfile(TableClientUiPreferenceProfileId id) {
    Map<TableClientUiPreferenceProfileId, TableClientUiPreferenceProfileDo> profiles = getTablePreferenceProfiles();
    if (profiles == null) {
      return false;
    }
    return profiles.remove(id) != null;
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferencesDo withTableId(TableId tableId) {
    tableId().set(tableId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableId getTableId() {
    return tableId().get();
  }

  /**
   * See {@link #userPreferenceContext()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferencesDo withUserPreferenceContext(String userPreferenceContext) {
    userPreferenceContext().set(userPreferenceContext);
    return this;
  }

  /**
   * See {@link #userPreferenceContext()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public String getUserPreferenceContext() {
    return userPreferenceContext().get();
  }

  /**
   * See {@link #tileMode()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferencesDo withTileMode(Boolean tileMode) {
    tileMode().set(tileMode);
    return this;
  }

  /**
   * See {@link #tileMode()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getTileMode() {
    return tileMode().get();
  }

  /**
   * See {@link #tileMode()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public boolean isTileMode() {
    return nvl(getTileMode());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferencesDo withTileGlobalKey(String tileGlobalKey) {
    tileGlobalKey().set(tileGlobalKey);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getTileGlobalKey() {
    return tileGlobalKey().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferencesDo withTablePreferenceProfiles(Map<TableClientUiPreferenceProfileId, TableClientUiPreferenceProfileDo> tablePreferenceProfiles) {
    tablePreferenceProfiles().set(tablePreferenceProfiles);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<TableClientUiPreferenceProfileId, TableClientUiPreferenceProfileDo> getTablePreferenceProfiles() {
    return tablePreferenceProfiles().get();
  }
}
