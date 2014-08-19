/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.shared.services.common.processing.IServerProcessingCancelService;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 *
 */
public interface IServiceTunnelRequest extends Serializable {

  /**
   * @return the request sequence for this session
   *         <p>
   *         The sequence can be used to find and manipulate transactions of the same session. Such a scenario is used
   *         when cancelling "old" lookup requests using {@link IServerProcessingCancelService#cancel(long)}
   */
  long getRequestSequence();

  /**
   * @return the service class name of the service to call.
   */
  String getServiceInterfaceClassName();

  String getVersion();

  String getOperation();

  Class[] getParameterTypes();

  Object[] getArgs();

  Locale getLocale();

  /**
   * The subject under which the request is done
   * <p>
   * Client only method. The member is transient and will be null on the server.
   */
  Subject getClientSubject();

  /**
   * The web (ajax) session under which the request is done
   */
  String getVirtualSessionId();

  /**
   * Represents the user interface on client side.<br/>
   * To parse an identifier use {@link UserAgent#createByIdentifier(String)}
   */
  String getUserAgent();

  Set<String> getConsumedNotifications();

}
