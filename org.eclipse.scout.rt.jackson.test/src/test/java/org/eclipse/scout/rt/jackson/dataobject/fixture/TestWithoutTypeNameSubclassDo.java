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

public class TestWithoutTypeNameSubclassDo extends TestWithoutTypeNameDo {

  public DoValue<String> idSub() {
    return doValue("idSub");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameSubclassDo withIdSub(String idSub) {
    idSub().set(idSub);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getIdSub() {
    return idSub().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameSubclassDo withId(String id) {
    id().set(id);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameSubclassDo withValue(String value) {
    value().set(value);
    return this;
  }
}
