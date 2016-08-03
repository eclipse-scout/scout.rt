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
   *          the config class
   * @return The value of the given {@link IConfigProperty}.
   * @throws PlatformException
   *           if the property is invalid
   */
  public static <DATA_TYPE> DATA_TYPE getPropertyValue(Class<? extends IConfigProperty<DATA_TYPE>> clazz) {
    return BEANS.get(clazz).getValue();
  }
}
