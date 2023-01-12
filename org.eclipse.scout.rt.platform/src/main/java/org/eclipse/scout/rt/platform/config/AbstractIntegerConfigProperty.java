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

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Config property for {@link Integer} values.
 */
public abstract class AbstractIntegerConfigProperty extends AbstractConfigProperty<Integer, String> {

  @Override
  protected Integer parse(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }
    return Integer.parseInt(value);
  }
}
