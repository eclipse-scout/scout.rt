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

@TypeName("TestEntityWithInterface2")
public class TestEntityWithInterface2Do extends DoEntity implements ITestBaseEntityDo {

  @Override
  public DoValue<String> stringAttribute() {
    return doValue("stringAttribute");
  }

  @Override
  public DoValue<Double> doubleAttribute() {
    return doValue("doubleAttribute");
  }

  @Override
  public DoValue<TestItemDo> itemDoAttribute() {
    return doValue("itemDoAttribute");
  }

  @Override
  public DoList<String> stringListAttribute() {
    return doList("stringListAttribute");
  }

  @Override
  public DoList<Double> doubleListAttribute() {
    return doList("doubleListAttribute");
  }

  @Override
  public DoList<TestItemDo> itemDoListAttribute() {
    return doList("itemDoListAttribute");
  }

  public DoValue<String> stringAttributeEx() {
    return doValue("stringAttributeEx");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withStringAttribute(String stringAttribute) {
    stringAttribute().set(stringAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStringAttribute() {
    return stringAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withDoubleAttribute(Double doubleAttribute) {
    doubleAttribute().set(doubleAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Double getDoubleAttribute() {
    return doubleAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withItemDoAttribute(TestItemDo itemDoAttribute) {
    itemDoAttribute().set(itemDoAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo getItemDoAttribute() {
    return itemDoAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withStringListAttribute(Collection<String> stringListAttribute) {
    stringListAttribute().clear();
    stringListAttribute().get().addAll(stringListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withStringListAttribute(String... stringListAttribute) {
    return withStringListAttribute(Arrays.asList(stringListAttribute));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getStringListAttribute() {
    return stringListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withDoubleListAttribute(Collection<Double> doubleListAttribute) {
    doubleListAttribute().clear();
    doubleListAttribute().get().addAll(doubleListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withDoubleListAttribute(Double... doubleListAttribute) {
    return withDoubleListAttribute(Arrays.asList(doubleListAttribute));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Double> getDoubleListAttribute() {
    return doubleListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withItemDoListAttribute(Collection<TestItemDo> itemDoListAttribute) {
    itemDoListAttribute().clear();
    itemDoListAttribute().get().addAll(itemDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withItemDoListAttribute(TestItemDo... itemDoListAttribute) {
    return withItemDoListAttribute(Arrays.asList(itemDoListAttribute));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemDo> getItemDoListAttribute() {
    return itemDoListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withStringAttributeEx(String stringAttributeEx) {
    stringAttributeEx().set(stringAttributeEx);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStringAttributeEx() {
    return stringAttributeEx().get();
  }
}
