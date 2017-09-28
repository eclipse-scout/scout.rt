/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.scout.rt.platform.Bean;

/**
 * This is the content handler that defines the format in which data is exchanged through a service tunnel.
 * <p>
 * Most implementations also implement {@link IServiceTunnelContentObserver}
 */
@Bean
public interface IServiceTunnelContentHandler {

  void initialize();

  String getContentType();

  void writeRequest(OutputStream out, ServiceTunnelRequest msg) throws IOException;

  ServiceTunnelRequest readRequest(InputStream in) throws IOException, ClassNotFoundException;

  void writeResponse(OutputStream out, ServiceTunnelResponse msg) throws IOException;

  ServiceTunnelResponse readResponse(InputStream in) throws IOException, ClassNotFoundException;
}
