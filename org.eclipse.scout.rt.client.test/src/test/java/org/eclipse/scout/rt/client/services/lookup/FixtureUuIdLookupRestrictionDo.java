/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.lookup;

import java.util.Collection;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRestrictionDo;

@TypeName("scout.FixtureUuIdLookupRestriction")
public class FixtureUuIdLookupRestrictionDo extends AbstractLookupRestrictionDo<FixtureUuId> {

  @Override
  public DoList<FixtureUuId> ids() {
    return doList(IDS);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRestrictionDo withIds(Collection<? extends FixtureUuId> ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRestrictionDo withIds(FixtureUuId... ids) {
    ids().updateAll(ids);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRestrictionDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRestrictionDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRestrictionDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRestrictionDo withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return this;
  }
}
