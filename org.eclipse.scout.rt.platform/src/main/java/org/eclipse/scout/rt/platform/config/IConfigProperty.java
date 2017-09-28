/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Represents a scout application configuration property. This property may be stored in any source but typically it is
 * stored in a properties file (see {@link ConfigUtility} and {@link PropertiesHelper}).
 */
@ApplicationScoped
public interface IConfigProperty<DATA_TYPE> {

  /**
   * Gets the key of this property
   *
   * @return The key of this property. May not be <code>null</code>.
   */
  String getKey();

  /**
   * Gets the configured value of this property in the given namespace. If no value is available for the given namespace
   * a default value is returned.
   *
   * @param namespace
   *          The namespace of the property or <code>null</code>.
   * @return The current value of this property. <code>null</code>, if and only if both the value and the default value
   *         are null.
   */
  DATA_TYPE getValue(String namespace);

  /**
   * Gets the configured value. If nothing is configured a default value is returned.
   *
   * @return The current value of this property. <code>null</code>, if and only if both the value and the default value
   *         are null.
   * @throws PlatformException
   *           if there was an error reading the value.
   */
  DATA_TYPE getValue();

  /**
   * Adds a new {@link IConfigChangedListener} to this property.
   *
   * @param listener
   *          The new listener. May not be <code>null</code>.
   */
  void addListener(IConfigChangedListener listener);

  /**
   * Removes an {@link IConfigChangedListener} from this property.
   *
   * @param listener
   *          The listener to remove. May not be <code>null</code>.
   */
  void removeListener(IConfigChangedListener listener);

  /**
   * Invalidates the value of this property.<br>
   * If the property has already been initialized and the environment changed, the current value may be invalidated. The
   * next time the property value is read it will be calculated again.<br>
   * Calling this method may be useful if e.g. a system property changed since the first use and should be reflected in
   * the value of this property.
   */
  void invalidate();

  /**
   * Sets a new value to this config property.
   *
   * @param newValue
   *          The new value.
   */
  void setValue(DATA_TYPE newValue);

  /**
   * Sets a new value to this config property.
   *
   * @param newValue
   *          The new value.
   * @param namespace
   *          The namespace of the new value.
   */
  void setValue(DATA_TYPE newValue, String namespace);
}
