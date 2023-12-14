/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("DataObjectWithCompositeId")
public class DataObjectWithCompositeIdDo extends DoEntity {

  public DoValue<FixtureCompositeId> id() {
    return doValue("id");
  }

  public DoList<FixtureCompositeId> ids() {
    return doList("ids");
  }

  public DoValue<FixtureWrapperCompositeId> wrappedCompositeId() {
    return doValue("wrappedCompositeId");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithCompositeIdDo withId(FixtureCompositeId id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureCompositeId getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithCompositeIdDo withIds(Collection<? extends FixtureCompositeId> ids) {
    ids().updateAll(ids);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithCompositeIdDo withIds(FixtureCompositeId... ids) {
    ids().updateAll(ids);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<FixtureCompositeId> getIds() {
    return ids().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithCompositeIdDo withWrappedCompositeId(FixtureWrapperCompositeId wrappedCompositeId) {
    wrappedCompositeId().set(wrappedCompositeId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureWrapperCompositeId getWrappedCompositeId() {
    return wrappedCompositeId().get();
  }
}
