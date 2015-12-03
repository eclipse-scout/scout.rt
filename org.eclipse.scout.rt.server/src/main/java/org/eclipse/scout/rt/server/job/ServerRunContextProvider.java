/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.job;

import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.job.IRunContextProvider;

/**
 * Provides a {@link ServerRunContext}.
 *
 * @since 5.1
 */
public class ServerRunContextProvider implements IRunContextProvider {

  @Override
  public ServerRunContext copyCurrent() {
    return ServerRunContexts.copyCurrent();
  }

  @Override
  public ServerRunContext empty() {
    return ServerRunContexts.empty();
  }
}
