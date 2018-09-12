/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Date;
import java.util.Set;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

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
