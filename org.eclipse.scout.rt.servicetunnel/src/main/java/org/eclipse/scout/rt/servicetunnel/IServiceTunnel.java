/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.servicetunnel;

import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Interface of a tunnel used to invoke a service through HTTP.
 *
 * @author awe (refactoring)
 */
public interface IServiceTunnel {
  String PROP_TARGET_URL = "org.eclipse.scout.rt.servicetunnel.targetUrl";
  String PROP_SHARED_SECRET = "org.eclipse.scout.rt.servicetunnel.sharedSecret";
  String PROP_MULTI_SESSION_COOKIE_STORE_ENABLED = "org.eclipse.scout.rt.servicetunnel.multiSessionCookieStoreEnabled";

  /**
   * Invoke a remote service through a service tunnel<br>
   * The argument array may contain IHolder values which are updated as OUT
   * parameters when the backend call has completed flags are custom flags not
   * used by the framework itself
   */
  Object invokeService(Class<?> serviceInterfaceClass, Method operation, Object[] args) throws ProcessingException;

  URL getServerURL();//TODO [nosgi] imo remove and use servicetunnel.targetUrl (legacy) support server.url

  void setServerURL(URL url);//TODO [nosgi] imo remove and use

}
