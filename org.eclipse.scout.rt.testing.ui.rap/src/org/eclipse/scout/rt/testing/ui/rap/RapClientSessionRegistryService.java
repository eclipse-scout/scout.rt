/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.testing.ui.rap;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.service.AbstractService;

/**
 * There is one session per ui app
 */
public class RapClientSessionRegistryService extends AbstractService implements IClientSessionRegistryService {

  private IClientSession m_session;

  public RapClientSessionRegistryService(IClientSession session) {
    m_session = session;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IClientSession> T getClientSession(Class<T> clazz) {
    if (clazz.isAssignableFrom(m_session.getClass())) {
      return (T) m_session;
    }
    throw new UnsupportedOperationException("the current rap session was created using " + m_session.getClass() + "; incompatible to " + clazz);
  }

  @Override
  public <T extends IClientSession> T newClientSession(Class<T> clazz, Subject subject, String webSessionId) {
    throw new UnsupportedOperationException("a rap session cannot create a new client session in a junit test");
  }
}
