/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
 * A cached value that may expire
 * 
 * @since 4.0.0
 */
public interface ICacheElement {

  /**
   * @return the actual value
   */
  Object getValue();

  /**
   * @return <code>true</code> if not expired
   */
  boolean isActive();

  /**
   * @param expiration
   *          time in ms until the value expires
   */
  void setExpiration(Integer expiration);

  /**
   * Resets the the time created to the current time
   */
  void resetCreationTime();
}
