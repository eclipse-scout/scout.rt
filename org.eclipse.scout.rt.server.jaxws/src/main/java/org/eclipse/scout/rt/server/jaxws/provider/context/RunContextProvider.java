/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.context;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

/**
 * Factory for {@link RunContext} objects.
 *
 * @since 5.1
 */
@ApplicationScoped
public class RunContextProvider {

  /**
   * Provides a {@link ServerRunContext} for the given {@link Subject}. This implementation uses a session cache to not
   * always create a new {@link IServerSession}.
   */
  public RunContext provide(final Subject subject) throws ProcessingException {
    final ServerRunContext serverRunContext = ServerRunContexts.empty();
    serverRunContext.subject(subject);
    serverRunContext.session(BEANS.get(ServerSessionProviderWithCache.class).provide(serverRunContext.copy()));
    return serverRunContext;
  }
}
