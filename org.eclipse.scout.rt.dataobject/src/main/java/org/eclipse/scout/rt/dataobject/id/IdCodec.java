/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.eclipse.scout.rt.platform.security.SecurityUtility.createMac;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.Base64Utility.encodeUrlSafe;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.hashSet;
import static org.eclipse.scout.rt.platform.util.ObjectUtility.isOneOf;
import static org.eclipse.scout.rt.platform.util.StringUtility.isNullOrEmpty;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Codec used to convert between {@link IId} instances and their qualified/unqualified representation as {@link String}.
 */
@ApplicationScoped
public class IdCodec {

  protected static final String ID_TYPENAME_DELIMITER = ":";
  protected static final String SIGNATURE_DELIMITER = "_-~SIG~-_";

  protected final LazyValue<IdFactory> m_idFactory = new LazyValue<>(IdFactory.class);
  protected final LazyValue<IdInventory> m_idInventory = new LazyValue<>(IdInventory.class);

  protected final Map<Class<?>, Function<String, Object>> m_rawTypeFromStringMapper = new HashMap<>();
  protected final Map<Class<?>, Function<Object, String>> m_rawTypeToStringMapper = new HashMap<>();

  /**
   * Interface for flags used to parametrize conversion between {@link IId} instances and their qualified/unqualified
   * representation as {@link String}. This interface is used for all method declarations and allows extension of the
   * {@link IdCodecFlag} enum with custom flags.
   */
  public interface IIdCodecFlag {
  }

  public enum IdCodecFlag implements IIdCodecFlag {
    /**
     * Does not throw an exception but return {@link UnknownId} if the given string does not match the expected format
     * or the referenced class is not found.
     */
    LENIENT,
    /**
     * This will create a signature using the unqualified serialized {@link IId} and add it as a suffix. If this suffix
     * is incorrect an error is thrown during deserialization. {@link IId}s can be excluded from using signature
     * creation using {@link IdSignature}.
     */
    SIGNATURE
  }

  @PostConstruct
  protected void initialize() {
    // setup default type mappings between raw type <--> string
    registerRawTypeMapper(String.class, s -> s, Object::toString);
    registerRawTypeMapper(UUID.class, UUID::fromString, Object::toString);
    registerRawTypeMapper(Long.class, Long::parseLong, Object::toString);
    registerRawTypeMapper(Integer.class, Integer::parseInt, Object::toString);
    registerRawTypeMapper(Date.class, d -> new Date(Long.parseLong(d)), d -> String.valueOf(d.getTime()));
    registerRawTypeMapper(Locale.class, Locale::forLanguageTag, Locale::toLanguageTag);
    registerRawTypeMapper(Boolean.class, Boolean::valueOf, Object::toString);
  }

  protected IdFactory idFactory() {
    return m_idFactory.get();
  }

