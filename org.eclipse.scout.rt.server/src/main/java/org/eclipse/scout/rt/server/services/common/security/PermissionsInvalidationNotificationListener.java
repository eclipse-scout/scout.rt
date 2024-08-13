/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.security;

import static org.eclipse.scout.rt.api.uinotification.UiNotificationPutOptions.noTransaction;

import java.util.function.Consumer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.scout.rt.api.data.security.PermissionUpdateMessageDo;
import org.eclipse.scout.rt.api.uinotification.UiNotificationRegistry;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.security.IAccessControlService;

/**
 * Listens for permission cache invalidation and notifies the UI to update its cache.
 */
@ApplicationScoped
@CreateImmediately
@SuppressWarnings("unchecked")
public class PermissionsInvalidationNotificationListener implements Consumer<IAccessControlService> {

  @PostConstruct
  protected void init() {
    BEANS.optional(IAccessControlService.class).ifPresent(svc -> svc.addInvalidationListener(this));
  }

  @PreDestroy
  protected void destroy() {
    BEANS.optional(IAccessControlService.class).ifPresent(svc -> svc.removeInvalidationListener(this));
  }

  @Override
  public void accept(IAccessControlService source) {
    ITransaction transaction = ITransaction.CURRENT.get();
    if (transaction == null) {
      return;
    }
    transaction.registerMemberIfAbsentAndNotCancelled(PermissionsUiNotificationTransactionMember.TRANSACTION_MEMBER_ID, this::createTransactionMember);
  }

  protected PermissionsUiNotificationTransactionMember createTransactionMember(String memberId) {
    return new PermissionsUiNotificationTransactionMember();
  }

  /**
   * Informs all UIs to update its permissions cache because something changed in the backend cache. Use a
   * {@link ITransactionMember} as during a transaction multiple invalidates could happen and the notification should
   * only be created once.
   */
  public static class PermissionsUiNotificationTransactionMember extends AbstractTransactionMember {

    public static final String TOPIC = "permissionsUpdate";
    public static final String TRANSACTION_MEMBER_ID = "permissionsUiNotification.transactionMemberId";

    public PermissionsUiNotificationTransactionMember() {
      super(TRANSACTION_MEMBER_ID);
    }

    @Override
    public boolean needsCommit() {
      return true;
    }

    @Override
    public void commitPhase2() {
      UiNotificationRegistry uiNotificationRegistry = BEANS.get(UiNotificationRegistry.class);
      long reloadDelayWindow = uiNotificationRegistry.computeNotificationHandlerDelayWindow(TOPIC);
      PermissionUpdateMessageDo updateDo = BEANS.get(PermissionUpdateMessageDo.class).withReloadDelayWindow(reloadDelayWindow);
      uiNotificationRegistry.put(TOPIC, updateDo, noTransaction());
    }
  }
}
