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
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRestrictionDo;

@TypeName("start.FixtureEnumLookupRestriction")
public class FixtureEnumLookupRestrictionDo extends AbstractLookupRestrictionDo<FixtureEnum> {

  @Override
  public DoList<FixtureEnum> ids() {
    return doList(IDS);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnumLookupRestrictionDo withIds(Collection<? extends FixtureEnum> ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnumLookupRestrictionDo withIds(FixtureEnum... ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnumLookupRestrictionDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnumLookupRestrictionDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnumLookupRestrictionDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnumLookupRestrictionDo withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return this;
  }
}
