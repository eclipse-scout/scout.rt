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

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractPositiveLongConfigProperty extends AbstractConfigProperty<Long, String> {

  private static final Pattern LONG_PAT = Pattern.compile("^\\d{1,18}$");

  @Override
  protected Long parse(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }

    // if specified: it must be a valid long
    if (!LONG_PAT.matcher(value).matches()) {
      throw new PlatformException("Invalid long value '" + value + "' for property '" + getKey() + "'.");
    }

    return Long.parseLong(value);
  }
}
