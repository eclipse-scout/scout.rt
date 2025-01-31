/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.cancellation;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST request registry providing cancellation support.
 * <p>
 * <b>Note:</b> the registry manages requests, not requests by user. It is therefore important that concurrently
 * executed requests use unique requestIds.
 */
@ApplicationScoped
public class RestRequestCancellationRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(RestRequestCancellationRegistry.class);

  private final ConcurrentMap<String, RequestCancellationInfo> m_requestCancellationInfos = new ConcurrentHashMap<>();

  protected ConcurrentMap<String, RequestCancellationInfo> getRequestCancellationInfos() {
    return m_requestCancellationInfos;
  }

  public IRegistrationHandle register(String requestId, Object userId, RunMonitor runMonitor) {
    assertNotNull(requestId, "requestId is required");
    assertNotNull(runMonitor, "runMonitor is required");

    final ConcurrentMap<String, RequestCancellationInfo> cancellationInfos = getRequestCancellationInfos();

    if (cancellationInfos.putIfAbsent(requestId, new RequestCancellationInfo(runMonitor, userId)) != null) {
      // request id is already in use
      LOG.warn("Duplicate request id. Ignoring this request: [requestId:{}]", requestId);
      return null;
    }

    return () -> cancellationInfos.remove(requestId);
  }

  public boolean cancel(String requestId, Object userId) {
    return cancel(requestId, userId, this::handleCancellationInfoNotExists);
  }

  /**
   * @param requestId
   *     id of the request
   * @param userId
   *     id of the user that submits the cancellation request
   * @param requestNotExistsHandler
   *     handler that is executed in the case the request could not be found
   * @return {@code true} if the cancellation request was successful, {@code false} otherwise
   */
  public boolean cancel(String requestId, Object userId, BiFunction<String, Object, Boolean> requestNotExistsHandler) {
    if (requestId == null) {
      return false;
    }
    RequestCancellationInfo cancellationInfo = getRequestCancellationInfos().get(requestId);
    if (cancellationInfo == null) {
      return requestNotExistsHandler.apply(requestId, userId);
    }

    if (!checkAccess(userId, cancellationInfo)) {
      LOG.info("Cannot cancel requests of other users [requestId={}, executingUserId={}, requestingUserId={}]", requestId, cancellationInfo.getUserId(), userId);
      throw new AccessForbiddenException();
    }

    return cancellationInfo.getRunMonitor().cancel(true);
  }

  protected boolean checkAccess(Object requestingUserId, RequestCancellationInfo cancellationInfo) {
    return cancellationInfo.getUserId() == null
        || Objects.equals(cancellationInfo.getUserId(), requestingUserId);
  }

  protected boolean handleCancellationInfoNotExists(String requestId, Object userId) {
    LOG.debug("Cancellation item does not exist [requestId={}]", requestId);
    return false;
  }

  public static class RequestCancellationInfo {

    private final RunMonitor m_runMonitor;
    private final Object m_userId;

    public RequestCancellationInfo(RunMonitor runMonitor, Object userId) {
      m_runMonitor = runMonitor;
      m_userId = userId;
    }

    public RunMonitor getRunMonitor() {
      return m_runMonitor;
    }

    public Object getUserId() {
      return m_userId;
    }
  }
}
