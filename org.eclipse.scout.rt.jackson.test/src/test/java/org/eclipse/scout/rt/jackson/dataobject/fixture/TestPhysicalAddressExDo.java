/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("TestPhysicalAddressEx")
public class TestPhysicalAddressExDo extends TestPhysicalAddressDo {

  @Override
  public TestPhysicalAddressExDo withId(String id) {
    return (TestPhysicalAddressExDo) super.withId(id);
  }

  public DoValue<String> poBox() {
    return doValue("poBox");
  }
}
