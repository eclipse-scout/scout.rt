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
package org.eclipse.scout.rt.client.servicetunnel;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

/**
 * @Deprecated: use {@link org.eclipse.scout.rt.client.servicetunnel.http.ClientHttpServiceTunnel} instead
 *              To be removed with the M-Release
 */
@Deprecated
public abstract class AbstractServiceTunnel extends org.eclipse.scout.rt.client.servicetunnel.http.ClientHttpServiceTunnel {

  /**
   * @param session
   * @param version
   */
  public AbstractServiceTunnel(IClientSession session, String version) {
    super(session, null, version);
  }

  protected IClientSession getClientSession() {
    return super.getSession();
  }

  @Override
  protected abstract ServiceTunnelResponse tunnelOnline(ServiceTunnelRequest call);

  @Override
  protected ServiceTunnelResponse tunnel(ServiceTunnelRequest call) {
    return tunnelOnline(call);
  }
}
