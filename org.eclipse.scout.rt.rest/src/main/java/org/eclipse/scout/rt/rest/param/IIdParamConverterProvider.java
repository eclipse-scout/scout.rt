/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IIdCodecFlag;
import org.eclipse.scout.rt.platform.ApplicationScoped;
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

  @Inject
  protected Provider<IdCodecFlags> m_idCodecFlagsProvider;

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (IId.class.isAssignableFrom(rawType)) {
      Class<? extends IId> idClass = rawType.asSubclass(IId.class);
      if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
        return (ParamConverter<T>) m_idParamConverters.computeIfAbsent(idClass, clazz -> createQualifiedIIdParamConverter(m_idCodecFlagsProvider));
      }
      else {
        return (ParamConverter<T>) m_idParamConverters.computeIfAbsent(idClass, clazz -> createUnqualifiedIIdParamConverter(clazz, m_idCodecFlagsProvider));
      }
    }
    return null;
  }

  protected UnqualifiedIIdParamConverter createUnqualifiedIIdParamConverter(Class<? extends IId> idClass, Provider<IdCodecFlags> idCodecFlagsProvider) {
    return new UnqualifiedIIdParamConverter(idClass, idCodecFlagsProvider);
  }

  protected QualifiedIIdParamConverter createQualifiedIIdParamConverter(Provider<IdCodecFlags> idCodecFlagsProvider) {
    return new QualifiedIIdParamConverter(idCodecFlagsProvider);
  }

  /**
   * {@link ParamConverter} handling {@link IId} in unqualified form.
   */
  public static class UnqualifiedIIdParamConverter extends AbstractIdCodecParamConverter<IId> {

    protected final Class<? extends IId> m_idClass;

    public UnqualifiedIIdParamConverter(Class<? extends IId> idClass, Provider<IdCodecFlags> idCodecFlagsProvider) {
      super(idCodecFlagsProvider);
      m_idClass = idClass;
    }

    @Override
    public IId fromString(String value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on IIdParamConverterProvider
      }
      return idCodec().fromUnqualified(m_idClass, value, idCodecFlags());
    }

    @Override
    public String toString(IId value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on IIdParamConverterProvider
      }
      return idCodec().toUnqualified(value, idCodecFlags());
    }
  }

  /**
   * {@link ParamConverter} handling {@link IId} in qualified form.
   */
  public static class QualifiedIIdParamConverter extends AbstractIdCodecParamConverter<IId> {

    public QualifiedIIdParamConverter(Provider<IdCodecFlags> idCodecFlagsProvider) {
      super(idCodecFlagsProvider);
    }

    @Override
    public IId fromString(String value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on IIdParamConverterProvider
      }
      return idCodec().fromQualified(value, idCodecFlags());
    }

    @Override
    public String toString(IId value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on IIdParamConverterProvider
      }
      return idCodec().toQualified(value, idCodecFlags());
    }
  }

  /**
   * Abstract {@link ParamConverter} that provides an {@link IdCodec} and information about the {@link IIdCodecFlag}s of
   * the context.
   */
  public abstract static class AbstractIdCodecParamConverter<T> implements ParamConverter<T> {

    protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);
    protected final Provider<IdCodecFlags> m_idCodecFlagsProvider;

    public AbstractIdCodecParamConverter(Provider<IdCodecFlags> idCodecFlagsProvider) {
      m_idCodecFlagsProvider = idCodecFlagsProvider;
    }

    protected IdCodec idCodec() {
      return m_idCodec.get();
    }

    protected Set<IIdCodecFlag> idCodecFlags() {
      return Optional.ofNullable(m_idCodecFlagsProvider)
          .map(Provider::get)
          .map(IdCodecFlags::get)
          .orElse(Collections.emptySet());
    }
  }

  /**
   * Holder to inject a {@link Set} of {@link IIdCodecFlag}s into a local {@link Provider} at runtime.
   */
  public static class IdCodecFlags extends AtomicReference<Set<IIdCodecFlag>> {
    private static final long serialVersionUID = 1L;
  }
}
