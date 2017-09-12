/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * This transaction member is used to collect all transactional notifications issued during a transaction.
 * <p>
 * On successful commit, the notifications will be added to the {@link ServerRunContext#getClientNotificationCollector()
 * )} to be included in the request's response (piggyback). That allows immediate processing of the transactional
 * notifications on client side.
 * </p>
 */
public class ClientNotificationTransactionMember extends AbstractTransactionMember {

  public static final String TRANSACTION_MEMBER_ID = "clientNotification.transactionMemberId";

  private final List<ClientNotificationMessage> m_notifications = new ArrayList<>();
  private final ClientNotificationCoalescer m_coalescer;
  private final ClientNotificationRegistry m_notificationRegistry;

  public ClientNotificationTransactionMember(ClientNotificationRegistry reg) {
    super(TRANSACTION_MEMBER_ID);
    m_notificationRegistry = reg;
    m_coalescer = BEANS.get(ClientNotificationCoalescer.class);
  }

  @Override
  public void rollback() {
    m_notifications.clear();
  }

  @Override
  public void cancel() {
    m_notifications.clear();
  }

  @Override
  public boolean needsCommit() {
    return !m_notifications.isEmpty();
  }

  public void addNotification(ClientNotificationMessage message) {
    m_notifications.add(message);
  }

  @Override
  public void commitPhase2() {
    List<ClientNotificationMessage> coalescedNotifications = m_coalescer.coalesce(new ArrayList<>(m_notifications));
    publish(coalescedNotifications);
    m_notifications.clear();
  }

  private void publish(List<ClientNotificationMessage> notifications) {
    if (tryPiggyBack(notifications)) {
      m_notificationRegistry.publish(notifications, Assertions.assertNotNull(IClientNodeId.CURRENT.get(), "Missing 'client node id' on current calling context"));
    }
    else {
      m_notificationRegistry.publish(notifications);
    }

  }

  /**
   * Register client notifications of the current transaction in the collector to be included in the service response
   * (piggyback).
   */
  private boolean tryPiggyBack(List<ClientNotificationMessage> notifications) {
    ClientNotificationCollector collector = ClientNotificationCollector.CURRENT.get();
    if (IClientNodeId.CURRENT.get() != null && collector != null) {
      return collector.addAll(notifications);
    }
    return false;
  }

}
