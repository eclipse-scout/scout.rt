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
package org.eclipse.scout.rt.client.servicetunnel.http.tests;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.client.servicetunnel.http.HttpServiceTunnel;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;

/**
 *
 */
public final class TestHttpServiceTunnel {

  private TestHttpServiceTunnel() {
  }

  public static void main(String[] args) {
    ClientSession session = new ClientSession();
    session.startSession(null);
    //
    IPingService service = ServiceTunnelUtility.createProxy(IPingService.class, session.getServiceTunnel());
    String s = service.ping("abc");
    System.out.println("ping: " + s);
    System.exit(0);
  }

  private static class ClientSession extends AbstractClientSession {
    public ClientSession() {
      super(true);
    }

    @Override
    protected void execLoadSession() throws ProcessingException {
      HttpServiceTunnel tunnel = new HttpServiceTunnel(this, "https://tools.bsiag.com/bsicrm/process", "999.999.999");
      tunnel.setClientNotificationPollInterval(-1);
      setServiceTunnel(tunnel);
    }
  }
}
