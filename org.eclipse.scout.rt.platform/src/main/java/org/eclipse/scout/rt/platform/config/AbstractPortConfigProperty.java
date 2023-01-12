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

public abstract class AbstractPortConfigProperty extends AbstractPositiveIntegerConfigProperty {

  @Override
  protected Integer parse(String value) {
    Integer i = super.parse(value);
    if (i == null) {
      return i;
    }
    int port = i.intValue();
    if (port >= 1 && port <= 65535) {
      return i;
    }
    throw new PlatformException("Invalid value for port config with key '" + getKey() + "': '" + value + "'. Valid values are integers in range [1, 65535].");
  }
}
