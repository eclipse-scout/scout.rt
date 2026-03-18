/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.prefs;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.table.TableClientUiPreferencesDo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_002__uiPreferences;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("scout.UiPreferences")
@TypeVersion(Scout_25_2_002__uiPreferences.class)
public class UiPreferencesDo extends DoEntity {

  public DoList<TableClientUiPreferencesDo> tablePreferences() {
    return doList("tablePreferences");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public UiPreferencesDo withTablePreferences(Collection<? extends TableClientUiPreferencesDo> tablePreferences) {
    tablePreferences().updateAll(tablePreferences);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiPreferencesDo withTablePreferences(TableClientUiPreferencesDo... tablePreferences) {
    tablePreferences().updateAll(tablePreferences);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TableClientUiPreferencesDo> getTablePreferences() {
    return tablePreferences().get();
  }
}
