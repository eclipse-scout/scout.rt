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

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestEntityWithGenericValues")
public class TestEntityWithGenericValuesDo extends DoEntity {

  public DoList<TestGenericDo<?>> genericListAttribute() {
    return doList("genericListAttribute");
  }

  public DoValue<TestGenericDo<?>> genericAttribute() {
    return doValue("genericAttribute");
  }

  public DoValue<TestGenericDo<String>> genericStringAttribute() {
    return doValue("genericStringAttribute");
  }

  public DoValue<TestGenericDo<Double>> genericDoubleAttribute() {
    return doValue("genericDoubleAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithGenericValuesDo withGenericListAttribute(Collection<? extends TestGenericDo<?>> genericListAttribute) {
    genericListAttribute().updateAll(genericListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithGenericValuesDo withGenericListAttribute(TestGenericDo<?>... genericListAttribute) {
    genericListAttribute().updateAll(genericListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestGenericDo<?>> getGenericListAttribute() {
    return genericListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithGenericValuesDo withGenericAttribute(TestGenericDo<?> genericAttribute) {
    genericAttribute().set(genericAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<?> getGenericAttribute() {
    return genericAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithGenericValuesDo withGenericStringAttribute(TestGenericDo<String> genericStringAttribute) {
    genericStringAttribute().set(genericStringAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<String> getGenericStringAttribute() {
    return genericStringAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithGenericValuesDo withGenericDoubleAttribute(TestGenericDo<Double> genericDoubleAttribute) {
    genericDoubleAttribute().set(genericDoubleAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<Double> getGenericDoubleAttribute() {
    return genericDoubleAttribute().get();
  }
}
