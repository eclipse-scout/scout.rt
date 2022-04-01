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
package org.eclipse.scout.rt.rest.jersey.client;

import javax.ws.rs.core.HttpHeaders;

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
