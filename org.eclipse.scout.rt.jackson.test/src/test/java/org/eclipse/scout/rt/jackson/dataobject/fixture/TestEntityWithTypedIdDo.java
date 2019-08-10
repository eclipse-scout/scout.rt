/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.TypedId;

@TypeName("scout.TestEntityWithTypedId")
public class TestEntityWithTypedIdDo extends DoEntity {

  public DoValue<TypedId<FixtureUuId>> uuId() {
    return doValue("uuId");
  }

  public DoValue<TypedId<FixtureStringId>> stringId() {
    return doValue("stringId");
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
}
