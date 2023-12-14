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

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestPhysicalAddressEx")
public class TestPhysicalAddressExDo extends TestPhysicalAddressDo {

  public DoValue<String> poBox() {
    return doValue("poBox");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressExDo withPoBox(String poBox) {
    poBox().set(poBox);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getPoBox() {
    return poBox().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressExDo withStreet(String street) {
    street().set(street);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressExDo withCity(String city) {
    city().set(city);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressExDo withZipCode(String zipCode) {
    zipCode().set(zipCode);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressExDo withId(String id) {
    id().set(id);
    return this;
  }
}
