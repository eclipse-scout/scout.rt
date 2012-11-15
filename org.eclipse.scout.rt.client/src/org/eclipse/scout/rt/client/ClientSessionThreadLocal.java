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

/**
 * This class represents the client session for the current thread.
 */
public final class ClientSessionThreadLocal {
  private static final ThreadLocal<IClientSession> THREAD_LOCAL = new ThreadLocal<IClientSession>();

  private ClientSessionThreadLocal() {
  }

  public static IClientSession get() {
    return THREAD_LOCAL.get();
  }

  public static void set(IClientSession l) {
    THREAD_LOCAL.set(l);
  }

}
