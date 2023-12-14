/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing.signature.fixture;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_0_0;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.id.AbstractStringId;

@TypeName("dataObjectFixture.SignatureFixture")
@TypeVersion(DataObjectFixture_1_0_0.class)
public class SignatureFixtureDo extends DoEntity {

  public DoList<FixtureStringId> idAttribute() {
    return doList("idAttribute");
  }

  public DoList<AbstractStringId> abstractIdAttribute() {
    return doList("abstractIdAttribute");
  }

  public DoValue<FixtureEnum> enumAttribute() {
    return doValue("enumAttribute");
  }

  public DoValue<SignatureSubFixtureDo> doAttribute() {
    return doValue("doAttribute");
  }

  public DoValue<ISignatureFixtureAttributeDo> doInterfaceAttribute() {
    return doValue("doInterfaceAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureFixtureDo withIdAttribute(Collection<? extends FixtureStringId> idAttribute) {
    idAttribute().updateAll(idAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureFixtureDo withIdAttribute(FixtureStringId... idAttribute) {
    idAttribute().updateAll(idAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<FixtureStringId> getIdAttribute() {
    return idAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureFixtureDo withAbstractIdAttribute(Collection<? extends AbstractStringId> abstractIdAttribute) {
    abstractIdAttribute().updateAll(abstractIdAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureFixtureDo withAbstractIdAttribute(AbstractStringId... abstractIdAttribute) {
    abstractIdAttribute().updateAll(abstractIdAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<AbstractStringId> getAbstractIdAttribute() {
    return abstractIdAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureFixtureDo withEnumAttribute(FixtureEnum enumAttribute) {
    enumAttribute().set(enumAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureEnum getEnumAttribute() {
    return enumAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureFixtureDo withDoAttribute(SignatureSubFixtureDo doAttribute) {
    doAttribute().set(doAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureSubFixtureDo getDoAttribute() {
    return doAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureFixtureDo withDoInterfaceAttribute(ISignatureFixtureAttributeDo doInterfaceAttribute) {
    doInterfaceAttribute().set(doInterfaceAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ISignatureFixtureAttributeDo getDoInterfaceAttribute() {
    return doInterfaceAttribute().get();
  }
}
