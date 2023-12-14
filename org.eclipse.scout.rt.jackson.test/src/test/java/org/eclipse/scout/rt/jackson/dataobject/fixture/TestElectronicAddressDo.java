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

@TypeName("TestElectronicAddress")
public class TestElectronicAddressDo extends AbstractTestAddressDo implements ITestAddressDo {

  public DoValue<String> email() {
    return doValue("email");
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

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestElectronicAddressDo withId(String id) {
    id().set(id);
    return this;
  }
}
