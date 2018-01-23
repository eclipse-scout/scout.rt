/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@Replace
@TypeName("TestProjectExample2")
public class TestProjectExample2Do extends TestCoreExample2Do {

  public DoValue<String> nameEx() {
    return doValue("nameEx");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestProjectExample2Do withNameEx(String nameEx) {
    nameEx().set(nameEx);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getNameEx() {
    return nameEx().get();
  }
}
