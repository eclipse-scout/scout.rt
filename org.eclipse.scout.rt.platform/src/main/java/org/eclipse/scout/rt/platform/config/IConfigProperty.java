/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Represents a scout application configuration property. This property may be stored in any source.
 * <p>
 * A config property has a constant value never changing during runtime.
 */
@Bean
public interface IConfigProperty<DATA_TYPE> {

  /**
   * Gets the configured value. If nothing is configured the default value ({@link #getDefaultValue()}) is returned.
   * <p>
   * A config property has a constant value, never changing during runtime.
   * <p>
   * Callers may therefore cache or reference the returned value.
   *
   * @return The actual value of this property. May be <code>null</code>.
   */
  DATA_TYPE getValue();

  /**
   * Gets the default value of this property.
   *
   * @return The default value. May be <code>null</code>.
   */
  DATA_TYPE getDefaultValue();

  /**
   * Gets the key of this property
   *
   * @return The key of this property. May not be <code>null</code>.
   */
  String getKey();
}
