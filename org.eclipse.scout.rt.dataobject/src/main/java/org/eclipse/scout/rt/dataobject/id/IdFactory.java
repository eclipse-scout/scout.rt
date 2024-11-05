/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Factory for instances of {@link IId}.
 */
@ApplicationScoped
public class IdFactory {

  protected final ConcurrentMap<Class<? extends IId>, Method> m_ofMethodsByIdType = new ConcurrentHashMap<>();
  protected final ConcurrentMap<Class<? extends IId>, List<Class<?>>> m_rawTypesByIdType = new ConcurrentHashMap<>();

  /**
   * Creates a new wrapped {@link IId} by calling the <code>of(values)</code> method of the given id class.
   * <p>
   * <b>WARNING!</b> This internal method does not prevent type mismatches between the id type and the given object.
   * This may lead to ClassCastExceptions and other unexpected errors when invoked with wrong argument types. Consider
   * use the concrete factory methods e.g. {@code MyIdClass.of(value)} to create new id instances whenever possible.
   *
   * @throws PlatformException
   *           if an exception occurred while creating the id
   */
  public <ID extends IId> ID createInternal(Class<ID> idClass, Object... values) {
    try {
      Method createMethod = lookupCreateMethod(idClass);
      return idClass.cast(createMethod.invoke(null, values));
    }
    catch (Exception e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("idClass", idClass.getName())
          .withContextInfo("values", Arrays.toString(values));
    }
  }

  /**
   * @return {@link List} of raw type of the wrapped components of given {@code idClass}
   */
  public <ID extends IId> List<Class<?>> getRawTypes(Class<ID> idClass) {
    return m_rawTypesByIdType.computeIfAbsent(idClass, this::findTypeParameters);
  }

  // ---------------- helper methods ----------------

  protected List<Class<?>> findTypeParameters(Class<? extends IId> idClass) {
    return List.of(lookupCreateMethod(idClass).getParameterTypes());
  }

  protected <ID extends IId> Method lookupCreateMethod(Class<ID> idClass) {
    return m_ofMethodsByIdType.computeIfAbsent(idClass, this::findOfByTypesMethod);
  }

  protected Method findOfByTypesMethod(Class<? extends IId> idClass) {
    // (1) look for method annotated by @RawTypes
    for (Method m : idClass.getMethods()) {
      // NOTE: Name of method annotated by @RawTypes is 'of' by convention
      if (m.getAnnotation(RawTypes.class) != null && "of".equals(m.getName())) {
        assertTrue(Modifier.isStatic(m.getModifiers()), "method 'of({})' is expected to be static [method={}]", Arrays.toString(m.getParameterTypes()), m);
        return m;
      }
    }
    // (2) fallback to of method by generic type
    if (AbstractRootId.class.isAssignableFrom(idClass)) {
      Class<?> parameterType = TypeCastUtility.getGenericsParameterClass(idClass, AbstractRootId.class);
      return findOfMethod(idClass, parameterType);
    }
    throw new PlatformException("Cannot find a static method 'of({})' on id class {}.", idClass.getName(), idClass);
  }

  protected Method findOfMethod(Class<? extends IId> idClass, Class<?>... parameterTypes) {
    try {
      Method m = idClass.getMethod("of", parameterTypes);
      assertTrue(Modifier.isStatic(m.getModifiers()), "method 'of({})' is expected to be static [method={}]", Arrays.toString(parameterTypes), m);
      return m;
    }
    catch (@SuppressWarnings("squid:S1166") NoSuchMethodException e) {
      throw new PlatformException("Cannot find a static method 'of({})' on id class {}.", Arrays.toString(parameterTypes), idClass.getName());
    }
  }
}
