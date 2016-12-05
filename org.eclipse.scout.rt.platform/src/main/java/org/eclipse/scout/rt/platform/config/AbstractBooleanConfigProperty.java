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
