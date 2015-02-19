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
package org.eclipse.scout.jaxws.internal;

import org.eclipse.scout.jaxws.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerSession;

public class SessionContextBean {
  private Class<? extends IServerSessionFactory> m_factoryClass;
  private IServerSession m_session;

  public SessionContextBean(Class<? extends IServerSessionFactory> factoryClass, IServerSession session) {
    m_factoryClass = factoryClass;
    m_session = session;
  }

  public Class<? extends IServerSessionFactory> getFactoryClass() {
    return m_factoryClass;
  }

  public void setFactoryClass(Class<? extends IServerSessionFactory> factoryClass) {
    m_factoryClass = factoryClass;
  }

  public IServerSession getSession() {
    return m_session;
  }

  public void setSession(IServerSession session) {
    m_session = session;
  }
}
