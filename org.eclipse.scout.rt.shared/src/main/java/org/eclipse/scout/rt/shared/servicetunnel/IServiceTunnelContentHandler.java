/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
