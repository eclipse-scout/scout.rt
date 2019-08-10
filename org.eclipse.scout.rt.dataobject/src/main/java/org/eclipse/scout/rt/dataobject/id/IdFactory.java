/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.id;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Factory for instances of {@link IId}.
 */
@ApplicationScoped
public class IdFactory {

  protected final ConcurrentMap<Class<? extends IId<?>>, Method> m_ofMethodsByIdType = new ConcurrentHashMap<>();
  protected final ConcurrentMap<Class<? extends IId<?>>, Method> m_ofMethodsByString = new ConcurrentHashMap<>();

  /**
   * Creates a new wrapped {@link IId} by calling the <code>of(value)</code> method of the given id class.
   * <p>
   * <b>WARNING!</b> This internal method does not prevent type mismatches between the id type and the given object.
   * This may lead to ClassCastExceptions and other unexpected errors. Consider using the type-safe utility function
   * {@link IIds#create(Class, UUID)} instead.
   *
   * @throws PlatformException
   *           if an exception occurred while creating the id
   */
  public <ID extends IId<?>> ID createInternal(Class<ID> idClass, Object value) {
    try {
      Method createMethod = m_ofMethodsByIdType.computeIfAbsent(idClass, this::findOfByTypeMethod);
      return idClass.cast(createMethod.invoke(null, value));
    }
    catch (Exception e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("idClass", idClass.getName())
          .withContextInfo("value", value);
    }
  }

  /**
   * Creates a new wrapped {@link IId} by calling the <code>fromString(string)</code> method of the given id class. If
   * no such method exists, the factory may use some default mechanism to convert the string to the wrapped type and
   * then call {@link #createInternal(Class, Object)}. If no default conversion is available, an exception is thrown.
   *
   * @throws PlatformException
   *           if an exception occurred while creating the id
   */
  public <ID extends IId<?>> ID createFromString(Class<ID> idClass, String string) {
    try {
      Method createMethod = m_ofMethodsByString.computeIfAbsent(idClass, this::findOfByStringMethod);
      return idClass.cast(createMethod.invoke(null, string));
    }
    catch (Exception e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("idClass", idClass.getName())
          .withContextInfo("string", string);
    }
  }

  protected Method findOfByTypeMethod(Class<? extends IId<?>> idClass) {
    Class<?> parameterType = TypeCastUtility.getGenericsParameterClass(idClass, IId.class);
    return findOfMethod(idClass, parameterType);
  }

  protected Method findOfByStringMethod(Class<? extends IId<?>> idClass) {
    return findOfMethod(idClass, String.class);
  }

  protected Method findOfMethod(Class<? extends IId<?>> idClass, Class<?> parameterType) {
    try {
      Method m = idClass.getMethod("of", parameterType);
      Assertions.assertTrue(Modifier.isStatic(m.getModifiers()), "method 'of({})' is expected to be static [method={}]", parameterType.getName(), m);
      return m;
    }
    catch (@SuppressWarnings("squid:S1166") NoSuchMethodException e) {
      throw new PlatformException("Cannot find a static method 'of({})' on id class {}.", parameterType.getName(), idClass.getName());
    }
  }
}
