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
package org.eclipse.scout.rt.client.servicetunnel.http.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.servicetunnel.AbstractServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;

/**
 * @deprecated: the new class is
 *              {@link org.eclipse.scout.rt.client.servicetunnel.http.internal.InternalClientHttpServiceTunnel}.
 *              If you have used this internal class, consider
 *              {@link org.eclipse.scout.rt.client.servicetunnel.http.ClientHttpServiceTunnel} instead.
 *              Will be removed in the 5.0 Release
 */
@Deprecated
@SuppressWarnings("deprecation")
public class InternalHttpServiceTunnel extends AbstractServiceTunnel {

  public InternalHttpServiceTunnel(IClientSession session, String url) throws ProcessingException {
    this(session, url, null);
  }

  public InternalHttpServiceTunnel(IClientSession session, String url, String version) throws ProcessingException {
    super(session, version);
    try {
      if (url != null) {
        setServerURL(new URL(url));
      }
    }
    catch (MalformedURLException e) {
      throw new ProcessingException(url, e);
    }
  }

  @Override
  protected IServiceTunnelResponse tunnelOnline(IServiceTunnelRequest call) {
    return super.tunnelOffline(call);
  }
}
