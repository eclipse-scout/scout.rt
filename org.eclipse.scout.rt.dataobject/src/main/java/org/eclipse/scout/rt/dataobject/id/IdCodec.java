/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.dataobject.migration.IIdTypeNameMigrationHandler;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Codec used to convert between {@link IId} instances and their qualified/unqualified representation as {@link String}.
 */
@ApplicationScoped
public class IdCodec {

  protected final LazyValue<IdFactory> m_idFactory = new LazyValue<>(IdFactory.class);
  protected final LazyValue<IdInventory> m_idInventory = new LazyValue<>(IdInventory.class);

  protected final Map<Class<?>, Function<String, Object>> m_rawTypeFromStringMapper = new HashMap<>();
  protected final Map<Class<?>, Function<Object, String>> m_rawTypeToStringMapper = new HashMap<>();

  protected final Map<String, String> m_legacyIdTypeNameMappings = new HashMap<>();

  @PostConstruct
  protected void initialize() {
    // setup default type mappings between raw type <--> string
    registerRawTypeMapper(String.class, s -> s, Object::toString);
    registerRawTypeMapper(UUID.class, UUID::fromString, Object::toString);
    registerRawTypeMapper(Long.class, Long::parseLong, Object::toString);
    registerRawTypeMapper(Integer.class, Integer::parseInt, Object::toString);
    registerRawTypeMapper(Date.class, d -> new Date(Long.parseLong(d)), d -> String.valueOf(d.getTime()));
    registerRawTypeMapper(Locale.class, Locale::forLanguageTag, Locale::toLanguageTag);

    // setup type name migrations for legacy id type names
    initTypeNameMigrations();
  }

  protected void initTypeNameMigrations() {
    BEANS.all(IIdTypeNameMigrationHandler.class).stream()
        .flatMap(h -> h.getIdTypeNameTranslations().entrySet().stream())
        .forEach(e -> m_legacyIdTypeNameMappings.put(e.getKey(), e.getValue()));
  }

  // ---------------- IId to String ----------------

  /**
   * Returns a string in the format <code>"[type-name]:[raw-id;raw-id;...]"</code>.
   * <ul>
   * <li><b>type-name</b> is computed by {@link IdInventory#getTypeName(IId)}.
   * <li><b>raw-id's</b> are the wrapped ids converted to their string representation (see
   * {@link #registerRawTypeMapper(Class, Function, Function)}), composite ids are unwrapped to their root ids and then
   * converted to their string representation, separated by ';'.
   * </ul>
   */
  public String toQualified(IId id) {
    if (id == null) {
      return null;
    }
    String typeName = m_idInventory.get().getTypeName(id);
    if (StringUtility.isNullOrEmpty(typeName)) {
      throw new PlatformException("Missing @{} in class {}", IdTypeName.class.getSimpleName(), id.getClass());
    }
    return typeName + ":" + toUnqualified(id);
  }

  /**
   * Returns a string in the format <code>"[raw-id;raw-id;...]"</code>.
   * <ul>
   * <li><b>raw-id's</b> are the wrapped ids converted to their string representation (see
   * {@link #registerRawTypeMapper(Class, Function, Function)}), composite ids are unwrapped to their root ids and then
   * converted to their string representation, separated by ';'.
   * </ul>
   */
  public String toUnqualified(IId id) {
    if (id == null) {
      return null;
    }
    if (id instanceof IRootId) {
      Object value = id.unwrap();
      Function<Object, String> mapper = m_rawTypeToStringMapper.get(value.getClass());
      if (mapper == null) {
        throw new PlatformException("Missing raw type mapper for wrapped type {}, id type {}", value.getClass(), id.getClass());
      }
      return mapper.apply(value);
    }
    else if (id instanceof ICompositeId) {
      List<? extends IId> components = ((ICompositeId) id).unwrap();
      return components.stream()
          .map(this::toUnqualified)
          .map(s -> s == null ? "" : s) // empty string if component is null just in case of composite id
          .collect(Collectors.joining(";"));
    }
    return handleToUnqualifiedUnknownIdType(id);
  }

