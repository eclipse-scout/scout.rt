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
package org.eclipse.scout.rt.shared.services.common.clientnotification;

import java.util.UUID;

public abstract class AbstractClientNotification implements IClientNotification {
  private static final long serialVersionUID = 1L;
  private static long s_idCounter = 0;
  private int m_node;
  private final String m_notificationId = UUID.randomUUID().toString();

  @Override
  public String getId() {
    return m_notificationId;
  }

  @Override
  public int getOriginNode() {
    return m_node;
  }

  @Override
  public void setOriginNode(int node) {
    m_node = node;
  }

  /**
   * backward legacy, this method is removed in the next release
   * 
   * @deprecated use IClientNotificationConsumerListener on the ClientNotificationConsumerService
   */
  @Deprecated
  public final void run() throws Throwable {
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
