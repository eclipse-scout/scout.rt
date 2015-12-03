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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.service.IService;

/**
 * Service for caching server side data dependent on request/response.
 * <p>
 * Cached values may expire. Expired Values are removed from the cache.
 * </p>
 * 
 * @since 4.0.0
 */
public interface IHttpSessionCacheService extends IService {

  /**
   * Stores the value in dependency of the requesting client and given the key using default expiration time.
   * 
   * @param key
   *          Key for the value
   * @param value
   *          value to be stored
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   */
  void put(String key, Object value, HttpServletRequest req, HttpServletResponse res);

  /**
   * Stores the value in dependency of the requesting client and given the key using given expiration time.
   * 
   * @param key
   *          Key for the value
   * @param value
   *          value to be stored
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param expiration
   *          time [ms] until the value expires
   */
  void put(String key, Object value, HttpServletRequest req, HttpServletResponse res, Long expiration);

  /**
   * Returns the value to the given key in dependency of the requesting client, refreshes the expiration time with
   * default expiration time
   * 
   * @param key
   *          value identifying key
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @return the value
   */
  Object getAndTouch(String key, HttpServletRequest req, HttpServletResponse res);

  /**
   * Returns the value to the given key in dependency of the requesting client and refreshes the expiration time with
   * the given expiration time.
   * 
   * @param key
   *          value identifying key
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param expiration
   *          time until the value expires in ms
   * @return the value
   */
  Object getAndTouch(String key, HttpServletRequest req, HttpServletResponse res, Long expiration);

  /**
   * returns the value to the given key in dependency of the requesting client
   * 
   * @param key
   *          value identifying key
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @return the value
   */
  Object get(String key, HttpServletRequest req, HttpServletResponse res);

  /**
   * Updates the expiration time with the default expiration time
   * 
   * @param key
   *          value identifying key
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   */
  void touch(String key, HttpServletRequest req, HttpServletResponse res);

  /**
   * Updates the expiration time with the given expiration time
   * 
   * @param key
   *          value identifying key
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param expiration
   *          time [ms] until the value expires
   */
  void touch(String key, HttpServletRequest req, HttpServletResponse res, Long expiration);

  /**
   * Deletes the data from the cache
   * 
   * @param key
   *          value identifying key
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   */
  void remove(String key, HttpServletRequest req, HttpServletResponse res);

  /**
   * @return the default expiration time of the cached values in ms
   */
  Long getExpiration();

}
