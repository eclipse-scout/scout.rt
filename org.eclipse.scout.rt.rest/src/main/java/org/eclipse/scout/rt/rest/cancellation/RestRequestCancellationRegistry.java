/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.cancellation;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    if (requestId == null) {
      return false;
    }
    RequestCancellationInfo cancellationInfo = getRequestCancellationInfos().get(requestId);
    if (cancellationInfo == null) {
      LOG.debug("Cancellation item does not exist [requestId={}]", requestId);
      return false;
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
