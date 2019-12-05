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
package org.eclipse.scout.rt.platform.dataobject;

import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Helper class dealing with {@link IDoEntity} and its attributes.
 */
@ApplicationScoped
public class DataObjectHelper {

  protected IDataObjectMapper getDataObjectMapper() {
    return BEANS.get(IDataObjectMapper.class);
  }

  /**
   * Returns attribute {@code attributeName} converted to a {@link Integer} value.
   */
  public Integer getIntegerAttribute(IDoEntity entity, String attributeName) {
    return TypeCastUtility.castValue(entity.get(attributeName), Integer.class);
  }

  /**
   * Returns attribute {@code attributeName} converted to a {@link Double} value.
   */
  public Double getDoubleAttribute(IDoEntity entity, String attributeName) {
    return TypeCastUtility.castValue(entity.get(attributeName), Double.class);
  }

  /**
   * Returns attribute {@code attributeName} converted to a {@link BigInteger} value.
   */
  public BigInteger getBigIntegerAttribute(IDoEntity entity, String attributeName) {
    return TypeCastUtility.castValue(entity.get(attributeName), BigInteger.class);
  }

  /**
   * Returns attribute {@code attributeName} converted to a {@link Date} value.
   *
   * @see {@link IValueFormatConstants#parseDefaultDate} for default parse method for string-formatted using default
   *      format
   */
  public Date getDateAttribute(IDoEntity entity, String attributeName) {
    Object value = entity.get(attributeName);
    if (value instanceof String) {
      return IValueFormatConstants.parseDefaultDate.apply(value);
    }
    else {
      return TypeCastUtility.castValue(value, Date.class);
    }
  }

  /**
   * Returns attribute {@code attributeName} converted to a {@link UUID} value.
   */
  public UUID getUuidAttribute(IDoEntity entity, String attributeName) {
    Object value = entity.get(attributeName);
    if (value == null) {
      return null;
    }
    if (value instanceof UUID) {
      return UUID.class.cast(value);
    }
    else if (value instanceof String) {
      return UUID.fromString((String) value);
    }
    throw new IllegalArgumentException("Cannot convert value '" + value + "' to UUID");
  }

  /**
   * Returns attribute {@code attributeName} converted to a {@link Locale} value.
   */
  public Locale getLocaleAttribute(IDoEntity entity, String attributeName) {
    Object value = entity.get(attributeName);
    if (value == null) {
      return null;
    }
    if (value instanceof Locale) {
      return Locale.class.cast(value);
    }
    else if (value instanceof String) {
      return Locale.forLanguageTag(String.class.cast(value));
    }
    throw new IllegalArgumentException("Cannot convert value '" + value + "' to Locale");
  }

  /**
   * Returns attribute {@code attributeName} converted to a {@link IDoEntity} value.
   */
  public IDoEntity getEntityAttribute(IDoEntity entity, String propertyName) {
    return entity.get(propertyName, IDoEntity.class);
  }

  /**
   * Clones the given object using data object serialization and deserialization.
   *
   * @see IDataObjectMapper#writeValue(Object)
   * @see IDataObjectMapper#readValue(String, Class)
   */
  public <T extends IDoEntity> T clone(T value) {
    if (value == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    Class<T> valueType = (Class<T>) value.getClass();
    IDataObjectMapper mapper = getDataObjectMapper();
    String clone = mapper.writeValue(value);
    return mapper.readValue(clone, valueType);
  }

  /**
   * Clones the given object using data object serialization and deserialization.
   * <p>
   * Result is a generic {@link IDoEntity} object tree ignoring any available type attributes.
   *
   * @see IDataObjectMapper#writeValue(Object)
   * @see IDataObjectMapper#readValueRaw(String, Class)
   */
  public IDoEntity cloneRaw(IDoEntity value) {
    if (value == null) {
      return null;
    }
    IDataObjectMapper mapper = getDataObjectMapper();
    String clone = mapper.writeValue(value);
    return (IDoEntity) mapper.readValueRaw(clone);
  }

  /**
   * @return Serialized, human-readable representation of specified {@code entity}
   */
  public String toString(IDoEntity entity) {
    if (entity == null) {
      return Objects.toString(entity);
    }
    return getDataObjectMapper().writeValue(entity);
  }

  /**
   * Asserts that the given {@link DoValue} exists and its value is != <code>null</code>.
   *
   * @return the unwrapped value (never <code>null</code>)
   * @throws AssertionException
   *           if the given {@link DoValue} is null, does not exist, or has no value
   */
  public <T> T assertValue(DoValue<T> doValue) {
    Assertions.assertNotNull(doValue);
    Assertions.assertTrue(doValue.exists(), "Missing mandatory attribute '{}'", doValue.getAttributeName());
    return Assertions.assertNotNull(doValue.get(), "Value of property '{}' must not be null", doValue.getAttributeName());
  }

  /**
   * Asserts that the given {@link DoValue} exists and its value has text (according to
   * {@link StringUtility#hasText(CharSequence)}).
   *
   * @return the unwrapped value (never <code>null</code> or empty)
   * @throws AssertionException
   *           if the given {@link DoValue} is null, does not exist, or has no text
   */
  public String assertValueHasText(DoValue<String> doValue) {
    String value = assertValue(doValue);
    Assertions.assertTrue(StringUtility.hasText(value), "Value of property '{}' must have text", doValue.getAttributeName());
    return value;
  }
}
