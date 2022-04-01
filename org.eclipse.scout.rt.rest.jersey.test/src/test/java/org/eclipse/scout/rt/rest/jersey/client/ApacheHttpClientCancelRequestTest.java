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
package org.eclipse.scout.rt.rest.jersey.client;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.rest.client.RestClientProperties;
import org.eclipse.scout.rt.rest.jersey.EchoServletParameters;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.RequestSynchronizer;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ApacheHttpClientCancelRequestTest {

  private WebTarget m_target;
  private JerseyTestRestClientHelper m_helper;

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
    BEANS.get(RestEnsureHttpHeaderConnectionCloseProperty.class).setValue(Boolean.TRUE);
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(RestEnsureHttpHeaderConnectionCloseProperty.class).invalidate();
  }

  @Before
  public void before() {
    Thread.interrupted();
    m_helper = BEANS.get(JerseyTestRestClientHelper.class);
    m_target = m_helper.target("echo");
  }

  @Test
  public void testCustomCancellableSyncGet() throws Exception {
    // Note: async requests are not executed within a run context. Hence they cannot be cancelled using Scout means.
    RequestSynchronizer requestSynchronizer = BEANS.get(RequestSynchronizer.class);
    final String requestId = requestSynchronizer.announceRequest();
    ICancellable cancellable = new ICancellable() {
      private final AtomicBoolean m_cancelled = new AtomicBoolean();

      @Override
      public boolean isCancelled() {
        return m_cancelled.get();
      }

      @Override
      public boolean cancel(boolean interruptIfRunning) {
        if (!m_cancelled.compareAndSet(false, true)) {
          return false;
        }
        requestSynchronizer.cancelRequest(requestId);
        return true;
      }
    };

    IFuture<?> future = Jobs.schedule(() -> {
      Builder builder = m_target
          .queryParam(EchoServletParameters.STATUS, Response.Status.OK.getStatusCode())
          .queryParam(EchoServletParameters.SLEEP_SEC, 5)
          .queryParam(EchoServletParameters.REQUEST_ID, requestId)
          .request()
          .property(RestClientProperties.CANCELLABLE, cancellable) // register custom cancellable
          .accept(MediaType.APPLICATION_JSON);

      return builder.get();
    }, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));

    requestSynchronizer.awaitRequest(requestId, 5);
    Assert.assertThrows(TimedOutError.class, () -> future.awaitDoneAndGet(300, TimeUnit.MILLISECONDS));
    future.cancel(true);

    assertTrue(cancellable.isCancelled());
  }
}
