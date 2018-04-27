/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("TestElectronicAddress")
public class TestElectronicAddressDo extends AbstractTestAddressDo {

  public DoValue<String> email() {
    return doValue("email");
  }

  @Override
  public TestElectronicAddressDo withId(String id) {
    return (TestElectronicAddressDo) super.withId(id);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestElectronicAddressDo withEmail(String email) {
    email().set(email);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getEmail() {
    return email().get();
  }
}
