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
import org.eclipse.scout.rt.platform.Replace;

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

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestProjectExample2Do withName(String name) {
    name().set(name);
    return this;
  }
}
