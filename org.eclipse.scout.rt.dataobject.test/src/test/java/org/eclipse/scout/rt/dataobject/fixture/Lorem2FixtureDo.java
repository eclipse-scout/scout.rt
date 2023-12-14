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

@TypeName("scout.Lorem2Fixture")
public class Lorem2FixtureDo extends AbstractLoremFixtureDo {

  public DoValue<String> value2() {
    return doValue("value2");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public Lorem2FixtureDo withValue2(String value2) {
    value2().set(value2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getValue2() {
    return value2().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public Lorem2FixtureDo withValue(String value) {
    value().set(value);
    return this;
  }
}