  // ---------------- String to IId ----------------

  /**
   * Parses a string in the format {@code [type-name]:[raw-id;raw-id;...]}.
   *
   * @return {@code IId} parsed from {@code qualifiedId}
   * @throws PlatformException
   *           if the given string does not match the expected format or the referenced class is not found.
   */
  public IId fromQualified(String qualifiedId) {
    return fromQualifiedInternal(qualifiedId, false);
  }

  /**
   * Parses a string in the format {@code [type-name]:[raw-id;raw-id;...]}.
   *
   * @return {@code IId} parsed from {@code qualifiedId} or {@code null} if the given string does not match the expected
   *         format or the referenced class is not found.
   */
  public IId fromQualifiedLenient(String qualifiedId) {
    return fromQualifiedInternal(qualifiedId, true);
  }

  /**
   * Parses a string in the format {@code [raw-id;raw-id;...]}.
   *
   * @return {@code IId} parsed from {@code qualifiedId} or {@code null} if the given class or string is {@code null}
   * @throws PlatformException
   *           if the given string does not match the expected format
   */
  public <ID extends IId> ID fromUnqualified(Class<ID> idClass, String unqualifiedId) {
    if (idClass == null) {
      throw new PlatformException("Missing id class to parse unqualified id {}", unqualifiedId);
    }
    if (StringUtility.isNullOrEmpty(unqualifiedId)) {
      return null;
    }
    return fromUnqualifiedUnchecked(idClass, unqualifiedId);
  }

  /**
   * Register type mapping between a string representation and the corresponding raw type.
   * <p>
   * Note: The access to the type mapping data structure is not synchronized and therefore not thread safe. Use this
   * method to set up the {@link IdCodec} instance directly after platform start and not to change the {@link IdCodec}
   * behavior dynamically at runtime.
   */
  public <T> void registerRawTypeMapper(Class<T> rawType, Function<String, Object> fromStringMapper, Function<T, String> toStringMapper) {
    assertNotNull(rawType, "cannot register type mapper for null type");
    assertNotNull(fromStringMapper, "cannot register null type mapper from string");
    assertNotNull(toStringMapper, "cannot register null type mapper to string");

    //noinspection unchecked
    m_rawTypeToStringMapper.put(rawType, (Function<Object, String>) toStringMapper);
    m_rawTypeFromStringMapper.put(rawType, fromStringMapper);
  }

  /**
   * Unregister type mapping between a string representation and the given {@code rawType}.
   * <p>
   * Note: The access to the type mapping data structure is not synchronized and therefore not thread safe. Use this
   * method to set up the {@link IdCodec} instance directly after platform start and not to change the {@link IdCodec}
   * behavior dynamically at runtime.
   */
  public void unregisterRawTypeMapper(Class<?> rawType) {
    m_rawTypeToStringMapper.remove(rawType);
    m_rawTypeFromStringMapper.remove(rawType);
  }

  // TODO PBZ Add Javadoc
  public void registerLegacyIdTypeNameMapping(String legacyIdTypeName, String newIdTypeName) {
    m_legacyIdTypeNameMappings.put(legacyIdTypeName, newIdTypeName);
  }

  // TODO PBZ Add Javadoc
  public void unregisterLegacyIdTypeNameMapping(String legacyIdTypeName) {
    m_legacyIdTypeNameMappings.remove(legacyIdTypeName);
  }

  // ---------------- helper methods ----------------

  /**
   * Callback method to implement if the codec should be extended to handle qualification of unknown {@link IId} types.
   */
  protected String handleToUnqualifiedUnknownIdType(IId id) {
    throw new PlatformException("Unsupported id type {}, cannot convert id {}", id.getClass(), id);
  }

