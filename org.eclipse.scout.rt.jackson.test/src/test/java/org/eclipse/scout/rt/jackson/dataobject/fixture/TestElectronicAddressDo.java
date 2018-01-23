/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

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
}
