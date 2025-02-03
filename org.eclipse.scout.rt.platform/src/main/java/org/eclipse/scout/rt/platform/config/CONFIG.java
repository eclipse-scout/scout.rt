/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Class to simplify access to scout project configuration
 *
 * @see IConfigProperty
 */
public final class CONFIG {

  private CONFIG() {
  }

  /**
   * Gets the configured value of the given {@link IConfigProperty}. If no value is configured the default of the
   * property is returned ({@link IConfigProperty#getValue()}).
   *
   * @param clazz
   *     the config class
   * @return The value of the given {@link IConfigProperty}.
   * @throws PlatformException
   *     if the property is invalid
   */
  public static <DATA_TYPE> DATA_TYPE getPropertyValue(Class<? extends IConfigProperty<DATA_TYPE>> clazz) {
    return getPropertyValue(clazz, null);
  }

  /**
   * Gets the configured value of the given {@link IConfigProperty}. If no value is configured the default of the
   * property is returned ({@link IConfigProperty#getValue()}).
   *
   * @param clazz
   *     the config class
   * @param namespace
   *     The namespace of the property value to get.
   * @return The value of the given {@link IConfigProperty}.
   * @throws PlatformException
   *     if the property is invalid
   */
  public static <DATA_TYPE> DATA_TYPE getPropertyValue(Class<? extends IConfigProperty<DATA_TYPE>> clazz, String namespace) {
    return BEANS.get(clazz).getValue(namespace);
  }
}
