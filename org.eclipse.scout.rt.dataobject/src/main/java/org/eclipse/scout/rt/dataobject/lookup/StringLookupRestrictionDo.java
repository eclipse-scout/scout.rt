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

@TypeName("scout.StringLookupRestriction")
public class StringLookupRestrictionDo extends AbstractLookupRestrictionDo<String> {

  @Override
  public DoList<String> ids() {
    return doList(IDS);
  }

  /**
   * @return Specified {@code restriction} if not null, otherwise a new {@link StringLookupRestrictionDo} instance.
   */
  public static StringLookupRestrictionDo ensure(StringLookupRestrictionDo restriction) {
    return ObjectUtility.nvl(restriction, BEANS.get(StringLookupRestrictionDo.class));
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRestrictionDo withIds(Collection<? extends String> ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRestrictionDo withIds(String... ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRestrictionDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRestrictionDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRestrictionDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRestrictionDo withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return this;
  }
}
