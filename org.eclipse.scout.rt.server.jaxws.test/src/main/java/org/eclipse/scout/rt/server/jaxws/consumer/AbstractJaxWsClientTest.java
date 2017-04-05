/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.EchoRequest;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.EchoResponse;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.GetHeaderRequest;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.GetHeaderResponse;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.JaxWsConsumerTestServicePortType;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.SetHeaderRequest;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.SetHeaderResponse;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.SleepRequest;
import org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.JaxWsPingTestServicePortType;
import org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.PingRequest;
import org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.PingResponse;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Abstract test for web service consumers. <br>
 * <b>Note:</b> Subclasses are responsible for exposing {@link JaxWsConsumerTestServiceProvider}.
 *
 * @since 6.0.300
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(JaxWsConsumerTestServerSession.class)
@RunWithSubject("default")
public abstract class AbstractJaxWsClientTest {

  private static final String X_SCOUT_JAX_WS_TEST_HEADER = "X-Scout-JaxWsTestHeader";

  @BeforeClass
  public static void startupWsProvider() {
    BEANS.get(JaxWsTestProviderInstaller.class).install();
  }

  @AfterClass
  public static void stopWsProvider() {
    BEANS.get(JaxWsTestProviderInstaller.class).uninstall();
  }

  /*
   * ************************************************************
   * Test setup
   * ************************************************************/
  @Test
  public void testSetupEcho() {
    final String messageSent = "Test Message";
    EchoRequest req = new EchoRequest();
    req.setMessage(messageSent);
    EchoResponse response = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort()
        .echo(req);
    assertNotNull(response);
    assertEquals(messageSent, response.getMessage());
  }

  @Test
  public void testSetupGetHeaderNoHeaderSet() {
    GetHeaderRequest req = new GetHeaderRequest();
    req.setHeaderName(X_SCOUT_JAX_WS_TEST_HEADER);
    GetHeaderResponse resp = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort()
        .getHeader(req);
    assertNotNull(resp);
    assertFalse(resp.isHeaderSet());
    assertNull(resp.getHeaderValue());
  }

  @Test
  public void testSetupGetHeaderHeaderSet() {
    GetHeaderRequest req = new GetHeaderRequest();
    req.setHeaderName(X_SCOUT_JAX_WS_TEST_HEADER);
    final String headerValueSent = "test header value";
    GetHeaderResponse resp = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .withHttpRequestHeader(X_SCOUT_JAX_WS_TEST_HEADER, headerValueSent)
        .getPort()
        .getHeader(req);
    assertNotNull(resp);
    assertTrue(resp.isHeaderSet());
    assertEquals(headerValueSent, resp.getHeaderValue());
  }

  @Test
  public void testSetupSetHeader() {
    final String expectedHeader = "test header value";
    SetHeaderRequest req = new SetHeaderRequest();
    req.setHeaderName(X_SCOUT_JAX_WS_TEST_HEADER);
    req.setHeaderValue(expectedHeader);
    JaxWsConsumerTestServicePortType port = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();
    SetHeaderResponse resp = port.setHeader(req);
    assertNotNull(resp);
    assertEquals("ok", resp.getMessage());

    assertHttpResponseHeader(port, X_SCOUT_JAX_WS_TEST_HEADER, expectedHeader);
  }

  @Test
  public void testSetupSleep() {
    final int sleepTimeMillis = 500;

    SleepRequest req = new SleepRequest();
    req.setMillis(sleepTimeMillis);

    final JaxWsConsumerTestServicePortType port = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    final long start = System.currentTimeMillis();
    try {
      port.sleep(req);
    }
    finally {
      long dt = System.currentTimeMillis() - start;
      if (dt < sleepTimeMillis) {
        fail("Operation took less than " + sleepTimeMillis);
      }
    }
  }

  @Test
  public void testSetupSleepWithReadTimeout() {
    final int sleepTimeMillis = 5000;

    SleepRequest req = new SleepRequest();
    req.setMillis(sleepTimeMillis);

    final JaxWsConsumerTestServicePortType port = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .withReadTimeout(1, TimeUnit.SECONDS)
        .getPort();

    try {
      port.sleep(req);
      fail("invocation is expected to be cancelled");
    }
    catch (WebServiceException e) {
      if (!(e.getCause() instanceof SocketTimeoutException)) {
        throw e;
      }
    }
  }

  /*
   * ************************************************************
   * Test use same port multiple times
   * ************************************************************/
  @Test
  public void testSamePortMultipleTimesEcho() {
    JaxWsConsumerTestServicePortType port = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    for (int i = 0; i < 10; i++) {
      assertSendEcho(port, i);
    }
  }

