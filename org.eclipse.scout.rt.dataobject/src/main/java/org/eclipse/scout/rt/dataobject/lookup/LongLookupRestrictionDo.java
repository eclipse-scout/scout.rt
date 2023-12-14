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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

@TypeName("scout.LongLookupRestriction")
public class LongLookupRestrictionDo extends AbstractLookupRestrictionDo<Long> {

  @Override
  public DoList<Long> ids() {
    return doList(IDS);
  }

  /**
   * @return Specified {@code restriction} if not null, otherwise a new {@link LongLookupRestrictionDo} instance.
   */
  public static LongLookupRestrictionDo ensure(LongLookupRestrictionDo restriction) {
    return ObjectUtility.nvl(restriction, BEANS.get(LongLookupRestrictionDo.class));
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public LongLookupRestrictionDo withIds(Collection<? extends Long> ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public LongLookupRestrictionDo withIds(Long... ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public LongLookupRestrictionDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public LongLookupRestrictionDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public LongLookupRestrictionDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public LongLookupRestrictionDo withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return this;
  }
}
