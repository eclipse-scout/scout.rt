/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper for synchronizing HTTP requests that allows a client to wait until a request is received by the HTTP
 * service.
 */
@ApplicationScoped
public class RequestSynchronizer {

  private static final Logger LOG = LoggerFactory.getLogger(RequestSynchronizer.class);

  private final ConcurrentMap<String, CountDownLatch> m_requestLatches = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Thread> m_requestThreads = new ConcurrentHashMap<>();

  public String announceRequest() {
    String requestId = UUID.randomUUID().toString();
    LOG.info("announcing request: {}", requestId);
    m_requestLatches.put(requestId, new CountDownLatch(1));
    return requestId;
  }

  public IRegistrationHandle notifyRequestArrived(String requestId) {
    LOG.info("notifying request arrived: {}", requestId);
    CountDownLatch latch = m_requestLatches.get(requestId);
    if (latch != null) {
      latch.countDown();
      m_requestThreads.put(requestId, Thread.currentThread());
      return () -> m_requestThreads.remove(requestId);
    }
    return null;
  }

  public void awaitRequest(String requestId, int timeoutSeconds) throws InterruptedException {
    LOG.info("awaiting request: {}", requestId);
    try {
      CountDownLatch latch = m_requestLatches.get(requestId);
      assertNotNull(latch).await(timeoutSeconds, TimeUnit.SECONDS);
    }
    finally {
      m_requestLatches.remove(requestId);
    }
  }

  public void cancelRequest(String requestId) {
    Thread workerThread = m_requestThreads.get(requestId);
    if (workerThread != null) {
      workerThread.interrupt();
    }
  }
}
