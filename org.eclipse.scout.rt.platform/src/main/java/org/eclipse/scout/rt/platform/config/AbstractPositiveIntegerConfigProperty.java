/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractPositiveIntegerConfigProperty extends AbstractConfigProperty<Integer, String> {

  private static final Pattern INT_PAT = Pattern.compile("^\\d{1,9}$");

  @Override
  protected Integer parse(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }

    // if specified: it must be a valid integer
    if (!INT_PAT.matcher(value).matches()) {
      throw new PlatformException("Invalid integer value '" + value + "' for property '" + getKey() + "'.");
    }

    return Integer.parseInt(value);
  }
}