  protected IdInventory idInventory() {
    return m_idInventory.get();
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
  public String toQualified(IId id, IIdCodecFlag... flags) {
    return toQualified(id, hashSet(flags));
  }

  /**
   * @see #toQualified(IId, IIdCodecFlag...)
   */
  public String toQualified(IId id, Set<IIdCodecFlag> flags) {
    if (id == null) {
      return null;
    }
    String typeName = idInventory().getTypeName(id);
    if (isNullOrEmpty(typeName)) {
      if (id instanceof UnknownId) {
        // typeName of unknown id could be null, retain unknown id as-is for later migration
        return StringUtility.join(ID_TYPENAME_DELIMITER, ((UnknownId) id).getIdTypeName(), toUnqualified(id, flags));
      }
      else {
        throw new IdCodecException("Missing @{} in class {}", IdTypeName.class.getSimpleName(), id.getClass());
      }
    }
    return typeName + ID_TYPENAME_DELIMITER + toUnqualified(id, flags);
  }

  /**
   * Returns a string in the format <code>"[raw-id;raw-id;...]"</code>.
   * <ul>
   * <li><b>raw-id's</b> are the wrapped ids converted to their string representation (see
   * {@link #registerRawTypeMapper(Class, Function, Function)}), composite ids are unwrapped to their root ids and then
   * converted to their string representation, separated by ';'.
   * </ul>
   */
  public String toUnqualified(IId id, IIdCodecFlag... flags) {
    return toUnqualified(id, hashSet(flags));
  }

  /**
   * @see #toUnqualified(IId, IIdCodecFlag...)
   */
  public String toUnqualified(IId id, Set<IIdCodecFlag> flags) {
    if (id == null) {
      return null;
    }
    if (id instanceof IRootId) {
      Object value = id.unwrap();
      Function<Object, String> mapper = m_rawTypeToStringMapper.get(value.getClass());
      if (mapper == null) {
        throw new IdCodecException("Missing raw type mapper for wrapped type {}, id type {}", value.getClass(), id.getClass());
      }
      return addSignature(id.getClass(), mapper.apply(value), flags);
    }
    else if (id instanceof ICompositeId) {
      List<? extends IId> components = ((ICompositeId) id).unwrap();
      // remove signature flag as composites are signed as one and not part by part
      Set<IIdCodecFlag> flagsWithoutSignature = flags.stream()
          .filter(Predicate.not(IdCodecFlag.SIGNATURE::equals))
          .collect(Collectors.toSet());
      return addSignature(id.getClass(), components.stream()
          .map(comp -> toUnqualified(comp, flagsWithoutSignature))
          .map(s -> s == null ? "" : s) // empty string if component is null just in case of composite id
          .collect(Collectors.joining(";")), flags);
    }
    else if (id instanceof UnknownId) {
      return addSignature(UnknownId.class, ((UnknownId) id).getId(), flags);
    }
    return addSignature(id.getClass(), handleToUnqualifiedUnknownIdType(id, flags), flags);
  }

  /**
   * Adds a signature to the given unqualifiedId iff
   * <ul>
   * <li>signatures need to be added to the given idClass (see {@link IdSignature})
   * <li>the given flags contain {@link IdCodecFlag#SIGNATURE}
   * </ul>
   * This will create a signature (see {@link #createSignature(String)}) and add it and a delimiter as a suffix to the
   * given unqualifiedId.
   */
  protected String addSignature(Class<? extends IId> idClass, String unqualifiedId, Set<IIdCodecFlag> flags) {
    if (isNullOrEmpty(unqualifiedId) || !isOneOf(IdCodecFlag.SIGNATURE, flags) || !idInventory().isIdSignature(idClass)) {
      return unqualifiedId;
    }
    return unqualifiedId + SIGNATURE_DELIMITER + createSignature(unqualifiedId);
  }

  /**
   * Create a signature of the given unqualifiedId. Subclasses can override this method and use e.g. the current user id
   * as well.
   *
   * @return an url safe signature
   */
  public String createSignature(String unqualifiedId) {
    if (isNullOrEmpty(unqualifiedId)) {
      return "";
    }
    return encodeUrlSafe(createMac(getIdSignaturePassword(), unqualifiedId.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * @return private key used to create a signature (see {@link #createSignature(String)}).
   */
  protected byte[] getIdSignaturePassword() {
    String password = CONFIG.getPropertyValue(IdSignaturePasswordProperty.class);
    if (password == null) {
      throw new IdCodecException("Password property {} not set.", BEANS.get(IdSignaturePasswordProperty.class).getKey());
    }
    return password.getBytes(StandardCharsets.UTF_8);
  }

  // ---------------- String to IId ----------------

  /**
   * Parses a string in the format {@code [type-name]:[raw-id;raw-id;...]}.
   *
   * @return {@code IId} parsed from {@code qualifiedId}
   * @throws PlatformException
   *     if the given string does not match the expected format or the referenced class is not found.
   */
  public IId fromQualified(String qualifiedId, IIdCodecFlag... flags) {
    return fromQualified(qualifiedId, hashSet(flags));
  }

  /**
   * @see #fromQualified(String, IIdCodecFlag...)
   */
  public IId fromQualified(String qualifiedId, Set<IIdCodecFlag> flags) {
    return fromQualifiedInternal(qualifiedId, flags);
  }

  /**
   * Parses a string in the format {@code [raw-id;raw-id;...]}.
   *
   * @return {@code IId} parsed from {@code qualifiedId} or {@code null} if the given class or string is {@code null}
   * @throws PlatformException
   *     if the given string does not match the expected format
   */
  public <ID extends IId> ID fromUnqualified(Class<ID> idClass, String unqualifiedId, IIdCodecFlag... flags) {
    return fromUnqualified(idClass, unqualifiedId, hashSet(flags));
  }

  /**
   * @see #fromUnqualified(Class, String, IIdCodecFlag...)
   */
  public <ID extends IId> ID fromUnqualified(Class<ID> idClass, String unqualifiedId, Set<IIdCodecFlag> flags) {
    if (idClass == null) {
      throw new IdCodecException("Missing id class to parse unqualified id {}", unqualifiedId);
    }
    if (isNullOrEmpty(unqualifiedId)) {
      return null;
    }
    return fromUnqualifiedUnchecked(idClass, unqualifiedId, flags);
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

  // ---------------- helper methods ----------------

  /**
   * Callback method to implement if the codec should be extended to handle qualification of unknown {@link IId} types.
   */
  protected String handleToUnqualifiedUnknownIdType(IId id, Set<IIdCodecFlag> flags) {
    throw new IdCodecException("Unsupported id type {}, cannot convert id {}", id.getClass(), id);
  }

  /**
   * Parses a string in the format {@code [type-name]:[raw-id;raw-id;...]}.
   *
   * @param flags
   *     If the structure of the given {@code qualifiedId} is invalid and {@code IdCodecFlag.LENIENT} flag is set,
   *     value {@code null} is returned. If {@code IdCodecFlag.LENIENT} flag is not set, an exception is thrown.
   * @return {@code IId} parsed from {@code qualifiedId}
   */
  protected IId fromQualifiedInternal(String qualifiedId, Set<IIdCodecFlag> flags) {
    if (isNullOrEmpty(qualifiedId)) {
      return null;
    }
    boolean lenient = isOneOf(IdCodecFlag.LENIENT, flags);
    String[] tmp = qualifiedId.split(ID_TYPENAME_DELIMITER, 2); // split into at most two parts
    if (tmp.length < 2) { // no ":" found
      if (lenient) {
        //noinspection deprecation
        return UnknownId.of(null, qualifiedId);
      }
      else {
        throw new IdCodecException("Qualified id '{}' format is invalid", qualifiedId);
      }
    }
    String typeName = tmp[0];
    Class<? extends IId> idClass = idInventory().getIdClass(typeName);
    if (idClass == null) {
      if (lenient) {
        //noinspection deprecation
        return UnknownId.of(typeName, tmp[1]);
      }
      else {
        throw new IdCodecException("No class found for type name '{}'", typeName);
      }
    }

    try {
      return fromUnqualified(idClass, tmp[1], flags);
    }
    catch (Exception e) {
      // handle any deserialization issues in lenient mode by retaining the raw id as UnknownId instance
      if (lenient) {
        //noinspection deprecation
        return UnknownId.of(typeName, tmp[1]);
      }
      throw e;
    }
  }

  /**
   * Parses a string in the format {@code [raw-id;raw-id;...]} assuming inputs were checked for null/empty values
   * before.
   *
   * @return {@code IId} parsed from {@code qualifiedId} or {@code null} if the given class or string is {@code null}
   * @throws PlatformException
   *     if the given string does not match the expected format
   */
  protected <ID extends IId> ID fromUnqualifiedUnchecked(Class<ID> idClass, String unqualifiedId, Set<IIdCodecFlag> flags) {
    unqualifiedId = removeSignature(idClass, unqualifiedId, flags);
    String[] rawComponents = unqualifiedId.split(";", -1 /* force empty strings for empty components */);
    Object[] components = parseComponents(idClass, rawComponents, flags);
    return idFactory().createInternal(idClass, components);
  }

  /**
   * Removes the signature of the given unqualifiedId iff
   * <ul>
   * <li>signatures need to be added to the given idClass (see {@link IdSignature})
   * <li>the given flags contain {@link IdCodecFlag#SIGNATURE}
   * </ul>
   * This will split the signed unqualifiedId into the id and its signature and assert the signature's validity.
   */
  public String removeSignature(Class<? extends IId> idClass, String unqualifiedId, Set<IIdCodecFlag> flags) {
    String[] unqualifiedIdSignatureParts = splitToSignatureParts(unqualifiedId);
    assertSignature(idClass, unqualifiedId, unqualifiedIdSignatureParts, flags);
    return unqualifiedIdSignatureParts[0];
  }

  /**
   * Assert the validity of the given unqualifiedIdSignatureParts (see {@link #splitToSignatureParts(String)}). The id
   * needs to be signed iff
   * <ul>
   * <li>signatures need to be added to the given idClass (see {@link IdSignature})
   * <li>the given flags contain {@link IdCodecFlag#SIGNATURE}
   * </ul>
   * This will check the presence and a signature if the id needs to be signed and the absence of such a signature if
   * the id needs to be unsigned. In addition, the signature is verified for a signed id.
   */
  protected void assertSignature(Class<? extends IId> idClass, String unqualifiedId, String[] unqualifiedIdSignatureParts, Set<IIdCodecFlag> flags) {
    if (!isOneOf(IdCodecFlag.SIGNATURE, flags) || !idInventory().isIdSignature(idClass)) {
      checkEquals(unqualifiedIdSignatureParts.length, 1, "Unqualified id must not be signed.");
      checkEquals(unqualifiedIdSignatureParts[0], unqualifiedId, "Unqualified id must not be signed.");
      return;
    }
    checkEquals(unqualifiedIdSignatureParts.length, 2, "Unqualified id must be signed.");
    checkEquals(unqualifiedIdSignatureParts[1], createSignature(unqualifiedIdSignatureParts[0]), "Signature of unqualified id does not match.");
  }

  /**
   * Checks if the two given {@link Object}s are equal and throws an {@link IdCodecException} with the given message if
   * they are not equal.
   */
  protected void checkEquals(Object o1, Object o2, String message) {
    if (!Objects.equals(o1, o2)) {
      throw new IdCodecException(message);
    }
  }

  /**
   * Split the given unqualifiedId into id and signature using the {@link #SIGNATURE_DELIMITER}.
   */
  protected String[] splitToSignatureParts(String unqualifiedId) {
    int i = unqualifiedId.lastIndexOf(SIGNATURE_DELIMITER);
    if (i < 0) {
      return new String[]{unqualifiedId};
    }
    if (i + SIGNATURE_DELIMITER.length() == unqualifiedId.length()) {
      return new String[]{unqualifiedId.substring(0, i)};
    }
    return new String[]{unqualifiedId.substring(0, i), unqualifiedId.substring(i + SIGNATURE_DELIMITER.length())};
  }

  /**
   * Parses given {@code rawComponents} based on the declared component types of given {@code idClass}.
   */
  protected Object[] parseComponents(Class<? extends IId> idClass, String[] rawComponents, Set<IIdCodecFlag> flags) {
    List<Class<?>> componentTypes = idFactory().getRawTypes(idClass);
    if (!(componentTypes.size() == rawComponents.length)) {
      throw new IdCodecException("Wrong argument size, expected {} parameter, got {} raw components {}, idType={}", componentTypes.size(), rawComponents.length, Arrays.toString(rawComponents), idClass.getName());
    }

    Object[] components = new Object[rawComponents.length];
    for (int i = 0; i < rawComponents.length; i++) {
      Class<?> type = componentTypes.get(i);
      Function<String, ?> mapper = m_rawTypeFromStringMapper.get(type);
      if (mapper == null) {
        throw new IdCodecException("Missing raw type mapper for wrapped type {}, id type {}", type, idClass);
      }
      try {
        String raw = rawComponents[i];
        if (isNullOrEmpty(raw)) {
          components[i] = null;
        }
        else {
          components[i] = mapper.apply(raw);
        }
      }
      catch (Exception e) {
        throw new IdCodecException("Failed to parse component value={}, rawType={}, idType={}", rawComponents[i], type.getName(), idClass.getName(), e);
      }
    }
    return components;
  }

  public static class IdSignaturePasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.idSignaturePassword";
    }

    @Override
    public String description() {
      return "Password to create signatures for ids that are serialized or deserialized. The value of this password must be equal for all parts of an application.";
    }
  }
}
