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

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.id.IId;

@TypeName("scout.TestTypedUntypedInnerIfc")
public class TestTypedUntypedInnerIfcDo extends DoEntity implements ITestTypedUntypedInnerDo {

  @Override
  public DoValue<FixtureStringId> stringId() {
    return doValue("stringId");
  }

  @Override
  public DoValue<IId> iId() {
    return doValue("iId");
  }

  public DoValue<FixtureStringId> stringId2() {
    return doValue("stringId2");
  }

  public DoValue<IId> iId2() {
    return doValue("iId2");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestTypedUntypedInnerIfcDo withStringId(FixtureStringId stringId) {
    stringId().set(stringId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureStringId getStringId() {
    return stringId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestTypedUntypedInnerIfcDo withIId(IId iId) {
    iId().set(iId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IId getIId() {
    return iId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestTypedUntypedInnerIfcDo withStringId2(FixtureStringId stringId2) {
    stringId2().set(stringId2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureStringId getStringId2() {
    return stringId2().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestTypedUntypedInnerIfcDo withIId2(IId iId2) {
    iId2().set(iId2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IId getIId2() {
    return iId2().get();
  }
}
