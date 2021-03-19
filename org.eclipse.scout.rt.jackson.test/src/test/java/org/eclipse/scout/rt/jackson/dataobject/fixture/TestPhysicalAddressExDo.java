/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
