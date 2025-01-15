/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.lookup;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;

/**
 * Abstract base class for lookup call restrictions
 *
 * @param <ID>
 *     Lookup row id type
 */
public abstract class AbstractLookupRestrictionDo<ID> extends DoEntity {

  public static final String IDS = "ids";
  public static final String TEXT = "text";
  public static final String PARENT_IDS = "parentIds";
  public static final String ACTIVE = "active";
  public static final String ENABLED = "enabled";
  public static final String MAX_ROW_COUNT = "maxRowCount";

  public abstract DoList<ID> ids();

  public DoValue<String> text() {
    return doValue(TEXT);
  }

  public DoValue<Boolean> active() {
    return doValue(ACTIVE);
  }

  public DoValue<Boolean> enabled() {
    return doValue(ENABLED);
  }

  public DoValue<Integer> maxRowCount() {
    return doValue(MAX_ROW_COUNT);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRestrictionDo<ID> withIds(Collection<? extends ID> ids) {
    ids().updateAll(ids);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRestrictionDo<ID> withIds(ID... ids) {
    ids().updateAll(ids);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<ID> getIds() {
    return ids().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRestrictionDo<ID> withText(String text) {
    text().set(text);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getText() {
    return text().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRestrictionDo<ID> withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getActive() {
    return active().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isActive() {
    return nvl(getActive());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRestrictionDo<ID> withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getEnabled() {
    return enabled().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isEnabled() {
    return nvl(getEnabled());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRestrictionDo<ID> withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getMaxRowCount() {
    return maxRowCount().get();
  }
}
