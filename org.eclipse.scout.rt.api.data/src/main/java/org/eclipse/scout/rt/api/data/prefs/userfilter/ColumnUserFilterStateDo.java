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
 * Column filter state used for all other columns than the "typed" ones (date, text, number, ...).
 * <p>
 * Selected values are always {@link String} (UI filter, thus never the internal value, e.g. domain key).
 */
@TypeName("scout.ColumnUserFilterState")
@TypeVersion(Scout_25_2_001.class)
public class ColumnUserFilterStateDo extends DoEntity implements IUserFilterStateDo {

  public DoValue<TableColumnId> columnId() {
    return doValue("columnId");
  }

  public DoSet<String> selectedValues() {
    return doSet("selectedValues");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ColumnUserFilterStateDo withColumnId(TableColumnId columnId) {
    columnId().set(columnId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnId getColumnId() {
    return columnId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ColumnUserFilterStateDo withSelectedValues(Collection<? extends String> selectedValues) {
    selectedValues().updateAll(selectedValues);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ColumnUserFilterStateDo withSelectedValues(String... selectedValues) {
    selectedValues().updateAll(selectedValues);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<String> getSelectedValues() {
    return selectedValues().get();
  }
}
