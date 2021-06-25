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
package org.eclipse.scout.rt.dataobject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
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
   * @see IValueFormatConstants#parseDefaultDate for default parse method for string-formatted using default format
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
      return (UUID) value;
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
      return (Locale) value;
    }
    else if (value instanceof String) {
      return Locale.forLanguageTag((String) value);
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
   * @see IDataObjectMapper#readValueRaw(String)
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

  /**
   * Normalize the data object, i.e. applies a deterministic sorting to collections of type {@link DoSet} and
   * {@link DoCollection}. This is useful to have a comparable output if the same data object is serialized twice.
   *
   * @param dataObject
   *          Data object to normalize.
   */
  public void normalize(IDataObject dataObject) {
    new P_NormalizationDataObjectVisitor().normalize(dataObject);
  }

  protected static class P_NormalizationDataObjectVisitor extends AbstractDataObjectVisitor {

    public void normalize(IDataObject dataObject) {
      visit(dataObject);
    }

    @Override
    protected void caseDoSet(DoSet<?> doSet) {
      super.caseDoSet(doSet); // deep first
      if (doSet.exists()) {
        normalizeInternal(doSet.get());
      }
    }

    @Override
    protected void caseDoCollection(DoCollection<?> doCollection) {
      super.caseDoCollection(doCollection); // deep first
      if (doCollection.exists()) {
        normalizeInternal(doCollection.get());
      }
    }

    @Override
    protected void caseDoEntityContributions(Collection<IDoEntityContribution> contributions) {
      super.caseDoEntityContributions(contributions); // deep first
      normalizeInternal(contributions);
    }

    protected <V> void normalizeInternal(Collection<V> collection) {
      if (collection.isEmpty()) {
        return;
      }

      List<V> list = new ArrayList<>(collection);
      boolean comparable = list.stream().allMatch(item -> item instanceof Comparable);
      if (comparable) {
        // Directly comparable
        list.sort(null);
      }
      else {
        // Not comparable, use string representation
        IDataObjectMapper mapper = BEANS.get(IDataObjectMapper.class);
        IdentityHashMap<V, String> jsons = new IdentityHashMap<>();
        list.sort((o1, o2) -> {
          String o1Json = jsons.computeIfAbsent(o1, mapper::writeValue);
          String o2Json = jsons.computeIfAbsent(o2, mapper::writeValue);
          return ObjectUtility.compareTo(o1Json, o2Json);
        });
      }

      collection.clear();
      collection.addAll(list); // replace
    }
  }
}
