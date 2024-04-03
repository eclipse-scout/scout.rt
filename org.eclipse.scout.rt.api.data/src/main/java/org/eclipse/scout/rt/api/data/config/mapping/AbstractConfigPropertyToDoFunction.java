/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.config.mapping;

import org.eclipse.scout.rt.api.data.config.ConfigPropertyDo;
import org.eclipse.scout.rt.dataobject.mapping.AbstractToDoFunction;
import org.eclipse.scout.rt.platform.config.IConfigProperty;

/**
 * Base implementation of a function that converts {@link IConfigProperty} to {@link ConfigPropertyDo}. Only
 * {@link IConfigProperty#getKey()} and {@link IConfigProperty#getValue()} is exported.
 */
public abstract class AbstractConfigPropertyToDoFunction<EXPLICIT_SOURCE extends IConfigProperty<?>, EXPLICIT_TARGET extends ConfigPropertyDo>
    extends AbstractToDoFunction<EXPLICIT_SOURCE, EXPLICIT_TARGET, IConfigProperty<?>, ConfigPropertyDo>
    implements IConfigPropertyToDoFunction {

  @Override
  public void apply(EXPLICIT_SOURCE property, EXPLICIT_TARGET propertyDo) {
    propertyDo
        .withKey(property.getKey())
        .withValue(getPropertyValue(property));
  }

  /**
   * Gets the value of an {@link IConfigProperty}. Overwrite this method to e.g. apply data type conversions for special
   * properties.
   *
   * @param property
   *     The {@link IConfigProperty} whose value should be returned. Is never {@code null}.
   * @return The value of the property given that should be exposed to the api.
   */
  protected Object getPropertyValue(IConfigProperty<?> property) {
    // no conversion by default. Jackson can handle the required data types just fine for now.
    return property.getValue();
  }
}
