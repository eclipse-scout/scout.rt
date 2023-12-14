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

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;

public class TestWithoutTypeNameDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<String> value() {
    return doValue("value");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameDo withValue(String value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getValue() {
    return value().get();
  }
}