  @Test
  public void testSamePortMultipleTimesGetHeader() {
    JaxWsConsumerTestServicePortType port = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    GetHeaderRequest req = new GetHeaderRequest();
    req.setHeaderName(X_SCOUT_JAX_WS_TEST_HEADER);

    // 1. no HTTP headers set
    GetHeaderResponse resp = port.getHeader(req);
    assertNotNull(resp);
    assertFalse(resp.isHeaderSet());
    assertNull(resp.getHeaderValue());

    // 2. add HTTP header
    final String headerValueSent = "test header value";
    BEANS.get(JaxWsImplementorSpecifics.class).setHttpRequestHeader(((BindingProvider) port).getRequestContext(), X_SCOUT_JAX_WS_TEST_HEADER, headerValueSent);
    resp = port.getHeader(req);
    assertNotNull(resp);
    assertTrue(resp.isHeaderSet());
    assertEquals(headerValueSent, resp.getHeaderValue());

    // 3. do not change HTTP headers
    resp = port.getHeader(req);
    assertNotNull(resp);
    assertTrue(resp.isHeaderSet());
    assertEquals(headerValueSent, resp.getHeaderValue());

    // 4. remove HTTP header
    BEANS.get(JaxWsImplementorSpecifics.class).removeHttpRequestHeader(((BindingProvider) port).getRequestContext(), X_SCOUT_JAX_WS_TEST_HEADER);
    resp = port.getHeader(req);
    assertNotNull(resp);
    assertFalse(resp.isHeaderSet());
    assertNull(resp.getHeaderValue());
  }

  @Test
  public void testSamePortMultipleTimesSetHeader() {
    JaxWsConsumerTestServicePortType port = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    // 1. invoke echo without header
    EchoRequest echoReq = new EchoRequest();
    echoReq.setMessage("test message");
    port.echo(echoReq);
    assertHttpResponseHeader(port, X_SCOUT_JAX_WS_TEST_HEADER, null);

    // 2. set header
    final String expectedHeader = "test header value";
    SetHeaderRequest req = new SetHeaderRequest();
    req.setHeaderName(X_SCOUT_JAX_WS_TEST_HEADER);
    req.setHeaderValue(expectedHeader);

    SetHeaderResponse resp = port.setHeader(req);
    assertNotNull(resp);
    assertEquals("ok", resp.getMessage());
    assertHttpResponseHeader(port, X_SCOUT_JAX_WS_TEST_HEADER, expectedHeader);

    // 3. invoke echo without header
    echoReq.setMessage("test message");
    port.echo(echoReq);
    assertHttpResponseHeader(port, X_SCOUT_JAX_WS_TEST_HEADER, null);
  }

  @Test
  public void testSamePortMultipleTimesSleepWithReadTimeoutCheckResponseCode() {
    final JaxWsConsumerTestServicePortType port = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .withReadTimeout(500, TimeUnit.MILLISECONDS)
        .getPort();

    // 1. invoke echo, response code 200 expected
    EchoRequest echoReq = new EchoRequest();
    echoReq.setMessage("test message");
    port.echo(echoReq);
    assertHttpResponseCode(port, 200);

    // 2. invoke sleep
    SleepRequest req = new SleepRequest();
    req.setMillis(5000);
    try {
      port.sleep(req);
      fail("invocation is expected to be cancelled");
    }
    catch (WebServiceException e) {
      if (!(e.getCause() instanceof SocketTimeoutException)) {
        throw e;
      }
    }
    assertHttpResponseCode(port, 0);

    // 3. invoke echo again, response code 200 expected
    port.echo(echoReq);
    assertHttpResponseCode(port, 200);
  }

  @Test
  public void testSamePortMultipleTimesSleepWithReadTimeoutCheckResponseHeaders() {
    final JaxWsConsumerTestServicePortType port = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .withReadTimeout(500, TimeUnit.MILLISECONDS)
        .getPort();

    // 1. invoke set header
    final String testHeaderValue = "test header value";
    SetHeaderRequest headerReq = new SetHeaderRequest();
    headerReq.setHeaderName(X_SCOUT_JAX_WS_TEST_HEADER);
    headerReq.setHeaderValue(testHeaderValue);
    port.setHeader(headerReq);
    assertHttpResponseHeader(port, X_SCOUT_JAX_WS_TEST_HEADER, testHeaderValue);

    // 2. invoke sleep
    SleepRequest req = new SleepRequest();
    req.setMillis(5000);
    try {
      port.sleep(req);
      fail("invocation is expected to be cancelled");
    }
    catch (WebServiceException e) {
      if (!(e.getCause() instanceof SocketTimeoutException)) {
        throw e;
      }
    }
    assertHttpResponseCode(port, 0);
    assertHttpResponseHeader(port, X_SCOUT_JAX_WS_TEST_HEADER, null);

    // 3. invoke echo
    EchoRequest echoReq = new EchoRequest();
    echoReq.setMessage("test message");
    port.echo(echoReq);
    assertHttpResponseCode(port, 200);
    assertHttpResponseHeader(port, X_SCOUT_JAX_WS_TEST_HEADER, null);
  }

