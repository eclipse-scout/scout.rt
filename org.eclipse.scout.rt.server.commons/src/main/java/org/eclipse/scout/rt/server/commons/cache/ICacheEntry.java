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
package org.eclipse.scout.rt.server.commons.cache;

/**
 * A cached value that may expire. Used to be shared between server nodes.
 * 
 * @param T
 *          type of the cached value
 * @since 4.0.0
 */
public interface ICacheEntry<T> {

  /**
   * cached value
   */
  T getValue();

  /**
   * @return <code>true</code> if not expired
   */
  boolean isActive();

  /**
   * @param expiration
   *          time in ms until the value expires
   */
  void setExpiration(Long expiration);

  /**
   * Resets the the time created to the current time
   */
  void touch();

}
