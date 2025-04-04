/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.prefs.userfilter;

import java.util.Collection;
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.TableColumnId;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

/**
 * Stores selected values for boolean column filters.
 * <p>
 * For boolean columns the UI does not have a dedicated filter state but uses a TextColumnFilterState, storing integers
 * for the selected boolean values. As we require stronger typing for user filters, we can no longer store these
 * integers in {@link TextColumnUserFilterStateDo}'s selected String values list.
 */
@TypeName("scout.BooleanColumnUserFilterState")
@TypeVersion(Scout_25_2_001.class)
public class BooleanColumnUserFilterStateDo extends DoEntity implements IUserFilterStateDo {

  public DoValue<TableColumnId> columnId() {
    return doValue("columnId");
  }

  public DoSet<Boolean> selectedValues() {
    return doSet("selectedValues");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BooleanColumnUserFilterStateDo withColumnId(TableColumnId columnId) {
    columnId().set(columnId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnId getColumnId() {
    return columnId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BooleanColumnUserFilterStateDo withSelectedValues(Collection<? extends Boolean> selectedValues) {
    selectedValues().updateAll(selectedValues);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BooleanColumnUserFilterStateDo withSelectedValues(Boolean... selectedValues) {
    selectedValues().updateAll(selectedValues);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<Boolean> getSelectedValues() {
    return selectedValues().get();
  }
}
