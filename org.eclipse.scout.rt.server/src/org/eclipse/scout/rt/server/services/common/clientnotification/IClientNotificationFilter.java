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
package org.eclipse.scout.rt.server.services.common.clientnotification;

import java.io.Serializable;

import org.eclipse.scout.rt.server.services.common.clientnotification.internal.ClientNotificationQueue;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 * Filter to add a {@link org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification} to the
 * {@link ClientNotificationQueue} <br>
 * The filter is only used on the server side, it is not sent to the client
 * <p>
 * Make sure to implement the {@link #hashCode()} and {@link #equals(Object)} method of the filter to allow for
 * {@link IClientNotification#coalesce(IClientNotification)}
 */
public interface IClientNotificationFilter extends Serializable {

  /**
   * @return true if notification is still valid and has not timed out <br>
   *         The notification is discarded when deactivated. In case the
   *         notification started already it is normally finished.
   *         <p>
   *         This method is used on the server side
   */
  boolean isActive();

  /**
   * @return true if notification is mutlicast <br>
   *         A multicast notification can be sent to multiple consumers
   *         (clients) until it becomes inactive <br>
   *         A singlecast notification is discarded once it has been fetched by
   *         a client <br>
   *         Most notifications are singlecast.
   *         <p>
   *         This method is used on the server side
   */
  boolean isMulticast();

  /**
   * @return true if the notification should be delivered to the session's
   *         frontend <br>
   *         If this is a multicast notification, it remains queued until it is
   *         deativated <br>
   *         If this is a singlecast notification it is removed after it
   *         returned true for at least one session
   *         <p>
   *         This method is used on the server side, so you can get the server session with
   *         ThreadContext.get(IServerSession.class)
   */
  boolean accept();
}
