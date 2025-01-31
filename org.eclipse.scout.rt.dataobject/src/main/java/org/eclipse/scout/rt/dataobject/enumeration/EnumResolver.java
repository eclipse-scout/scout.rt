/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.enumeration;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.*;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EnumResolver {

  private static final Logger LOG = LoggerFactory.getLogger(EnumResolver.class);

  private final Method m_sentinelMethod;
  private final ConcurrentMap<Class<? extends IEnum>, Map<String, IEnum>> m_enumCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<Class<? extends IEnum>, Method> m_resolveMethodsByEnumType = new ConcurrentHashMap<>();

  public EnumResolver() {
    Method m = null;
    try {
      m = EnumResolver.class.getDeclaredMethod("sentinelMethod");
    }
    catch (@SuppressWarnings("squid:S1166") Exception e) {
      LOG.warn("Cannot find sentinelMethod. Enum resolver will run with reduced performance.");
    }
    m_sentinelMethod = m;
  }

  public <ENUM extends IEnum> ENUM resolve(Class<ENUM> enumClass, String value) {
    assertNotNull(enumClass, "enumClass is required");
    assertTrue(enumClass.isEnum(), "enumClass is required to be an enum");
    try {
      Method resolveMethod = m_resolveMethodsByEnumType.computeIfAbsent(enumClass, this::findResolveMethodInternal);
      if (ObjectUtility.notEquals(resolveMethod, m_sentinelMethod)) {
        return enumClass.cast(resolveMethod.invoke(null, value));
      }
      return resolveByReflection(enumClass, value);
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  protected Method findResolveMethodInternal(Class<? extends IEnum> enumClass) {
    Method m = findResolveMethod(enumClass);
    return m == null ? m_sentinelMethod : m;
  }

  protected Method findResolveMethod(Class<? extends IEnum> enumClass) {
    try {
      Method m = enumClass.getMethod("resolve", String.class);
      Assertions.assertTrue(Modifier.isStatic(m.getModifiers()), "resolve method is expected to be static [method={}]", m);
      return m;
    }
    catch (@SuppressWarnings("squid:S1166") NoSuchMethodException e) {
      return null;
    }
  }

  protected <ENUM extends IEnum> ENUM resolveByReflection(Class<ENUM> enumClass, String value) {
    if (value == null) {
      return null;
    }

    Map<String, IEnum> values = m_enumCache.computeIfAbsent(enumClass, c -> {
      final ENUM[] enumConstants = enumClass.getEnumConstants();
      final Map<String, IEnum> mappings = new HashMap<>(enumConstants.length);
      for (ENUM e : enumConstants) {
        mappings.put(e.stringValue(), e);
      }
      return Collections.unmodifiableMap(mappings);
    });

    final ENUM result = enumClass.cast(values.get(value));
    if (result == null) {
      throw new AssertionException("unknown string value '{}' [enum={}]", value, enumClass.getName());
    }
    return result;
  }

  /**
   * Sentinel method used for enum classes which are not implementing a resolve method
   * ({@link Map#computeIfAbsent(Object, java.util.function.Function)} assumes {@code null} values as undefined and
   * triggers another computation).
   */
  @SuppressWarnings("unused")
  private static void sentinelMethod() {
    // NOP
  }
}
