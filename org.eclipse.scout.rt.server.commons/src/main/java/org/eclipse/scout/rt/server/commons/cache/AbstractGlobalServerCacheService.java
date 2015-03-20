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

import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public abstract class AbstractGlobalServerCacheService extends AbstractHttpSessionCacheService implements IGlobalServerCacheService {

  @Override
  public Object get(String key, HttpServletRequest req, HttpServletResponse res) {
    return get(getKeyWithId(key, req, res));
  }

  @Override
  public void put(String key, Object value, HttpServletRequest req, HttpServletResponse res, Long expiration) {
    put(getKeyWithId(key, req, res), value, expiration);
  }

  @Override
  public void remove(String key, HttpServletRequest req, HttpServletResponse res) {
    remove(getKeyWithId(key, req, res));
  }

  @Override
  public Object getAndTouch(String key, HttpServletRequest req, HttpServletResponse res, Long expiration) {
    return getAndTouch(getKeyWithId(key, req, res), expiration);
  }

  @Override
  public void touch(String key, HttpServletRequest req, HttpServletResponse res, Long expiration) {
    touch(getKeyWithId(key, req, res), expiration);
  }

  @Override
  public void put(String key, Object value) {
    put(key, value, getExpiration());
  }

  @Override
  public Object getAndTouch(String key) {
    return getAndTouch(key, getExpiration());
  }

  @Override
  public void touch(String key) {
    touch(key, getExpiration());
  }

  public abstract void touch(String key, Long expiration);

  protected String getKeyWithId(String key, HttpServletRequest req, HttpServletResponse res) {
    String sessionId = SERVICES.getService(IClientIdentificationService.class).getClientId(req, res);
    return sessionId + "_" + key;
  }

}