  /*
   * ************************************************************
   * Test acquire port in same transaction multiple times
   * ************************************************************/
  @Test
  public void testAcquirePortInSameTransactionMultipleTimesEcho() {
    JaxWsConsumerTestServicePortType port0 = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();
    assertSendEcho(port0, 0);

    JaxWsConsumerTestServicePortType port1 = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    if (BEANS.get(JaxWsImplementorSpecifics.class).isPoolingSupported()) {
      assertSamePort(port0, port1);
    }
    else {
      assertDifferentPort(port0, port1);
    }

    assertSendEcho(port1, 1);
  }

  @Test
  public void testAcquirePortInSameTransactionMultipleTimesGetHeader() {
    GetHeaderRequest req = new GetHeaderRequest();
    req.setHeaderName(X_SCOUT_JAX_WS_TEST_HEADER);

    // 1. add HTTP header on port0
    JaxWsConsumerTestServicePortType port0 = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    final String headerValueSent = "test header value";
    BEANS.get(JaxWsImplementorSpecifics.class).setHttpRequestHeader(((BindingProvider) port0).getRequestContext(), X_SCOUT_JAX_WS_TEST_HEADER, headerValueSent);
    GetHeaderResponse resp = port0.getHeader(req);
    assertNotNull(resp);
    assertTrue(resp.isHeaderSet());
    assertEquals(headerValueSent, resp.getHeaderValue());

    // 2. acquire port1 and do not set a header
    JaxWsConsumerTestServicePortType port1 = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    if (BEANS.get(JaxWsImplementorSpecifics.class).isPoolingSupported()) {
      assertSamePort(port0, port1);
    }
    else {
      assertDifferentPort(port0, port1);
    }

    resp = port1.getHeader(req);
    assertNotNull(resp);
    assertFalse(resp.isHeaderSet());
    assertNull(resp.getHeaderValue());
  }

  @Test
  public void testAcquirePortInSameTransactionMultipleTimesSleepWithReadTimeoutCheckResponseHeaders() {
    // 1. invoke set header on port0
    final JaxWsConsumerTestServicePortType port0 = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    final String testHeaderValue = "test header value";
    SetHeaderRequest headerReq = new SetHeaderRequest();
    headerReq.setHeaderName(X_SCOUT_JAX_WS_TEST_HEADER);
    headerReq.setHeaderValue(testHeaderValue);
    port0.setHeader(headerReq);
    assertHttpResponseHeader(port0, X_SCOUT_JAX_WS_TEST_HEADER, testHeaderValue);

    // 2. invoke sleep on port1
    final JaxWsConsumerTestServicePortType port1 = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .withReadTimeout(500, TimeUnit.MILLISECONDS)
        .getPort();

    if (BEANS.get(JaxWsImplementorSpecifics.class).isPoolingSupported()) {
      assertSamePort(port0, port1);
    }
    else {
      assertDifferentPort(port0, port1);
    }

    SleepRequest req = new SleepRequest();
    req.setMillis(5000);
    try {
      port1.sleep(req);
      fail("invocation is expected to be cancelled");
    }
    catch (WebServiceException e) {
      if (!(e.getCause() instanceof SocketTimeoutException)) {
        throw e;
      }
    }
    assertHttpResponseCode(port1, 0);
    assertHttpResponseHeader(port1, X_SCOUT_JAX_WS_TEST_HEADER, null);

    // 3. invoke echo on port1
    EchoRequest echoReq = new EchoRequest();
    echoReq.setMessage("test message");
    port1.echo(echoReq);
    assertHttpResponseCode(port1, 200);
    assertHttpResponseHeader(port1, X_SCOUT_JAX_WS_TEST_HEADER, null);
  }

