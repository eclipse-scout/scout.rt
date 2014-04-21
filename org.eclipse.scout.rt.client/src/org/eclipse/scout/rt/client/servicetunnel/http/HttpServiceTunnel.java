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
package org.eclipse.scout.rt.client.servicetunnel.http;

import org.eclipse.scout.commons.UriUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * @deprecated: use {@link ClientHttpServiceTunnel} instead
 *              Will be removed in the 5.0 Release
 */
@Deprecated
@SuppressWarnings("deprecation")
public class HttpServiceTunnel extends ClientHttpServiceTunnel implements org.eclipse.scout.rt.client.servicetunnel.IServiceTunnel {

  public HttpServiceTunnel(IClientSession session, String url) throws ProcessingException {
    super(session, UriUtility.toUrl(url));
  }

  public HttpServiceTunnel(IClientSession session, String url, String version) throws ProcessingException {
    super(session, UriUtility.toUrl(url), version);
  }
}
