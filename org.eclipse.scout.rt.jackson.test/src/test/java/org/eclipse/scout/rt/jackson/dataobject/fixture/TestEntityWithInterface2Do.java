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
  public TestEntityWithInterface2Do withStringListAttribute(Collection<? extends String> stringListAttribute) {
    stringListAttribute().updateAll(stringListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withStringListAttribute(String... stringListAttribute) {
    stringListAttribute().updateAll(stringListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getStringListAttribute() {
    return stringListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withDoubleListAttribute(Collection<? extends Double> doubleListAttribute) {
    doubleListAttribute().updateAll(doubleListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withDoubleListAttribute(Double... doubleListAttribute) {
    doubleListAttribute().updateAll(doubleListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Double> getDoubleListAttribute() {
    return doubleListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withItemDoListAttribute(Collection<? extends TestItemDo> itemDoListAttribute) {
    itemDoListAttribute().updateAll(itemDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithInterface2Do withItemDoListAttribute(TestItemDo... itemDoListAttribute) {
    itemDoListAttribute().updateAll(itemDoListAttribute);
    return this;
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
