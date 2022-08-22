/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.clientnotification;

import java.util.List;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceWithoutAuthorization;

/**
 * Service to consume notifications. Accessible from the client.
 */
@ApplicationScoped
@TunnelToServer
@RemoteServiceWithoutAuthorization
public interface IClientNotificationService {

  /**
   * Register a session with the corresponding user and client node
   *
   * @param nodeId
   *          unique id of the client node
   */
  void registerNode(NodeId nodeId);

  /**
   * Unregister a node with all its registered session and users.
   *
   * @param nodeId
   *          unique id of the client node
   */
  void unregisterNode(NodeId nodeId);

  /**
   * Receive new notifications relevant for the given node
   *
   * @param nodeId
   *          unique id of the client node
   * @return list of new notification messages never <code>null</code>
   */
  List<ClientNotificationMessage> getNotifications(NodeId nodeId);
}
