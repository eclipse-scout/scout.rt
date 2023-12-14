/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.TypedId;

@TypeName("scout.TestEntityWithTypedId")
public class TestEntityWithTypedIdDo extends DoEntity {

  public DoValue<TypedId<FixtureUuId>> uuId() {
    return doValue("uuId");
  }

  public DoValue<TypedId<FixtureStringId>> stringId() {
    return doValue("stringId");
  }

  public DoValue<TypedId<IId>> iid() {
    return doValue("iid");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithTypedIdDo withUuId(TypedId<FixtureUuId> uuId) {
    uuId().set(uuId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TypedId<FixtureUuId> getUuId() {
    return uuId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithTypedIdDo withStringId(TypedId<FixtureStringId> stringId) {
    stringId().set(stringId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TypedId<FixtureStringId> getStringId() {
    return stringId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithTypedIdDo withIid(TypedId<IId> iid) {
    iid().set(iid);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TypedId<IId> getIid() {
    return iid().get();
  }
}
