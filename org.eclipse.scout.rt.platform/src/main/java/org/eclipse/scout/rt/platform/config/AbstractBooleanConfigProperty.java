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

public abstract class AbstractBooleanConfigProperty extends AbstractConfigProperty<Boolean, String> {

  @Override
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean parse(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }

    // if specified: it must be true or false
    if ("true".equalsIgnoreCase(value)) {
      return Boolean.TRUE;
    }
    if ("false".equalsIgnoreCase(value)) {
      return Boolean.FALSE;
    }
    throw new PlatformException("Invalid boolean value '" + value + "' for property '" + getKey() + "'.");
  }
}
