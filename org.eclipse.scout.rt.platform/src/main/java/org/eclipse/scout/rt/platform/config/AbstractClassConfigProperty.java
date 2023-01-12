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

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * A config property which represents a Java class.
 */
public abstract class AbstractClassConfigProperty<TYPE> extends AbstractConfigProperty<Class<? extends TYPE>, String> {

  @SuppressWarnings("unchecked")
  @Override
  protected Class<? extends TYPE> parse(final String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }

    try {
      return (Class<? extends TYPE>) Class.forName(value);
    }
    catch (final ClassNotFoundException | ClassCastException e) {
      throw new PlatformException("Failed to load class specified by config-property '{}' [config-value={}]", getKey(), value, e);
    }
  }
}