  /**
   * Parses a string in the format {@code [type-name]:[raw-id;raw-id;...]}.
   *
   * @param lenient
   *          If the structure of the given {@code qualifiedId} is invalid and {@code lenient} flag is set to
   *          {@code true}, value {@code null} is returned. If {@code lenient} flag is set to {@code false}, an
   *          exception is thrown.
   * @return {@code IId} parsed from {@code qualifiedId}
   */
  protected IId fromQualifiedInternal(String qualifiedId, boolean lenient) {
    if (StringUtility.isNullOrEmpty(qualifiedId)) {
      return null;
    }
    String[] tmp = qualifiedId.split(":", 2); // split into at most two parts
    if (tmp.length < 2) { // no ":" found
      if (lenient) {
        return null;
      }
      else {
        throw new PlatformException("Qualified id '{}' format is invalid", qualifiedId);
      }
    }
    String typeName = tmp[0];
    Class<? extends IId> idClass = m_idInventory.get().getIdClass(typeName);

    // (1) idClass not found, try to re-map legacy id type names
    if (idClass == null) {
      String newTypeName = typeName;
      Set<String> remapping = new LinkedHashSet<>();
      remapping.add(typeName);
      // TODO PBZ Consider pre-calculate re-mappings A -> B -> C into A -> C and B -> C
      while (m_legacyIdTypeNameMappings.containsKey(newTypeName)) {
        newTypeName = m_legacyIdTypeNameMappings.get(newTypeName);
        if (!remapping.add(newTypeName)) {
          throw new PlatformException("Found remapping cycle for type name '{}', remapping {}", typeName, remapping);
        }
      }
      idClass = m_idInventory.get().getIdClass(newTypeName);
    }

    // (2) check if mapping found
    if (idClass == null) {
      if (lenient) {
        return null;
      }
      else {
        throw new PlatformException("No class found for type name '{}'", typeName);
      }
    }
    return fromUnqualified(idClass, tmp[1]);
  }

  /**
   * Parses a string in the format {@code [raw-id;raw-id;...]} assuming inputs were checked for null/empty values
   * before.
   *
   * @return {@code IId} parsed from {@code qualifiedId} or {@code null} if the given class or string is {@code null}
   * @throws PlatformException
   *           if the given string does not match the expected format
   */
  protected <ID extends IId> ID fromUnqualifiedUnchecked(Class<ID> idClass, String unqualifiedId) {
    String[] rawComponents = unqualifiedId.split(";", -1 /* force empty strings for empty components */);
    Object[] components = parseComponents(idClass, rawComponents);
    return m_idFactory.get().createInternal(idClass, components);
  }

  /**
   * Parses given {@code rawComponents} based on the declared component types of given {@code idClass}.
   */
  protected Object[] parseComponents(Class<? extends IId> idClass, String[] rawComponents) {
    List<Class<?>> componentTypes = m_idFactory.get().getRawTypes(idClass);
    if (!(componentTypes.size() == rawComponents.length)) {
      throw new PlatformException("Wrong argument size, expected {} parameter, got {} raw components {}, idType={}", componentTypes.size(), rawComponents.length, Arrays.toString(rawComponents), idClass.getName());
    }

    Object[] components = new Object[rawComponents.length];
    for (int i = 0; i < rawComponents.length; i++) {
      Class<?> type = componentTypes.get(i);
      Function<String, ?> mapper = m_rawTypeFromStringMapper.get(type);
      if (mapper == null) {
        throw new PlatformException("Missing raw type mapper for wrapped type {}, id type {}", type, idClass);
      }
      try {
        String raw = rawComponents[i];
        if (StringUtility.isNullOrEmpty(raw)) {
          components[i] = null;
        }
        else {
          components[i] = mapper.apply(raw);
        }
      }
      catch (Exception e) {
        throw new PlatformException("Failed to parse component value={}, rawType={}, idType={}", rawComponents[i], type.getName(), idClass.getName(), e);
      }
    }
    return components;
  }
}
