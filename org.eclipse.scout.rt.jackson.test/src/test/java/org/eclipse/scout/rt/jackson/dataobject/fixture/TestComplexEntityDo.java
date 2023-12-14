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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestComplexEntity")
public class TestComplexEntityDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<String> stringAttribute() {
    return doValue("stringAttribute");
  }

  public DoValue<Integer> integerAttribute() {
    return doValue("integerAttribute");
  }

  public DoValue<Long> longAttribute() {
    return doValue("longAttribute");
  }

  public DoValue<Float> floatAttribute() {
    return doValue("floatAttribute");
  }

  public DoValue<Double> doubleAttribute() {
    return doValue("doubleAttribute");
  }

  public DoValue<BigInteger> bigIntegerAttribute() {
    return doValue("bigIntegerAttribute");
  }

  public DoValue<BigDecimal> bigDecimalAttribute() {
    return doValue("bigDecimalAttribute");
  }

  public DoValue<Date> dateAttribute() {
    return doValue("dateAttribute");
  }

  public DoValue<Object> objectAttribute() {
    return doValue("objectAttribute");
  }

  public DoValue<List<String>> stringListAttribute() {
    return doValue("stringListAttribute");
  }

  public DoValue<TestItemDo> itemAttribute() {
    return doValue("itemAttribute");
  }

  public DoValue<List<TestItemDo>> itemsAttribute() {
    return doValue("itemsAttribute");
  }

  public DoValue<UUID> uuidAttribute() {
    return doValue("uuidAttribute");
  }

  public DoValue<Locale> localeAttribute() {
    return doValue("localeAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withStringAttribute(String stringAttribute) {
    stringAttribute().set(stringAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStringAttribute() {
    return stringAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withIntegerAttribute(Integer integerAttribute) {
    integerAttribute().set(integerAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getIntegerAttribute() {
    return integerAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withLongAttribute(Long longAttribute) {
    longAttribute().set(longAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Long getLongAttribute() {
    return longAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withFloatAttribute(Float floatAttribute) {
    floatAttribute().set(floatAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Float getFloatAttribute() {
    return floatAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withDoubleAttribute(Double doubleAttribute) {
    doubleAttribute().set(doubleAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Double getDoubleAttribute() {
    return doubleAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withBigIntegerAttribute(BigInteger bigIntegerAttribute) {
    bigIntegerAttribute().set(bigIntegerAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigInteger getBigIntegerAttribute() {
    return bigIntegerAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withBigDecimalAttribute(BigDecimal bigDecimalAttribute) {
    bigDecimalAttribute().set(bigDecimalAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigDecimal getBigDecimalAttribute() {
    return bigDecimalAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withDateAttribute(Date dateAttribute) {
    dateAttribute().set(dateAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getDateAttribute() {
    return dateAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withObjectAttribute(Object objectAttribute) {
    objectAttribute().set(objectAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Object getObjectAttribute() {
    return objectAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withStringListAttribute(List<String> stringListAttribute) {
    stringListAttribute().set(stringListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getStringListAttribute() {
    return stringListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withItemAttribute(TestItemDo itemAttribute) {
    itemAttribute().set(itemAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo getItemAttribute() {
    return itemAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withItemsAttribute(List<TestItemDo> itemsAttribute) {
    itemsAttribute().set(itemsAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemDo> getItemsAttribute() {
    return itemsAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withUuidAttribute(UUID uuidAttribute) {
    uuidAttribute().set(uuidAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UUID getUuidAttribute() {
    return uuidAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestComplexEntityDo withLocaleAttribute(Locale localeAttribute) {
    localeAttribute().set(localeAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Locale getLocaleAttribute() {
    return localeAttribute().get();
  }
}
