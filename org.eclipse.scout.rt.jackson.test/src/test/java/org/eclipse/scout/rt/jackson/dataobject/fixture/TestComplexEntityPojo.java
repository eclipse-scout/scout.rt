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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Entity object with same fields as {@link TestComplexEntityDo} but using POJO style getter/setter and plain jackson
 * features.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("TestComplexEntity")
public class TestComplexEntityPojo {

  // TODO pbz: Remove this class when Jackson issue 1600 is fixed
  static class P_CustomLocaleSerializer extends JsonSerializer<Locale> {
    @Override
    public void serialize(Locale value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeObject(value.toLanguageTag());
    }
  }

  private String id;
  private String stringAttribute;
  private Integer integerAttribute;
  private Long longAttribute;
  private Float floatAttribute;
  private Double doubleAttribute;
  private BigDecimal bigDecimalAttribute;
  private BigInteger bigIntegerAttribute;
  private Object objectAttribute;
  private Date dateAttribute;
  private List<String> stringListAttribute;
  private TestItemPojo itemAttribute;
  private List<TestItemPojo> itemsAttribute;
  private UUID uuidAttribute;
  @JsonSerialize(using = P_CustomLocaleSerializer.class)
  private Locale localeAttribute;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStringAttribute() {
    return stringAttribute;
  }

  public void setStringAttribute(String stringAttribute) {
    this.stringAttribute = stringAttribute;
  }

  public Integer getIntegerAttribute() {
    return integerAttribute;
  }

  public void setIntegerAttribute(Integer integerAttribute) {
    this.integerAttribute = integerAttribute;
  }

  public Long getLongAttribute() {
    return longAttribute;
  }

  public void setLongAttribute(Long longAttribute) {
    this.longAttribute = longAttribute;
  }

  public Float getFloatAttribute() {
    return floatAttribute;
  }

  public void setFloatAttribute(Float floatAttribute) {
    this.floatAttribute = floatAttribute;
  }

  public Double getDoubleAttribute() {
    return doubleAttribute;
  }

  public void setDoubleAttribute(Double doubleAttribute) {
    this.doubleAttribute = doubleAttribute;
  }

  public BigDecimal getBigDecimalAttribute() {
    return bigDecimalAttribute;
  }

  public void setBigDecimalAttribute(BigDecimal bigDecimalAttribute) {
    this.bigDecimalAttribute = bigDecimalAttribute;
  }

  public BigInteger getBigIntegerAttribute() {
    return bigIntegerAttribute;
  }

  public void setBigIntegerAttribute(BigInteger bigIntegerAttribute) {
    this.bigIntegerAttribute = bigIntegerAttribute;
  }

  public Date getDateAttribute() {
    return dateAttribute;
  }

  public void setDateAttribute(Date dateAttribute) {
    this.dateAttribute = dateAttribute;
  }

  public Object getObjectAttribute() {
    return objectAttribute;
  }

  public void setObjectAttribute(Object objectAttribute) {
    this.objectAttribute = objectAttribute;
  }

  public List<String> getStringListAttribute() {
    return stringListAttribute;
  }

  public void setStringListAttribute(List<String> stringListAttribute) {
    this.stringListAttribute = stringListAttribute;
  }

  public TestItemPojo getItemAttribute() {
    return itemAttribute;
  }

  public void setItemAttribute(TestItemPojo itemAttribute) {
    this.itemAttribute = itemAttribute;
  }

  public List<TestItemPojo> getItemsAttribute() {
    return itemsAttribute;
  }

  public void setItemsAttribute(List<TestItemPojo> itemsAttribute) {
    this.itemsAttribute = itemsAttribute;
  }

  public UUID getUuidAttribute() {
    return uuidAttribute;
  }

  public void setUuidAttribute(UUID uuidAttribute) {
    this.uuidAttribute = uuidAttribute;
  }

  public Locale getLocaleAttribute() {
    return localeAttribute;
  }

  public void setLocaleAttribute(Locale localeAttribute) {
    this.localeAttribute = localeAttribute;
  }
}
