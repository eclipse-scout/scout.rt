/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.uinotification;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;

public class UiNotificationTransactionMember extends AbstractTransactionMember {

  public static final String TRANSACTION_MEMBER_ID = "uiNotification.transactionMemberId";

  private final List<Pair<UiNotificationMessageDo, UiNotificationPutOptions>> m_messages = new ArrayList<>();
  private final UiNotificationRegistry m_registry;

  public UiNotificationTransactionMember(UiNotificationRegistry registry) {
    super(TRANSACTION_MEMBER_ID);
    m_registry = registry;
  }

  @Override
  public void rollback() {
    m_messages.clear();
  }

  @Override
  public void cancel() {
    m_messages.clear();
  }

  @Override
  public boolean needsCommit() {
    return !m_messages.isEmpty();
  }

  public void addNotification(UiNotificationMessageDo message, UiNotificationPutOptions options) {
    m_messages.add(ImmutablePair.of(message, options));
  }

  @Override
  public void commitPhase2() {
    for (Pair<UiNotificationMessageDo, UiNotificationPutOptions> message : m_messages) {
      m_registry.putInternal(message.getLeft(), message.getRight());
    }
    m_messages.clear();
  }
}
