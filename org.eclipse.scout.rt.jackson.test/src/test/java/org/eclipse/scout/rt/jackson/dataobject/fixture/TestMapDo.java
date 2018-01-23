/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("TestMap")
public class TestMapDo extends DoEntity {

  public DoValue<Map<String, String>> stringStringMapAttribute() {
    return doValue("stringStringMapAttribute");
  }

  public DoValue<Map<Integer, Integer>> integerIntegerMapAttribute() {
    return doValue("integerIntegerMapAttribute");
  }

  public DoValue<Map<Double, TestItemDo>> doubleTestItemDoMapAttribute() {
    return doValue("doubleTestItemDoMapAttribute");
  }

  public DoValue<Map<String, TestItemPojo>> stringTestItemPojoMapAttribute() {
    return doValue("stringTestItemPojoMapAttribute");
  }

  public DoValue<Map<String, TestItemDo>> stringDoTestItemMapAttribute() {
    return doValue("stringDoTestItemMapAttribute");
  }

  public DoValue<Map<Date, UUID>> dateUUIDMapAttribute() {
    return doValue("dateUUIDMapAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestMapDo withStringStringMapAttribute(Map<String, String> stringStringMapAttribute) {
    stringStringMapAttribute().set(stringStringMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, String> getStringStringMapAttribute() {
    return stringStringMapAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestMapDo withIntegerIntegerMapAttribute(Map<Integer, Integer> integerIntegerMapAttribute) {
    integerIntegerMapAttribute().set(integerIntegerMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<Integer, Integer> getIntegerIntegerMapAttribute() {
    return integerIntegerMapAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestMapDo withDoubleTestItemDoMapAttribute(Map<Double, TestItemDo> doubleTestItemDoMapAttribute) {
    doubleTestItemDoMapAttribute().set(doubleTestItemDoMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<Double, TestItemDo> getDoubleTestItemDoMapAttribute() {
    return doubleTestItemDoMapAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestMapDo withStringTestItemPojoMapAttribute(Map<String, TestItemPojo> stringTestItemPojoMapAttribute) {
    stringTestItemPojoMapAttribute().set(stringTestItemPojoMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, TestItemPojo> getStringTestItemPojoMapAttribute() {
    return stringTestItemPojoMapAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestMapDo withStringDoTestItemMapAttribute(Map<String, TestItemDo> stringDoTestItemMapAttribute) {
    stringDoTestItemMapAttribute().set(stringDoTestItemMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, TestItemDo> getStringDoTestItemMapAttribute() {
    return stringDoTestItemMapAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestMapDo withDateUUIDMapAttribute(Map<Date, UUID> dateUUIDMapAttribute) {
    dateUUIDMapAttribute().set(dateUUIDMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<Date, UUID> getDateUUIDMapAttribute() {
    return dateUUIDMapAttribute().get();
  }
}
