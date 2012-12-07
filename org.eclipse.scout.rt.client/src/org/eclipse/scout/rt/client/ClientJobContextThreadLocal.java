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
 * This class represents the client job context for the current thread.
 * 
 * @since 3.8.2
 */
public final class ClientJobContextThreadLocal {
  private static final ThreadLocal<ClientJobContext> THREAD_LOCAL = new ThreadLocal<ClientJobContext>();

  private ClientJobContextThreadLocal() {
  }

  public static ClientJobContext get() {
    return THREAD_LOCAL.get();
  }

  public static void set(ClientJobContext properties) {
    if (properties == null) {
      return;
    }
    THREAD_LOCAL.set(properties);
  }
}
