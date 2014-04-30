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

import org.eclipse.scout.service.IService;

/**
 * Service for caching server side data independent of the session
 * 
 * @since 4.0.0
 */
public interface IGlobalServerCacheService extends IService {

  /**
   * stores the value in dependency of the key and independent of the requesting client or node, using default
   * expiration time from config.ini
   * 
   * @param key
   *          Key for the value
   * @param value
   *          value to be stored
   */
  void put(String key, Object value);

  /**
   * stores the value in dependency of the key and independent of the requesting client or node
   * 
   * @param key
   *          value identifying key
   * @param value
   *          value to be stored
   * @param expiration
   *          time in seconds when the data expires
   */
  void put(String key, Object value, Long expiration);

  /**
   * returns the value for the given key, independent from the requesting client or node. Updates the expiration time
   * with default expiration time
   * 
   * @param key
   *          value identifying key
   * @return the value
   */
  Object getAndTouch(String key);

  /**
   * returns the value for the given key, independent from the requesting client or node. Updates the expiration time
   * 
   * @param key
   *          value identifying key
   * @param expiration
   *          time in seconds when the data expires
   * @return the value
   */
  Object getAndTouch(String key, Long expiration);

  /**
   * returns the value for the given key, independent from the requesting client or node
   * 
   * @param key
   *          value identifying key
   * @return the value
   */
  Object get(String key);

  /**
   * updates the expiration time by default expiration time
   * 
   * @param key
   */
  void touch(String key);

  /**
   * removes the data from the cache
   * 
   * @param key
   *          value identifying key
   */
  void remove(String key);

  /**
   * @return the default expiration time of the cached values in ms
   */
  Long getExpiration();

}
