/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.clientnotification;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;

/**
 * Represents the <code>client node ID</code> currently associated with the current thread.
 * <p>
 * Every client node (that is every UI server node) has its unique 'node ID' which is included with every
 * 'client-server' request, and is mainly used to publish client notifications.
 * <p>
 * Typically, this node ID is set by {@link ServiceTunnelServlet} for the processing of a service request.
 */
public interface IClientNodeId {

  /**
   * The client node id which is currently associated with the current thread.
   */
  ThreadLocal<NodeId> CURRENT = new ThreadLocal<>();
}
