/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.LazyValue;

/**
 * Provides a JAX-RS parameter converter for {@link IId}-based types.
 * <p>
 * <b>Handling Default Values:</b>
 * <p>
 * {@link ParamConverter#fromString(String)} defines that methods should throw {@link IllegalArgumentException} when the
 * parameter value cannot be parsed or is absent. The framework would then check any &#64;{@link DefaultValue}
 * definitions for an appropriate default value.
 * <p>
 * However, the {@link ParamConverter}s provided by this class simply return <code>null</code> instead of throwing an
 * exception. This might break {@link DefaultValue} definitions (but apparently they would not be considered anyway for
 * some reason). In Scout applications, such definitions are never used, <code>null</code> is always the default.
 */
@ApplicationScoped
public class IIdParamConverterProvider implements ParamConverterProvider {

  private final ConcurrentMap<Class<? extends IId>, ParamConverter<? extends IId>> m_idParamConverters = new ConcurrentHashMap<>();

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (IId.class.isAssignableFrom(rawType)) {
      Class<? extends IId> idClass = rawType.asSubclass(IId.class);
      if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
        return (ParamConverter<T>) m_idParamConverters.computeIfAbsent(idClass, k -> BEANS.get(QualifiedIIdParamConverter.class));
      }
      else {
        return (ParamConverter<T>) m_idParamConverters.computeIfAbsent(idClass, UnqualifiedIIdParamConverter::new);
      }
    }
    return null;
  }

  /**
   * {@link ParamConverter} handling {@link IId} in unqualified form.
   */
  public static class UnqualifiedIIdParamConverter implements ParamConverter<IId> {

    protected final LazyValue<IdCodec> m_codec = new LazyValue<>(IdCodec.class);
    protected final Class<? extends IId> m_idClass;

    public UnqualifiedIIdParamConverter(Class<? extends IId> idClass) {
      m_idClass = idClass;
    }

    @Override
    public IId fromString(String value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on IIdParamConverterProvider
      }
      return m_codec.get().fromUnqualified(m_idClass, value);
    }

    @Override
    public String toString(IId value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on IIdParamConverterProvider
      }
      return m_codec.get().toUnqualified(value);
    }
  }

  /**
   * {@link ParamConverter} handling {@link IId} in qualified form.
   */
  @ApplicationScoped // use same instance for all qualified IDs
  public static class QualifiedIIdParamConverter implements ParamConverter<IId> {

    protected final LazyValue<IdCodec> m_codec = new LazyValue<>(IdCodec.class);

    @Override
    public IId fromString(String value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on IIdParamConverterProvider
      }
      return m_codec.get().fromQualified(value);
    }

    @Override
    public String toString(IId value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on IIdParamConverterProvider
      }
      return m_codec.get().toQualified(value);
    }
  }
}
