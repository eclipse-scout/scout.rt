/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

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

  public DoValue<Map<String, IDoEntity>> stringIDoEntityMapAttribute() {
    return doValue("stringIDoEntityMapAttribute");
  }

  public DoValue<IDoEntity> iDoEntityAttribute() {
    return doValue("iDoEntityAttribute");
  }

  public DoValue<Map<Date, UUID>> dateUUIDMapAttribute() {
    return doValue("dateUUIDMapAttribute");
  }

  public DoValue<Map<Locale, Locale>> localeLocaleMapAttribute() {
    return doValue("localeLocaleMapAttribute");
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
  public TestMapDo withStringIDoEntityMapAttribute(Map<String, IDoEntity> stringIDoEntityMapAttribute) {
    stringIDoEntityMapAttribute().set(stringIDoEntityMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, IDoEntity> getStringIDoEntityMapAttribute() {
    return stringIDoEntityMapAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestMapDo withIDoEntityAttribute(IDoEntity iDoEntityAttribute) {
    iDoEntityAttribute().set(iDoEntityAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getIDoEntityAttribute() {
    return iDoEntityAttribute().get();
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

  @Generated("DoConvenienceMethodsGenerator")
  public TestMapDo withLocaleLocaleMapAttribute(Map<Locale, Locale> localeLocaleMapAttribute) {
    localeLocaleMapAttribute().set(localeLocaleMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<Locale, Locale> getLocaleLocaleMapAttribute() {
    return localeLocaleMapAttribute().get();
  }
}
