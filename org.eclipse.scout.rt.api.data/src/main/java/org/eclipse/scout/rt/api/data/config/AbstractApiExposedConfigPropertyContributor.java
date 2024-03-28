/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.config;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.eclipse.scout.rt.api.data.config.mapping.IConfigPropertyToDoFunction;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.IConfigProperty;

/**
 * Base implementation for config property contributors.
 */
public abstract class AbstractApiExposedConfigPropertyContributor implements IApiExposedConfigPropertyContributor {

  /**
   * @return The {@link Collection} of {@link IConfigProperty} instances that should be exposed. Must not be
   * {@code null}.
   */
  protected abstract Collection<? extends IConfigProperty<?>> getExposedProperties();

  @Override
  public void contribute(Set<ConfigPropertyDo> configPropertyDos) {
    getExposedProperties().stream()
        .map(this::convert)
        .filter(Objects::nonNull)
        .forEach(configPropertyDos::add);
  }

  /**
   * Converts an {@link IConfigProperty} to a DO.
   *
   * @param property
   *     The property to convert.
   * @return The corresponding {@link ConfigPropertyDo} or {@code null} if it cannot be converted.
   */
  public static ConfigPropertyDo create(IConfigProperty<?> property) {
    return BEANS.get(ToDoFunctionHelper.class).toDo(property, IConfigPropertyToDoFunction.class);
  }

  protected ConfigPropertyDo convert(IConfigProperty<?> configProperty) {
    return create(configProperty);
  }
}
