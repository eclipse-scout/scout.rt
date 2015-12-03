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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Represents a scout application configuration property. This property may be stored in any source.
 * <p>
 * A config property has a constant value never changing during runtime.
 */
@Bean
@ApplicationScoped
public interface IConfigProperty<DATA_TYPE> {

  /**
   * Gets the key of this property
   *
   * @return The key of this property. May not be <code>null</code>
   */
  String getKey();

  /**
   * Gets the configured value. If nothing is configured the default value is returned.
   * <p>
   * A config property has a constant value, never changing during runtime.
   * </p>
   * Callers may therefore cache or reference the returned value.
   *
   * @return The actual value of this property. <code>null</code>, if and only if both the value and the default value
   *         are null.
   * @throws PlatformException
   */
  DATA_TYPE getValue();

  /**
   * @return the error that occured when creating this property value. This error is thrown in {@link #getValue()}
   */
  PlatformException getError();

}
