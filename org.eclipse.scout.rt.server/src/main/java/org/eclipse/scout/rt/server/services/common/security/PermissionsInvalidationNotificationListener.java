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
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;
import org.eclipse.scout.rt.server.context.ServerRunContexts;

/**
 * Listens for permission cache invalidation and notifies the UI to update its cache.
 */
@ApplicationScoped
@CreateImmediately
@SuppressWarnings("unchecked")
public class PermissionsInvalidationNotificationListener implements ICacheInvalidationListener<Object, IPermissionCollection> {

  @PostConstruct
  protected void init() {
    BEANS.optional(IAccessControlService.class).ifPresent(svc -> svc.addInvalidationListener(this));
  }

  @PreDestroy
  protected void destroy() {
    BEANS.optional(IAccessControlService.class).ifPresent(svc -> svc.removeInvalidationListener(this));
  }

  @Override
  public void invalidated(ICacheEntryFilter<Object, IPermissionCollection> filter, boolean propagate) {
    if (filter == null) {
      return; // nothing has been invalidated
    }
    ITransaction transaction = ITransaction.CURRENT.get();
    if (transaction == null) {
      return;
    }
    transaction.registerMemberIfAbsentAndNotCancelled(PermissionsUiNotificationTransactionMember.TRANSACTION_MEMBER_ID, id -> createTransactionMember(filter));
  }

  protected PermissionsUiNotificationTransactionMember createTransactionMember(ICacheEntryFilter<Object, IPermissionCollection> filter) {
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
    private final ICacheEntryFilter<Object, IPermissionCollection> m_filter;

    public PermissionsUiNotificationTransactionMember(ICacheEntryFilter<Object, IPermissionCollection> filter) {
      super(TRANSACTION_MEMBER_ID);
      m_filter = filter;
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
      if (m_filter instanceof KeyCacheEntryFilter) {
        IAccessControlService accessControlService = BEANS.get(IAccessControlService.class);
        // only Permissions of specific users are invalidated: only inform the affected clients
        Set<Object> cacheKeys = ((KeyCacheEntryFilter<Object, IPermissionCollection>) m_filter).getKeys();

        // create new run context to ensure new transaction is available in case getUserIdForCacheKey requires one to map the cacheKey to the userId
        ServerRunContexts.copyCurrent()
            .run(() -> cacheKeys.stream()
                .map(accessControlService::getUserIdForCacheKey)
                .filter(Objects::nonNull)
                .forEach(userId -> uiNotificationRegistry.put(TOPIC, userId, updateDo, noTransaction().withPublishOverCluster(false))));
      }
      else {
        // update for all clients
        uiNotificationRegistry.put(TOPIC, updateDo, noTransaction().withPublishOverCluster(false));
      }
    }
  }
}
