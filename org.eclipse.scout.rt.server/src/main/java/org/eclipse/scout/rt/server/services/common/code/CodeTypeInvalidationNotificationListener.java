/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.code;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.rt.api.uinotification.UiNotificationPutOptions.noTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.api.data.code.CodeTypeUpdateMessageDo;
import org.eclipse.scout.rt.api.uinotification.UiNotificationRegistry;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICacheInvalidationListener;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.services.common.code.ApiExposedCodeTypeDoProvider;
import org.eclipse.scout.rt.shared.services.common.code.CodeService;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeCacheKey;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeCacheUtility;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * Listens for CodeType cache invalidation and notifies the UI with the new CodeType after invalidation.
 */
@ApplicationScoped
@CreateImmediately
public class CodeTypeInvalidationNotificationListener implements ICacheInvalidationListener<CodeTypeCacheKey, ICodeType<?, ?>> {

  @PostConstruct
  protected void init() {
    BEANS.get(CodeService.class).addInvalidationListener(this);
  }

  @PreDestroy
  protected void destroy() {
    BEANS.get(CodeService.class).removeInvalidationListener(this);
  }

  @Override
  public void invalidated(ICacheEntryFilter<CodeTypeCacheKey, ICodeType<?, ?>> filter, boolean propagate) {
    if (filter == null) {
      return; // nothing has been invalidated
    }
    ITransaction transaction = ITransaction.CURRENT.get();
    if (transaction == null) {
      return;
    }
    CodeTypeUiNotificationTransactionMember member = transaction.registerMemberIfAbsentAndNotCancelled(CodeTypeUiNotificationTransactionMember.TRANSACTION_MEMBER_ID, this::createTransactionMember);
    if (member == null) {
      return; // transaction has been cancelled
    }
    member.registerCodeTypeInvalidate(filter);
  }

  protected CodeTypeUiNotificationTransactionMember createTransactionMember(String memberId) {
    return new CodeTypeUiNotificationTransactionMember();
  }

  /**
   * Collects the exposed CodeTypes which have been invalidated in the transaction and sends them to all UIs for update.
   * Use a {@link ITransactionMember} as during a transaction multiple invalidates could happen and the new instances
   * should only be created once.
   */
  public static class CodeTypeUiNotificationTransactionMember extends AbstractTransactionMember {

    public static final String TRANSACTION_MEMBER_ID = "codeTypeUiNotification.transactionMemberId";
    public static final String TOPIC = "codeTypeUpdate";
    private final List<ICacheEntryFilter<CodeTypeCacheKey, ICodeType<?, ?>>> m_invalidations = new ArrayList<>();

    public CodeTypeUiNotificationTransactionMember() {
      super(TRANSACTION_MEMBER_ID);
    }

    public void registerCodeTypeInvalidate(ICacheEntryFilter<CodeTypeCacheKey, ICodeType<?, ?>> filter) {
      m_invalidations.add(filter);
    }

    @Override
    public void rollback() {
      m_invalidations.clear();
    }

    @Override
    public void cancel() {
      m_invalidations.clear();
    }

    @Override
    public boolean needsCommit() {
      return !m_invalidations.isEmpty();
    }

    @Override
    public void commitPhase2() {
      // use new transaction to load the new CodeTypes and notify the browser because the current transaction is no longer available
      ServerRunContexts.copyCurrent()
          .withTransactionScope(TransactionScope.REQUIRES_NEW)
          .run(this::notifyInvalidatedCodeTypes);
      m_invalidations.clear();
    }

    public void notifyInvalidatedCodeTypes() {
      CodeTypeCacheUtility codeTypeCacheUtility = BEANS.get(CodeTypeCacheUtility.class);
      List<CodeTypeDo> updatedAndExposedCodeTypes = BEANS.get(ApiExposedCodeTypeDoProvider.class)
          .getExposedCodeTypes().stream()
          .filter(codeType -> needsNotify(codeTypeCacheUtility.createCacheKey(codeType.getClass()), codeType))
          .map(ICodeType::toDo)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
      notifyExposedCodeTypeUpdate(updatedAndExposedCodeTypes);
    }

    public boolean needsNotify(CodeTypeCacheKey cacheKey, ICodeType<?, ?> codeType) {
      return m_invalidations.stream().anyMatch(filter -> filter.accept(cacheKey, codeType));
    }

    public void notifyExposedCodeTypeUpdate(List<CodeTypeDo> codeTypes) {
      if (codeTypes.isEmpty()) {
        return;
      }

      UiNotificationRegistry uiNotificationRegistry = BEANS.get(UiNotificationRegistry.class);
      String currentUserId = getUserId();

      // send message to current user directly including the new code types
      // this is required so that the caches are update to date very soon after the user applied the change
      CodeTypeUpdateMessageDo messageWithNewCodeTypes = BEANS.get(CodeTypeUpdateMessageDo.class).withCodeTypes(codeTypes);
      uiNotificationRegistry.put(TOPIC, currentUserId, messageWithNewCodeTypes, noTransaction().withPublishOverCluster(false));

      // send updated codeType ids to other users so that they can reload them
      Set<String> idsToUpdate = codeTypes.stream().map(CodeTypeDo::getId).collect(toSet());
      long reloadDelayWindow = uiNotificationRegistry.computeNotificationHandlerDelayWindow(TOPIC);
      CodeTypeUpdateMessageDo messageWithCodeTypeIdsToUpdate = BEANS.get(CodeTypeUpdateMessageDo.class)
          .withCodeTypeIds(idsToUpdate)
          .withReloadDelayWindow(reloadDelayWindow);
      uiNotificationRegistry.putExcept(TOPIC, singleton(currentUserId), messageWithCodeTypeIdsToUpdate, noTransaction().withPublishOverCluster(false));
    }

    protected String getUserId() {
      return BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
    }
  }
}
