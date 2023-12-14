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

@TypeName("TestEntityWithLists")
public class TestEntityWithListsDo extends DoEntity {

  public DoValue<List<TestItemDo>> itemsListAttribute() {
    return doValue("itemsListAttribute");
  }

  public DoValue<List<String>> stringListAttribute() {
    return doValue("stringListAttribute");
  }

  public DoList<TestItemDo> itemsDoListAttribute() {
    return doList("itemsDoListAttribute");
  }

  public DoList<String> stringDoListAttribute() {
    return doList("stringDoListAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withItemsListAttribute(List<TestItemDo> itemsListAttribute) {
    itemsListAttribute().set(itemsListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemDo> getItemsListAttribute() {
    return itemsListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withStringListAttribute(List<String> stringListAttribute) {
    stringListAttribute().set(stringListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getStringListAttribute() {
    return stringListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withItemsDoListAttribute(Collection<? extends TestItemDo> itemsDoListAttribute) {
    itemsDoListAttribute().updateAll(itemsDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withItemsDoListAttribute(TestItemDo... itemsDoListAttribute) {
    itemsDoListAttribute().updateAll(itemsDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemDo> getItemsDoListAttribute() {
    return itemsDoListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withStringDoListAttribute(Collection<? extends String> stringDoListAttribute) {
    stringDoListAttribute().updateAll(stringDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withStringDoListAttribute(String... stringDoListAttribute) {
    stringDoListAttribute().updateAll(stringDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getStringDoListAttribute() {
    return stringDoListAttribute().get();
  }
}
