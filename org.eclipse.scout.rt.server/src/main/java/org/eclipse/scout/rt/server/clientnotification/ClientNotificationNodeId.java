/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.clientnotification;

import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;

/**
 * Every client node opens a connection to a server node to access notifications. This connection is addressed by the
 * {@link ClientNotificationNodeId}. The node id is provided on every {@link ServiceTunnelRequest#getClientNotificationNodeId()}.
 */
public class ClientNotificationNodeId {

  /**
   * The notification node id which is currently associated with the current thread.
   */
  public static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

  private ClientNotificationNodeId() {
  }

  /**
   * Returns the notification node id which is currently associated with the current thread.
   */
  public static String get() {
    return CURRENT.get();
  }

}
