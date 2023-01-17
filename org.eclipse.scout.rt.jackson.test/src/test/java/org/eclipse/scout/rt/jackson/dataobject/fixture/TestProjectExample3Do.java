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

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestProjectExample3")
public class TestProjectExample3Do extends TestCoreExample3Do {

  public DoValue<String> nameEx() {
    return doValue("nameEx");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestProjectExample3Do withNameEx(String nameEx) {
    nameEx().set(nameEx);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getNameEx() {
    return nameEx().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestProjectExample3Do withName(String name) {
    name().set(name);
    return this;
  }
}
