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

/**
 * A config property which represents a Java class.
 */
public abstract class AbstractClassConfigProperty<TYPE> extends AbstractConfigProperty<Class<? extends TYPE>> {

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
