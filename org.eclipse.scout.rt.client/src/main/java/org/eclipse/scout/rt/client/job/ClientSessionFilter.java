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
package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * Filter which accepts Futures only if belonging to the given session.
 *
 * @since 5.1
 */
public class ClientSessionFilter implements IFilter<IFuture<?>> {

  private final IClientSession m_session;

  public ClientSessionFilter(final IClientSession session) {
    m_session = Assertions.assertNotNull(session, "Session must not be null");
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return m_session == ((ClientJobInput) future.getJobInput()).getSession();
  }
}
