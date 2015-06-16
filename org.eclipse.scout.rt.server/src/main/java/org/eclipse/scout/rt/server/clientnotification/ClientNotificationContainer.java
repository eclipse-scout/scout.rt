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
package org.eclipse.scout.rt.server.clientnotification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * The container of all transactional notifications during a server request. Is kept on the {@link ServerRunContext}.
 *
 * @see ClientNotificationTransactionMember
 */
public class ClientNotificationContainer {

  /**
   * The {@link Locale} which is currently associated with the current thread.
   */
  public static final ThreadLocal<ClientNotificationContainer> CURRENT = new ThreadLocal<>();

  private final Set<ClientNotificationMessage> m_notifications = new HashSet<>();

  public ClientNotificationContainer() {
  }

  /**
   */
  public static ClientNotificationContainer get() {
    return CURRENT.get();
  }

  public boolean add(ClientNotificationMessage message) {
    return m_notifications.add(message);
  }

  public void addAll(Collection<ClientNotificationMessage> messages) {
    m_notifications.addAll(messages);
  }

  public Set<ClientNotificationMessage> getNotifications() {
    return new HashSet<ClientNotificationMessage>(m_notifications);
  }

}
