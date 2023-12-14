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

@TypeName("TestPhysicalAddress")
public class TestPhysicalAddressDo extends AbstractTestAddressDo implements ITestAddressDo {

  public DoValue<String> street() {
    return doValue("street");
  }

  public DoValue<String> city() {
    return doValue("city");
  }

  public DoValue<String> zipCode() {
    return doValue("zipCode");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressDo withStreet(String street) {
    street().set(street);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStreet() {
    return street().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressDo withCity(String city) {
    city().set(city);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getCity() {
    return city().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressDo withZipCode(String zipCode) {
    zipCode().set(zipCode);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getZipCode() {
    return zipCode().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestPhysicalAddressDo withId(String id) {
    id().set(id);
    return this;
  }
}
