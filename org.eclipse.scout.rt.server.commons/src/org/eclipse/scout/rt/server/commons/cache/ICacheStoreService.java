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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.service.IService;

/**
 * Service for caching server side data.
 */
public interface ICacheStoreService extends IService {

  /**
   * stores the value in dependency of the requesting client and given the key, using default expiration time from
   * config.ini
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param key
   *          Key for the value
   * @param value
   *          value to be stored
   */
  void setClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Object value);

  /**
   * stores the value in dependency of the requesting client and given the key
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param key
   *          value identifying key
   * @param value
   *          value to be stored
   * @param expiration
   *          time in seconds when the data expires
   */
  void setClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Object value, Integer expiration);

  /**
   * returns the value to the given key in dependency of the requesting client
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param key
   *          value identifying key
   * @return the value
   */
  public Object getClientAttribute(HttpServletRequest req, HttpServletResponse res, String key);

  /**
   * returns the value to the given key in dependency of the requesting client, refreshes the expiration time
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param key
   *          value identifying key
   * @param expiration
   *          time in seconds when the data expires
   * @return the value
   */
  Object getClientAttributeAndTouch(HttpServletRequest req, HttpServletResponse res, String key, Integer expiration);

  /**
   * returns the value to the given key in dependency of the requesting client, refreshes the expiration time with
   * default expiration time
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param key
   *          value identifying key
   * @return the value
   */
  Object getClientAttributeAndTouch(HttpServletRequest req, HttpServletResponse res, String key);

  /**
   * Updates the expiration time
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param key
   *          value identifying key
   * @param expiration
   *          time in seconds when the data expires
   */
  void touchClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Integer expiration);

  /**
   * Updates the expiration time with the default expiration time
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param key
   *          value identifying key
   */
  void touchClientAttribute(HttpServletRequest req, HttpServletResponse res, String key);

  /**
   * deletes the data from the cache
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @param key
   *          value identifying key
   */
  void removeClientAttribute(HttpServletRequest req, HttpServletResponse res, String key);

  /**
   * stores the value in dependency of the key and independent of the requesting client or node, using default
   * expiration time from config.ini
   * 
   * @param key
   *          Key for the value
   * @param value
   *          value to be stored
   */
  void setGlobalAttribute(String key, Object value);

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
  void setGlobalAttribute(String key, Object value, Integer expiration);

  /**
   * returns the value for the given key, independent from the requesting client or node
   * 
   * @param key
   *          value identifying key
   * @return the value
   */
  Object getGlobalAttribute(String key);

  /**
   * returns the value for the given key, independent from the requesting client or node. Updates the expiration time
   * 
   * @param key
   *          value identifying key
   * @param expiration
   *          time in seconds when the data expires
   * @return the value
   */
  Object getGlobalAttributeAndTouch(String key, Integer expiration);

  /**
   * returns the value for the given key, independent from the requesting client or node. Updates the expiration time
   * with default expiration time
   * 
   * @param key
   *          value identifying key
   * @return the value
   */
  Object getGlobalAttributeAndTouch(String key);

  /**
   * updates the expiration time
   * 
   * @param key
   *          value identifying key
   * @param expiration
   *          time in seconds when the data expires
   * @return
   */
  void touchGlobalAttribute(String key, Integer expiration);

  /**
   * updates the expiration time by default expiration time
   * 
   * @param key
   */
  void touchGlobalAttribute(String key);

  /**
   * removes the data from the cache
   * 
   * @param key
   *          value identifying key
   */
  void removeGlobalAttribute(String key);

}
