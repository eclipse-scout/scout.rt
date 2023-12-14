/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.lookup.fixture;

import java.util.Collection;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRestrictionDo;

@TypeName("start.FixtureDataLookupRestriction")
public class FixtureDataLookupRestrictionDo extends AbstractLookupRestrictionDo<Long> {

  @Override
  public DoList<Long> ids() {
    return doList(IDS);
  }

  public DoValue<String> startsWith() {
    return doValue("startsWith");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRestrictionDo withIds(Collection<? extends Long> ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRestrictionDo withIds(Long... ids) {
    ids().updateAll(ids);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRestrictionDo withStartsWith(String startsWith) {
    startsWith().set(startsWith);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStartsWith() {
    return startsWith().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRestrictionDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRestrictionDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRestrictionDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRestrictionDo withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return this;
  }
}
