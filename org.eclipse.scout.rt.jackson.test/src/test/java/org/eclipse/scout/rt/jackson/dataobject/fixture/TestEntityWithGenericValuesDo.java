/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

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
    genericListAttribute().clear();
    genericListAttribute().get().addAll(genericListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithGenericValuesDo withGenericListAttribute(TestGenericDo<?>... genericListAttribute) {
    return withGenericListAttribute(Arrays.asList(genericListAttribute));
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
