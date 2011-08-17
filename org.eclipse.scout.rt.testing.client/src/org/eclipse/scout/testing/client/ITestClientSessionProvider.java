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
package org.eclipse.scout.testing.client;

import org.eclipse.scout.rt.client.IClientSession;

/**
 * Provides {@link IClientSession} for Scout tests and allows to create more than one instance of the same
 * session type (e.g. for different users).
 */
public interface ITestClientSessionProvider {

  /**
   * Returns an already existing client session of the given type and for the given user or creates a new one.
   * 
   * @param <T>
   *          type of the resulting client session.
   * @param clazz
   *          requested client session.
   * @param user
   *          name of the user the session belongs to or is created for.
   * @param forceNewSession
   *          controls whether a new session is created even if there already exists one of the given type and for the
   *          given user. <code>true</code> for creating a new session, <code>false</code> for reusing an already
   *          existing session.
   * @return
   */
  <T extends IClientSession> T getOrCreateClientSession(Class<T> clazz, String user, boolean forceNewSession);
}
