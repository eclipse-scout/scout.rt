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
package org.eclipse.scout.rt.mom.jms;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.activemq.jndi.ActiveMQInitialContextFactory;
import org.eclipse.scout.rt.mom.api.AbstractMomTransport;
import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.IMomTransport;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.IDisposable;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PlatformTestRunner.class)
public class JmsMomImplementorTest {

  private static final Logger LOG = LoggerFactory.getLogger(JmsMomImplementorTest.class);

  private static IBean<? extends IMomTransport> s_momBean;

  private List<IDisposable> m_disposables;
  private String m_testJobExecutionHint;

  @Rule
  public TestName m_testName = new TestName();
  public long m_t0;

  @BeforeClass
  public static void beforeClass() throws Exception {
    installTestMom(JmsTestMom.class);
  }

  @AfterClass
  public static void afterClass() {
    uninstallTestMom();
  }

  protected static void installTestMom(Class<? extends IMomTransport> transportType) {
    if (s_momBean != null) {
      uninstallTestMom();
    }
    s_momBean = BEANS.getBeanManager().registerClass(transportType);
  }

  protected static void uninstallTestMom() {
    s_momBean.getInstance().destroy();
    BEANS.getBeanManager().unregisterBean(s_momBean);
    s_momBean = null;
  }

  @Before
  public void before() {
    LOG.info("---------------------------------------------------");
    LOG.info("<{}>", m_testName.getMethodName());
    m_t0 = System.nanoTime();
    m_testJobExecutionHint = UUID.randomUUID().toString();
    m_disposables = new ArrayList<>();
  }

  @After
  public void after() {
    // Dispose resources
    if (m_disposables.size() > 0) {
      LOG.info("Disposing {} objects: {}", m_disposables.size(), m_disposables);
      for (IDisposable disposable : m_disposables) {
        disposable.dispose();
      }
    }

    // Cancel jobs
    IFilter<IFuture<?>> testJobsFilter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(m_testJobExecutionHint)
        .toFilter();
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(testJobsFilter);
    if (futures.size() > 0) {
      LOG.info("Cancelling {} jobs: {}", futures.size(), futures);
      Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
          .andMatchFuture(futures)
          .andMatchNotState(JobState.DONE)
          .toFilter(), true);
      long t0 = System.nanoTime();
      try {
        Jobs.getJobManager().awaitDone(testJobsFilter, 10, TimeUnit.SECONDS);
        LOG.info("All jobs have finished after {} ms", StringUtility.formatNanos(System.nanoTime() - t0));
      }
      catch (TimedOutError e) {
        LOG.warn("Some cancelled jobs are still running after {} ms! Please check their implementation.", StringUtility.formatNanos(System.nanoTime() - t0));
      }
    }

