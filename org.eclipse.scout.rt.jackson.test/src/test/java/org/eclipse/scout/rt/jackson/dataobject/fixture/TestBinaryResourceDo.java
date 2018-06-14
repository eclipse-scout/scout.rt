/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

@TypeName("TestBinaryResource")
public class TestBinaryResourceDo extends DoEntity {

  public DoValue<BinaryResource> brDefault() {
    return doValue("brDefault");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestBinaryResourceDo withBrDefault(BinaryResource brDefault) {
    brDefault().set(brDefault);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BinaryResource getBrDefault() {
    return brDefault().get();
  }
}
