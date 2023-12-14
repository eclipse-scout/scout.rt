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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.rest.RestHttpHeaders;
import org.eclipse.scout.rt.rest.client.RestClientProperties;

/**
 * REST client request filter that sets a random request ID (UUID) HTTP header and puts an {@link ICancellable} to the
 * request that may be used by the HTTP connection sub-system to abort a running request.
 * <p>
 * <b>Note:</b> REST requests must run within a {@link RunContext} in order to be cancelled.
 */
public class RestRequestCancellationClientRequestFilter implements ClientRequestFilter {

  private final Consumer<String> m_requestCanceller;

  /**
   * @param requestCanceller
   *          consumes the requestId of an aborted request in order to perform the actual cancel operation (i.e. sending
   *          a cancel request to an appropriate REST resource).
   */
  public RestRequestCancellationClientRequestFilter(Consumer<String> requestCanceller) {
    m_requestCanceller = requestCanceller;
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    final String requestId = UUID.randomUUID().toString();
    requestContext.getHeaders().putSingle(RestHttpHeaders.REQUEST_ID, requestId);
    requestContext.setProperty(RestClientProperties.CANCELLABLE, new ScoutRestRequestCancellable(requestId, m_requestCanceller));
  }

  public static class ScoutRestRequestCancellable implements ICancellable {

    private final String m_requestId;
    private final AtomicBoolean m_cancelled;
    private final Consumer<String> m_requestCanceller;

    public ScoutRestRequestCancellable(String requestId, Consumer<String> requestCanceller) {
      m_requestId = requestId;
      m_requestCanceller = requestCanceller;
      m_cancelled = new AtomicBoolean();
    }

    @Override
    public boolean isCancelled() {
      return m_cancelled.get();
    }

    @Override
    public boolean cancel(boolean interruptIfRunning) {
      if (!m_cancelled.compareAndSet(false, true)) {
        return false;
      }

      RunContexts.copyCurrent()
          .withRunMonitor(BEANS.get(RunMonitor.class)) // execute with a new RunMonitor
          .run(() -> m_requestCanceller.accept(m_requestId));

      return true;
    }
  }
}