    LOG.info("Finished test in {} ms", StringUtility.formatNanos(System.nanoTime() - m_t0));
    LOG.info("</{}>", m_testName.getMethodName());
  }

  @Test
  public void testInstanceScoped() {
    JmsMomImplementor mom1 = BEANS.get(JmsMomImplementor.class);
    JmsMomImplementor mom2 = BEANS.get(JmsMomImplementor.class);
    assertNotSame(mom1, mom2);
  }

  @Test
  public void testCreateContextNullMap() throws NamingException {
    new JmsMomImplementor().createContext(null);
  }

  @Test
  public void testCreateContextEmptyMap() throws NamingException {
    new JmsMomImplementor().createContext(Collections.emptyMap());
  }

  @Test
  public void testCreateContextOrdinaryMap() throws NamingException {
    new JmsMomImplementor().createContext(Collections.<Object, Object> singletonMap("key", "value"));
  }

  @Test
  public void testCreateContextMapWithNullEntries() throws NamingException {
    new JmsMomImplementor().createContext(Collections.<Object, Object> singletonMap("key", null));
  }

  @Test
  public void testPublishObject() throws InterruptedException {
    final Capturer<Person> capturer = new Capturer<>();

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    IDestination<Person> queue = MOM.newDestination("test/mom/testPublishObject", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(ObjectMarshaller.class)));

    MOM.publish(JmsTestMom.class, queue, person);
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<Person>() {

      @Override
      public void onMessage(IMessage<Person> message) {
        capturer.set(message.getTransferObject());
      }
    }));

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @Test
  public void testPublishBytes() throws InterruptedException {
    final Capturer<byte[]> capturer = new Capturer<>();

    IDestination<byte[]> queue = MOM.newDestination("test/mom/testPublishBytes", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(BytesMarshaller.class)));

    MOM.publish(JmsTestMom.class, queue, "hello world".getBytes(StandardCharsets.UTF_8));
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<byte[]>() {

      @Override
      public void onMessage(IMessage<byte[]> message) {
        capturer.set(message.getTransferObject());
      }
    }));

    // Verify
    byte[] testee = capturer.get();
    assertEquals("hello world", new String(testee, StandardCharsets.UTF_8));
  }

  @Test
  public void testPublishText() throws InterruptedException {
    final Capturer<String> capturer = new Capturer<>();

    IDestination<String> queue = MOM.newDestination("test/mom/testPublishText", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(TextMarshaller.class)));

    MOM.publish(JmsTestMom.class, queue, "hello world");
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        capturer.set(message.getTransferObject());
      }
    }));

    // Verify
    String testee = capturer.get();
    assertEquals("hello world", testee);
  }

  @Test
  public void testPublishSubscribe() throws InterruptedException {
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(TextMarshaller.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(ObjectMarshaller.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(JsonMarshaller.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testPublishAndConsumeInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class)));

    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(TextMarshaller.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(ObjectMarshaller.class)));
    assertEquals("Hello World", testPublishAndConsumeInternal("Hello World", BEANS.get(JsonMarshaller.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testPublishAndConsumeInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class)));
  }

  @Test(timeout = 200_000)
  public void testRequestReply() throws InterruptedException {
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(TextMarshaller.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(ObjectMarshaller.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(JsonMarshaller.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testRequestReplyInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class)));

    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(TextMarshaller.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(ObjectMarshaller.class)));
    assertEquals("Hello World", testRequestReplyInternal("Hello World", BEANS.get(JsonMarshaller.class)));
    assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) testRequestReplyInternal("Hello World".getBytes(StandardCharsets.UTF_8), BEANS.get(BytesMarshaller.class)));
  }

  @Test
  public void testPublishEmpty() throws InterruptedException {
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(TextMarshaller.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(ObjectMarshaller.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(JsonMarshaller.class)));
    assertArrayEquals(new byte[0], (byte[]) testPublishAndConsumeInternal(new byte[0], BEANS.get(BytesMarshaller.class)));

    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(TextMarshaller.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(ObjectMarshaller.class)));
    assertEquals("", testPublishAndConsumeInternal("", BEANS.get(JsonMarshaller.class)));
    assertArrayEquals(new byte[0], (byte[]) testPublishAndConsumeInternal(new byte[0], BEANS.get(BytesMarshaller.class)));
  }

  @Test
  public void testPublishNull() throws InterruptedException {
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(TextMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(ObjectMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(JsonMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(BytesMarshaller.class)));

    assertNull(testPublishAndConsumeInternal(null, BEANS.get(TextMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(ObjectMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(JsonMarshaller.class)));
    assertNull(testPublishAndConsumeInternal(null, BEANS.get(BytesMarshaller.class)));
  }

  @Test(timeout = 200_000)
  public void testRequestReplyEmpty() throws InterruptedException {
    assertEquals("", testRequestReplyInternal("", BEANS.get(TextMarshaller.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(ObjectMarshaller.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(JsonMarshaller.class)));
    assertArrayEquals(new byte[0], (byte[]) testRequestReplyInternal(new byte[0], BEANS.get(BytesMarshaller.class)));

    assertEquals("", testRequestReplyInternal("", BEANS.get(TextMarshaller.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(ObjectMarshaller.class)));
    assertEquals("", testRequestReplyInternal("", BEANS.get(JsonMarshaller.class)));
    assertArrayEquals(new byte[0], (byte[]) testRequestReplyInternal(new byte[0], BEANS.get(BytesMarshaller.class)));
  }

  @Test(timeout = 200_000)
  public void testRequestReplyNull() throws InterruptedException {
    assertNull(testRequestReplyInternal(null, BEANS.get(TextMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(ObjectMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(JsonMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(BytesMarshaller.class)));

    assertNull(testRequestReplyInternal(null, BEANS.get(TextMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(ObjectMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(JsonMarshaller.class)));
    assertNull(testRequestReplyInternal(null, BEANS.get(BytesMarshaller.class)));
  }

  @Test
  public void testPublishJsonData() throws InterruptedException {
    final Capturer<Person> capturer = new Capturer<>();

    IDestination<Person> queue = MOM.newDestination("test/mom/testPublishJsonData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(JsonMarshaller.class)));

    Person person = new Person();
    person.setFirstname("anna");
    person.setLastname("smith");

    MOM.publish(JmsTestMom.class, queue, person);
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<Person>() {

      @Override
      public void onMessage(IMessage<Person> message) {
        capturer.set(message.getTransferObject());
      }
    }));

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @Test
  public void testTopicPublishSubscribe() throws InterruptedException {
    IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishSubscribe", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final Capturer<String> capturer = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        capturer.set(message.getTransferObject());
      }
    }));

    // Publish a message
    MOM.publish(JmsTestMom.class, topic, "hello world");

    // Verify
    assertEquals("hello world", capturer.get());
  }

  @Test
  public void testTopicPublishSubscribeMultipleSubscriptions() throws InterruptedException {
    IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishSubscribeMultipleSubscriptions", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final CountDownLatch latch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }));

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }));

    // Publish a message
    MOM.publish(JmsTestMom.class, topic, "hello world");

    // Verify
    assertTrue(latch.await(5, TimeUnit.SECONDS));
  }

  @Test
  @Times(10) // regression
  public void testTopicPublishFirst() throws InterruptedException {
    IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishFirst", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    // Publish a message
    MOM.publish(JmsTestMom.class, topic, "hello world");

    // Subscribe for the destination
    final CountDownLatch latch = new CountDownLatch(1);
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }));

    // Verify
    assertFalse(latch.await(200, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testQueuePublishSubscribe() throws InterruptedException {
    IDestination<String> queue = MOM.newDestination("test/mom/testQueuePublishSubscribe", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final Capturer<String> capturer = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        capturer.set(message.getTransferObject());
      }
    }));

    // Publish a message
    MOM.publish(JmsTestMom.class, queue, "hello world");

    // Verify
    assertEquals("hello world", capturer.get());
  }

  @Test
  @Times(10) // regression
  public void testQueuePublishSubscribeMultipleSubscriptions() throws InterruptedException {
    IDestination<String> queue = MOM.newDestination("test/mom/testQueuePublishSubscribeMultipleSubscriptions", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final AtomicInteger msgCounter = new AtomicInteger();
    final CountDownLatch latch = new CountDownLatch(1);

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        msgCounter.incrementAndGet();
        latch.countDown();
      }
    }));

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        msgCounter.incrementAndGet();
        latch.countDown();
      }
    }));

    // Publish a message
    MOM.publish(JmsTestMom.class, queue, "hello world");

    // Verify
    latch.await(5, TimeUnit.SECONDS);
    Thread.sleep(50);
    assertEquals(1, msgCounter.get());
  }

  @Test
  @Times(10) // regression
  public void testQueuePublishFirst() throws InterruptedException {
    IDestination<String> queue = MOM.newDestination("test/mom/testQueuePublishFirst", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // Publish a message
    MOM.publish(JmsTestMom.class, queue, "hello world");

    // Subscribe for the destination
    final CountDownLatch latch = new CountDownLatch(1);
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }));

    // Verify
    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void testQueuePublishSubscribeCorrelationId() throws InterruptedException {
    final IDestination<String> queue = MOM.newDestination("test/mom/testQueuePublishSubscribeCorrelationId", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testPublishSubscribeCorrelationIdInternal(queue);
  }

  @Test
  public void testTopicPublishSubscribeCorrelationId() throws InterruptedException {
    final IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishSubscribeCorrelationId", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testPublishSubscribeCorrelationIdInternal(topic);
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReply() throws InterruptedException {
    IBiDestination<String, String> queue = MOM.newBiDestination("test/momtestQueueRequestReply", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, queue, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        return request.getTransferObject().toUpperCase();
      }
    }));

    // Initiate 'request-reply' communication
    String testee = MOM.request(JmsTestMom.class, queue, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyCorrelationId() throws InterruptedException {
    IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyCorrelationId", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyCorrelationIdInternal(queue);
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyCorrelationId() throws InterruptedException {
    IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyCorrelationId", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testRequestReplyCorrelationIdInternal(topic);
  }

  @Test
  public void testProperties() throws InterruptedException {
    IDestination<String> topic = MOM.newDestination("test/mom/testProperties", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final Capturer<String> capturer1 = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {
      @Override
      public void onMessage(IMessage<String> message) {
        capturer1.set(message.getProperty("prop"));
      }
    }));

    // Publish a message
    MOM.publish(JmsTestMom.class, topic, "hello world", MOM.newPublishInput()
        .withProperty("prop", "propValue"));

    // Verify
    assertEquals("propValue", capturer1.get());
  }

  @Test
  public void testMultipleProperties() throws InterruptedException {
    IDestination<String> topic = MOM.newDestination("test/mom/testProperties", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final Capturer<String> capturer1 = new Capturer<>();

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {
      @Override
      public void onMessage(IMessage<String> message) {
        String result = StringUtility.join("|",
            message.getProperty("prop"),
            message.getProperty("123"),
            message.getProperty("null"),
            message.getProperty("last"));
        capturer1.set(result);
      }
    }));

    // Publish a message
    Map<String, String> myMap = new HashMap<>();
    myMap.put("prop", "propValue");
    myMap.put("anotherProp", "not used");
    myMap.put("123", "one-two-three");
    myMap.put("null", null);
    MOM.publish(JmsTestMom.class, topic, "hello world", MOM.newPublishInput()
        .withProperties(null) // test null-safety
        .withProperty("prop", "propValue")
        .withProperties(myMap)
        .withProperty("last", "."));

    // Verify
    assertEquals("propValue|one-two-three|.", capturer1.get());
  }

  @Test
  @Times(10)
  public void testTimeToLive() throws InterruptedException {
    IDestination<String> queue = MOM.newDestination("test/mom/testTimeToLive", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final CountDownLatch latch = new CountDownLatch(1);

    // Publish a message
    MOM.publish(JmsTestMom.class, queue, "hello world", MOM.newPublishInput()
        .withTimeToLive(1, TimeUnit.MILLISECONDS));

    Thread.sleep(100);

    // Subscribe for the destination
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        latch.countDown();
      }
    }));

    // Verify
    assertFalse(latch.await(50, TimeUnit.MILLISECONDS)); // expect the message not to be received
  }

  private void testRequestReplyCorrelationIdInternal(final IBiDestination<String, String> destination) throws InterruptedException {
    m_disposables.add(MOM.reply(JmsTestMom.class, destination, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        return CorrelationId.CURRENT.get();
      }
    }, MOM.newSubscribeInput()
        .withRunContext(RunContexts.copyCurrent()
            .withCorrelationId("cid:xyz"))));

    // Initiate 'request-reply' communication
    RunContexts.empty()
        .withCorrelationId("cid:abc")
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            String testee = MOM.request(JmsTestMom.class, destination, "hello world");
            // Verify
            assertEquals("cid:abc", testee);
          }
        });
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReply() throws InterruptedException {
    IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReply", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, topic, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        return request.getTransferObject().toUpperCase();
      }
    }));

    // Initiate 'request-reply' communication
    String testee = MOM.request(JmsTestMom.class, topic, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
  }

  @Test(timeout = 200_000)
  @Times(10) // regression
  public void testQueueRequestReplyMultipleSubscriptions() throws InterruptedException {
    IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyMultipleSubscriptions", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final CountDownLatch msgLatch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, queue, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        msgLatch.countDown();
        return request.getTransferObject().toUpperCase();
      }
    }));

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, queue, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        msgLatch.countDown();
        return request.getTransferObject().toUpperCase();
      }
    }));

    // Initiate 'request-reply' communication
    String testee = MOM.request(JmsTestMom.class, queue, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
    assertFalse(msgLatch.await(50, TimeUnit.MILLISECONDS));
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyMultipleSubscriptions() throws InterruptedException {
    IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyMultipleSubscriptions", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    final CountDownLatch msgLatch = new CountDownLatch(2);

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, topic, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        msgLatch.countDown();
        return request.getTransferObject().toUpperCase();
      }
    }));

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, topic, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        msgLatch.countDown();
        return request.getTransferObject().toUpperCase();
      }
    }));

    // Initiate 'request-reply' communication
    String testee = MOM.request(JmsTestMom.class, topic, "hello world");

    // Verify
    assertEquals("HELLO WORLD", testee);
    assertTrue(msgLatch.await(5, TimeUnit.SECONDS));
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyRequestFirst() throws InterruptedException {
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyRequestFirst", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // Test setup: simulate to initiate 'request-reply' before subscription
    IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    // 1. Initiate 'request-reply' communication
    IFuture<String> requestFuture = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return MOM.request(JmsTestMom.class, queue, "hello world");
      }
    }, Jobs.newInput()
        .withName("requester (Q)")
        .withExecutionHint(m_testJobExecutionHint)
        .withExecutionSemaphore(mutex));

    // 2. Subscribe for reply
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Subscribe replier
        m_disposables.add(MOM.reply(JmsTestMom.class, queue, new IRequestListener<String, String>() {

          @Override
          public String onRequest(IMessage<String> request) {
            return request.getTransferObject().toUpperCase();
          }
        }));
      }
    }, Jobs.newInput()
        .withName("replier (Q)")
        .withExecutionHint(m_testJobExecutionHint)
        .withExecutionSemaphore(mutex));

    String testee = requestFuture.awaitDoneAndGet(10, TimeUnit.SECONDS);

    // Verify
    assertEquals("HELLO WORLD", testee);
  }

  @Test(timeout = 200_000)
  @Times(10) // regression
  public void testTopicRequestReplyRequestFirst() throws InterruptedException {
    final IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyRequestFirst", DestinationType.TOPIC, ResolveMethod.DEFINE, null);

    // Test setup: simulate to initiate 'request-reply' before subscription
    IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    // 1. Initiate 'request-reply' communication
    IFuture<String> requestFuture = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return MOM.request(JmsTestMom.class, topic, "hello world");
      }
    }, Jobs.newInput()
        .withName("requester (T)")
        .withExecutionHint(m_testJobExecutionHint)
        .withExceptionHandling(null, false)
        .withExecutionSemaphore(mutex));

    // 2. Subscribe for reply
    IFuture<Void> replyFuture = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Subscribe replier
        m_disposables.add(MOM.reply(JmsTestMom.class, topic, new IRequestListener<String, String>() {

          @Override
          public String onRequest(IMessage<String> request) {
            return request.getTransferObject().toUpperCase();
          }
        }));
      }
    }, Jobs.newInput()
        .withName("replier (T)")
        .withExecutionHint(m_testJobExecutionHint)
        .withExecutionSemaphore(mutex));

    replyFuture.awaitDone();
    // Verify
    try {
      requestFuture.awaitDoneAndGet(200, TimeUnit.MILLISECONDS);
      fail();
    }
    catch (TimedOutError e) {
      assertTrue(true);
    }
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyCancellation() throws InterruptedException {
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyCancellation", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyCancellationInternal(queue);
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyCancellation() throws InterruptedException {
    final IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyCancellation", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testRequestReplyCancellationInternal(topic);
  }

  private void testRequestReplyCancellationInternal(final IBiDestination<String, String> destination) throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(2);

    final AtomicBoolean requestorInterrupted = new AtomicBoolean();
    final AtomicBoolean replierInterrupted = new AtomicBoolean();
    final AtomicBoolean replierCancelled = new AtomicBoolean();

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, destination, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          replierInterrupted.set(true);
        }
        finally {
          replierCancelled.set(RunMonitor.CURRENT.get().isCancelled());
          verifyLatch.countDown();
        }
        return request.getTransferObject().toUpperCase();
      }
    }));

    final String requestReplyJobId = UUID.randomUUID().toString();

    // Prepare cancellation job
    IFuture<Void> cancellationJob = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Wait until message processing started
        assertTrue(setupLatch.await());

        // Cancel the publishing thread
        Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
            .andMatchExecutionHint(requestReplyJobId)
            .toFilter(), true);
      }
    }, Jobs.newInput()
        .withName("canceller")
        .withExecutionHint(m_testJobExecutionHint));

    // Initiate 'request-reply' communication
    final FinalValue<String> testee = new FinalValue<>();
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          String result = MOM.request(JmsTestMom.class, destination, "hello world");
          testee.set(result);
        }
        catch (ThreadInterruptedError e) {
          requestorInterrupted.set(true);
        }
        finally {
          verifyLatch.countDown();
        }
      }
    }, Jobs.newInput()
        .withName("initiator")
        .withExecutionHint(requestReplyJobId)
        .withExecutionHint(m_testJobExecutionHint));

    // Wait until cancelled requestor thread
    cancellationJob.awaitDoneAndGet();

    assertTrue(verifyLatch.await());

    // Verify
    assertTrue(requestorInterrupted.get());
    assertTrue(replierInterrupted.get());
    assertTrue(replierCancelled.get());
    assertFalse(testee.isSet());
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyTimeout() throws InterruptedException {
    final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyTimeout", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyTimeoutInternal(queue);
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyTimeout() throws InterruptedException {
    final IBiDestination<String, String> topic = MOM.newBiDestination("test/mom/testTopicRequestReplyTimeout", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testRequestReplyTimeoutInternal(topic);
  }

  private void testRequestReplyTimeoutInternal(final IBiDestination<String, String> destination) throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(2);

    final AtomicBoolean requestorTimedOut = new AtomicBoolean();
    final AtomicBoolean replierInterrupted = new AtomicBoolean();

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, destination, new IRequestListener<String, String>() {

      @Override
      public String onRequest(IMessage<String> request) {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          replierInterrupted.set(true);
        }
        finally {
          verifyLatch.countDown();
        }
        return request.getTransferObject().toUpperCase();
      }
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(JmsTestMom.class, destination, "hello world", MOM.newPublishInput()
          .withRequestReplyTimeout(1, TimeUnit.SECONDS));
    }
    catch (TimedOutError e) {
      requestorTimedOut.set(true);
    }
    finally {
      verifyLatch.countDown();
    }

    assertTrue(verifyLatch.await());

    // Verify
    assertTrue(requestorTimedOut.get());
    assertTrue(replierInterrupted.get());
  }

  private void testPublishSubscribeCorrelationIdInternal(final IDestination<String> destination) throws InterruptedException {
    final Capturer<String> cid = new Capturer<>();

    m_disposables.add(MOM.subscribe(JmsTestMom.class, destination, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        cid.set(CorrelationId.CURRENT.get());
      }
    }, MOM.newSubscribeInput()
        .withRunContext(RunContexts.copyCurrent()
            .withCorrelationId("cid:xyz"))));

    RunContexts.copyCurrent()
        .withCorrelationId("cid:abc")
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            MOM.publish(JmsTestMom.class, destination, "hello world");
          }
        });

    assertEquals("cid:abc", cid.get());
  }

  @Test(timeout = 200_000)
  public void testTopicRequestReplyJsonObjectMarshaller() throws InterruptedException {
    IBiDestination<Person, Person> queue = MOM.newBiDestination("test/mom/testTopicRequestReplyJsonObjectMarshaller", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    testRequestReplyJsonObjectMarshallerInternal(queue);
  }

  @Test(timeout = 200_000)
  public void testQueueRequestReplyJsonObjectMarshaller() throws InterruptedException {
    IBiDestination<Person, Person> topic = MOM.newBiDestination("test/mom/testQueueRequestReplyJsonObjectMarshaller", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    testRequestReplyJsonObjectMarshallerInternal(topic);
  }

  private void testRequestReplyJsonObjectMarshallerInternal(IBiDestination<Person, Person> destination) throws InterruptedException {
    m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, destination, BEANS.get(JsonMarshaller.class)));

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, destination, new IRequestListener<Person, Person>() {

      @Override
      public Person onRequest(IMessage<Person> request) {
        Person result = new Person();
        result.setLastname(request.getTransferObject().getLastname().toUpperCase());
        result.setFirstname(request.getTransferObject().getFirstname().toUpperCase());
        return result;
      }
    }));

    // Initiate 'request-reply' communication
    Person testee = MOM.request(JmsTestMom.class, destination, person);

    // Verify
    assertEquals("ANNA", testee.getFirstname());
    assertEquals("SMITH", testee.getLastname());
  }

  @Test(timeout = 200_000)
  public void testRequestReply_ObjectMarshaller_Exception() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IBiDestination<Void, String> destination = MOM.newBiDestination("test/mom/testRequestReply_ObjectMarshaller_Exception", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, destination, new IRequestListener<Void, String>() {

      @Override
      public String onRequest(IMessage<Void> request) {
        throw runtimeException;
      }
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(JmsTestMom.class, destination, null);
      fail("SomethingWrongException expected");
    }
    catch (SomethingWrongException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 200_000)
  public void testRequestReply_JsonMarshaller_Exception1() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IBiDestination<Void, String> destination = MOM.newBiDestination("test/mom/testRequestReply_JsonMarshaller_Exception1", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    MOM.registerMarshaller(JmsTestMom.class, destination, BEANS.get(JsonMarshaller.class));

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, destination, new IRequestListener<Void, String>() {

      @Override
      public String onRequest(IMessage<Void> request) {
        throw runtimeException;
      }
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(JmsTestMom.class, destination, null);
      fail("SomethingWrongException expected");
    }
    catch (SomethingWrongException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 200_000)
  public void testRequestReply_JsonMarshaller_Exception2() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IBiDestination<Void, String> destination = MOM.newBiDestination("test/mom/testRequestReply_JsonMarshaller_Exception2", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    MOM.registerMarshaller(JmsTestMom.class, destination, BEANS.get(JsonMarshaller.class));

    final RuntimeException runtimeException = new VetoException();

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, destination, new IRequestListener<Void, String>() {

      @Override
      public String onRequest(IMessage<Void> request) {
        throw runtimeException;
      }
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(JmsTestMom.class, destination, null);
      fail("RuntimeException expected");
    }
    catch (RuntimeException e) {
      assertEquals(0, e.getStackTrace().length); // security
    }
  }

  @Test(timeout = 200_000)
  public void testRequestReply_StringMarshaller_Exception() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    IBiDestination<Void, String> destination = MOM.newBiDestination("test/mom/testRequestReply_StringMarshaller_Exception", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    MOM.registerMarshaller(JmsTestMom.class, destination, BEANS.get(TextMarshaller.class));

    final RuntimeException runtimeException = new SomethingWrongException("expected-expected-junit-exception");

    // Subscribe for the destination
    m_disposables.add(MOM.reply(JmsTestMom.class, destination, new IRequestListener<Void, String>() {

      @Override
      public String onRequest(IMessage<Void> request) {
        throw runtimeException;
      }
    }));

    // Initiate 'request-reply' communication
    try {
      MOM.request(JmsTestMom.class, destination, null);
      fail("ProcessingException expected");
    }
    catch (ProcessingException e) {
      assertTrue(e.getContextInfos().isEmpty()); // security
      assertFalse(e.getDisplayMessage().contains("expected-junit-exception"));
    }
  }

  private class Capturer<TYPE> {

    private volatile TYPE m_message;
    private final CountDownLatch m_latch = new CountDownLatch(1);

    public void set(TYPE message) {
      m_message = message;
      m_latch.countDown();
    }

    public TYPE get() throws InterruptedException {
      return get(15, TimeUnit.SECONDS);
    }

    public TYPE get(long timeout, TimeUnit unit) throws InterruptedException {
      if (!m_latch.await(timeout, unit)) {
        throw new TimedOutError("timout elapsed while waiting for message");
      }
      return m_message;
    }

    public void assertEmpty(int timeout, TimeUnit unit) throws InterruptedException {
      try {
        TYPE result = get(timeout, unit);
        fail("Found unexpected captured value: " + result);
      }
      catch (TimedOutError e) {
        // is empty -> ok
      }
    }
  }

  private class CapturerListener<DTO> implements IMessageListener<DTO> {

    private final Capturer<DTO> m_capturer;

    public CapturerListener(Capturer<DTO> capturer) {
      m_capturer = capturer;
    }

    @Override
    public void onMessage(IMessage<DTO> message) {
      m_capturer.set(message.getTransferObject());
    }
  }

  public static class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private long m_id;
    private String m_firstname;
    private String m_lastname;

    public long getId() {
      return m_id;
    }

    public void setId(long id) {
      m_id = id;
    }

    public String getFirstname() {
      return m_firstname;
    }

    public void setFirstname(String firstname) {
      m_firstname = firstname;
    }

    public String getLastname() {
      return m_lastname;
    }

    public void setLastname(String lastname) {
      m_lastname = lastname;
    }
  }

  @Test
  @Times(1)
  public void testPublishJsonDataSecure() throws InterruptedException {
    final Capturer<Person> capturer = new Capturer<>();

    IDestination<Person> queue = MOM.newDestination("test/mom/testPublishJsonDataSecure", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(JsonMarshaller.class)));

    Person person = new Person();
    person.setFirstname("anna");
    person.setLastname("smith");

    MOM.publish(JmsTestMom.class, queue, person);
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<Person>() {

      @Override
      public void onMessage(IMessage<Person> message) {
        capturer.set(message.getTransferObject());
      }
    }));

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCurrentMessagePubSub() throws InterruptedException {
    CorrelationId.CURRENT.set("cid_test");
    try {
      final Capturer<IMessage<?>> capturer = new Capturer<>();

      Person person = new Person();
      person.setLastname("smith");
      person.setFirstname("anna");

      IDestination<Person> queue = MOM.newDestination("test/mom/testCurrentMessagePubSub", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(ObjectMarshaller.class)));

      MOM.publish(JmsTestMom.class, queue, person);
      m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<Person>() {

        @Override
        public void onMessage(IMessage<Person> message) {
          capturer.set(IMessage.CURRENT.get());
        }
      }));

      // Verify
      IMessage<Person> testee = (IMessage<Person>) capturer.get();
      assertNotNull(testee);
      assertEquals("smith", testee.getTransferObject().getLastname());
      assertEquals("anna", testee.getTransferObject().getFirstname());
      assertThat(testee.getAdapter(Message.class), instanceOf(Message.class));
    }
    finally {
      CorrelationId.CURRENT.remove();
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCurrentMessageRequestReply() throws InterruptedException, JMSException {
    CorrelationId.CURRENT.set("cid_test");
    try {

      final Capturer<IMessage<?>> capturer = new Capturer<>();

      Person person = new Person();
      person.setLastname("smith");
      person.setFirstname("anna");

      IBiDestination<Person, Void> queue = MOM.newBiDestination("test/mom/testCurrentMessageRequestReply", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(ObjectMarshaller.class)));

      m_disposables.add(MOM.reply(JmsTestMom.class, queue, new IRequestListener<Person, Void>() {

        @Override
        public Void onRequest(IMessage<Person> request) {
          capturer.set(IMessage.CURRENT.get());
          return null;
        }
      }));
      MOM.request(JmsTestMom.class, queue, person);

      // Verify
      IMessage<Person> testee = (IMessage<Person>) capturer.get();
      assertNotNull(testee);
      assertEquals("smith", testee.getTransferObject().getLastname());
      assertEquals("anna", testee.getTransferObject().getFirstname());
      assertThat(testee.getAdapter(Message.class), instanceOf(Message.class));
      assertEquals("cid_test", testee.getAdapter(Message.class).getJMSCorrelationID());
    }
    finally {
      CorrelationId.CURRENT.remove();
    }
  }

  @Test
  public void testPublishTransactional() throws InterruptedException {
    final Capturer<Person> capturer = new Capturer<>();

    Person person = new Person();
    person.setLastname("smith");
    person.setFirstname("anna");

    ITransaction tx = BEANS.get(ITransaction.class);
    ITransaction.CURRENT.set(tx);

    IDestination<Person> queue = MOM.newDestination("test/mom/testPublishTransactional", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(ObjectMarshaller.class)));

    MOM.publish(JmsTestMom.class, queue, person, MOM.newPublishInput().withTransactional(true));
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<Person>() {

      @Override
      public void onMessage(IMessage<Person> message) {
        capturer.set(message.getTransferObject());
      }
    }));

    try {
      capturer.get(2, TimeUnit.SECONDS);
      fail();
    }
    catch (TimedOutError e) {
      // expected
    }

    tx.commitPhase1();
    tx.commitPhase2();

    try {
      capturer.get(5, TimeUnit.SECONDS);
    }
    catch (TimedOutError e) {
      fail();
    }
    finally {
      tx.release();
    }

    // Verify
    Person testee = capturer.get();
    assertEquals("smith", testee.getLastname());
    assertEquals("anna", testee.getFirstname());
  }

  @Test
  public void testSubscribeTransactional() throws InterruptedException {
    IDestination<String> queue = MOM.newDestination("test/mom/testSubscribeTransactional", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, BEANS.get(ObjectMarshaller.class)));

    MOM.publish(JmsTestMom.class, queue, "message-1", MOM.newPublishInput());

    final AtomicInteger messageCounter = new AtomicInteger();

    // 1. Receive message, but reject it (rollback)
    final BlockingCountDownLatch message1Latch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch message2Latch = new BlockingCountDownLatch(1);

    IMessageListener<String> listener = new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        switch (message.getTransferObject()) {
          case "message-1":
            switch (messageCounter.incrementAndGet()) {
              case 1:
              case 2:
              case 3:
                ITransaction.CURRENT.get().rollback();
                break;
              default:
                message1Latch.countDown();
                break;
            }
            return;
          case "message-2":
            message2Latch.countDown();
            break;
          default:
            throw new IllegalArgumentException("illegal message");
        }
      }
    };

    // Register transactional subscriber
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, listener, MOM.newSubscribeInput().withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_TRANSACTED)));
    assertTrue("message expected to be rejected 3 times", message1Latch.await());

    // Publish a next message
    MOM.publish(JmsTestMom.class, queue, "message-2", MOM.newPublishInput());

    // Check that the message was received
    assertTrue("message expected to be received", message2Latch.await());

    Thread.sleep(1000); // Wait some time to verify that 'message-1' is no longer received.
    assertEquals(messageCounter.get(), 4);
  }

  @Test
  public void testConcurrentMessageConsumption() throws InterruptedException {
    IDestination<Object> queue = MOM.newDestination("test/mom/testConcurrentMessageConsumption", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // 1. Publish some messages
    int msgCount = 10;
    for (int i = 0; i < msgCount; i++) {
      MOM.publish(JmsTestMom.class, queue, "hello");
    }

    // 1. Publish some messages
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(msgCount, 3, TimeUnit.SECONDS);
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<Object>() {

      @Override
      public void onMessage(IMessage<Object> message) {
        try {
          latch.countDownAndBlock(1, TimeUnit.MINUTES); // timeout must be greater than the default latch timeout
        }
        catch (InterruptedException e) {
          throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
        }
      }
    }));

    try {
      assertTrue("messages expected to be consumed concurrently", latch.await());
    }
    finally {
      latch.unblock();
    }
  }

  @Test
  public void testSerialMessageConsumption() throws InterruptedException {
    IDestination<Object> queue = MOM.newDestination("test/mom/testSerialMessageConsumption", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    // 1. Publish some messages
    int msgCount = 10;
    for (int i = 0; i < msgCount; i++) {
      MOM.publish(JmsTestMom.class, queue, "hello");
    }

    // 2. Consume the messages
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(msgCount, 3, TimeUnit.SECONDS);
    m_disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<Object>() {

      @Override
      public void onMessage(IMessage<Object> message) {
        try {
          latch.countDownAndBlock(1, TimeUnit.MINUTES); // timeout must be greater than the default latch timeout
        }
        catch (InterruptedException e) {
          throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
        }
      }
    }, MOM.newSubscribeInput().withAcknowledgementMode(SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED)));

    try {
      assertFalse("messages not expected to be consumed concurrently", latch.await());
      assertEquals("one message expected to be consumed", msgCount - 1, latch.getCount());
    }
    finally {
      latch.unblock();
    }
  }

  @Test
  public void testMessageSelector() throws InterruptedException {
    final Capturer<String> allCapturer = new Capturer<>();
    final Capturer<String> johnCapturer = new Capturer<>();
    final Capturer<String> annaCapturer = new Capturer<>();

    IDestination<String> topic = MOM.newDestination("test/mom/testMessageSelector", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    // register subscriber without selector
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        allCapturer.set(message.getTransferObject());
      }
    }));
    // register subscriber with selector '"user = 'john''
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        johnCapturer.set(message.getTransferObject());
      }
    }, MOM.newSubscribeInput().withSelector("user = 'john'")));

    // register subscriber with selector 'user = 'anna''
    m_disposables.add(MOM.subscribe(JmsTestMom.class, topic, new IMessageListener<String>() {

      @Override
      public void onMessage(IMessage<String> message) {
        annaCapturer.set(message.getTransferObject());
      }
    }, MOM.newSubscribeInput().withSelector("user = 'anna'")));

    // Publish the message for anna
    MOM.publish(JmsTestMom.class, topic, "message-for-anna", MOM.newPublishInput().withProperty("user", "anna"));

    // Verify
    try {
      johnCapturer.get(2, TimeUnit.SECONDS);
      fail("timeout expected");
    }
    catch (TimedOutError e) {
      // NOOP
    }

    assertEquals("message-for-anna", allCapturer.get(2, TimeUnit.SECONDS));
    assertEquals("message-for-anna", annaCapturer.get(2, TimeUnit.SECONDS));
  }

  private Object testPublishAndConsumeInternal(Object transferObject, IMarshaller marshaller) throws InterruptedException {
    final Capturer<Object> capturer = new Capturer<>();

    IDestination<Object> queue = MOM.newDestination("test/mom/testPublishAndConsumeInternal", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    List<IDisposable> disposables = new ArrayList<>();
    if (marshaller != null) {
      disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, marshaller));
    }

    MOM.publish(JmsTestMom.class, queue, transferObject);
    disposables.add(MOM.subscribe(JmsTestMom.class, queue, new IMessageListener<Object>() {

      @Override
      public void onMessage(IMessage<Object> msg) {
        capturer.set(msg.getTransferObject());
      }
    }));

    // Verify
    try {
      return capturer.get();
    }
    finally {
      dispose(disposables);
    }
  }

  private Object testRequestReplyInternal(Object request, IMarshaller marshaller) throws InterruptedException {
    IBiDestination<Object, Object> queue = MOM.newBiDestination("test/mom/testRequestReplyInternal", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    List<IDisposable> disposables = new ArrayList<>();
    if (marshaller != null) {
      disposables.add(MOM.registerMarshaller(JmsTestMom.class, queue, marshaller));
    }

    try {
      disposables.add(MOM.reply(JmsTestMom.class, queue, new IRequestListener<Object, Object>() {

        @Override
        public Object onRequest(IMessage<Object> req) {
          return req.getTransferObject();
        }
      }));

      return MOM.request(JmsTestMom.class, queue, request);
    }
    finally {
      dispose(disposables);
    }
  }

  private void dispose(Collection<IDisposable> disposables) {
    for (IDisposable disposable : disposables) {
      disposable.dispose();
    }
  }

  @Test
  public void testTopicDurableSubscription() throws InterruptedException {
    final IDestination<String> topic = MOM.newDestination("test/mom/testTopicPublishSubscribe", DestinationType.TOPIC, ResolveMethod.DEFINE, null);
    final String durableSubscriptionName = "Durable-Test-Subscription";

    // 1. Subscribe (non-durable)
    final Capturer<String> capturer1 = new Capturer<>();
    final ISubscription subscription1 = MOM.subscribe(JmsTestMom.class, topic, new CapturerListener<>(capturer1), MOM.newSubscribeInput());
    m_disposables.add(subscription1);

    // 2. Disconnect
    subscription1.dispose();

    // 3. Publish a message
    MOM.publish(JmsTestMom.class, topic, "lost message");
    capturer1.assertEmpty(1, TimeUnit.SECONDS); // no one is listening

    // 4. Subscribe again (durable)
    final Capturer<String> capturer2 = new Capturer<>();
    final ISubscription subscription2 = MOM.subscribe(JmsTestMom.class, topic, new CapturerListener<>(capturer2), MOM.newSubscribeInput()
        .withDurableSubscription(durableSubscriptionName));
    m_disposables.add(subscription2);

    // 5. Assert that message is lost
    capturer2.assertEmpty(1, TimeUnit.SECONDS);

    // 6. Disconnect
    subscription2.dispose();

    // 7. Publish an other message
    MOM.publish(JmsTestMom.class, topic, "hello world");
    capturer2.assertEmpty(1, TimeUnit.SECONDS); // not yet

    // 8. Subscribe again (durable, same name)
    final Capturer<String> capturer3 = new Capturer<>();
    final ISubscription subscription3 = MOM.subscribe(JmsTestMom.class, topic, new CapturerListener<>(capturer3), MOM.newSubscribeInput()
        .withDurableSubscription(durableSubscriptionName));
    m_disposables.add(subscription3);

    // 9. Assert that the message is received
    assertEquals("hello world", capturer3.get(1, TimeUnit.SECONDS));

    // 10. Disconnect and cancel the durable subscription
    subscription3.dispose();
    MOM.cancelDurableSubscription(JmsTestMom.class, durableSubscriptionName);

    // 11. Publish another message
    MOM.publish(JmsTestMom.class, topic, "hello universe");
    assertEquals("hello world", capturer3.get(1, TimeUnit.SECONDS)); // still the same old message

    // 12. Subscribe again (durable, same name)
    final Capturer<String> capturer4 = new Capturer<>();
    final ISubscription subscription4 = MOM.subscribe(JmsTestMom.class, topic, new CapturerListener<>(capturer4), MOM.newSubscribeInput()
        .withDurableSubscription(durableSubscriptionName));
    m_disposables.add(subscription4);

    // 13. Assert that message is still lost, even if the same name was used (because the previous subscription was cancelled explicitly)
    capturer4.assertEmpty(1, TimeUnit.SECONDS);
  }

  @Test
  public void testMomEnvironmentWithCustomDefaultMarshaller() throws InterruptedException {
    installTestMom(JmsTestMomWithTextMarshaller.class);
    try {
      final Capturer<String> capturer1 = new Capturer<>();
      final Capturer<Object> capturer2 = new Capturer<>();

      IDestination<String> queueString = MOM.newDestination("test/mom/testPublishStringData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      IDestination<Object> queueObject = MOM.newDestination("test/mom/testPublishObjectData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      m_disposables.add(MOM.registerMarshaller(JmsTestMom.class, queueObject, BEANS.get(ObjectMarshaller.class)));

      MOM.publish(JmsTestMom.class, queueString, "Hello MOM!");
      MOM.publish(JmsTestMom.class, queueObject, new StringHolder("Hello MOM! (holder)"));
      m_disposables.add(MOM.subscribe(JmsTestMom.class, queueString, new IMessageListener<String>() {
        @Override
        public void onMessage(IMessage<String> message) {
          capturer1.set(message.getTransferObject());
        }
      }));
      m_disposables.add(MOM.subscribe(JmsTestMom.class, queueObject, new IMessageListener<Object>() {
        @Override
        public void onMessage(IMessage<Object> message) {
          capturer2.set(message.getTransferObject());
        }
      }));

      // Verify
      String received1 = capturer1.get();
      Object received2 = capturer2.get();
      assertEquals("Hello MOM!", received1);
      assertEquals("Hello MOM! (holder)", Objects.toString(received2));
    }
    finally {
      installTestMom(JmsTestMom.class);
    }
  }

  @Test
  public void testMomEnvironmentWithConfiguredDefaultMarshaller() throws InterruptedException {
    installTestMom(JmsTestMomWithConfiguredTextMarshaller.class);
    try {
      final Capturer<String> capturer = new Capturer<>();

      IDestination<String> queueString = MOM.newDestination("test/mom/testPublishStringData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

      MOM.publish(JmsTestMom.class, queueString, "Hello MOM!");
      m_disposables.add(MOM.subscribe(JmsTestMom.class, queueString, new IMessageListener<String>() {
        @Override
        public void onMessage(IMessage<String> message) {
          capturer.set(message.getTransferObject());
        }
      }));

      // Verify
      String received = capturer.get();
      assertEquals("!MOM olleH", received);
    }
    finally {
      installTestMom(JmsTestMom.class);
    }
  }

  @Test(expected = PlatformException.class)
  public void testMomEnvironmentWithInvalidMarshaller() throws InterruptedException {
    installTestMom(JmsTestMomWithInvalidMarshaller.class);
    try {
      IDestination<String> queueString = MOM.newDestination("test/mom/testPublishStringData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      MOM.publish(JmsTestMom.class, queueString, "Hello MOM!");
    }
    finally {
      installTestMom(JmsTestMom.class);
    }
  }

  @Test(expected = AssertionException.class)
  public void testMomEnvironmentWithoutRequestReply() throws InterruptedException {
    installTestMom(JmsTestMomWithoutRequestReply.class);
    try {
      testRequestReplyInternal("Hello World", null);
    }
    finally {
      installTestMom(JmsTestMom.class);
    }
  }

  @Test(timeout = 200_000)
  public void testMomEnvironmentWithCustomCancellationTopicAsString() throws InterruptedException {
    installTestMom(JmsTestMomWithCustomRequestReplyCancellationTopicAsString.class);
    try {
      IDestination<String> defaultTopic = CONFIG.getPropertyValue(IMom.RequestReplyCancellationTopicProperty.class);
      IDestination<String> differentTopic = MOM.newDestination("differentTopic", IDestination.DestinationType.TOPIC, IDestination.ResolveMethod.DEFINE, null);
      final Capturer<String> capturer1 = new Capturer<>();
      final Capturer<String> capturer2 = new Capturer<>();
      m_disposables.add(MOM.subscribe(JmsTestMom.class, defaultTopic, new IMessageListener<String>() {
        @Override
        public void onMessage(IMessage<String> message) {
          capturer1.set("cancelled!"); // should not be called
        }
      }));
      m_disposables.add(MOM.subscribe(JmsTestMom.class, differentTopic, new IMessageListener<String>() {
        @Override
        public void onMessage(IMessage<String> message) {
          capturer2.set("cancelled!"); // should be called
        }
      }));

      // Run test
      final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyCancellation", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      testRequestReplyCancellationInternal(queue);

      // Verify
      capturer1.assertEmpty(1, TimeUnit.SECONDS);
      assertNotNull(capturer2.get(1, TimeUnit.SECONDS));
    }
    finally {
      installTestMom(JmsTestMom.class);
    }
  }

  @Test(timeout = 200_000)
  public void testMomEnvironmentWithCustomCancellationTopic() throws InterruptedException {
    installTestMom(JmsTestMomWithCustomRequestReplyCancellationTopic.class);
    try {
      IDestination<String> differentTopic = MOM.newDestination("UnitTestTopic", IDestination.DestinationType.TOPIC, IDestination.ResolveMethod.JNDI, null);
      final Capturer<String> capturer = new Capturer<>();
      m_disposables.add(MOM.subscribe(JmsTestMom.class, differentTopic, new IMessageListener<String>() {
        @Override
        public void onMessage(IMessage<String> message) {
          capturer.set("cancelled!"); // should be called
        }
      }));

      // Run test
      final IBiDestination<String, String> queue = MOM.newBiDestination("test/mom/testQueueRequestReplyCancellation", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
      testRequestReplyCancellationInternal(queue);

      // Verify
      assertNotNull(capturer.get(1, TimeUnit.SECONDS));
    }
    finally {
      installTestMom(JmsTestMom.class);
    }
  }

  private static class SomethingWrongException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    public SomethingWrongException() {
    }

    public SomethingWrongException(String message) {
      super(message);
    }
  }

  /**
   * Encapsulates {@link JmsMomImplementor} for testing purpose.
   */
  @IgnoreBean
  public static class JmsTestMom extends AbstractMomTransport {

    private static final AtomicInteger MOM_COUNTER = new AtomicInteger(0);

    @Override
    protected Class<? extends IMomImplementor> getConfiguredImplementor() {
      return JmsMomImplementor.class;
    }

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = new HashMap<>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, ActiveMQInitialContextFactory.class.getName());
      env.put(Context.PROVIDER_URL, "vm://mom" + MOM_COUNTER.incrementAndGet() + "/junit?broker.persistent=false");
      env.put("connectionFactoryNames", "JUnitConnectionFactory"); // Active MQ specific
      env.put(IMomImplementor.CONNECTION_FACTORY, "JUnitConnectionFactory");
      env.put(IMomImplementor.SYMBOLIC_NAME, "Scout JUnit MOM #" + MOM_COUNTER.get());
      return env;
    }
  }

  @IgnoreBean
  @Replace
  public static class JmsTestMomWithTextMarshaller extends JmsTestMom {

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = super.getConfiguredEnvironment();
      env.put(IMomImplementor.MARSHALLER, TextMarshaller.class.getName());
      env.put(JmsMomImplementor.JMS_CLIENT_ID, "junit_mom_client");
      return env;
    }
  }

  @IgnoreBean
  @Replace
  public static class JmsTestMomWithConfiguredTextMarshaller extends JmsTestMom {

    @Override
    protected IMarshaller getConfiguredDefaultMarshaller() {
      return new TextMarshaller() {
        @Override
        public Object unmarshall(Object data, Map<String, String> context) {
          return new StringBuilder((String) data).reverse().toString();
        }
      };
    }
  }

  @IgnoreBean
  @Replace
  public static class JmsTestMomWithInvalidMarshaller extends JmsTestMom {

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = super.getConfiguredEnvironment();
      env.put(IMomImplementor.MARSHALLER, "Invalid Class Name");
      return env;
    }
  }

  @IgnoreBean
  @Replace
  public static class JmsTestMomWithoutRequestReply extends JmsTestMom {

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = super.getConfiguredEnvironment();
      env.put(IMomImplementor.REQUEST_REPLY_ENABLED, "false");
      return env;
    }
  }

  @IgnoreBean
  @Replace
  public static class JmsTestMomWithCustomRequestReplyCancellationTopicAsString extends JmsTestMom {

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = super.getConfiguredEnvironment();
      env.put(IMomImplementor.REQUEST_REPLY_ENABLED, null); // should yield "true"
      env.put(IMomImplementor.REQUEST_REPLY_CANCELLATION_TOPIC, "define:///differentTopic");
      return env;
    }
  }

  @IgnoreBean
  @Replace
  public static class JmsTestMomWithCustomRequestReplyCancellationTopic extends JmsTestMom {

    @Override
    protected Map<Object, Object> lookupEnvironment() {
      Map<Object, Object> env = super.lookupEnvironment();
      env.put("topic.UnitTestTopic", "scout.physical.UnitTestTopic");
      env.put(IMomImplementor.REQUEST_REPLY_CANCELLATION_TOPIC, MOM.newDestination("UnitTestTopic", IDestination.DestinationType.TOPIC, IDestination.ResolveMethod.JNDI, null));
      return env;
    }
  }
}
