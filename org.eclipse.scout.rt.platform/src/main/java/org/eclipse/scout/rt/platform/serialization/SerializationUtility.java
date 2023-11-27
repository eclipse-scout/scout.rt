/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.serialization;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility for serializing and deserializing java objects. The utility works in a standard java environment.
 * <p/>
 * The utility uses an environment-dependent {@link IObjectSerializerFactory} for creating {@link IObjectSerializer}
 * instances. An optional {@link IObjectReplacer} can be used for replacing and resolving objects during the
 * serialization and deserialization process, respectively.
 * <p/>
 *
 * @since 3.8.2
 */
public final class SerializationUtility {

  private static final IObjectSerializerFactory FACTORY = new BasicObjectSerializerFactory();

  private SerializationUtility() {
    // nop
  }

  /**
   * Uses a {@link IObjectSerializerFactory} for creating a new {@link IObjectSerializer}.
   *
   * @return Returns a new {@link IObjectSerializer}.
   */
  public static IObjectSerializer createObjectSerializer() {
    return createObjectSerializer(null);
  }

  /**
   * Uses a {@link IObjectSerializerFactory} for creating a new {@link IObjectSerializer} which uses the given
   * {@link IObjectReplacer} for substituting objects during the serializing and deserializing process.
   *
   * @return Returns a new {@link IObjectSerializer}.
   */
  public static IObjectSerializer createObjectSerializer(IObjectReplacer objectReplacer) {
    return FACTORY.createObjectSerializer(objectReplacer);
  }

  /**
   * Create a blacklist policy
   *
   * @param regexLists
   *          regex expressions separated with comma
   * @return a composite regex based on the individual regex
   * @since 11.0
   */
  public static Predicate<String> createBlacklistPolicy(String... regexLists) {
    return createPolicy(false, regexLists);
  }

  /**
   * Create a whitelist policy
   *
   * @param regexLists
   *          regex expressions separated with comma
   * @return a composite regex based on the individual regex
   * @since 11.0
   */
  public static Predicate<String> createWhitelistPolicy(String... regexLists) {
    return createPolicy(true, regexLists);
  }

  /**
   * Create a minimalistic whitelist policy that only knows the eclipse scout namespace. This whitelist is an
   * illustration of what a whitelist could look like. Implementors using serialization must analyze and craft a
   * specific per case whitelist for every usage point.
   * <p>
   * This policy contains the following regex
   *
   * <pre>
  \[.*
  (byte|char|short|int|long|double|float|boolean)
  java\..*
  org\.eclipse\.scout\..*
  org\.eclipsescout\..*
   * </pre>
   *
   * @since 11.0
   */
  public static Predicate<String> createDefaultScoutWhitelistPolicy() {
    return createPolicy(true,
        "\\[.*",
        "(byte|char|short|int|long|double|float|boolean)",
        "java\\..*",
        "org\\.eclipse\\.scout\\..*",
        "org\\.eclipsescout\\..*");
  }

  /**
   * Create a blacklist or a whitelist policy
   *
   * @param regexLists
   *          regex expressions separated with comma
   * @return a composite regex based on the individual regex
   * @since 11.0
   */
  public static Predicate<String> createPolicy(boolean defaultWhenEmpty, String... regexLists) {
    if (regexLists == null || regexLists.length == 0) {
      return c -> defaultWhenEmpty;
    }
    List<String> regexParts = Arrays
        .stream(regexLists)
        .filter(Objects::nonNull)
        .flatMap(regexList -> Arrays.stream(regexList.split(",")))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
    if (regexParts.isEmpty()) {
      return c -> defaultWhenEmpty;
    }
    String compositeRegex = regexParts
        .stream()
        .collect(Collectors.joining("|", "(", ")"));
    Pattern p = Pattern.compile(compositeRegex);
    return c -> p.matcher(c).matches();
  }

  /**
   * @return Returns an environment-dependent {@link ClassLoader} that is able to load all classes that are available in
   *         the running environment.
   */
  public static ClassLoader getClassLoader() {
    return FACTORY.getClassLoader();
  }
}
