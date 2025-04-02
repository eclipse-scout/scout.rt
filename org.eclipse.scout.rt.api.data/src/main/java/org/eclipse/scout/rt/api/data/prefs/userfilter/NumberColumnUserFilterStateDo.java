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

import java.math.BigDecimal;
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

@TypeName("scout.NumberColumnUserFilterState")
@TypeVersion(Scout_25_2_001.class)
public class NumberColumnUserFilterStateDo extends DoEntity implements IUserFilterStateDo {

  public DoValue<TableColumnId> columnId() {
    return doValue("columnId");
  }

  public DoSet<Double> selectedValues() {
    return doSet("selectedValues");
  }

  public DoValue<BigDecimal> numberFrom() {
    return doValue("numberFrom");
  }

  public DoValue<BigDecimal> numberTo() {
    return doValue("numberTo");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public NumberColumnUserFilterStateDo withColumnId(TableColumnId columnId) {
    columnId().set(columnId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnId getColumnId() {
    return columnId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public NumberColumnUserFilterStateDo withSelectedValues(Collection<? extends Double> selectedValues) {
    selectedValues().updateAll(selectedValues);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public NumberColumnUserFilterStateDo withSelectedValues(Double... selectedValues) {
    selectedValues().updateAll(selectedValues);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<Double> getSelectedValues() {
    return selectedValues().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public NumberColumnUserFilterStateDo withNumberFrom(BigDecimal numberFrom) {
    numberFrom().set(numberFrom);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigDecimal getNumberFrom() {
    return numberFrom().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public NumberColumnUserFilterStateDo withNumberTo(BigDecimal numberTo) {
    numberTo().set(numberTo);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigDecimal getNumberTo() {
    return numberTo().get();
  }
}
