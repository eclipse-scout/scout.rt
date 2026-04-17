/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.prefs.userfilter;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.table.DateGroupType;
import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.TableColumnId;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("scout.DateColumnUserFilterState")
@TypeVersion(Scout_25_2_001.class)
public class DateColumnUserFilterStateDo extends DoEntity implements IUserFilterStateDo {

  public DoValue<TableColumnId> columnId() {
    return doValue("columnId");
  }

  public DoSet<Long> selectedValues() {
    return doSet("selectedValues");
  }

  public DoValue<DateGroupType> groupType() {
    return doValue("groupType");
  }

  public DoValue<Date> dateFrom() {
    return doValue("dateFrom");
  }

  public DoValue<Date> dateTo() {
    return doValue("dateTo");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DateColumnUserFilterStateDo withColumnId(TableColumnId columnId) {
    columnId().set(columnId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnId getColumnId() {
    return columnId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateColumnUserFilterStateDo withSelectedValues(Collection<? extends Long> selectedValues) {
    selectedValues().updateAll(selectedValues);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateColumnUserFilterStateDo withSelectedValues(Long... selectedValues) {
    selectedValues().updateAll(selectedValues);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<Long> getSelectedValues() {
    return selectedValues().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateColumnUserFilterStateDo withGroupType(DateGroupType groupType) {
    groupType().set(groupType);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateGroupType getGroupType() {
    return groupType().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateColumnUserFilterStateDo withDateFrom(Date dateFrom) {
    dateFrom().set(dateFrom);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getDateFrom() {
    return dateFrom().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateColumnUserFilterStateDo withDateTo(Date dateTo) {
    dateTo().set(dateTo);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getDateTo() {
    return dateTo().get();
  }
}
