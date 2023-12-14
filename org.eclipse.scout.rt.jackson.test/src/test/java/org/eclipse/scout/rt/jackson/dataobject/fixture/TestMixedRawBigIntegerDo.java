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

import java.math.BigInteger;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestMixedRawBigInteger")
public class TestMixedRawBigIntegerDo extends DoEntity {

  public DoValue<BigInteger> bigIntegerAttribute() {
    return doValue("bigIntegerAttribute");
  }

  public DoValue<Number> numberAttribute() {
    return doValue("numberAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestMixedRawBigIntegerDo withBigIntegerAttribute(BigInteger bigIntegerAttribute) {
    bigIntegerAttribute().set(bigIntegerAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigInteger getBigIntegerAttribute() {
    return bigIntegerAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestMixedRawBigIntegerDo withNumberAttribute(Number numberAttribute) {
    numberAttribute().set(numberAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Number getNumberAttribute() {
    return numberAttribute().get();
  }
}
