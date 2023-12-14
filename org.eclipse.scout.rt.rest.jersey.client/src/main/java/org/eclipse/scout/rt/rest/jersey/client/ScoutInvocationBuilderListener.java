/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client;

import jakarta.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.client.spi.InvocationBuilderListener;

/**
 * {@link InvocationBuilderListener} implementation disabling the default Jersey user agent header.
 */
public class ScoutInvocationBuilderListener implements InvocationBuilderListener {

  @Override
  public void onNewBuilder(InvocationBuilderContext context) {
    // disable default user agent header by setting null as agent, see org.glassfish.jersey.client.JerseyInvocation.Builder.header(String, Object)
    // sets internal flag ClientRequest.ignoreUserAgent, which prevents Jersey from sending a user agent header like "Jersey/2.31"
    context.header(HttpHeaders.USER_AGENT, null);
  }
}