  @Test
  public void testAcquirePortConcurrentlyInDifferentTransactions() throws InterruptedException {
    final CountDownLatch txn1InitLatch = new CountDownLatch(1);
    final CountDownLatch txn2InitLatch = new CountDownLatch(1);

    final Holder<JaxWsConsumerTestServicePortType> txn1PortHolder = new Holder<>(JaxWsConsumerTestServicePortType.class);
    final Holder<JaxWsConsumerTestServicePortType> txn2PortHolder = new Holder<>(JaxWsConsumerTestServicePortType.class);

    Jobs.schedule(
        new IRunnable() {
          @Override
          public void run() throws Exception {
            JaxWsConsumerTestServicePortType port0 = BEANS
                .get(JaxWsConsumerTestClient.class)
                .newInvocationContext()
                .getPort();
            assertSendEcho(port0, 0);
            txn1PortHolder.setValue(port0);
            txn1InitLatch.countDown();
            txn2InitLatch.await();

            JaxWsConsumerTestServicePortType port1 = BEANS
                .get(JaxWsConsumerTestClient.class)
                .newInvocationContext()
                .getPort();

            assertSamePort(port0, port1);
            assertSendEcho(port1, 1);
          }
        }, Jobs.newInput().withRunContext(ServerRunContexts.empty()));

    txn1InitLatch.await();

    Jobs.schedule(
        new IRunnable() {
          @Override
          public void run() throws Exception {
            JaxWsConsumerTestServicePortType port0 = BEANS
                .get(JaxWsConsumerTestClient.class)
                .newInvocationContext()
                .getPort();
            assertSendEcho(port0, 0);
            txn2PortHolder.setValue(port0);
            txn2InitLatch.countDown();

            JaxWsConsumerTestServicePortType port1 = BEANS
                .get(JaxWsConsumerTestClient.class)
                .newInvocationContext()
                .getPort();

            assertSamePort(port0, port1);
            assertSendEcho(port1, 1);
          }
        }, Jobs.newInput().withRunContext(ServerRunContexts.empty()));

    txn2InitLatch.await();
    assertDifferentPort(txn1PortHolder.getValue(), txn2PortHolder.getValue());
  }

  /*
   * ************************************************************
   * Test acquire port in different transactions
   * ************************************************************/
  @Test
  public void testAcquirePortInDifferentTransactions() throws InterruptedException {
    final Holder<JaxWsConsumerTestServicePortType> txn1PortHolder = new Holder<>(JaxWsConsumerTestServicePortType.class);
    final Holder<JaxWsConsumerTestServicePortType> txn2PortHolder = new Holder<>(JaxWsConsumerTestServicePortType.class);

    // This test case expects at most one port in the pool. It is guaranteed by discarding all pooled entries.
    BEANS.get(JaxWsConsumerTestClient.class).discardAllPoolEntries();

    ServerRunContexts.copyCurrent().run(
        new IRunnable() {
          @Override
          public void run() throws Exception {
            JaxWsConsumerTestServicePortType port = BEANS
                .get(JaxWsConsumerTestClient.class)
                .newInvocationContext()
                .getPort();
            assertSendEcho(port, 0);
            txn1PortHolder.setValue(port);
          }
        });

    ServerRunContexts.copyCurrent().run(
        new IRunnable() {
          @Override
          public void run() throws Exception {
            JaxWsConsumerTestServicePortType port = BEANS
                .get(JaxWsConsumerTestClient.class)
                .newInvocationContext()
                .getPort();
            assertSendEcho(port, 0);
            txn2PortHolder.setValue(port);
          }
        });

    if (BEANS.get(JaxWsImplementorSpecifics.class).isPoolingSupported()) {
      assertSamePort(txn1PortHolder.getValue(), txn2PortHolder.getValue());
    }
    else {
      assertDifferentPort(txn1PortHolder.getValue(), txn2PortHolder.getValue());
    }
  }

