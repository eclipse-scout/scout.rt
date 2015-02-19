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
package org.eclipse.scout.rt.client.services.common.clientnotification;

import java.util.EventObject;

import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

public class ClientNotificationConsumerEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private final IClientNotification m_notification;
  private boolean m_consumed;

  public ClientNotificationConsumerEvent(IClientNotificationConsumerService service, IClientNotification n) {
    super(service);
    m_notification = n;
  }

  @Override
  public final IClientNotificationConsumerService getSource() {
    return (IClientNotificationConsumerService) super.getSource();
  }

  public final IClientNotification getClientNotification() {
    return m_notification;
  }

  public final boolean isConsumed() {
    return m_consumed;
  }

  /**
   * When set to true, the event dispatching of this event is not ended, the
   * other listeners will also see this event. use {@link #isConsumed()} to
   * check for this property
   */
  public final void consume() {
    m_consumed = true;
  }

}
