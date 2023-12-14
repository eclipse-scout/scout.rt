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
import java.lang.reflect.Type;
import java.util.Locale;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Provides a JAX-RS parameter converter for {@link Locale}, using {@link Locale#forLanguageTag(String)}.
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
public class LocaleParamConverterProvider implements ParamConverterProvider {

  private final LocaleParamConverter m_localeParamConverter = new LocaleParamConverter();

  @SuppressWarnings("unchecked")
  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (Locale.class == rawType) {
      return (ParamConverter<T>) m_localeParamConverter;
    }
    return null;
  }

  /**
   * {@link ParamConverter} handling {@link Locale}.
   */
  public static class LocaleParamConverter implements ParamConverter<Locale> {

    @Override
    public Locale fromString(String value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on LocaleParamConverterProvider
      }
      return Locale.forLanguageTag(value);
    }

    @Override
    public String toString(Locale value) {
      if (value == null) {
        return null; // always use null as default value, see JavaDoc on LocaleParamConverterProvider
      }
      return value.toLanguageTag();
    }
  }
}
