/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.security;

import static org.eclipse.scout.rt.api.uinotification.UiNotificationPutOptions.noTransaction;

import java.util.Objects;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.scout.rt.api.data.security.PermissionUpdateMessageDo;
import org.eclipse.scout.rt.api.uinotification.UiNotificationRegistry;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICacheInvalidationListener;
import org.eclipse.scout.rt.platform.cache.KeyCacheEntryFilter;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;

/**
 * Listens for permission cache invalidation and notifies the UI to update its cache.
 */
@ApplicationScoped
@CreateImmediately
@SuppressWarnings("unchecked")
public class PermissionsInvalidationNotificationListener implements ICacheInvalidationListener<User, IPermissionCollection> {

  @PostConstruct
  protected void init() {
    BEANS.optional(IAccessControlService.class).ifPresent(svc -> svc.addInvalidationListener(this));
  }

  @PreDestroy
  protected void destroy() {
    BEANS.optional(IAccessControlService.class).ifPresent(svc -> svc.removeInvalidationListener(this));
  }

  @Override
  public void invalidated(ICacheEntryFilter<User, IPermissionCollection> filter, boolean propagate) {
    if (filter == null) {
      return; // nothing has been invalidated
    }
    ITransaction transaction = ITransaction.CURRENT.get();
    if (transaction == null) {
      return;
    }
    if (!propagate) {
      // Do not create ui notifications if propagate flag is false as ui notifications are already propagated to other cluster nodes by the UiNotificationRegistry.
      return;
    }
    transaction.registerMemberIfAbsentAndNotCancelled(PermissionsUiNotificationTransactionMember.TRANSACTION_MEMBER_ID, id -> createTransactionMember(filter));
  }

  protected PermissionsUiNotificationTransactionMember createTransactionMember(ICacheEntryFilter<User, IPermissionCollection> filter) {
    return new PermissionsUiNotificationTransactionMember(filter);
  }

  /**
   * Informs all UIs to update its permissions cache because something changed in the backend cache. Use a
   * {@link ITransactionMember} as during a transaction multiple invalidates could happen and the notification should
   * only be created once.
   */
  public static class PermissionsUiNotificationTransactionMember extends AbstractTransactionMember {

    public static final String TOPIC = "permissionsUpdate";
    public static final String TRANSACTION_MEMBER_ID = "permissionsUiNotification.transactionMemberId";
    private final ICacheEntryFilter<User, IPermissionCollection> m_filter;

    public PermissionsUiNotificationTransactionMember(ICacheEntryFilter<User, IPermissionCollection> filter) {
      super(TRANSACTION_MEMBER_ID);
      m_filter = filter;
    }

    @Override
    public boolean needsCommit() {
      return true;
    }

    protected ICacheEntryFilter<User, IPermissionCollection> getFilter() {
      return m_filter;
    }

    @Override
    public void commitPhase2() {
      UiNotificationRegistry uiNotificationRegistry = BEANS.get(UiNotificationRegistry.class);
      long reloadDelayWindow = uiNotificationRegistry.computeNotificationHandlerDelayWindow(TOPIC);
      PermissionUpdateMessageDo updateDo = BEANS.get(PermissionUpdateMessageDo.class).withReloadDelayWindow(reloadDelayWindow);
      sendUiNotification(uiNotificationRegistry, updateDo);
    }

    protected void sendUiNotification(UiNotificationRegistry uiNotificationRegistry, PermissionUpdateMessageDo updateDo) {
      if (m_filter instanceof KeyCacheEntryFilter) {
        // only Permissions of specific users are invalidated: only inform the affected clients
        Set<User> cacheKeys = ((KeyCacheEntryFilter<User, IPermissionCollection>) m_filter).getKeys();

        cacheKeys.stream()
            .map(User::getUserId)
            .filter(Objects::nonNull)
            .forEach(userId -> uiNotificationRegistry.put(TOPIC, userId, updateDo, noTransaction()));
      }
      else {
        // update for all clients
        uiNotificationRegistry.put(TOPIC, updateDo, noTransaction());
      }
    }
  }
}
