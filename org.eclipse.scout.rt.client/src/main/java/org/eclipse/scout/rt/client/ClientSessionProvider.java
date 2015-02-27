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
package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.shared.ISession;

/**
 *
 */
public final class ClientSessionProvider {
  private ClientSessionProvider() {
  }

  public static IClientSession get() {
    ISession session = ISession.CURRENT.get();
    return (IClientSession) (session instanceof IClientSession ? session : null);
  }

  @SuppressWarnings("unchecked")
  public static final <T extends IClientSession> T get(Class<T> type) {
    IClientSession s = get();
    if (s != null && type.isAssignableFrom(s.getClass())) {
      return (T) s;
    }
    return null;
  }
}
