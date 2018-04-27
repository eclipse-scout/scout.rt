/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("TestPhysicalAddress")
public class TestPhysicalAddressDo extends AbstractTestAddressDo {

  @Override
  public TestPhysicalAddressDo withId(String id) {
    return (TestPhysicalAddressDo) super.withId(id);
  }

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
}
