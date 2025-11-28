/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.cancellation;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.context.RunContext;
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

  private boolean m_destroyed = false;
  private final ConcurrentMap<String, RequestCancellationInfo> m_requestCancellationInfos = new ConcurrentHashMap<>();

  protected ConcurrentMap<String, RequestCancellationInfo> getRequestCancellationInfos() {
    return m_requestCancellationInfos;
  }

  /**
   * Register request with id {@code requestId} in registry using given {@code runContext} and {@code userId}.
   * <b>Important:</b> Ensure that the request is unregistered after termination using {@link #unregister(String)}.
   */
  public void register(String requestId, Object userId, RunContext runContext) {
    assertNotNull(requestId, "requestId is required");
    assertNotNull(runContext, "runContext is required");
    assertNotNull(runContext.getRunMonitor(), "runMonitor is required");
    assertFalse(m_destroyed, "Registry not alive anymore");

    LOG.debug("Register request id={} using run monitor={}, userId={}", requestId, runContext.getRunMonitor(), userId);
    final ConcurrentMap<String, RequestCancellationInfo> cancellationInfos = getRequestCancellationInfos();

    if (cancellationInfos.putIfAbsent(requestId, new RequestCancellationInfo(runContext, userId)) != null) {
      // request id is already in use
      LOG.warn("Duplicate request id. Ignoring this request: [requestId:{}]", requestId);
    }
  }

  public boolean cancel(String requestId, String userId) {
    return cancel(requestId, userId, this::handleCancellationInfoNotExists);
  }

  public void cancel(Predicate<RunContext> runContextPredicate) {
    m_requestCancellationInfos.values().stream()
        .filter(info -> runContextPredicate.test(info.getRunContext()))
        .forEach(this::cancel);
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
  public boolean cancel(String requestId, String userId, BiFunction<String, String, Boolean> requestNotExistsHandler) {
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

    return cancel(cancellationInfo);
  }

  /**
   * Unregister request with id {@code requestId} from registry.
   */
  public void unregister(String requestId) {
    LOG.debug("Unregister request id={}", requestId);
    RequestCancellationInfo info = getRequestCancellationInfos().remove(requestId);
    if (info != null && info.getRunContext().getRunMonitor().isCancelled() && Thread.interrupted()) {
      // as thread may be used by other operations as well; interrupted state must be reset after previous interruption
      LOG.trace("Reset interrupted state for cancelled and interrupted request {}", requestId);
    }
  }

  /**
   * Cancellation w/o further checks (e.g. forced).
   */
  protected boolean cancel(RequestCancellationInfo cancellationInfo) {
    LOG.debug("Cancel request run monitor={}, userId={}", cancellationInfo.getRunContext().getRunMonitor(), cancellationInfo.getUserId());
    return cancellationInfo.getRunContext().getRunMonitor().cancel(true);
  }

  public void destroy() {
    m_destroyed = true;
    cancel(runContext -> true);
  }

  protected boolean checkAccess(Object requestingUserId, RequestCancellationInfo cancellationInfo) {
    return cancellationInfo.getUserId() == null
        || Objects.equals(cancellationInfo.getUserId(), requestingUserId);
  }

  protected boolean handleCancellationInfoNotExists(String requestId, String userId) {
    LOG.debug("Cancellation item does not exist [requestId={}]", requestId);
    return false;
  }

  public static class RequestCancellationInfo {

    private final RunContext m_runContext;
    private final Object m_userId;

    public RequestCancellationInfo(RunContext runContext, Object userId) {
      m_runContext = runContext;
      m_userId = userId;
    }

    public RunContext getRunContext() {
      return m_runContext;
    }

    public Object getUserId() {
      return m_userId;
    }
  }

  /**
   * {@link IPlatformListener} to shutdown this registry upon platform shutdown.
   */
  public static class PlatformListener implements IPlatformListener {

    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.PlatformStopping) {
        BEANS.get(RestRequestCancellationRegistry.class).destroy();
      }
    }
  }
}
