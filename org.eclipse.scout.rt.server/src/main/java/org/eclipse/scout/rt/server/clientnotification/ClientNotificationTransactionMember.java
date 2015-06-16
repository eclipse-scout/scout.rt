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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

/**
 * This transaction member is used to collect all notifications during a transaction. On successful commit the
 * notifications will be added to the {@link ServerRunContext#txNotificationContainer()}. Further all notifications in
 * the txNotificationContainer will be added to the {@link ServiceTunnelResponse} and piggy backed to the client.
 * The reason to do so is to provide an immediate client side processing of transactional notifications.
 */
public class ClientNotificationTransactionMember extends AbstractTransactionMember {

  public static final String TRANSACTION_MEMBER_ID = "clientNotification.transactionMemberId";

  private final List<ClientNotificationMessage> m_notifications = new ArrayList<>();

  public ClientNotificationTransactionMember() {
    super(TRANSACTION_MEMBER_ID);
  }

  @Override
  public boolean needsCommit() {
    return m_notifications.size() > 0;
  }

  public void addNotification(ClientNotificationMessage message) {
    m_notifications.add(message);
  }

  @Override
  public void commitPhase2() {
    // coalease
    List<ClientNotificationMessage> coalescedNotifications = BEANS.get(ClientNotificationCoalescer.class).coalesce(m_notifications);
    m_notifications.clear();
    // piggy back
    ClientNotificationContainer.get().addAll(coalescedNotifications);
    // notify others
    BEANS.get(ClientNotificationRegistry.class).put(coalescedNotifications);
  }

}
