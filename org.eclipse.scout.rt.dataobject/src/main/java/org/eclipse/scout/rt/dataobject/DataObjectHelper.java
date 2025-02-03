/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Helper class dealing with {@link IDoEntity} and its attributes.
 */
@ApplicationScoped
public class DataObjectHelper {

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);
  protected final LazyValue<IDataObjectMapper> m_dataObjectMapper = new LazyValue<>(IDataObjectMapper.class);
  protected final LazyValue<ILenientDataObjectMapper> m_lenientDataObjectMapper = new LazyValue<>(ILenientDataObjectMapper.class);

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
    return cloneInternal(value, m_dataObjectMapper.get());
  }

  /**
   * Clones the given object using lenient data object serialization and deserialization.
   * <p>
   * Use only in special cases when provided object was already deserialized by using lenient mode.
   *
   * @see ILenientDataObjectMapper#writeValue(Object)
   * @see ILenientDataObjectMapper#readValue(String, Class)
   */
  public <T extends IDoEntity> T cloneLenient(T value) {
    return cloneInternal(value, m_lenientDataObjectMapper.get());
  }

  protected <T extends IDoEntity> T cloneInternal(T value, IDataObjectMapper mapper) {
    if (value == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    Class<T> valueType = (Class<T>) value.getClass();
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
    IDataObjectMapper mapper = m_dataObjectMapper.get();
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
    return m_dataObjectMapper.get().writeValue(entity);
  }

  /**
   * @return json as byte[]
   */
  public byte[] toBytes(IDoEntity entity) {
    if (entity == null) {
      return new byte[0];
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    m_dataObjectMapper.get().writeValue(out, entity);
    return out.toByteArray();
  }

  /**
   * Asserts that the given {@link DoValue} exists and its value is != <code>null</code>.
   *
   * @return the unwrapped value (never <code>null</code>)
   * @throws AssertionException
   *     if the given {@link DoValue} is null, does not exist, or has no value
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
   *     if the given {@link DoValue} is null, does not exist, or has no text
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
   *     Data object to normalize.
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

  /**
   * Cleans the data object, i.e. removes all {@link DoValue} nodes with value {@code null} and all
   * {@link IDoCollection} nodes containing an empty collection. This is useful to have a minimal data object.
   *
   * @param dataObject
   *     Data object to clean.
   */
  public void clean(IDataObject dataObject) {
    new P_CleanDataObjectVisitor().clean(dataObject);
  }

  protected static class P_CleanDataObjectVisitor extends AbstractDataObjectVisitor {

    public void clean(IDataObject dataObject) {
      visit(dataObject);
    }

    @Override
    protected void caseDoEntity(IDoEntity entity) {
      // (1) clean all nodes of entity
      Set<DoNode<?>> emptyNodes = new HashSet<>();
      for (DoNode<?> node : entity.allNodes().values()) {
        // (1a) clean null values
        if (node instanceof DoValue && node.get() == null) {
          emptyNodes.add(node);
        }
        // (1b) clean empty collections (i.e. DoCollection, DoList, DoSet)
        else if (node instanceof IDoCollection && ((IDoCollection) node).isEmpty()) {
          emptyNodes.add(node);
        }
        // (1c) clean nested DoEntities
        else {
          caseDoEntityNode(node);
        }
      }
      entity.removeIf(emptyNodes::contains);

      // (2) clean all contributions (i.e. itself implementations of DoEntity)
      caseDoEntityContributions(entity.getContributions());
    }
  }

  /**
   * Truncates the given {@link String}-typed {@link DoValue} if it exists, not {@code null} and longer than the
   * requested {@code maxLength}.
   */
  public void truncateStringValue(DoValue<String> doValue, int maxLength) {
    Assertions.assertGreater(maxLength, 0, "maxLength must be greater than 0");
    if (doValue == null || !doValue.exists() || StringUtility.length(doValue.get()) <= maxLength) {
      return;
    }
    doValue.set(doValue.get().substring(0, maxLength));
  }

  /**
   * Sets the content of the given {@link DoValue} to the given {@code value} if the current value is {@code null}.
   */
  public <V> void ensureValue(DoValue<V> doValue, V value) {
    if (doValue != null && doValue.get() == null) {
      doValue.set(value);
    }
  }

  /**
   * Sets the content of the given {@code DoValue} to the result of the given {@code valueSupplier} if the current value
   * is {@code null}.
   */
  public <V> void supplyValue(DoValue<V> doValue, Supplier<V> valueSupplier) {
    if (doValue != null && valueSupplier != null && doValue.get() == null) {
      doValue.set(valueSupplier.get());
    }
  }

  /**
   * Ensures that the given data object contains all declared nodes, e.g. all {@link DoValue} nodes are set to
   * {@code null} and all {@link DoList}, {@link DoSet} and {@link DoCollection} nodes are set to an empty value. If the
   * given object is {@code null}, {@code null} is returned.
   */
  public <E extends IDoEntity> E ensureDeclaredNodes(E entity) {
    if (entity == null) {
      return null;
    }
    m_dataObjectInventory.get().getAttributesDescription(entity.getClass())
        .values()
        .forEach(desc -> ensureNodeValue(entity, desc.getName(), desc.getType().getRawType(), null, false));
    return entity;
  }

  /**
   * Extends the {@code target} entity by adding those attributes from the {@code template} entity that are not already
   * available. The operation is not recursive.
   *
   * @param target
   *     target entity that is extended with attributes from the {@code template}. Must not be {@code null}.
   * @param template
   *     entity missing attributes are taken from. It is not modified and can be {@code null}.
   * @return the {@code target} object that was potentially modified
   */
  public <E extends IDoEntity> E extend(E target, IDoEntity template) {
    return ensureNodeValues(target, template, false);
  }

  /**
   * Applies all attribute values from the {@code template} entity to the {@code target} entity, overriding existing
   * values in @code target} entity. The operation is not recursive.
   *
   * @param target
   *     target entity where attributes from the {@code template} are applied. Must not be {@code null}.
   * @param template
   *     entity all attributes are taken from. It is not modified and can be {@code null}.
   * @return the {@code target} object that was potentially modified
   */
  public <E extends IDoEntity> E applyValues(E target, IDoEntity template) {
    return ensureNodeValues(target, template, true);
  }

  /**
   * Ensures that the all attributes of given data object {@code template} exists in data object {@code target}. If
   * {@code force} is set to {@code false} only attributes which do not exist in {@code target} are applied, if
   * {@code force} is set to {@code true} are attribute values from {@code template} are applied to {@code target}.
   */
  protected <E extends IDoEntity> E ensureNodeValues(E target, IDoEntity template, boolean force) {
    Assertions.assertNotNull(target, "target is required");
    if (template == null) {
      return target;
    }
    template.allNodes().forEach((name, node) -> ensureNodeValue(target, name, node.getClass(), node.get(), force));
    return target;
  }

  /**
   * Ensures that the given data object node exists. If {@code force} is set to {@code false} and the node did not exist
   * before, its value is set to the given {@code value} otherwise the value is unchanged. If {@code force} is set to
   * {@code true}, the node value is set to the given {@code value} independent of its former value.
   */
  protected void ensureNodeValue(IDoEntity entity, String attributeName, Type nodeType, Object value, boolean force) {
    if (!force && entity.has(attributeName)) {
      return;
    }

    if (nodeType == DoValue.class) {
      entity.put(attributeName, value);
    }
    else if (nodeType == DoList.class) {
      entity.putList(attributeName, (List<?>) value);
    }
    else if (nodeType == DoSet.class) {
      entity.putSet(attributeName, (Set<?>) value);
    }
    else if (nodeType == DoCollection.class) {
      entity.putCollection(attributeName, (Collection<?>) value);
    }
    else {
      Assertions.fail("unexpected DoNode of type [{}]", nodeType.getTypeName());
    }
  }
}
