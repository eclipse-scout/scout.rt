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
package org.eclipse.scout.rt.server.clientnotification;

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
  ThreadLocal<String> CURRENT = new ThreadLocal<>();
}
