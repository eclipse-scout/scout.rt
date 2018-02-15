/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject;

import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Helper class dealing with {@link IDoEntity} and it's attributes.
 */
@ApplicationScoped
public class DataObjectHelper {

  private LazyValue<IDataObjectMapper> m_dataObjectMapper = new LazyValue<>(IDataObjectMapper.class);

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
    throw new IllegalArgumentException("Cannot convert value " + value + " to UUID");
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
    throw new IllegalArgumentException("Cannot convert value " + value + " to Locale");
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
    String clone = m_dataObjectMapper.get().writeValue(value);
    return m_dataObjectMapper.get().readValue(clone, valueType);
  }

  /**
   * @return Serialized, human-readable representation of specified {@code entity}
   */
  public String toString(IDoEntity entity) {
    if (entity == null) {
      return Objects.toString(entity);
    }
    return m_dataObjectMapper.get().writeValue(entity);
  }
}
