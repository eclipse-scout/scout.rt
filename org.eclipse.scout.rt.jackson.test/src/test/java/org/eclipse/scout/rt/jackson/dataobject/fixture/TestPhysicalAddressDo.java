/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

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
}
