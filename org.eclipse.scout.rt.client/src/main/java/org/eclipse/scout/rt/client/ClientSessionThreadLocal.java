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
package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.shared.ISession;

/**
 * This class represents the client session for the current thread.
 * TODO [dwi/imo]: Remove on behalf of nOSGi.
 */
public final class ClientSessionThreadLocal {
  private ClientSessionThreadLocal() {
  }

  public static IClientSession get() {
    ISession session = ISession.CURRENT.get();
    return (IClientSession) (session instanceof IClientSession ? session : null);
  }

  public static void set(IClientSession session) {
    ISession.CURRENT.set(session);
  }
}
