/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.commons;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxy.SpecificSessionCookieStore;

/**
 * {@link SpecificSessionCookieStore} implementation for UI server.
 */
@Replace
public class ClientSessionSpecificCookieStore extends SpecificSessionCookieStore {

  @Override
  protected <R> R callWithSession(Callable<R> callable) {
    // setup client run context with specific session
    return ClientRunContexts.copyCurrent(true)
        .withSession((IClientSession) getSession(), false)
        .call(callable);
  }
}
