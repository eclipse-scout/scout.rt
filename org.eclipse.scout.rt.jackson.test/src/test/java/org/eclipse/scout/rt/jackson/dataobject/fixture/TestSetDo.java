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

import java.util.Date;
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestSet")
public class TestSetDo extends DoEntity {

  public DoValue<Set<String>> stringSetAttribute() {
    return doValue("stringSetAttribute");
  }

  public DoValue<Set<Integer>> integerSetAttribute() {
    return doValue("integerSetAttribute");
  }

  public DoValue<Set<TestItemDo>> itemDoSetAttribute() {
    return doValue("itemDoSetAttribute");
  }

  public DoValue<Set<TestItemPojo>> itemPojoSetAttribute() {
    return doValue("itemPojoSetAttribute");
  }

  public DoValue<Set<Date>> dateSetAttribute() {
    return doValue("dateSetAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestSetDo withStringSetAttribute(Set<String> stringSetAttribute) {
    stringSetAttribute().set(stringSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<String> getStringSetAttribute() {
    return stringSetAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestSetDo withIntegerSetAttribute(Set<Integer> integerSetAttribute) {
    integerSetAttribute().set(integerSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<Integer> getIntegerSetAttribute() {
    return integerSetAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestSetDo withItemDoSetAttribute(Set<TestItemDo> itemDoSetAttribute) {
    itemDoSetAttribute().set(itemDoSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<TestItemDo> getItemDoSetAttribute() {
    return itemDoSetAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestSetDo withItemPojoSetAttribute(Set<TestItemPojo> itemPojoSetAttribute) {
    itemPojoSetAttribute().set(itemPojoSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<TestItemPojo> getItemPojoSetAttribute() {
    return itemPojoSetAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestSetDo withDateSetAttribute(Set<Date> dateSetAttribute) {
    dateSetAttribute().set(dateSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<Date> getDateSetAttribute() {
    return dateSetAttribute().get();
  }
}
