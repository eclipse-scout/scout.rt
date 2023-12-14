/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.Lorem1Fixture")
public class Lorem1FixtureDo extends AbstractLoremFixtureDo {

  public DoValue<String> value1() {
    return doValue("value1");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public Lorem1FixtureDo withValue1(String value1) {
    value1().set(value1);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getValue1() {
    return value1().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public Lorem1FixtureDo withValue(String value) {
    value().set(value);
    return this;
  }
}