  /**
   * Canceling a running web service invocation invalidates the port. This test verifies, that the canceled port is not
   * put back into the pool.
   */
  @Test
  public void testAcquirePortInDifferentTransactionsCancelFirstOne() throws InterruptedException {
    final Holder<JaxWsConsumerTestServicePortType> txn1PortHolder = new Holder<>(JaxWsConsumerTestServicePortType.class);
    final Holder<JaxWsConsumerTestServicePortType> txn2PortHolder = new Holder<>(JaxWsConsumerTestServicePortType.class);

    // This test case expects at most one port in the pool. It is guaranteed by discarding all pooled entries.
    BEANS.get(JaxWsConsumerTestClient.class).discardAllPoolEntries();

    final CountDownLatch requestRunningLatch = new CountDownLatch(1);
    final IFuture<Void> future = Jobs.schedule(
        new IRunnable() {
          @Override
          public void run() throws Exception {
            JaxWsConsumerTestServicePortType port = BEANS
                .get(JaxWsConsumerTestClient.class)
                .newInvocationContext()
                .getPort();

            txn1PortHolder.setValue(port);
            SleepRequest req = new SleepRequest();
            req.setMillis(1000);
            requestRunningLatch.countDown();
            port.sleep(req);
          }
        }, Jobs
            .newInput()
            .withRunContext(ServerRunContexts.copyCurrent())
            .withExceptionHandling(null, true));

    requestRunningLatch.await();
    SleepUtil.sleepSafe(50, TimeUnit.MILLISECONDS);
    Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        future.cancel(true);
      }
    }, Jobs
        .newInput()
        .withRunContext(ServerRunContexts.copyCurrent()));

    try {
      future.awaitDone();
    }
    catch (WebServiceRequestCancelledException e) {
      // expected
    }
    assertTrue(future.isCancelled());

    ServerRunContexts.copyCurrent().run(
        new IRunnable() {
          @Override
          public void run() throws Exception {
            JaxWsConsumerTestServicePortType port = BEANS
                .get(JaxWsConsumerTestClient.class)
                .newInvocationContext()
                .getPort();
            assertSendEcho(port, 0);
            txn2PortHolder.setValue(port);
          }
        });

    assertDifferentPort(txn1PortHolder.getValue(), txn2PortHolder.getValue());
  }

  /*
   * ************************************************************
   * Test invoke different web services in same transaction
   * ************************************************************/
  @Test
  public void testDifferentPortsInSameTransaction() {
    JaxWsConsumerTestServicePortType echoPort = BEANS
        .get(JaxWsConsumerTestClient.class)
        .newInvocationContext()
        .getPort();

    assertSendEcho(echoPort, 0);

    JaxWsPingTestServicePortType pingPort = BEANS
        .get(JaxWsPingTestClient.class)
        .newInvocationContext()
        .getPort();

    assertNotSame(echoPort, pingPort);

    PingRequest pingRequest = new PingRequest();
    pingRequest.setMessage("ping");
    PingResponse pingResponse = pingPort.ping(pingRequest);
    assertNotNull(pingResponse);
    assertEquals("ping", pingResponse.getMessage());
  }

  /*
   * ************************************************************
   * Utility methods
   * ************************************************************/

  protected void assertHttpResponseHeader(JaxWsConsumerTestServicePortType port, String headerName, String expectedHeader) {
    List<String> responseHeader = BEANS.get(JaxWsImplementorSpecifics.class).getHttpResponseHeader(((BindingProvider) port).getResponseContext(), headerName);
    if (expectedHeader == null) {
      assertEquals(Collections.emptyList(), responseHeader);
    }
    else {
      assertEquals(Collections.singletonList(expectedHeader), responseHeader);
    }
  }

  protected void assertHttpResponseCode(JaxWsConsumerTestServicePortType port, int expectedResponseCode) {
    Integer httpResponseCode = BEANS.get(JaxWsImplementorSpecifics.class).getHttpResponseCode(((BindingProvider) port).getResponseContext());
    assertEquals(Integer.valueOf(expectedResponseCode), httpResponseCode);
  }

  protected void assertSendEcho(JaxWsConsumerTestServicePortType port, int i) {
    final String messageSent = "Test Message " + i;
    EchoRequest req = new EchoRequest();
    req.setMessage(messageSent);
    EchoResponse response = port.echo(req);
    assertNotNull(response);
    assertEquals(messageSent, response.getMessage());
  }

  protected void assertSamePort(Object port0, Object port1) {
    assertPorts(true, port0, port1);
  }

  protected void assertDifferentPort(Object port0, Object port1) {
    assertPorts(false, port0, port1);
  }

  protected void assertPorts(boolean expectedSame, Object port0, Object port1) {
    assertNotNull(port0);
    assertNotNull(port1);

    // extract Socut's JAX-WS invocation handler, i.e. InvocationContext.P_InvocationHandler<PORT>
    assertTrue(Proxy.isProxyClass(port0.getClass()));
    assertTrue(Proxy.isProxyClass(port1.getClass()));
    Object ih0 = Proxy.getInvocationHandler(port0);
    Object ih1 = Proxy.getInvocationHandler(port1);

    // dereference actual port
    assertTrue(ih0 instanceof IPortWrapper<?>);
    assertTrue(ih1 instanceof IPortWrapper<?>);
    Object p0 = ((IPortWrapper<?>) ih0).getPort();
    Object p1 = ((IPortWrapper<?>) ih1).getPort();

    if (expectedSame) {
      assertSame(p0, p1);
    }
    else {
      assertNotSame(p0, p1);
    }
  }
}
