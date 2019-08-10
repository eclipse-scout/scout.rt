/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.client.proxy.api;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public class ApiSignature {

  private final Set<Class<?>> m_pendingClasses = new HashSet<>();
  private final Set<Class<?>> m_processedClasses = new HashSet<>();
  private final List<Predicate<Class<?>>> m_classFilters = new ArrayList<>();
  private final List<Predicate<Method>> m_methodFilters = new ArrayList<>();

  public ApiSignature classFilter(Predicate<Class<?>> filter) {
    m_classFilters.add(filter);
    return this;
  }

  public ApiSignature methodFilter(Predicate<Method> filter) {
    m_methodFilters.add(filter);
    return this;
  }

  public ApiSignature collect(Class<?>... classes) {
    if (classes != null) {
      Stream.of(classes)
          .filter(Objects::nonNull)
          .filter(c -> !m_processedClasses.contains(c))
          .filter(c -> accept(m_classFilters, c))
          .forEach(m_pendingClasses::add);
    }
    return this;
  }

  private <T> boolean accept(List<Predicate<T>> filters, T obj) {
    for (Predicate<T> filter : filters) {
      if (!filter.test(obj)) {
        return false;
      }
    }
    return true;
  }

  public ApiSignatureDo build() {
    List<ClassSignatureDo> signatures = new ArrayList<>();

    while (!m_pendingClasses.isEmpty()) {
      for (Class<?> c : resetPendingClasses()) {
        signatures.add(signature(c));
        m_processedClasses.add(c);
      }
    }

    Collections.sort(signatures, Comparator.comparing(ClassSignatureDo::getName));
    return BEANS.get(ApiSignatureDo.class).withClasses(signatures);
  }

  private Set<Class<?>> resetPendingClasses() {
    HashSet<Class<?>> classes = new HashSet<>(m_pendingClasses);
    m_pendingClasses.clear();
    classes.removeAll(m_processedClasses);
    return classes;
  }

  protected ClassSignatureDo signature(Class<?> c) {
    collect(c.getSuperclass());
    collect(c.getInterfaces());

    return BEANS.get(ClassSignatureDo.class)
        .withName(c.getName())
        .withModifiers(c.getModifiers())
        .withSuperclass(c.getSuperclass() == null ? null : c.getSuperclass().getName())
        .withInterfaces(Stream.of(c.getInterfaces())
            .map(Class::getName)
            .collect(Collectors.toList()))
        .withMethods(Stream.of(c.getDeclaredMethods())
            .sorted(Comparator.comparing(Method::toString))
            .filter(m -> accept(m_methodFilters, m))
            .map(this::signature)
            .collect(Collectors.toList()));
  }

  protected MethodSignatureDo signature(Method m) {
    collect(m.getReturnType());
    collect(m.getParameterTypes());

    return BEANS.get(MethodSignatureDo.class)
        .withName(m.getName())
        .withModifiers(m.getModifiers())
        .withReturnType(m.getGenericReturnType().getTypeName())
        .withParameterTypes(Stream.of(m.getParameters())
            .map(Parameter::getParameterizedType)
            .map(Type::getTypeName)
            .collect(Collectors.toList()));
  }
}
